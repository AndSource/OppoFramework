package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.OppoRlog;

public class SmsStorageMonitor extends Handler {
    private static final int EVENT_ICC_FULL = 1;
    protected static final int EVENT_RADIO_ON = 3;
    protected static final int EVENT_REPORT_MEMORY_STATUS_DONE = 2;
    private static final String TAG = "SmsStorageMonitor";
    protected static final int WAKE_LOCK_TIMEOUT = 5000;
    @UnsupportedAppUsage
    protected final CommandsInterface mCi;
    protected final Context mContext;
    protected Phone mPhone;
    private boolean mReportMemoryStatusPending;
    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.SmsStorageMonitor.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.DEVICE_STORAGE_FULL")) {
                SmsStorageMonitor smsStorageMonitor = SmsStorageMonitor.this;
                smsStorageMonitor.mStorageAvailable = false;
                smsStorageMonitor.mCi.reportSmsMemoryStatus(false, SmsStorageMonitor.this.obtainMessage(2));
            } else if (intent.getAction().equals("android.intent.action.DEVICE_STORAGE_NOT_FULL")) {
                SmsStorageMonitor smsStorageMonitor2 = SmsStorageMonitor.this;
                smsStorageMonitor2.mStorageAvailable = true;
                smsStorageMonitor2.mCi.reportSmsMemoryStatus(true, SmsStorageMonitor.this.obtainMessage(2));
            }
        }
    };
    protected boolean mStorageAvailable = true;
    protected PowerManager.WakeLock mWakeLock;

    public SmsStorageMonitor(Phone phone) {
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mCi = phone.mCi;
        createWakelock();
        this.mCi.setOnIccSmsFull(this, 1, null);
        this.mCi.registerForOn(this, 3, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.DEVICE_STORAGE_FULL");
        filter.addAction("android.intent.action.DEVICE_STORAGE_NOT_FULL");
        this.mContext.registerReceiver(this.mResultReceiver, filter);
    }

    public void dispose() {
        this.mCi.unSetOnIccSmsFull(this);
        this.mCi.unregisterForOn(this);
        this.mContext.unregisterReceiver(this.mResultReceiver);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            handleIccFull();
        } else if (i != 2) {
            if (i == 3 && this.mReportMemoryStatusPending) {
                OppoRlog.Rlog.v(TAG, "Sending pending memory status report : mStorageAvailable = " + this.mStorageAvailable);
                this.mCi.reportSmsMemoryStatus(this.mStorageAvailable, obtainMessage(2));
            }
        } else if (((AsyncResult) msg.obj).exception != null) {
            this.mReportMemoryStatusPending = true;
            OppoRlog.Rlog.v(TAG, "Memory status report to modem pending : mStorageAvailable = " + this.mStorageAvailable);
        } else {
            this.mReportMemoryStatusPending = false;
        }
    }

    private void createWakelock() {
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, TAG);
        this.mWakeLock.setReferenceCounted(true);
    }

    public void handleIccFull() {
        Intent intent = new Intent("android.provider.Telephony.SIM_FULL");
        intent.setComponent(SmsApplication.getDefaultSimFullApplication(this.mContext, false));
        this.mWakeLock.acquire(5000);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
    }

    public boolean isStorageAvailable() {
        return this.mStorageAvailable;
    }
}