package com.android.server.net;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.security.KeyStore;
import android.system.Os;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.Preconditions;
import com.android.server.ConnectivityService;
import com.android.server.EventLogTags;
import com.android.server.connectivity.Vpn;
import java.util.List;

public class LockdownVpnTracker {
    private static final String ACTION_KEYSTORE_RESET = "com.mediatek.android.keystore.action.KEYSTORE_RESET";
    private static final String ACTION_LOCKDOWN_RESET = "com.android.server.action.LOCKDOWN_RESET";
    private static final int MAX_ERROR_COUNT = 4;
    private static final int ROOT_UID = 0;
    private static final String TAG = "LockdownVpnTracker";
    private String mAcceptedEgressIface;
    private String mAcceptedIface;
    private List<LinkAddress> mAcceptedSourceAddr;
    private final PendingIntent mConfigIntent;
    private final ConnectivityService mConnService;
    private final Context mContext;
    private int mErrorCount;
    private BroadcastReceiver mKeystoreResetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (LockdownVpnTracker.this.mVpn != null) {
                LockdownVpnTracker.this.mVpn.forceDisconnect();
            }
            LockdownVpnTracker.this.reset();
            if (LockdownVpnTracker.this.mConnService != null) {
                LockdownVpnTracker.this.mConnService.updateLockdownVpn();
            }
        }
    };
    private final INetworkManagementService mNetService;
    private final VpnProfile mProfile;
    private final PendingIntent mResetIntent;
    private BroadcastReceiver mResetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            LockdownVpnTracker.this.reset();
        }
    };
    private final Object mStateLock = new Object();
    private final Vpn mVpn;

    public static boolean isEnabled() {
        return KeyStore.getInstance().contains("LOCKDOWN_VPN");
    }

    public static boolean isFileUsable() {
        return KeyStore.getInstance().get("LOCKDOWN_VPN") != null;
    }

    public LockdownVpnTracker(Context context, INetworkManagementService netService, ConnectivityService connService, Vpn vpn, VpnProfile profile) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mNetService = (INetworkManagementService) Preconditions.checkNotNull(netService);
        this.mConnService = (ConnectivityService) Preconditions.checkNotNull(connService);
        this.mVpn = (Vpn) Preconditions.checkNotNull(vpn);
        this.mProfile = (VpnProfile) Preconditions.checkNotNull(profile);
        this.mConfigIntent = PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.VPN_SETTINGS"), 0);
        Intent resetIntent = new Intent(ACTION_LOCKDOWN_RESET);
        resetIntent.addFlags(1073741824);
        this.mResetIntent = PendingIntent.getBroadcast(this.mContext, 0, resetIntent, 0);
    }

    private void handleStateChangedLocked() {
        boolean egressDisconnected;
        NetworkInfo egressInfo = this.mConnService.getActiveNetworkInfoUnfiltered();
        LinkProperties egressProp = this.mConnService.getActiveLinkProperties();
        NetworkInfo vpnInfo = this.mVpn.getNetworkInfo();
        VpnConfig vpnConfig = this.mVpn.getLegacyVpnConfig();
        if (egressInfo != null) {
            egressDisconnected = State.DISCONNECTED.equals(egressInfo.getState());
        } else {
            egressDisconnected = true;
        }
        boolean egressChanged = egressProp != null ? !TextUtils.equals(this.mAcceptedEgressIface, egressProp.getInterfaceName()) : true;
        Slog.d(TAG, "handleStateChanged: egress=" + (egressInfo == null ? null : ConnectivityManager.getNetworkTypeName(egressInfo.getType())) + " " + this.mAcceptedEgressIface + "->" + (egressProp == null ? null : egressProp.getInterfaceName()));
        if (egressDisconnected || egressChanged) {
            clearSourceRulesLocked();
            this.mAcceptedEgressIface = null;
            this.mVpn.stopLegacyVpnPrivileged();
        }
        if (egressDisconnected) {
            hideNotification();
            return;
        }
        int egressType = egressInfo.getType();
        if (vpnInfo.getDetailedState() == DetailedState.FAILED) {
            EventLogTags.writeLockdownVpnError(egressType);
        }
        if (this.mErrorCount > 4) {
            showNotification(17040523, 17303510);
        } else if (!egressInfo.isConnected() || vpnInfo.isConnectedOrConnecting()) {
            if (vpnInfo.isConnected() && vpnConfig != null) {
                String iface = vpnConfig.interfaze;
                List<LinkAddress> sourceAddrs = vpnConfig.addresses;
                if (TextUtils.equals(iface, this.mAcceptedIface)) {
                    if (sourceAddrs.equals(this.mAcceptedSourceAddr)) {
                        return;
                    }
                }
                Slog.d(TAG, "VPN connected using iface=" + iface + ", sourceAddr=" + sourceAddrs.toString());
                EventLogTags.writeLockdownVpnConnected(egressType);
                showNotification(17040521, 17303509);
                try {
                    clearSourceRulesLocked();
                    this.mNetService.setFirewallInterfaceRule(iface, true);
                    for (LinkAddress addr : sourceAddrs) {
                        setFirewallEgressSourceRule(addr, true);
                    }
                    this.mNetService.setFirewallUidRule(0, 0, 1);
                    this.mNetService.setFirewallUidRule(0, Os.getuid(), 1);
                    this.mErrorCount = 0;
                    this.mAcceptedIface = iface;
                    this.mAcceptedSourceAddr = sourceAddrs;
                    NetworkInfo clone = new NetworkInfo(egressInfo);
                    augmentNetworkInfo(clone);
                    this.mConnService.sendConnectedBroadcast(clone);
                } catch (RemoteException e) {
                    throw new RuntimeException("Problem setting firewall rules", e);
                }
            }
        } else if (this.mProfile.isValidLockdownProfile()) {
            Slog.d(TAG, "Active network connected; starting VPN");
            EventLogTags.writeLockdownVpnConnecting(egressType);
            showNotification(17040520, 17303510);
            this.mAcceptedEgressIface = egressProp.getInterfaceName();
            try {
                this.mVpn.startLegacyVpnPrivileged(this.mProfile, KeyStore.getInstance(), egressProp);
            } catch (IllegalStateException e2) {
                this.mAcceptedEgressIface = null;
                Slog.e(TAG, "Failed to start VPN", e2);
                showNotification(17040523, 17303510);
            }
        } else {
            Slog.e(TAG, "Invalid VPN profile; requires IP-based server and DNS");
            showNotification(17040523, 17303510);
        }
    }

    public void init() {
        synchronized (this.mStateLock) {
            initLocked();
        }
    }

    private void initLocked() {
        Slog.d(TAG, "initLocked()");
        this.mVpn.setEnableTeardown(false);
        this.mContext.registerReceiver(this.mResetReceiver, new IntentFilter(ACTION_LOCKDOWN_RESET), "android.permission.CONNECTIVITY_INTERNAL", null);
        IntentFilter keystoreResetFilter = new IntentFilter(ACTION_KEYSTORE_RESET);
        keystoreResetFilter.addAction(ACTION_KEYSTORE_RESET);
        this.mContext.registerReceiver(this.mKeystoreResetReceiver, keystoreResetFilter);
        try {
            this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 500, true);
            this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 4500, true);
            this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 1701, true);
            if (this.mProfile.type == 0) {
                this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 1723, true);
                this.mNetService.setFirewallEgressProtoRule("gre", true);
            }
            handleStateChangedLocked();
        } catch (RemoteException e) {
            throw new RuntimeException("Problem setting firewall rules", e);
        }
    }

    public void shutdown() {
        synchronized (this.mStateLock) {
            shutdownLocked();
        }
    }

    private void shutdownLocked() {
        Slog.d(TAG, "shutdownLocked()");
        this.mAcceptedEgressIface = null;
        this.mErrorCount = 0;
        this.mVpn.stopLegacyVpnPrivileged();
        try {
            this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 500, false);
            this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 4500, false);
            this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 1701, false);
            if (this.mProfile.type == 0) {
                this.mNetService.setFirewallEgressDestRule(this.mProfile.server, 1723, false);
                this.mNetService.setFirewallEgressProtoRule("gre", false);
            }
            clearSourceRulesLocked();
            hideNotification();
            this.mContext.unregisterReceiver(this.mResetReceiver);
            this.mContext.unregisterReceiver(this.mKeystoreResetReceiver);
            this.mVpn.setEnableTeardown(true);
        } catch (RemoteException e) {
            throw new RuntimeException("Problem setting firewall rules", e);
        }
    }

    public void reset() {
        Slog.d(TAG, "reset()");
        synchronized (this.mStateLock) {
            shutdownLocked();
            initLocked();
            handleStateChangedLocked();
        }
    }

    private void clearSourceRulesLocked() {
        try {
            if (this.mAcceptedIface != null) {
                this.mNetService.setFirewallInterfaceRule(this.mAcceptedIface, false);
                this.mAcceptedIface = null;
            }
            if (this.mAcceptedSourceAddr != null) {
                for (LinkAddress addr : this.mAcceptedSourceAddr) {
                    setFirewallEgressSourceRule(addr, false);
                }
                this.mNetService.setFirewallUidRule(0, 0, 0);
                this.mNetService.setFirewallUidRule(0, Os.getuid(), 0);
                this.mAcceptedSourceAddr = null;
            }
        } catch (RemoteException e) {
            throw new RuntimeException("Problem setting firewall rules", e);
        }
    }

    private void setFirewallEgressSourceRule(LinkAddress address, boolean allow) throws RemoteException {
        this.mNetService.setFirewallEgressSourceRule(address.getAddress().getHostAddress(), allow);
    }

    public void onNetworkInfoChanged() {
        synchronized (this.mStateLock) {
            handleStateChangedLocked();
        }
    }

    public void onVpnStateChanged(NetworkInfo info) {
        if (info.getDetailedState() == DetailedState.FAILED) {
            this.mErrorCount++;
        }
        synchronized (this.mStateLock) {
            handleStateChangedLocked();
        }
    }

    public void augmentNetworkInfo(NetworkInfo info) {
        if (info.isConnected()) {
            NetworkInfo vpnInfo = this.mVpn.getNetworkInfo();
            info.setDetailedState(vpnInfo.getDetailedState(), vpnInfo.getReason(), null);
        }
    }

    private void showNotification(int titleRes, int iconRes) {
        NotificationManager.from(this.mContext).notify(TAG, 0, new Builder(this.mContext).setWhen(0).setSmallIcon(iconRes).setContentTitle(this.mContext.getString(titleRes)).setContentText(this.mContext.getString(17040524)).setContentIntent(this.mConfigIntent).setPriority(-1).setOngoing(true).addAction(17302518, this.mContext.getString(17040527), this.mResetIntent).setColor(this.mContext.getColor(17170523)).build());
    }

    private void hideNotification() {
        NotificationManager.from(this.mContext).cancel(TAG, 0);
    }
}
