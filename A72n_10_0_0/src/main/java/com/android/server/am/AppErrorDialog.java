package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.BidiFormatter;
import android.view.View;
import android.view.WindowManager;

/* access modifiers changed from: package-private */
public final class AppErrorDialog extends BaseErrorDialog implements View.OnClickListener {
    static int ALREADY_SHOWING = -3;
    static final int APP_INFO = 8;
    static int BACKGROUND_USER = -2;
    static final int CANCEL = 7;
    static int CANT_SHOW = -1;
    static final long DISMISS_TIMEOUT = 300000;
    static final int FORCE_QUIT = 1;
    static final int FORCE_QUIT_AND_REPORT = 2;
    static final int MUTE = 5;
    static final int RESTART = 3;
    static final int TIMEOUT = 6;
    private final Handler mHandler = new Handler() {
        /* class com.android.server.am.AppErrorDialog.AnonymousClass1 */

        public void handleMessage(Message msg) {
            AppErrorDialog.this.setResult(msg.what);
            AppErrorDialog.this.dismiss();
        }
    };
    private final boolean mIsRestartable;
    private final ProcessRecord mProc;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.AppErrorDialog.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                AppErrorDialog.this.cancel();
            }
        }
    };
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    public AppErrorDialog(Context context, ActivityManagerService service, Data data) {
        super(context);
        int i;
        CharSequence name;
        int i2;
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = data.proc;
        this.mResult = data.result;
        this.mIsRestartable = (data.taskId != -1 || data.isRestartableForService) && Settings.Global.getInt(context.getContentResolver(), "show_restart_in_crash_dialog", 0) != 0;
        BidiFormatter bidi = BidiFormatter.getInstance();
        if (this.mProc.pkgList.size() != 1 || (name = context.getPackageManager().getApplicationLabel(this.mProc.info)) == null) {
            CharSequence name2 = this.mProc.processName;
            if (data.repeating) {
                i = 17039476;
            } else {
                i = 17039475;
            }
            setTitle(res.getString(i, bidi.unicodeWrap(name2.toString())));
        } else {
            if (data.repeating) {
                i2 = 17039471;
            } else {
                i2 = 17039470;
            }
            setTitle(res.getString(i2, bidi.unicodeWrap(name.toString()), bidi.unicodeWrap(this.mProc.info.processName)));
        }
        setCancelable(true);
        setCancelMessage(this.mHandler.obtainMessage(7));
        if (this.mProc.errorReportReceiver != null) {
            setButton(-2, res.getText(17040923), this.mHandler.obtainMessage(2));
        }
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.setTitle("Application Error: " + this.mProc.info.processName);
        attrs.privateFlags = attrs.privateFlags | 272;
        getWindow().setAttributes(attrs);
        if (this.mProc.isPersistent()) {
            getWindow().setType(2010);
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(6), 300000);
    }

    @Override // com.android.server.am.BaseErrorDialog
    public void onStart() {
        super.onStart();
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.mReceiver);
    }

    public void dismiss() {
        if (!this.mResult.mHasResult) {
            setResult(1);
        }
        super.dismiss();
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setResult(int result) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mProc != null && this.mProc.crashDialog == this) {
                    this.mProc.crashDialog = null;
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        this.mResult.set(result);
        this.mHandler.removeMessages(6);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16908703:
                this.mHandler.obtainMessage(8).sendToTarget();
                return;
            case 16908704:
                this.mHandler.obtainMessage(1).sendToTarget();
                return;
            case 16908705:
                this.mHandler.obtainMessage(5).sendToTarget();
                return;
            case 16908706:
                this.mHandler.obtainMessage(2).sendToTarget();
                return;
            case 16908707:
                this.mHandler.obtainMessage(3).sendToTarget();
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Data {
        boolean isRestartableForService;
        ProcessRecord proc;
        boolean repeating;
        AppErrorResult result;
        int taskId;

        Data() {
        }
    }
}
