package com.android.server.wm;

import android.util.ArraySet;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import com.android.server.display.OppoBrightUtils;

/* access modifiers changed from: package-private */
public class RefreshRatePolicy {
    private final HighRefreshRateBlacklist mHighRefreshRateBlacklist;
    private final int mLowRefreshRateId;
    private final ArraySet<String> mNonHighRefreshRatePackages = new ArraySet<>();
    private final WindowManagerService mWmService;

    RefreshRatePolicy(WindowManagerService wmService, DisplayInfo displayInfo, HighRefreshRateBlacklist blacklist) {
        this.mLowRefreshRateId = findLowRefreshRateModeId(displayInfo);
        this.mHighRefreshRateBlacklist = blacklist;
        this.mWmService = wmService;
    }

    private int findLowRefreshRateModeId(DisplayInfo displayInfo) {
        Display.Mode mode = displayInfo.getDefaultMode();
        float[] refreshRates = displayInfo.getDefaultRefreshRates();
        float bestRefreshRate = mode.getRefreshRate();
        for (int i = refreshRates.length - 1; i >= 0; i--) {
            if (refreshRates[i] >= 60.0f && refreshRates[i] < bestRefreshRate) {
                bestRefreshRate = refreshRates[i];
            }
        }
        return displayInfo.findDefaultModeByRefreshRate(bestRefreshRate);
    }

    /* access modifiers changed from: package-private */
    public void addNonHighRefreshRatePackage(String packageName) {
        this.mNonHighRefreshRatePackages.add(packageName);
        this.mWmService.requestTraversal();
    }

    /* access modifiers changed from: package-private */
    public void removeNonHighRefreshRatePackage(String packageName) {
        this.mNonHighRefreshRatePackages.remove(packageName);
        this.mWmService.requestTraversal();
    }

    /* access modifiers changed from: package-private */
    public int getPreferredModeId(WindowState w) {
        if (w.isAnimating()) {
            return 0;
        }
        if (w.mAttrs.preferredRefreshRate != OppoBrightUtils.MIN_LUX_LIMITI || w.mAttrs.preferredDisplayModeId != 0) {
            return w.mAttrs.preferredDisplayModeId;
        }
        String packageName = w.getOwningPackage();
        if (this.mNonHighRefreshRatePackages.contains(packageName)) {
            return this.mLowRefreshRateId;
        }
        Slog.e("RefreshRatePolicy", "getPreferredModeId mWmService.isHighRefreshBlackListOn :" + this.mWmService.isHighRefreshBlackListOn);
        if (!this.mWmService.isHighRefreshBlackListOn || !this.mHighRefreshRateBlacklist.isBlacklisted(packageName)) {
            return 0;
        }
        return this.mLowRefreshRateId;
    }
}
