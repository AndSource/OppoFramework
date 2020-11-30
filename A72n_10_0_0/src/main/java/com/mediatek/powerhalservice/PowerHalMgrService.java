package com.mediatek.powerhalservice;

import android.content.Context;
import android.util.Slog;
import com.android.server.SystemService;

public class PowerHalMgrService extends SystemService {
    private final String TAG = "PowerHalMgrService";
    private PowerHalMgrServiceImpl mPowerHalMgrServiceImpl;

    public PowerHalMgrService(Context context) {
        super(context);
        this.mPowerHalMgrServiceImpl = new PowerHalMgrServiceImpl(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.mediatek.powerhalservice.PowerHalMgrService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.mediatek.powerhalservice.PowerHalMgrServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onStart() {
        Slog.d("PowerHalMgrService", "Start PowerHalMgrService.");
        publishBinderService("power_hal_mgr_service", this.mPowerHalMgrServiceImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            Slog.d("PowerHalMgrService", "onBootPhase PowerHalMgrService.");
        }
    }
}
