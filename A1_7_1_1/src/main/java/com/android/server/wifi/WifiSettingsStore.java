package com.android.server.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiSettingsStore {
    private static final String TAG = "WifiSettingsStore";
    static final int WIFI_DISABLED = 0;
    private static final int WIFI_DISABLED_AIRPLANE_ON = 3;
    static final int WIFI_ENABLED = 1;
    private static final int WIFI_ENABLED_AIRPLANE_OVERRIDE = 2;
    private boolean mAirplaneModeOn = false;
    private boolean mCheckSavedStateAtBoot = false;
    private final Context mContext;
    private int mPersistWifiState = 0;
    private boolean mScanAlwaysAvailable;

    WifiSettingsStore(Context context) {
        this.mContext = context;
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        this.mPersistWifiState = getPersistedWifiState();
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    /* JADX WARNING: Missing block: B:16:0x001c, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:22:0x0024, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isWifiToggleEnabled() {
        boolean z = true;
        synchronized (this) {
            if (!this.mCheckSavedStateAtBoot) {
                this.mCheckSavedStateAtBoot = true;
                if (testAndClearWifiSavedState()) {
                    return true;
                }
            }
            if (this.mAirplaneModeOn) {
                if (this.mPersistWifiState != 2) {
                    z = false;
                }
            } else if (this.mPersistWifiState == 0) {
                z = false;
            }
        }
    }

    public synchronized boolean isAirplaneModeOn() {
        return this.mAirplaneModeOn;
    }

    public synchronized boolean isScanAlwaysAvailable() {
        return !this.mAirplaneModeOn ? this.mScanAlwaysAvailable : false;
    }

    /* JADX WARNING: Missing block: B:12:0x0018, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean handleWifiToggled(boolean wifiEnabled) {
        if (this.mAirplaneModeOn && !isAirplaneToggleable()) {
            return false;
        }
        if (!wifiEnabled) {
            persistWifiState(0);
        } else if (this.mAirplaneModeOn) {
            persistWifiState(2);
        } else {
            persistWifiState(1);
        }
    }

    /* JADX WARNING: Missing block: B:15:0x001f, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:27:0x0035, code:
            if (r4.mPersistWifiState == 3) goto L_0x002b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized boolean handleAirplaneModeToggled() {
        if (!isAirplaneSensitive()) {
            return false;
        }
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        if (!this.mAirplaneModeOn) {
            if (!(testAndClearWifiSavedState() || this.mPersistWifiState == 2)) {
            }
            persistWifiState(1);
        } else if (this.mPersistWifiState == 1) {
            persistWifiState(3);
        }
    }

    synchronized void handleWifiScanAlwaysAvailableToggled() {
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mPersistWifiState " + this.mPersistWifiState);
        pw.println("mAirplaneModeOn " + this.mAirplaneModeOn);
    }

    private void persistWifiState(int state) {
        ContentResolver cr = this.mContext.getContentResolver();
        this.mPersistWifiState = state;
        Global.putInt(cr, "wifi_on", state);
    }

    private boolean isAirplaneSensitive() {
        String airplaneModeRadios = Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        if (airplaneModeRadios != null) {
            return airplaneModeRadios.contains("wifi");
        }
        return true;
    }

    private boolean isAirplaneToggleable() {
        String toggleableRadios = Global.getString(this.mContext.getContentResolver(), "airplane_mode_toggleable_radios");
        if (toggleableRadios != null) {
            return toggleableRadios.contains("wifi");
        }
        return false;
    }

    private boolean testAndClearWifiSavedState() {
        int wifiSavedState = getWifiSavedState();
        if (wifiSavedState == 1) {
            setWifiSavedState(0);
        }
        if (wifiSavedState == 1) {
            return true;
        }
        return false;
    }

    public void setWifiSavedState(int state) {
        Global.putInt(this.mContext.getContentResolver(), "wifi_saved_state", state);
    }

    public int getWifiSavedState() {
        try {
            return Global.getInt(this.mContext.getContentResolver(), "wifi_saved_state");
        } catch (SettingNotFoundException e) {
            return 0;
        }
    }

    private int getPersistedWifiState() {
        ContentResolver cr = this.mContext.getContentResolver();
        try {
            return Global.getInt(cr, "wifi_on");
        } catch (SettingNotFoundException e) {
            Global.putInt(cr, "wifi_on", 0);
            return 0;
        }
    }

    private boolean getPersistedAirplaneModeOn() {
        return Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private boolean getPersistedScanAlwaysAvailable() {
        return Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
    }

    public boolean hasConnectableAp() {
        int persistedWifiState = getPersistedWifiState();
        Slog.d(TAG, "hasConnectableAp, mPersistWifiState:" + persistedWifiState);
        if (persistedWifiState == 0 || persistedWifiState == 3) {
            return false;
        }
        return true;
    }

    public void setCheckSavedStateAtBoot(boolean flag) {
        this.mCheckSavedStateAtBoot = flag;
    }
}
