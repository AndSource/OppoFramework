package com.android.server.am;

import android.app.ActivityManager;
import android.common.OppoFeatureCache;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ProcessList;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.IColorEyeProtectManager;
import com.android.server.notification.ZenModeHelper;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.pm.DumpState;
import com.android.server.usage.AppStandbyController;
import com.android.server.wm.ActivityServiceConnectionsHolder;
import com.android.server.wm.ActivityTaskManagerDebugConfig;
import com.android.server.wm.IColorAthenaManager;
import com.android.server.wm.WindowProcessController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public final class OomAdjuster {
    static final String OOM_ADJ_REASON_ACTIVITY = "updateOomAdj_activityChange";
    static final String OOM_ADJ_REASON_BIND_SERVICE = "updateOomAdj_bindService";
    static final String OOM_ADJ_REASON_FINISH_RECEIVER = "updateOomAdj_finishReceiver";
    static final String OOM_ADJ_REASON_GET_PROVIDER = "updateOomAdj_getProvider";
    static final String OOM_ADJ_REASON_METHOD = "updateOomAdj";
    static final String OOM_ADJ_REASON_NONE = "updateOomAdj_meh";
    static final String OOM_ADJ_REASON_PROCESS_BEGIN = "updateOomAdj_processBegin";
    static final String OOM_ADJ_REASON_PROCESS_END = "updateOomAdj_processEnd";
    static final String OOM_ADJ_REASON_REMOVE_PROVIDER = "updateOomAdj_removeProvider";
    static final String OOM_ADJ_REASON_START_RECEIVER = "updateOomAdj_startReceiver";
    static final String OOM_ADJ_REASON_START_SERVICE = "updateOomAdj_startService";
    static final String OOM_ADJ_REASON_UI_VISIBILITY = "updateOomAdj_uiVisibility";
    static final String OOM_ADJ_REASON_UNBIND_SERVICE = "updateOomAdj_unbindService";
    static final String OOM_ADJ_REASON_WHITELIST = "updateOomAdj_whitelistChange";
    private static final String TAG = "OomAdjuster";
    ActiveUids mActiveUids;
    int mAdjSeq = 0;
    AppCompactor mAppCompact;
    ActivityManagerConstants mConstants;
    private int mLastTopUid = 0;
    PowerManagerInternal mLocalPowerManager;
    int mNewNumAServiceProcs = 0;
    int mNewNumServiceProcs = 0;
    int mNumCachedHiddenProcs = 0;
    int mNumNonCachedProcs = 0;
    int mNumServiceProcs = 0;
    private final Handler mProcessGroupHandler;
    private final ProcessList mProcessList;
    private final ActivityManagerService mService;
    private final ArraySet<BroadcastQueue> mTmpBroadcastQueue = new ArraySet<>();
    private final ComputeOomAdjWindowCallback mTmpComputeOomAdjWindowCallback = new ComputeOomAdjWindowCallback();
    final long[] mTmpLong = new long[3];

    OomAdjuster(ActivityManagerService service, ProcessList processList, ActiveUids activeUids) {
        this.mService = service;
        this.mProcessList = processList;
        this.mActiveUids = activeUids;
        this.mLocalPowerManager = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mConstants = this.mService.mConstants;
        this.mAppCompact = new AppCompactor(this.mService);
        ServiceThread adjusterThread = new ServiceThread(TAG, -10, false);
        adjusterThread.start();
        Process.setThreadGroupAndCpuset(adjusterThread.getThreadId(), 5);
        this.mProcessGroupHandler = new Handler(adjusterThread.getLooper(), $$Lambda$OomAdjuster$OVkqAAacT5taN3pgDzyZj3Ymvk.INSTANCE);
    }

    static /* synthetic */ boolean lambda$new$0(Message msg) {
        int pid = msg.arg1;
        int group = msg.arg2;
        Trace.traceBegin(64, "setProcessGroup " + pid + StringUtils.SPACE + group);
        try {
            Process.setProcessGroup(pid, group);
        } catch (Exception e) {
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.w(TAG, "Failed setting process group of " + pid + " to " + group, e);
            }
        } catch (Throwable th) {
            Trace.traceEnd(64);
            throw th;
        }
        Trace.traceEnd(64);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void initSettings() {
        this.mAppCompact.init();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean updateOomAdjLocked(ProcessRecord app, boolean oomAdjAll, String oomAdjReason) {
        ProcessRecord TOP_APP = this.mService.getTopAppLocked();
        boolean wasCached = app.cached;
        this.mAdjSeq++;
        boolean success = updateOomAdjLocked(app, app.getCurRawAdj() >= 900 ? app.getCurRawAdj() : 1001, TOP_APP, false, SystemClock.uptimeMillis());
        if (oomAdjAll && (wasCached != app.cached || app.getCurRawAdj() == 1001)) {
            updateOomAdjLocked(oomAdjReason);
        }
        return success;
    }

    @GuardedBy({"mService"})
    private final boolean updateOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now) {
        if (app.thread == null) {
            return false;
        }
        computeOomAdjLocked(app, cachedAdj, TOP_APP, doingAll, now, false);
        return applyOomAdjLocked(app, doingAll, now, SystemClock.elapsedRealtime());
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r3v1 boolean: [D('lastCachedGroupUid' int), D('lastCachedGroup' int), D('allChanged' boolean)] */
    /* JADX INFO: Multiple debug info for r4v10 int: [D('lastCachedGroup' int), D('numCached' int)] */
    /* JADX INFO: Multiple debug info for r5v6 int: [D('numTrimming' int), D('numCached' int)] */
    /* JADX INFO: Multiple debug info for r2v7 int: [D('lastCachedGroupUid' int), D('numEmpty' int)] */
    /* JADX INFO: Multiple debug info for r1v60 int: [D('stepEmpty' int), D('retryCycles' boolean)] */
    /* JADX INFO: Multiple debug info for r7v46 'lastCachedGroupImportance'  int: [D('emptyFactor' int), D('lastCachedGroupImportance' int)] */
    /* JADX WARN: Type inference failed for: r10v5 */
    /* JADX WARN: Type inference failed for: r10v6, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r10v9 */
    /* JADX WARN: Type inference failed for: r4v52, types: [int] */
    /* JADX WARN: Type inference failed for: r1v41, types: [int] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 3 */
    @GuardedBy({"mService"})
    public void updateOomAdjLocked(String oomAdjReason) {
        int emptyProcessLimit;
        int numCached;
        int numTrimming;
        boolean z;
        int uidChange;
        int cachedProcessLimit;
        int emptyProcessLimit2;
        int lastCachedGroup;
        int numEmpty;
        ProcessRecord app;
        boolean z2;
        boolean z3;
        boolean z4;
        int nextCachedAdj;
        int curCachedAdj;
        int lastCachedGroupImportance;
        int lastCachedGroup2;
        int i;
        int cycleCount;
        int lastCachedGroupImportance2;
        int curEmptyAdj;
        int lastCachedGroupUid;
        boolean z5;
        int emptyFactor;
        int emptyProcessLimit3;
        int cachedProcessLimit2;
        long nowElapsed;
        boolean z6;
        int numEmptyProcs;
        int numEmptyProcs2;
        int lastCachedGroupImportance3;
        int lastCachedGroupUid2;
        int lastCachedGroupImportance4;
        int lastCachedGroup3;
        int lastCachedGroupImportance5;
        int nextCachedAdj2;
        int lastCachedGroupImportance6;
        int nextCachedAdj3;
        int lastCachedGroupImportance7;
        int lastCachedGroupImportance8;
        boolean retryCycles;
        int stepEmpty;
        Trace.traceBegin(64, oomAdjReason);
        this.mService.mOomAdjProfiler.oomAdjStarted();
        ProcessRecord TOP_APP = this.mService.getTopAppLocked();
        long now = SystemClock.uptimeMillis();
        long nowElapsed2 = SystemClock.elapsedRealtime();
        long oldTime = now - 1800000;
        int emptyFactor2 = this.mProcessList.getLruSizeLocked();
        for (int i2 = this.mActiveUids.size() - 1; i2 >= 0; i2--) {
            this.mActiveUids.valueAt(i2).reset();
        }
        if (this.mService.mAtmInternal != null) {
            this.mService.mAtmInternal.rankTaskLayersIfNeeded();
        }
        this.mAdjSeq++;
        boolean allChanged = false;
        this.mNewNumServiceProcs = 0;
        this.mNewNumAServiceProcs = 0;
        int emptyProcessLimit4 = this.mConstants.CUR_MAX_EMPTY_PROCESSES;
        int cachedProcessLimit3 = this.mConstants.CUR_MAX_CACHED_PROCESSES - emptyProcessLimit4;
        int numEmptyProcs3 = (emptyFactor2 - this.mNumNonCachedProcs) - this.mNumCachedHiddenProcs;
        int numEmptyProcs4 = numEmptyProcs3 > cachedProcessLimit3 ? cachedProcessLimit3 : numEmptyProcs3;
        int emptyFactor3 = ((numEmptyProcs4 + 10) - 1) / 10;
        if (emptyFactor3 < 1) {
            emptyFactor3 = 1;
        }
        int i3 = this.mNumCachedHiddenProcs;
        int cachedFactor = (i3 > 0 ? (i3 + 10) - 1 : 1) / 10;
        if (cachedFactor < 1) {
            cachedFactor = 1;
        }
        int stepCached = -1;
        int stepEmpty2 = -1;
        int numCachedExtraGroup = 0;
        this.mNumNonCachedProcs = 0;
        this.mNumCachedHiddenProcs = 0;
        int nextCachedAdj4 = 900 + 10;
        int curCachedImpAdj = 0;
        int nextEmptyAdj = 905 + 10;
        boolean retryCycles2 = false;
        int i4 = emptyFactor2 - 1;
        while (true) {
            emptyProcessLimit = emptyProcessLimit4;
            if (i4 < 0) {
                break;
            }
            ProcessRecord app2 = this.mProcessList.mLruProcesses.get(i4);
            app2.containsCycle = false;
            app2.setCurRawProcState(20);
            app2.setCurRawAdj(1001);
            i4--;
            emptyProcessLimit4 = emptyProcessLimit;
        }
        int i5 = emptyFactor2 - 1;
        int lastCachedGroup4 = 0;
        int lastCachedGroupImportance9 = 0;
        int lastCachedGroupUid3 = 0;
        int curCachedAdj2 = 900;
        int nextCachedAdj5 = nextCachedAdj4;
        int nextCachedAdj6 = 905;
        int nextEmptyAdj2 = nextEmptyAdj;
        while (i5 >= 0) {
            ProcessRecord app3 = this.mProcessList.mLruProcesses.get(i5);
            if (app3.killedByAm || app3.thread == null) {
                cachedProcessLimit2 = cachedProcessLimit3;
                nowElapsed = nowElapsed2;
                numEmptyProcs = numEmptyProcs4;
                emptyProcessLimit3 = emptyProcessLimit;
                z6 = false;
                numEmptyProcs2 = emptyFactor2;
                lastCachedGroupImportance3 = emptyFactor3;
                nextEmptyAdj2 = nextEmptyAdj2;
                nextCachedAdj5 = nextCachedAdj5;
                curCachedAdj2 = curCachedAdj2;
                lastCachedGroup4 = lastCachedGroup4;
                lastCachedGroupUid3 = lastCachedGroupUid3;
                lastCachedGroupImportance9 = lastCachedGroupImportance9;
                nextCachedAdj6 = nextCachedAdj6;
            } else {
                app3.procStateChanged = false;
                cachedProcessLimit2 = cachedProcessLimit3;
                emptyProcessLimit3 = emptyProcessLimit;
                z6 = false;
                nowElapsed = nowElapsed2;
                numEmptyProcs = numEmptyProcs4;
                numEmptyProcs2 = emptyFactor2;
                computeOomAdjLocked(app3, 1001, TOP_APP, true, now, false);
                boolean retryCycles3 = retryCycles2 | app3.containsCycle;
                if (app3.curAdj >= 1001) {
                    switch (app3.getCurProcState()) {
                        case 17:
                        case 18:
                        case 19:
                            boolean inGroup = false;
                            inGroup = false;
                            if (app3.connectionGroup != 0) {
                                if (lastCachedGroupUid3 == app3.uid) {
                                    lastCachedGroup3 = lastCachedGroup4;
                                    if (lastCachedGroup3 == app3.connectionGroup) {
                                        lastCachedGroupUid2 = lastCachedGroupUid3;
                                        if (app3.connectionImportance > lastCachedGroupImportance9) {
                                            int lastCachedGroupImportance10 = app3.connectionImportance;
                                            nextCachedAdj2 = nextCachedAdj5;
                                            if (curCachedAdj2 >= nextCachedAdj2 || curCachedAdj2 >= 999) {
                                                lastCachedGroupImportance7 = lastCachedGroupImportance10;
                                                lastCachedGroupImportance5 = curCachedAdj2;
                                            } else {
                                                curCachedImpAdj++;
                                                lastCachedGroupImportance7 = lastCachedGroupImportance10;
                                                lastCachedGroupImportance5 = curCachedAdj2;
                                            }
                                        } else {
                                            lastCachedGroupImportance5 = curCachedAdj2;
                                            nextCachedAdj2 = nextCachedAdj5;
                                            lastCachedGroupImportance7 = lastCachedGroupImportance9;
                                        }
                                        inGroup = true;
                                        lastCachedGroupImportance4 = lastCachedGroupImportance7;
                                    } else {
                                        lastCachedGroupImportance6 = curCachedAdj2;
                                        nextCachedAdj3 = nextCachedAdj5;
                                    }
                                } else {
                                    lastCachedGroupImportance6 = curCachedAdj2;
                                    nextCachedAdj3 = nextCachedAdj5;
                                }
                                int lastCachedGroupUid4 = app3.uid;
                                lastCachedGroup3 = app3.connectionGroup;
                                lastCachedGroupImportance4 = app3.connectionImportance;
                                lastCachedGroupUid2 = lastCachedGroupUid4;
                            } else {
                                lastCachedGroupUid2 = lastCachedGroupUid3;
                                lastCachedGroup3 = lastCachedGroup4;
                                lastCachedGroupImportance4 = lastCachedGroupImportance9;
                                lastCachedGroupImportance5 = curCachedAdj2;
                                nextCachedAdj2 = nextCachedAdj5;
                            }
                            if (inGroup || lastCachedGroupImportance5 == nextCachedAdj2) {
                                curCachedAdj2 = lastCachedGroupImportance5;
                            } else {
                                int stepCached2 = stepCached + 1;
                                curCachedImpAdj = 0;
                                curCachedImpAdj = 0;
                                curCachedImpAdj = 0;
                                if (stepCached2 >= cachedFactor) {
                                    stepCached = 0;
                                    stepCached = 0;
                                    curCachedAdj2 = nextCachedAdj2;
                                    int nextCachedAdj7 = nextCachedAdj2 + 10;
                                    if (nextCachedAdj7 > 999) {
                                        nextCachedAdj2 = 999;
                                    } else {
                                        nextCachedAdj2 = nextCachedAdj7;
                                    }
                                } else {
                                    stepCached = stepCached2;
                                    curCachedAdj2 = lastCachedGroupImportance5;
                                }
                            }
                            app3.setCurRawAdj(curCachedAdj2 + curCachedImpAdj);
                            app3.curAdj = app3.modifyRawOomAdj(curCachedAdj2 + curCachedImpAdj);
                            boolean z7 = ActivityManagerDebugConfig.DEBUG_LRU;
                            retryCycles2 = retryCycles3;
                            nextCachedAdj5 = nextCachedAdj2;
                            lastCachedGroup4 = lastCachedGroup3;
                            lastCachedGroupImportance9 = lastCachedGroupImportance4;
                            lastCachedGroupUid3 = lastCachedGroupUid2;
                            lastCachedGroupImportance3 = emptyFactor3;
                            nextCachedAdj6 = nextCachedAdj6;
                            continue;
                        default:
                            int nextEmptyAdj3 = nextEmptyAdj2;
                            if (nextCachedAdj6 != nextEmptyAdj3) {
                                retryCycles = retryCycles3;
                                int stepEmpty3 = stepEmpty2 + 1;
                                lastCachedGroupImportance8 = lastCachedGroupImportance9;
                                lastCachedGroupImportance3 = emptyFactor3;
                                if (stepEmpty3 >= lastCachedGroupImportance3) {
                                    stepEmpty2 = 0;
                                    stepEmpty2 = 0;
                                    stepEmpty = nextEmptyAdj3;
                                    nextEmptyAdj3 += 10;
                                    if (nextEmptyAdj3 > 999) {
                                        nextEmptyAdj3 = 999;
                                    }
                                } else {
                                    stepEmpty2 = stepEmpty3;
                                    stepEmpty = nextCachedAdj6;
                                }
                            } else {
                                retryCycles = retryCycles3;
                                lastCachedGroupImportance8 = lastCachedGroupImportance9;
                                lastCachedGroupImportance3 = emptyFactor3;
                                stepEmpty = nextCachedAdj6;
                            }
                            app3.setCurRawAdj(stepEmpty);
                            app3.curAdj = app3.modifyRawOomAdj(stepEmpty);
                            boolean z8 = ActivityManagerDebugConfig.DEBUG_LRU;
                            nextEmptyAdj2 = nextEmptyAdj3;
                            nextCachedAdj5 = nextCachedAdj5;
                            curCachedAdj2 = curCachedAdj2;
                            lastCachedGroup4 = lastCachedGroup4;
                            lastCachedGroupUid3 = lastCachedGroupUid3;
                            retryCycles2 = retryCycles;
                            lastCachedGroupImportance9 = lastCachedGroupImportance8;
                            nextCachedAdj6 = stepEmpty;
                            continue;
                    }
                } else {
                    lastCachedGroupImportance3 = emptyFactor3;
                    retryCycles2 = retryCycles3;
                    nextCachedAdj6 = nextCachedAdj6;
                }
            }
            i5--;
            emptyFactor3 = lastCachedGroupImportance3;
            emptyFactor2 = numEmptyProcs2;
            numEmptyProcs4 = numEmptyProcs;
            allChanged = z6;
            nowElapsed2 = nowElapsed;
            cachedProcessLimit3 = cachedProcessLimit2;
            emptyProcessLimit = emptyProcessLimit3;
        }
        int cachedProcessLimit4 = cachedProcessLimit3;
        int curEmptyAdj2 = nextCachedAdj6;
        long nowElapsed3 = nowElapsed2;
        int emptyProcessLimit5 = emptyProcessLimit;
        int nextEmptyAdj4 = nextEmptyAdj2;
        int emptyFactor4 = lastCachedGroupUid3;
        int lastCachedGroup5 = lastCachedGroup4;
        int lastCachedGroupUid5 = lastCachedGroupImportance9;
        int curCachedAdj3 = curCachedAdj2;
        int nextCachedAdj8 = nextCachedAdj5;
        int emptyFactor5 = emptyFactor3;
        int cycleCount2 = 0;
        while (retryCycles2 && cycleCount2 < 10) {
            int cycleCount3 = cycleCount2 + 1;
            boolean retryCycles4 = false;
            int i6 = 0;
            while (i6 < emptyFactor2) {
                ProcessRecord app4 = this.mProcessList.mLruProcesses.get(i6);
                if (app4.killedByAm || app4.thread == null) {
                    emptyFactor = emptyFactor5;
                } else {
                    emptyFactor = emptyFactor5;
                    if (app4.containsCycle) {
                        app4.adjSeq--;
                        app4.completedAdjSeq--;
                    }
                }
                i6++;
                retryCycles4 = retryCycles4;
                nextEmptyAdj4 = nextEmptyAdj4;
                emptyFactor5 = emptyFactor;
            }
            int emptyFactor6 = emptyFactor5;
            boolean z9 = true;
            int i7 = 0;
            retryCycles2 = retryCycles4;
            while (i7 < emptyFactor2) {
                ProcessRecord app5 = this.mProcessList.mLruProcesses.get(i7);
                if (app5.killedByAm || app5.thread == null || app5.containsCycle != z9) {
                    i = i7;
                    curEmptyAdj = curEmptyAdj2;
                    nextCachedAdj = nextCachedAdj8;
                    curCachedAdj = curCachedAdj3;
                    lastCachedGroup2 = lastCachedGroup5;
                    cycleCount = cycleCount3;
                    lastCachedGroupImportance = lastCachedGroupUid5;
                    z5 = z9;
                    lastCachedGroupImportance2 = emptyFactor4;
                    lastCachedGroupUid = emptyFactor6;
                } else {
                    i = i7;
                    curEmptyAdj = curEmptyAdj2;
                    nextCachedAdj = nextCachedAdj8;
                    curCachedAdj = curCachedAdj3;
                    lastCachedGroup2 = lastCachedGroup5;
                    cycleCount = cycleCount3;
                    lastCachedGroupImportance = lastCachedGroupUid5;
                    z5 = z9;
                    lastCachedGroupImportance2 = emptyFactor4;
                    lastCachedGroupUid = emptyFactor6;
                    if (computeOomAdjLocked(app5, app5.getCurRawAdj(), TOP_APP, true, now, true)) {
                        retryCycles2 = true;
                    }
                }
                i7 = i + 1;
                z9 = z5;
                emptyFactor6 = lastCachedGroupUid;
                curEmptyAdj2 = curEmptyAdj;
                emptyFactor4 = lastCachedGroupImportance2;
                cycleCount3 = cycleCount;
                lastCachedGroup5 = lastCachedGroup2;
                lastCachedGroupUid5 = lastCachedGroupImportance;
                curCachedAdj3 = curCachedAdj;
                nextCachedAdj8 = nextCachedAdj;
            }
            emptyFactor5 = emptyFactor6;
            nextEmptyAdj4 = nextEmptyAdj4;
            emptyFactor4 = emptyFactor4;
            cycleCount2 = cycleCount3;
            lastCachedGroupUid5 = lastCachedGroupUid5;
        }
        ? r10 = 1;
        int i8 = emptyFactor2 - 1;
        boolean z10 = allChanged;
        boolean z11 = allChanged;
        int numTrimming2 = 0;
        int lastCachedGroupUid6 = 0;
        int emptyProcessLimit6 = 0;
        while (i8 >= 0) {
            ProcessRecord app6 = this.mProcessList.mLruProcesses.get(i8);
            if (app6.killedByAm || app6.thread == null) {
                cachedProcessLimit = cachedProcessLimit4;
                emptyProcessLimit2 = emptyProcessLimit5;
                z11 = z11;
                lastCachedGroup = emptyProcessLimit6;
                numTrimming2 = numTrimming2;
                lastCachedGroupUid6 = lastCachedGroupUid6;
                z10 = z10;
            } else {
                boolean z12 = z10;
                boolean z13 = z11;
                int numCached2 = numTrimming2;
                applyOomAdjLocked(app6, true, now, nowElapsed3);
                int curProcState = app6.getCurProcState();
                if (curProcState == 17 || curProcState == 18) {
                    app = app6;
                    emptyProcessLimit2 = emptyProcessLimit5;
                    numEmpty = lastCachedGroupUid6;
                    this.mNumCachedHiddenProcs += r10;
                    int numCached3 = numCached2 + 1;
                    if (app.connectionGroup != 0) {
                        if (z12 == app.info.uid) {
                            if (z13 == app.connectionGroup) {
                                numCachedExtraGroup++;
                                z2 = z13;
                                z3 = z12;
                            }
                        }
                        ? r4 = app.info.uid;
                        z2 = app.connectionGroup;
                        z3 = r4;
                    } else {
                        z2 = allChanged;
                        z3 = allChanged;
                    }
                    cachedProcessLimit = cachedProcessLimit4;
                    if (numCached3 - numCachedExtraGroup > cachedProcessLimit) {
                        StringBuilder sb = new StringBuilder();
                        z4 = z3;
                        sb.append("cached #");
                        sb.append(numCached3);
                        app.kill(sb.toString(), true);
                    } else {
                        z4 = z3;
                    }
                    z13 = z2;
                    numCached2 = numCached3;
                    z12 = z4;
                } else {
                    if (curProcState != 20) {
                        this.mNumNonCachedProcs += r10;
                        app = app6;
                        numEmpty = lastCachedGroupUid6;
                    } else if (OppoFeatureCache.get(IColorAthenaManager.DEFAULT).skipAmsEmptyKill(app6.getWindowProcessController())) {
                        app = app6;
                        numEmpty = lastCachedGroupUid6;
                    } else {
                        numEmpty = lastCachedGroupUid6;
                        if (numEmpty > this.mConstants.CUR_TRIM_EMPTY_PROCESSES) {
                            app = app6;
                            if (app.lastActivityTime < oldTime) {
                                app.kill("empty for " + (((oldTime + 1800000) - app.lastActivityTime) / 1000) + "s", r10);
                            }
                        } else {
                            app = app6;
                        }
                        int numEmpty2 = numEmpty + 1;
                        emptyProcessLimit2 = emptyProcessLimit5;
                        if (numEmpty2 > emptyProcessLimit2 && !OppoFeatureCache.get(IColorAthenaManager.DEFAULT).skipAmsEmptyKillBootUp(app.getWindowProcessController())) {
                            app.kill("empty #" + numEmpty2, r10);
                        }
                        numEmpty = numEmpty2;
                        cachedProcessLimit = cachedProcessLimit4;
                    }
                    cachedProcessLimit = cachedProcessLimit4;
                    emptyProcessLimit2 = emptyProcessLimit5;
                }
                if (!app.isolated || app.services.size() > 0 || app.isolatedEntryPoint != null) {
                    UidRecord uidRec = app.uidRecord;
                    if (uidRec != null) {
                        uidRec.ephemeral = app.info.isInstantApp();
                        if (uidRec.getCurProcState() > app.getCurProcState()) {
                            uidRec.setCurProcState(app.getCurProcState());
                        }
                        if (app.hasForegroundServices()) {
                            uidRec.foregroundServices = true;
                        }
                    }
                } else {
                    app.kill("isolated not needed", true);
                }
                if (app.getCurProcState() < 15 || app.killedByAm) {
                    lastCachedGroupUid6 = numEmpty;
                    lastCachedGroup = emptyProcessLimit6;
                    z10 = z12;
                    z11 = z13;
                    numTrimming2 = numCached2;
                } else {
                    lastCachedGroup = emptyProcessLimit6 + 1;
                    lastCachedGroupUid6 = numEmpty;
                    z10 = z12;
                    z11 = z13;
                    numTrimming2 = numCached2;
                }
            }
            i8--;
            emptyProcessLimit5 = emptyProcessLimit2;
            cachedProcessLimit4 = cachedProcessLimit;
            r10 = 1;
            emptyProcessLimit6 = lastCachedGroup;
        }
        int numCached4 = numTrimming2;
        int numTrimming3 = emptyProcessLimit6;
        int emptyProcessLimit7 = emptyProcessLimit5;
        int numEmpty3 = lastCachedGroupUid6;
        this.mService.incrementProcStateSeqAndNotifyAppsLocked();
        this.mNumServiceProcs = this.mNewNumServiceProcs;
        boolean allChanged2 = this.mService.updateLowMemStateLocked(numCached4, numEmpty3, numTrimming3);
        if (this.mService.mAlwaysFinishActivities) {
            this.mService.mAtmInternal.scheduleDestroyAllActivities("always-finish");
        }
        if (allChanged2) {
            ActivityManagerService activityManagerService = this.mService;
            activityManagerService.requestPssAllProcsLocked(now, allChanged, activityManagerService.mProcessStats.isMemFactorLowered());
        }
        ArrayList<UidRecord> becameIdle = null;
        PowerManagerInternal powerManagerInternal = this.mLocalPowerManager;
        if (powerManagerInternal != null) {
            powerManagerInternal.startUidChanges();
        }
        int i9 = this.mActiveUids.size() - 1;
        while (i9 >= 0) {
            UidRecord uidRec2 = this.mActiveUids.valueAt(i9);
            int uidChange2 = 0;
            uidChange2 = 0;
            if (uidRec2.getCurProcState() == 21) {
                numCached = numCached4;
                numTrimming = numTrimming3;
            } else if (uidRec2.setProcState == uidRec2.getCurProcState() && uidRec2.setWhitelist == uidRec2.curWhitelist) {
                numCached = numCached4;
                numTrimming = numTrimming3;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                    String str = ActivityManagerService.TAG_UID_OBSERVERS;
                    StringBuilder sb2 = new StringBuilder();
                    numCached = numCached4;
                    sb2.append("Changes in ");
                    sb2.append(uidRec2);
                    sb2.append(": proc state from ");
                    sb2.append(uidRec2.setProcState);
                    sb2.append(" to ");
                    numTrimming = numTrimming3;
                    sb2.append(uidRec2.getCurProcState());
                    sb2.append(", whitelist from ");
                    sb2.append(uidRec2.setWhitelist);
                    sb2.append(" to ");
                    sb2.append(uidRec2.curWhitelist);
                    Slog.i(str, sb2.toString());
                } else {
                    numCached = numCached4;
                    numTrimming = numTrimming3;
                }
                if (!ActivityManager.isProcStateBackground(uidRec2.getCurProcState()) || uidRec2.curWhitelist) {
                    if (uidRec2.idle) {
                        uidChange2 = 4;
                        EventLogTags.writeAmUidActive(uidRec2.uid);
                        z = false;
                        uidRec2.idle = false;
                    } else {
                        z = false;
                    }
                    uidRec2.lastBackgroundTime = 0;
                } else {
                    if (!ActivityManager.isProcStateBackground(uidRec2.setProcState) || uidRec2.setWhitelist) {
                        uidRec2.lastBackgroundTime = nowElapsed3;
                        if (!this.mService.mHandler.hasMessages(58)) {
                            nowElapsed3 = nowElapsed3;
                            this.mService.mHandler.sendEmptyMessageDelayed(58, this.mConstants.BACKGROUND_SETTLE_TIME);
                        } else {
                            nowElapsed3 = nowElapsed3;
                        }
                    }
                    if (!uidRec2.idle || uidRec2.setIdle) {
                        z = false;
                    } else {
                        uidChange2 = 2;
                        if (becameIdle == null) {
                            becameIdle = new ArrayList<>();
                        }
                        becameIdle.add(uidRec2);
                        z = false;
                    }
                }
                boolean wasCached = uidRec2.setProcState > 12 ? true : z;
                boolean isCached = uidRec2.getCurProcState() > 12 ? true : z;
                if (wasCached != isCached || uidRec2.setProcState == 21) {
                    uidChange = uidChange2 | (isCached ? 8 : 16);
                } else {
                    uidChange = uidChange2;
                }
                uidRec2.setProcState = uidRec2.getCurProcState();
                uidRec2.setWhitelist = uidRec2.curWhitelist;
                uidRec2.setIdle = uidRec2.idle;
                this.mService.mAtmInternal.onUidProcStateChanged(uidRec2.uid, uidRec2.setProcState);
                this.mService.enqueueUidChangeLocked(uidRec2, -1, uidChange);
                this.mService.noteUidProcessState(uidRec2.uid, uidRec2.getCurProcState());
                if (uidRec2.foregroundServices) {
                    this.mService.mServices.foregroundServiceProcStateChangedLocked(uidRec2);
                }
                becameIdle = becameIdle;
            }
            i9--;
            emptyProcessLimit7 = emptyProcessLimit7;
            numTrimming3 = numTrimming;
            numCached4 = numCached;
            numEmpty3 = numEmpty3;
        }
        PowerManagerInternal powerManagerInternal2 = this.mLocalPowerManager;
        if (powerManagerInternal2 != null) {
            powerManagerInternal2.finishUidChanges();
        }
        if (becameIdle != null) {
            for (int i10 = becameIdle.size() - 1; i10 >= 0; i10--) {
                this.mService.mServices.stopInBackgroundLocked(becameIdle.get(i10).uid);
            }
        }
        if (this.mService.mProcessStats.shouldWriteNowLocked(now)) {
            ActivityManagerService.MainHandler mainHandler = this.mService.mHandler;
            ActivityManagerService activityManagerService2 = this.mService;
            mainHandler.post(new ActivityManagerService.ProcStatsRunnable(activityManagerService2, activityManagerService2.mProcessStats));
        }
        this.mService.mProcessStats.updateTrackingAssociationsLocked(this.mAdjSeq, now);
        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
            Slog.d(ActivityManagerService.TAG_OOM_ADJ, "Did OOM ADJ in " + (SystemClock.uptimeMillis() - now) + "ms");
        }
        this.mService.mOomAdjProfiler.oomAdjEnded();
        Trace.traceEnd(64);
    }

    private final class ComputeOomAdjWindowCallback implements WindowProcessController.ComputeOomAdjCallback {
        int adj;
        ProcessRecord app;
        int appUid;
        boolean foregroundActivities;
        int logUid;
        int procState;
        int processStateCurTop;
        int schedGroup;

        private ComputeOomAdjWindowCallback() {
        }

        /* access modifiers changed from: package-private */
        public void initialize(ProcessRecord app2, int adj2, boolean foregroundActivities2, int procState2, int schedGroup2, int appUid2, int logUid2, int processStateCurTop2) {
            this.app = app2;
            this.adj = adj2;
            this.foregroundActivities = foregroundActivities2;
            this.procState = procState2;
            this.schedGroup = schedGroup2;
            this.appUid = appUid2;
            this.logUid = logUid2;
            this.processStateCurTop = processStateCurTop2;
        }

        @Override // com.android.server.wm.WindowProcessController.ComputeOomAdjCallback
        public void onVisibleActivity() {
            if (this.adj > 100) {
                this.adj = 100;
                this.app.adjType = "vis-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    String str = ActivityManagerService.TAG_OOM_ADJ;
                    oomAdjuster.reportOomAdjMessageLocked(str, "Raise adj to vis-activity: " + this.app);
                }
            }
            int i = this.procState;
            int i2 = this.processStateCurTop;
            if (i > i2) {
                this.procState = i2;
                this.app.adjType = "vis-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster2 = OomAdjuster.this;
                    String str2 = ActivityManagerService.TAG_OOM_ADJ;
                    oomAdjuster2.reportOomAdjMessageLocked(str2, "Raise procstate to vis-activity (top): " + this.app);
                }
            }
            if (this.schedGroup < 2) {
                this.schedGroup = 2;
            }
            ProcessRecord processRecord = this.app;
            processRecord.cached = false;
            processRecord.empty = false;
            this.foregroundActivities = true;
        }

        @Override // com.android.server.wm.WindowProcessController.ComputeOomAdjCallback
        public void onPausedActivity() {
            if (this.adj > 200) {
                this.adj = 200;
                this.app.adjType = "pause-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    String str = ActivityManagerService.TAG_OOM_ADJ;
                    oomAdjuster.reportOomAdjMessageLocked(str, "Raise adj to pause-activity: " + this.app);
                }
            }
            int i = this.procState;
            int i2 = this.processStateCurTop;
            if (i > i2) {
                this.procState = i2;
                this.app.adjType = "pause-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster2 = OomAdjuster.this;
                    String str2 = ActivityManagerService.TAG_OOM_ADJ;
                    oomAdjuster2.reportOomAdjMessageLocked(str2, "Raise procstate to pause-activity (top): " + this.app);
                }
            }
            if (this.schedGroup < 2) {
                this.schedGroup = 2;
            }
            ProcessRecord processRecord = this.app;
            processRecord.cached = false;
            processRecord.empty = false;
            this.foregroundActivities = true;
        }

        @Override // com.android.server.wm.WindowProcessController.ComputeOomAdjCallback
        public void onStoppingActivity(boolean finishing) {
            if (this.adj > 200) {
                this.adj = 200;
                this.app.adjType = "stop-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    String str = ActivityManagerService.TAG_OOM_ADJ;
                    oomAdjuster.reportOomAdjMessageLocked(str, "Raise adj to stop-activity: " + this.app);
                }
            }
            if (!finishing && this.procState > 16) {
                this.procState = 16;
                this.app.adjType = "stop-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster2 = OomAdjuster.this;
                    String str2 = ActivityManagerService.TAG_OOM_ADJ;
                    oomAdjuster2.reportOomAdjMessageLocked(str2, "Raise procstate to stop-activity: " + this.app);
                }
            }
            ProcessRecord processRecord = this.app;
            processRecord.cached = false;
            processRecord.empty = false;
            this.foregroundActivities = true;
        }

        @Override // com.android.server.wm.WindowProcessController.ComputeOomAdjCallback
        public void onOtherActivity() {
            if (this.procState > 17) {
                this.procState = 17;
                this.app.adjType = "cch-act";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    String str = ActivityManagerService.TAG_OOM_ADJ;
                    oomAdjuster.reportOomAdjMessageLocked(str, "Raise procstate to cached activity: " + this.app);
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r12v38 'appUid'  int: [D('clist' java.util.ArrayList<com.android.server.am.ConnectionRecord>), D('appUid' int)] */
    /* JADX INFO: Multiple debug info for r15v16 'cr'  com.android.server.am.ConnectionRecord: [D('cr' com.android.server.am.ConnectionRecord), D('PROCESS_STATE_CUR_TOP' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x0350, code lost:
        if (r10 == r1) goto L_0x0355;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x0377, code lost:
        if (r11 > 3) goto L_0x037b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x041d, code lost:
        if (r8.setProcState <= 2) goto L_0x0421;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:538:0x0c1e, code lost:
        if (r6 == r5) goto L_0x0c25;
     */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x0454 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x0497  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x04ef  */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x0551  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x057c  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x05b4  */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x05b6  */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x05c3  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x05c5  */
    /* JADX WARNING: Removed duplicated region for block: B:231:0x05e6  */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0675  */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x08e6  */
    /* JADX WARNING: Removed duplicated region for block: B:419:0x0955  */
    /* JADX WARNING: Removed duplicated region for block: B:430:0x097d  */
    /* JADX WARNING: Removed duplicated region for block: B:435:0x098d  */
    /* JADX WARNING: Removed duplicated region for block: B:437:0x0994  */
    /* JADX WARNING: Removed duplicated region for block: B:446:0x09ad  */
    /* JADX WARNING: Removed duplicated region for block: B:451:0x0a07  */
    /* JADX WARNING: Removed duplicated region for block: B:481:0x0b03  */
    /* JADX WARNING: Removed duplicated region for block: B:570:0x0d1b  */
    /* JADX WARNING: Removed duplicated region for block: B:576:0x0d47  */
    /* JADX WARNING: Removed duplicated region for block: B:582:0x0d6e  */
    /* JADX WARNING: Removed duplicated region for block: B:590:0x0d89  */
    /* JADX WARNING: Removed duplicated region for block: B:611:0x0dd0  */
    /* JADX WARNING: Removed duplicated region for block: B:633:0x0e1b  */
    /* JADX WARNING: Removed duplicated region for block: B:637:0x0e2b  */
    /* JADX WARNING: Removed duplicated region for block: B:640:0x0e30  */
    /* JADX WARNING: Removed duplicated region for block: B:644:0x0e3b  */
    /* JADX WARNING: Removed duplicated region for block: B:647:0x0e5a  */
    /* JADX WARNING: Removed duplicated region for block: B:650:0x0e65  */
    /* JADX WARNING: Removed duplicated region for block: B:651:0x0ae9 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:668:0x0cff A[SYNTHETIC] */
    private final boolean computeOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now, boolean cycleReEval) {
        int schedGroup;
        int adj;
        int adj2;
        boolean foregroundActivities;
        int prevAppAdj;
        int prevProcState;
        String str;
        int logUid;
        int schedGroup2;
        int procState;
        int logUid2;
        int schedGroup3;
        String adjType;
        int PROCESS_STATE_CUR_TOP;
        int adj3;
        int schedGroup4;
        int i;
        BackupRecord backupTarget;
        int schedGroup5;
        int is;
        int procState2;
        boolean foregroundActivities2;
        WindowProcessController wpc;
        int adj4;
        String str2;
        int appUid;
        ProcessRecord processRecord;
        long j;
        int adj5;
        int provi;
        int logUid3;
        int appUid2;
        long j2;
        boolean z;
        int schedGroup6;
        boolean z2;
        int i2;
        int provi2;
        String str3;
        int logUid4;
        int i3;
        ContentProviderRecord cpr;
        String str4;
        int schedGroup7;
        long j3;
        int procState3;
        String str5;
        int i4;
        String str6;
        int provi3;
        ContentProviderRecord cpr2;
        long j4;
        int logUid5;
        int appUid3;
        int logUid6;
        ContentProviderRecord cpr3;
        int appUid4;
        int adj6;
        int procState4;
        int schedGroup8;
        int schedGroup9;
        BackupRecord backupTarget2;
        WindowProcessController wpc2;
        int adj7;
        boolean foregroundActivities3;
        int appUid5;
        int is2;
        boolean foregroundActivities4;
        int conni;
        ServiceRecord s;
        int is3;
        int conni2;
        ArrayList<ConnectionRecord> clist;
        int is4;
        int PROCESS_STATE_CUR_TOP2;
        boolean foregroundActivities5;
        int i5;
        int appUid6;
        ProcessRecord processRecord2;
        long j5;
        String str7;
        ServiceRecord s2;
        ConnectionRecord cr;
        int adj8;
        ProcessRecord client;
        int clientAdj;
        boolean z3;
        int clientProcState;
        int clientProcState2;
        int schedGroup10;
        int bestState;
        int newAdj;
        int i6;
        int procState5;
        int adj9;
        int adj10;
        int procState6;
        int schedGroup11;
        int schedGroup12;
        int schedGroup13;
        int adj11;
        int procState7;
        int adj12;
        int schedGroup14;
        int adj13;
        int schedGroup15;
        int adj14;
        ProcessRecord processRecord3 = app;
        long j6 = now;
        if (this.mAdjSeq == processRecord3.adjSeq) {
            if (processRecord3.adjSeq == processRecord3.completedAdjSeq) {
                return false;
            }
            processRecord3.containsCycle = true;
            return false;
        } else if (processRecord3.thread == null) {
            processRecord3.adjSeq = this.mAdjSeq;
            processRecord3.setCurrentSchedulingGroup(0);
            processRecord3.setCurProcState(20);
            processRecord3.curAdj = ZenModeHelper.OPPO_MULTI_USER_ID;
            processRecord3.setCurRawAdj(ZenModeHelper.OPPO_MULTI_USER_ID);
            processRecord3.completedAdjSeq = processRecord3.adjSeq;
            return false;
        } else {
            processRecord3.adjTypeCode = 0;
            processRecord3.adjSource = null;
            processRecord3.adjTarget = null;
            processRecord3.empty = false;
            processRecord3.cached = false;
            WindowProcessController wpc3 = app.getWindowProcessController();
            int appUid7 = processRecord3.info.uid;
            int logUid7 = this.mService.mCurOomAdjUid;
            int prevAppAdj2 = processRecord3.curAdj;
            int prevProcState2 = app.getCurProcState();
            if (processRecord3.maxAdj <= 0) {
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                    this.mService.reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Making fixed: " + processRecord3);
                }
                processRecord3.adjType = "fixed";
                processRecord3.adjSeq = this.mAdjSeq;
                processRecord3.setCurRawAdj(processRecord3.maxAdj);
                processRecord3.setHasForegroundActivities(false);
                processRecord3.setCurrentSchedulingGroup(2);
                processRecord3.setCurProcState(0);
                processRecord3.systemNoUi = true;
                if (processRecord3 == TOP_APP) {
                    processRecord3.systemNoUi = false;
                    processRecord3.setCurrentSchedulingGroup(3);
                    processRecord3.adjType = "pers-top-activity";
                } else if (app.hasTopUi()) {
                    processRecord3.systemNoUi = false;
                    processRecord3.adjType = "pers-top-ui";
                } else if (wpc3.hasVisibleActivities()) {
                    processRecord3.systemNoUi = false;
                }
                if (!processRecord3.systemNoUi) {
                    if (this.mService.mWakefulness == 1) {
                        processRecord3.setCurProcState(1);
                        processRecord3.setCurrentSchedulingGroup(3);
                    } else {
                        processRecord3.setCurProcState(6);
                        processRecord3.setCurrentSchedulingGroup(1);
                    }
                }
                processRecord3.setCurRawProcState(app.getCurProcState());
                processRecord3.curAdj = processRecord3.maxAdj;
                processRecord3.completedAdjSeq = processRecord3.adjSeq;
                if (processRecord3.curAdj < prevAppAdj2 || app.getCurProcState() < prevProcState2) {
                    return true;
                }
                return false;
            }
            processRecord3.systemNoUi = false;
            int PROCESS_STATE_CUR_TOP3 = this.mService.mAtmInternal.getTopProcessState();
            this.mTmpBroadcastQueue.clear();
            if (PROCESS_STATE_CUR_TOP3 == 2 && processRecord3 == TOP_APP) {
                processRecord3.adjType = "top-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                    String str8 = ActivityManagerService.TAG_OOM_ADJ;
                    StringBuilder sb = new StringBuilder();
                    adj14 = 0;
                    sb.append("Making top: ");
                    sb.append(processRecord3);
                    reportOomAdjMessageLocked(str8, sb.toString());
                } else {
                    adj14 = 0;
                }
                foregroundActivities = true;
                schedGroup = 3;
                adj2 = adj14;
                adj = PROCESS_STATE_CUR_TOP3;
            } else {
                if (processRecord3.runningRemoteAnimation) {
                    processRecord3.adjType = "running-remote-anim";
                    procState7 = PROCESS_STATE_CUR_TOP3;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                        String str9 = ActivityManagerService.TAG_OOM_ADJ;
                        adj12 = 100;
                        StringBuilder sb2 = new StringBuilder();
                        schedGroup14 = 3;
                        sb2.append("Making running remote anim: ");
                        sb2.append(processRecord3);
                        reportOomAdjMessageLocked(str9, sb2.toString());
                    } else {
                        adj12 = 100;
                        schedGroup14 = 3;
                    }
                } else if (app.getActiveInstrumentation() != null) {
                    processRecord3.adjType = "instrumentation";
                    procState7 = 5;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                        String str10 = ActivityManagerService.TAG_OOM_ADJ;
                        adj12 = 0;
                        StringBuilder sb3 = new StringBuilder();
                        schedGroup14 = 2;
                        sb3.append("Making instrumentation: ");
                        sb3.append(processRecord3);
                        reportOomAdjMessageLocked(str10, sb3.toString());
                    } else {
                        adj12 = 0;
                        schedGroup14 = 2;
                    }
                } else if (this.mService.isReceivingBroadcastLocked(processRecord3, this.mTmpBroadcastQueue)) {
                    try {
                        schedGroup15 = (this.mTmpBroadcastQueue.contains(this.mService.mFgBroadcastQueue) || this.mTmpBroadcastQueue.contains(OppoFeatureCache.get(IColorBroadcastManager.DEFAULT).getOppoFgBroadcastQueue())) ? 2 : 0;
                        adj13 = 0;
                    } catch (Exception e) {
                        schedGroup15 = 2;
                        StringBuilder sb4 = new StringBuilder();
                        adj13 = 0;
                        sb4.append("mTmpBroadcastQueue contains exception ");
                        sb4.append(e);
                        Slog.d(TAG, sb4.toString());
                    }
                    processRecord3.adjType = "broadcast";
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                        String str11 = ActivityManagerService.TAG_OOM_ADJ;
                        StringBuilder sb5 = new StringBuilder();
                        adj = 12;
                        sb5.append("Making broadcast: ");
                        sb5.append(processRecord3);
                        reportOomAdjMessageLocked(str11, sb5.toString());
                    } else {
                        adj = 12;
                    }
                    schedGroup = schedGroup15;
                    foregroundActivities = false;
                    adj2 = adj13;
                } else if (processRecord3.executingServices.size() > 0) {
                    int schedGroup16 = processRecord3.execServicesFg ? 2 : 0;
                    processRecord3.adjType = "exec-service";
                    procState7 = 11;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                        String str12 = ActivityManagerService.TAG_OOM_ADJ;
                        adj12 = 0;
                        StringBuilder sb6 = new StringBuilder();
                        schedGroup14 = schedGroup16;
                        sb6.append("Making exec-service: ");
                        sb6.append(processRecord3);
                        reportOomAdjMessageLocked(str12, sb6.toString());
                    } else {
                        adj12 = 0;
                        schedGroup14 = schedGroup16;
                    }
                } else if (processRecord3 == TOP_APP) {
                    processRecord3.adjType = "top-sleeping";
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                        adj11 = 0;
                        String str13 = ActivityManagerService.TAG_OOM_ADJ;
                        StringBuilder sb7 = new StringBuilder();
                        schedGroup13 = 0;
                        sb7.append("Making top (sleeping): ");
                        sb7.append(processRecord3);
                        reportOomAdjMessageLocked(str13, sb7.toString());
                    } else {
                        adj11 = 0;
                        schedGroup13 = 0;
                    }
                    foregroundActivities = true;
                    adj2 = adj11;
                    schedGroup = schedGroup13;
                    adj = PROCESS_STATE_CUR_TOP3;
                } else {
                    int schedGroup17 = 0;
                    if ("com.vv51.vvim".equals(processRecord3.processName)) {
                        schedGroup17 = 2;
                        Slog.i(TAG, "schedGroup reset to default");
                    }
                    adj2 = cachedAdj;
                    processRecord3.cached = true;
                    processRecord3.empty = true;
                    processRecord3.adjType = "cch-empty";
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid7 == appUid7) {
                        String str14 = ActivityManagerService.TAG_OOM_ADJ;
                        StringBuilder sb8 = new StringBuilder();
                        schedGroup12 = schedGroup17;
                        sb8.append("Making empty: ");
                        sb8.append(processRecord3);
                        reportOomAdjMessageLocked(str14, sb8.toString());
                    } else {
                        schedGroup12 = schedGroup17;
                    }
                    adj = 20;
                    foregroundActivities = false;
                    schedGroup = schedGroup12;
                }
                foregroundActivities = false;
                adj2 = adj12;
                schedGroup = schedGroup14;
                adj = procState7;
            }
            if (foregroundActivities || !wpc3.hasActivities()) {
                str = TAG;
                prevProcState = prevProcState2;
                prevAppAdj = prevAppAdj2;
                logUid = logUid7;
                procState = adj;
                schedGroup2 = schedGroup;
            } else {
                ComputeOomAdjWindowCallback computeOomAdjWindowCallback = this.mTmpComputeOomAdjWindowCallback;
                str = TAG;
                prevProcState = prevProcState2;
                prevAppAdj = prevAppAdj2;
                logUid = logUid7;
                computeOomAdjWindowCallback.initialize(app, adj2, foregroundActivities, adj, schedGroup, appUid7, logUid, PROCESS_STATE_CUR_TOP3);
                int minLayer = wpc3.computeOomAdjFromActivities(99, this.mTmpComputeOomAdjWindowCallback);
                adj2 = this.mTmpComputeOomAdjWindowCallback.adj;
                foregroundActivities = this.mTmpComputeOomAdjWindowCallback.foregroundActivities;
                procState = this.mTmpComputeOomAdjWindowCallback.procState;
                schedGroup2 = this.mTmpComputeOomAdjWindowCallback.schedGroup;
                if (adj2 == 100) {
                    adj2 += minLayer;
                }
            }
            if (procState <= 19 || !app.hasRecentTasks()) {
                logUid2 = logUid;
            } else {
                procState = 19;
                processRecord3.adjType = "cch-rec";
                if (!ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON) {
                    logUid2 = logUid;
                } else {
                    logUid2 = logUid;
                }
                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise procstate to cached recent: " + processRecord3);
            }
            String str15 = "Raise to ";
            if (adj2 <= 200) {
            }
            if (app.hasForegroundServices()) {
                if (app.hasLocationForegroundServices()) {
                    procState6 = 3;
                    processRecord3.adjType = "fg-service-location";
                } else {
                    processRecord3.adjType = "fg-service";
                    procState6 = 5;
                }
                processRecord3.cached = false;
                schedGroup11 = 2;
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                    String str16 = ActivityManagerService.TAG_OOM_ADJ;
                    StringBuilder sb9 = new StringBuilder();
                    sb9.append(str15);
                    adj10 = 200;
                    sb9.append(processRecord3.adjType);
                    sb9.append(": ");
                    sb9.append(processRecord3);
                    sb9.append(StringUtils.SPACE);
                    reportOomAdjMessageLocked(str16, sb9.toString());
                } else {
                    adj10 = 200;
                }
            } else {
                if (app.hasOverlayUi()) {
                    procState6 = 7;
                    processRecord3.cached = false;
                    processRecord3.adjType = "has-overlay-ui";
                    schedGroup11 = 2;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                        String str17 = ActivityManagerService.TAG_OOM_ADJ;
                        StringBuilder sb10 = new StringBuilder();
                        adj10 = 200;
                        sb10.append("Raise to overlay ui: ");
                        sb10.append(processRecord3);
                        reportOomAdjMessageLocked(str17, sb10.toString());
                    } else {
                        adj10 = 200;
                    }
                }
                if (app.hasForegroundServices() || adj2 <= 50) {
                    adj9 = adj2;
                    PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP3;
                    schedGroup3 = schedGroup2;
                    adjType = ": ";
                } else {
                    PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP3;
                    adj9 = adj2;
                    schedGroup3 = schedGroup2;
                    adjType = ": ";
                    if (processRecord3.lastTopTime + this.mConstants.TOP_TO_FGS_GRACE_DURATION <= j6) {
                    }
                    adj3 = 50;
                    processRecord3.adjType = "fg-service-act";
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                        reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise to recent fg: " + processRecord3);
                    }
                    if ((adj3 <= 200 || procState > 9) && processRecord3.forcingToImportant != null) {
                        adj3 = 200;
                        procState = 9;
                        processRecord3.cached = false;
                        processRecord3.adjType = "force-imp";
                        processRecord3.adjSource = processRecord3.forcingToImportant;
                        schedGroup4 = 2;
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                            reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise to force imp: " + processRecord3);
                        }
                    } else {
                        schedGroup4 = schedGroup3;
                    }
                    if (this.mService.mAtmInternal.isHeavyWeightProcess(app.getWindowProcessController())) {
                        if (adj3 > 400) {
                            adj3 = IColorEyeProtectManager.LEVEL_COLOR_MATRIX_COLOR;
                            schedGroup4 = 0;
                            processRecord3.cached = false;
                            processRecord3.adjType = "heavy";
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise adj to heavy: " + processRecord3);
                            }
                        }
                        if (procState > 14) {
                            procState = 14;
                            processRecord3.adjType = "heavy";
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise procstate to heavy: " + processRecord3);
                            }
                        }
                    }
                    if (wpc3.isHomeProcess()) {
                        if (adj3 > 600) {
                            adj3 = SystemService.PHASE_THIRD_PARTY_APPS_CAN_START;
                            schedGroup4 = 0;
                            processRecord3.cached = false;
                            processRecord3.adjType = "home";
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise adj to home: " + processRecord3);
                            }
                        }
                        if (procState > 15) {
                            procState = 15;
                            processRecord3.adjType = "home";
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise procstate to home: " + processRecord3);
                            }
                        }
                    }
                    if (wpc3.isPreviousProcess() && app.hasActivities()) {
                        if (adj3 > 700) {
                            adj3 = CompatibilityHelper.FORCE_DELAY_TO_USE_POST;
                            schedGroup4 = 0;
                            processRecord3.cached = false;
                            processRecord3.adjType = "previous";
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise adj to prev: " + processRecord3);
                            }
                        }
                        if (procState > 16) {
                            procState = 16;
                            processRecord3.adjType = "previous";
                            if ("com.vv51.vvim".equals(processRecord3.processName)) {
                                schedGroup4 = 2;
                                Slog.i(str, "previous schedGroup reset to default");
                            }
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise procstate to prev: " + processRecord3);
                            }
                        }
                    }
                    processRecord3.setCurRawAdj(!cycleReEval ? adj3 : Math.min(adj3, app.getCurRawAdj()));
                    if (!cycleReEval) {
                        i = procState;
                    } else {
                        i = Math.min(procState, app.getCurRawProcState());
                    }
                    processRecord3.setCurRawProcState(i);
                    processRecord3.hasStartedServices = false;
                    processRecord3.adjSeq = this.mAdjSeq;
                    backupTarget = this.mService.mBackupTargets.get(processRecord3.userId);
                    if (backupTarget == null && processRecord3 == backupTarget.app) {
                        if (adj3 > 300) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
                                String str18 = ActivityManagerService.TAG_BACKUP;
                                StringBuilder sb11 = new StringBuilder();
                                schedGroup5 = schedGroup4;
                                sb11.append("oom BACKUP_APP_ADJ for ");
                                sb11.append(processRecord3);
                                Slog.v(str18, sb11.toString());
                            } else {
                                schedGroup5 = schedGroup4;
                            }
                            if (procState > 9) {
                                procState = 9;
                            }
                            processRecord3.adjType = BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD;
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise adj to backup: " + processRecord3);
                            }
                            processRecord3.cached = false;
                            adj3 = 300;
                        } else {
                            schedGroup5 = schedGroup4;
                        }
                        if (procState > 10) {
                            procState = 10;
                            processRecord3.adjType = BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD;
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise procstate to backup: " + processRecord3);
                            }
                        }
                    } else {
                        schedGroup5 = schedGroup4;
                    }
                    is = processRecord3.services.size() - 1;
                    procState2 = procState;
                    while (true) {
                        if (is >= 0) {
                            if (adj3 <= 0 && schedGroup5 != 0 && procState2 <= 2) {
                                foregroundActivities2 = foregroundActivities;
                                wpc = wpc3;
                                adj4 = adj3;
                                processRecord = processRecord3;
                                str2 = adjType;
                                appUid = appUid7;
                                j = j6;
                                break;
                            }
                            ServiceRecord s3 = processRecord3.services.valueAt(is);
                            if (s3.startRequested) {
                                processRecord3.hasStartedServices = true;
                                if (procState2 > 11) {
                                    processRecord3.adjType = "started-services";
                                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                        String str19 = ActivityManagerService.TAG_OOM_ADJ;
                                        procState5 = 11;
                                        StringBuilder sb12 = new StringBuilder();
                                        backupTarget2 = backupTarget;
                                        sb12.append("Raise procstate to started service: ");
                                        sb12.append(processRecord3);
                                        reportOomAdjMessageLocked(str19, sb12.toString());
                                    } else {
                                        procState5 = 11;
                                        backupTarget2 = backupTarget;
                                    }
                                    procState2 = procState5;
                                } else {
                                    backupTarget2 = backupTarget;
                                }
                                if (!processRecord3.hasShownUi || wpc3.isHomeProcess()) {
                                    wpc2 = wpc3;
                                    if (j6 < s3.lastActivity + this.mConstants.MAX_SERVICE_INACTIVITY) {
                                        if (adj3 > 500) {
                                            processRecord3.adjType = "started-services";
                                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid7) {
                                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise adj to started service: " + processRecord3);
                                            }
                                            processRecord3.cached = false;
                                            adj3 = 500;
                                        }
                                    }
                                    if (adj3 > 500) {
                                        processRecord3.adjType = "cch-started-services";
                                    }
                                    procState2 = procState2;
                                } else {
                                    if (adj3 > 500) {
                                        processRecord3.adjType = "cch-started-ui-services";
                                    }
                                    wpc2 = wpc3;
                                }
                            } else {
                                wpc2 = wpc3;
                                backupTarget2 = backupTarget;
                            }
                            ArrayMap<IBinder, ArrayList<ConnectionRecord>> serviceConnections = s3.getConnections();
                            int conni3 = serviceConnections.size() - 1;
                            int adj15 = adj3;
                            while (true) {
                                if (conni3 >= 0) {
                                    if (adj7 <= 0 && schedGroup5 != 0 && procState2 <= 2) {
                                        foregroundActivities3 = foregroundActivities;
                                        appUid5 = appUid7;
                                        is2 = is;
                                        break;
                                    }
                                    ArrayList<ConnectionRecord> clist2 = serviceConnections.valueAt(conni3);
                                    int procState8 = procState2;
                                    int procState9 = 0;
                                    while (true) {
                                        if (procState9 < clist2.size()) {
                                            if (adj7 <= 0 && schedGroup5 != 0 && procState8 <= 2) {
                                                foregroundActivities4 = foregroundActivities;
                                                conni = conni3;
                                                s = s3;
                                                is3 = is;
                                                break;
                                            }
                                            ConnectionRecord cr2 = clist2.get(procState9);
                                            if (cr2.binding.client == processRecord3) {
                                                foregroundActivities5 = foregroundActivities;
                                                i5 = procState9;
                                                conni2 = conni3;
                                                s2 = s3;
                                                is4 = is;
                                                clist = clist2;
                                                PROCESS_STATE_CUR_TOP2 = PROCESS_STATE_CUR_TOP;
                                                appUid6 = appUid7;
                                                processRecord2 = processRecord3;
                                            } else {
                                                boolean trackedProcState = false;
                                                if ((cr2.flags & 32) == 0) {
                                                    ProcessRecord client2 = cr2.binding.client;
                                                    clist = clist2;
                                                    appUid6 = appUid7;
                                                    cr = cr2;
                                                    foregroundActivities5 = foregroundActivities;
                                                    i5 = procState9;
                                                    is4 = is;
                                                    conni2 = conni3;
                                                    s2 = s3;
                                                    PROCESS_STATE_CUR_TOP2 = PROCESS_STATE_CUR_TOP;
                                                    processRecord2 = processRecord3;
                                                    computeOomAdjLocked(client2, cachedAdj, TOP_APP, doingAll, now, cycleReEval);
                                                    if (!shouldSkipDueToCycle(app, client2, procState8, adj7, cycleReEval)) {
                                                        int clientAdj2 = client2.getCurRawAdj();
                                                        int clientProcState3 = client2.getCurRawProcState();
                                                        if (clientProcState3 >= 17) {
                                                            clientProcState3 = 20;
                                                        }
                                                        String adjType2 = null;
                                                        if ((cr.flags & 16) == 0) {
                                                            j5 = now;
                                                            adj8 = adj7;
                                                        } else if (!processRecord2.hasShownUi || wpc2.isHomeProcess()) {
                                                            adj8 = adj7;
                                                            j5 = now;
                                                            if (j5 >= s2.lastActivity + this.mConstants.MAX_SERVICE_INACTIVITY) {
                                                                if (adj8 > clientAdj2) {
                                                                    adjType2 = "cch-bound-services";
                                                                }
                                                                clientAdj2 = adj8;
                                                            }
                                                        } else {
                                                            adj8 = adj7;
                                                            if (adj8 > clientAdj2) {
                                                                adjType2 = "cch-bound-ui-services";
                                                            }
                                                            processRecord2.cached = false;
                                                            clientAdj2 = adj8;
                                                            clientProcState3 = procState8;
                                                            j5 = now;
                                                        }
                                                        if (adj8 <= clientAdj2) {
                                                            client = client2;
                                                        } else if (!processRecord2.hasShownUi || wpc2.isHomeProcess() || clientAdj2 <= 200) {
                                                            if ((cr.flags & 72) != 0) {
                                                                if (clientAdj2 >= -700) {
                                                                    newAdj = clientAdj2;
                                                                } else {
                                                                    schedGroup5 = 2;
                                                                    procState8 = 0;
                                                                    cr.trackProcState(0, this.mAdjSeq, j5);
                                                                    trackedProcState = true;
                                                                    newAdj = -700;
                                                                }
                                                            } else if ((cr.flags & 256) == 0 || clientAdj2 >= 200 || adj8 <= 250) {
                                                                if ((cr.flags & 1073741824) != 0) {
                                                                    i6 = 200;
                                                                    if (clientAdj2 < 200 && adj8 > 200) {
                                                                        newAdj = 200;
                                                                    }
                                                                } else {
                                                                    i6 = 200;
                                                                }
                                                                if (clientAdj2 >= i6) {
                                                                    newAdj = clientAdj2;
                                                                } else if (adj8 > 100) {
                                                                    newAdj = Math.max(clientAdj2, 100);
                                                                } else {
                                                                    newAdj = adj8;
                                                                }
                                                            } else {
                                                                newAdj = 250;
                                                            }
                                                            client = client2;
                                                            if (!client.cached) {
                                                                processRecord2.cached = false;
                                                            }
                                                            if (adj8 > newAdj) {
                                                                processRecord2.setCurRawAdj(newAdj);
                                                                adjType2 = IColorAppStartupManager.TYPE_SERVICE;
                                                                adj8 = newAdj;
                                                                clientAdj = schedGroup5;
                                                            } else {
                                                                clientAdj = schedGroup5;
                                                            }
                                                            if ((cr.flags & 8388612) == 0) {
                                                                int curSchedGroup = client.getCurrentSchedulingGroup();
                                                                if (curSchedGroup > clientAdj) {
                                                                    if ((cr.flags & 64) != 0) {
                                                                        clientAdj = curSchedGroup;
                                                                    } else {
                                                                        clientAdj = 2;
                                                                    }
                                                                }
                                                                if (clientProcState3 < 2) {
                                                                    if (cr.hasFlag(4096)) {
                                                                        bestState = 3;
                                                                    } else {
                                                                        bestState = 6;
                                                                    }
                                                                    if ((cr.flags & 67108864) != 0) {
                                                                        clientProcState3 = bestState;
                                                                        z3 = true;
                                                                    } else {
                                                                        z3 = true;
                                                                        if (this.mService.mWakefulness != 1 || (cr.flags & DumpState.DUMP_APEX) == 0) {
                                                                            clientProcState3 = 7;
                                                                        } else {
                                                                            clientProcState3 = bestState;
                                                                        }
                                                                    }
                                                                } else {
                                                                    z3 = true;
                                                                    if (clientProcState3 == 2) {
                                                                        if (cr.notHasFlag(4096)) {
                                                                            clientProcState3 = 4;
                                                                        }
                                                                    } else if (clientProcState3 <= 5 && cr.notHasFlag(4096)) {
                                                                        clientProcState3 = 5;
                                                                    }
                                                                }
                                                                clientProcState = clientProcState3;
                                                                clientProcState2 = clientAdj;
                                                            } else {
                                                                z3 = true;
                                                                if ((cr.flags & DumpState.DUMP_VOLUMES) == 0) {
                                                                    if (clientProcState3 < 9) {
                                                                        clientProcState = 9;
                                                                        clientProcState2 = clientAdj;
                                                                    }
                                                                } else if (clientProcState3 < 8) {
                                                                    clientProcState = 8;
                                                                    clientProcState2 = clientAdj;
                                                                }
                                                                clientProcState = clientProcState3;
                                                                clientProcState2 = clientAdj;
                                                            }
                                                            if (clientProcState2 < 3 || (cr.flags & DumpState.DUMP_FROZEN) == 0) {
                                                                schedGroup10 = clientProcState2;
                                                            } else {
                                                                schedGroup10 = 3;
                                                            }
                                                            if (!trackedProcState) {
                                                                cr.trackProcState(clientProcState, this.mAdjSeq, j5);
                                                            }
                                                            if (procState8 > clientProcState) {
                                                                procState8 = clientProcState;
                                                                processRecord2.setCurRawProcState(procState8);
                                                                if (adjType2 == null) {
                                                                    adjType2 = IColorAppStartupManager.TYPE_SERVICE;
                                                                }
                                                            }
                                                            if (procState8 < 8 && (cr.flags & 536870912) != 0) {
                                                                processRecord2.setPendingUiClean(z3);
                                                            }
                                                            if (adjType2 != null) {
                                                                processRecord2.adjType = adjType2;
                                                                processRecord2.adjTypeCode = 2;
                                                                processRecord2.adjSource = cr.binding.client;
                                                                processRecord2.adjSourceProcState = clientProcState;
                                                                processRecord2.adjTarget = s2.instanceName;
                                                                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid6) {
                                                                    String str20 = ActivityManagerService.TAG_OOM_ADJ;
                                                                    StringBuilder sb13 = new StringBuilder();
                                                                    sb13.append(str15);
                                                                    sb13.append(adjType2);
                                                                    str7 = adjType;
                                                                    sb13.append(str7);
                                                                    sb13.append(processRecord2);
                                                                    sb13.append(", due to ");
                                                                    sb13.append(cr.binding.client);
                                                                    sb13.append(" adj=");
                                                                    sb13.append(adj8);
                                                                    sb13.append(" procState=");
                                                                    sb13.append(ProcessList.makeProcStateString(procState8));
                                                                    reportOomAdjMessageLocked(str20, sb13.toString());
                                                                } else {
                                                                    str7 = adjType;
                                                                }
                                                            } else {
                                                                str7 = adjType;
                                                            }
                                                            adj7 = adj8;
                                                        } else if (adj8 >= 900) {
                                                            adjType2 = "cch-bound-ui-services";
                                                            clientAdj = schedGroup5;
                                                            client = client2;
                                                            if ((cr.flags & 8388612) == 0) {
                                                            }
                                                            if (clientProcState2 < 3) {
                                                            }
                                                            schedGroup10 = clientProcState2;
                                                            if (!trackedProcState) {
                                                            }
                                                            if (procState8 > clientProcState) {
                                                            }
                                                            processRecord2.setPendingUiClean(z3);
                                                            if (adjType2 != null) {
                                                            }
                                                            adj7 = adj8;
                                                        } else {
                                                            client = client2;
                                                        }
                                                        clientAdj = schedGroup5;
                                                        if ((cr.flags & 8388612) == 0) {
                                                        }
                                                        if (clientProcState2 < 3) {
                                                        }
                                                        schedGroup10 = clientProcState2;
                                                        if (!trackedProcState) {
                                                        }
                                                        if (procState8 > clientProcState) {
                                                        }
                                                        processRecord2.setPendingUiClean(z3);
                                                        if (adjType2 != null) {
                                                        }
                                                        adj7 = adj8;
                                                    }
                                                } else {
                                                    foregroundActivities5 = foregroundActivities;
                                                    i5 = procState9;
                                                    conni2 = conni3;
                                                    s2 = s3;
                                                    is4 = is;
                                                    clist = clist2;
                                                    PROCESS_STATE_CUR_TOP2 = PROCESS_STATE_CUR_TOP;
                                                    str7 = adjType;
                                                    appUid6 = appUid7;
                                                    cr = cr2;
                                                    processRecord2 = processRecord3;
                                                    j5 = j6;
                                                    adj7 = adj7;
                                                }
                                                if ((cr.flags & 134217728) != 0) {
                                                    processRecord2.treatLikeActivity = true;
                                                }
                                                ActivityServiceConnectionsHolder a = cr.activity;
                                                if ((cr.flags & 128) != 0 && a != null && adj7 > 0 && a.isActivityVisible()) {
                                                    adj7 = 0;
                                                    processRecord2.setCurRawAdj(0);
                                                    if ((cr.flags & 4) == 0) {
                                                        if ((cr.flags & 64) != 0) {
                                                            schedGroup5 = 4;
                                                        } else {
                                                            schedGroup5 = 2;
                                                        }
                                                    }
                                                    processRecord2.cached = false;
                                                    processRecord2.adjType = IColorAppStartupManager.TYPE_SERVICE;
                                                    processRecord2.adjTypeCode = 2;
                                                    processRecord2.adjSource = a;
                                                    processRecord2.adjSourceProcState = procState8;
                                                    processRecord2.adjTarget = s2.instanceName;
                                                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid6) {
                                                        reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise to service w/activity: " + processRecord2);
                                                    }
                                                }
                                                procState9 = i5 + 1;
                                                adjType = str7;
                                                j6 = j5;
                                                processRecord3 = processRecord2;
                                                appUid7 = appUid6;
                                                PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP2;
                                                is = is4;
                                                clist2 = clist;
                                                conni3 = conni2;
                                                s3 = s2;
                                                foregroundActivities = foregroundActivities5;
                                            }
                                            j5 = now;
                                            str7 = adjType;
                                            adj7 = adj7;
                                            procState9 = i5 + 1;
                                            adjType = str7;
                                            j6 = j5;
                                            processRecord3 = processRecord2;
                                            appUid7 = appUid6;
                                            PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP2;
                                            is = is4;
                                            clist2 = clist;
                                            conni3 = conni2;
                                            s3 = s2;
                                            foregroundActivities = foregroundActivities5;
                                        } else {
                                            foregroundActivities4 = foregroundActivities;
                                            conni = conni3;
                                            s = s3;
                                            is3 = is;
                                            break;
                                        }
                                    }
                                    adj15 = adj7;
                                    adjType = adjType;
                                    j6 = j6;
                                    processRecord3 = processRecord3;
                                    procState2 = procState8;
                                    PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP;
                                    is = is3;
                                    serviceConnections = serviceConnections;
                                    s3 = s;
                                    conni3 = conni - 1;
                                    appUid7 = appUid7;
                                    foregroundActivities = foregroundActivities4;
                                } else {
                                    foregroundActivities3 = foregroundActivities;
                                    appUid5 = appUid7;
                                    is2 = is;
                                    break;
                                }
                            }
                            adjType = adjType;
                            j6 = j6;
                            processRecord3 = processRecord3;
                            appUid7 = appUid5;
                            PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP;
                            backupTarget = backupTarget2;
                            is = is2 - 1;
                            adj3 = adj7;
                            foregroundActivities = foregroundActivities3;
                            wpc3 = wpc2;
                        } else {
                            foregroundActivities2 = foregroundActivities;
                            wpc = wpc3;
                            adj4 = adj3;
                            str2 = adjType;
                            appUid = appUid7;
                            processRecord = processRecord3;
                            j = j6;
                            break;
                        }
                    }
                    provi = processRecord.pubProviders.size() - 1;
                    while (true) {
                        if (provi >= 0) {
                            if (adj5 <= 0 && schedGroup5 != 0 && procState2 <= 2) {
                                logUid3 = logUid2;
                                appUid2 = appUid;
                                j2 = j;
                                break;
                            }
                            ContentProviderRecord cpr4 = processRecord.pubProviders.valueAt(provi);
                            int appUid8 = cpr4.connections.size() - 1;
                            int adj16 = adj5;
                            int procState10 = procState2;
                            int schedGroup18 = schedGroup5;
                            while (true) {
                                if (appUid8 >= 0) {
                                    if (adj16 <= 0 && schedGroup18 != 0 && procState10 <= 2) {
                                        provi2 = provi;
                                        str3 = str2;
                                        logUid4 = logUid2;
                                        i3 = appUid;
                                        cpr = cpr4;
                                        str4 = str15;
                                        schedGroup7 = schedGroup18;
                                        j3 = j;
                                        procState3 = procState10;
                                        break;
                                    }
                                    ContentProviderConnection conn = cpr4.connections.get(appUid8);
                                    ProcessRecord client3 = conn.client;
                                    if (client3 == processRecord) {
                                        provi3 = provi;
                                        procState4 = procState10;
                                        adj6 = adj16;
                                        i4 = appUid8;
                                        str5 = str2;
                                        logUid6 = logUid2;
                                        appUid4 = appUid;
                                        cpr3 = cpr4;
                                        str6 = str15;
                                        schedGroup8 = schedGroup18;
                                        j4 = j;
                                    } else {
                                        provi3 = provi;
                                        procState4 = procState10;
                                        str6 = str15;
                                        schedGroup8 = schedGroup18;
                                        adj6 = adj16;
                                        i4 = appUid8;
                                        logUid6 = logUid2;
                                        appUid4 = appUid;
                                        cpr3 = cpr4;
                                        j4 = j;
                                        str5 = str2;
                                        computeOomAdjLocked(client3, cachedAdj, TOP_APP, doingAll, now, cycleReEval);
                                        if (!shouldSkipDueToCycle(app, client3, procState4, adj6, cycleReEval)) {
                                            int clientAdj3 = client3.getCurRawAdj();
                                            int clientProcState4 = client3.getCurRawProcState();
                                            if (clientProcState4 >= 17) {
                                                clientProcState4 = 20;
                                            }
                                            String adjType3 = null;
                                            adj16 = adj6;
                                            if (adj16 > clientAdj3) {
                                                if (processRecord.hasShownUi && !wpc.isHomeProcess()) {
                                                    if (clientAdj3 > 200) {
                                                        adjType3 = "cch-ui-provider";
                                                        processRecord.cached &= client3.cached;
                                                    }
                                                }
                                                adj16 = clientAdj3 > 0 ? clientAdj3 : 0;
                                                processRecord.setCurRawAdj(adj16);
                                                adjType3 = IColorAppStartupManager.TYPE_PROVIDER;
                                                processRecord.cached &= client3.cached;
                                            }
                                            if (clientProcState4 <= 5) {
                                                if (adjType3 == null) {
                                                    adjType3 = IColorAppStartupManager.TYPE_PROVIDER;
                                                }
                                                if (clientProcState4 == 2) {
                                                    clientProcState4 = 4;
                                                } else {
                                                    clientProcState4 = 6;
                                                }
                                            }
                                            conn.trackProcState(clientProcState4, this.mAdjSeq, j4);
                                            int procState11 = procState4;
                                            if (procState11 > clientProcState4) {
                                                procState11 = clientProcState4;
                                                processRecord.setCurRawProcState(procState11);
                                            }
                                            if (client3.getCurrentSchedulingGroup() > schedGroup8) {
                                                schedGroup9 = 2;
                                            } else {
                                                schedGroup9 = schedGroup8;
                                            }
                                            if (adjType3 != null) {
                                                processRecord.adjType = adjType3;
                                                processRecord.adjTypeCode = 1;
                                                processRecord.adjSource = client3;
                                                processRecord.adjSourceProcState = clientProcState4;
                                                cpr2 = cpr3;
                                                processRecord.adjTarget = cpr2.name;
                                                if (!ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON) {
                                                    appUid3 = appUid4;
                                                    logUid5 = logUid6;
                                                } else {
                                                    appUid3 = appUid4;
                                                    logUid5 = logUid6;
                                                }
                                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, str6 + adjType3 + str5 + processRecord + ", due to " + client3 + " adj=" + adj16 + " procState=" + ProcessList.makeProcStateString(procState11));
                                            } else {
                                                appUid3 = appUid4;
                                                cpr2 = cpr3;
                                                logUid5 = logUid6;
                                            }
                                            procState10 = procState11;
                                            schedGroup18 = schedGroup9;
                                            logUid2 = logUid5;
                                            j = j4;
                                            cpr4 = cpr2;
                                            str15 = str6;
                                            str2 = str5;
                                            appUid = appUid3;
                                            appUid8 = i4 - 1;
                                            provi = provi3;
                                        }
                                    }
                                    schedGroup18 = schedGroup8;
                                    procState10 = procState4;
                                    adj16 = adj6;
                                    appUid3 = appUid4;
                                    cpr2 = cpr3;
                                    logUid5 = logUid6;
                                    logUid2 = logUid5;
                                    j = j4;
                                    cpr4 = cpr2;
                                    str15 = str6;
                                    str2 = str5;
                                    appUid = appUid3;
                                    appUid8 = i4 - 1;
                                    provi = provi3;
                                } else {
                                    provi2 = provi;
                                    str3 = str2;
                                    logUid4 = logUid2;
                                    i3 = appUid;
                                    cpr = cpr4;
                                    str4 = str15;
                                    schedGroup7 = schedGroup18;
                                    j3 = j;
                                    procState3 = procState10;
                                    break;
                                }
                            }
                            if (cpr.hasExternalProcessHandles()) {
                                if (adj16 > 0) {
                                    adj16 = 0;
                                    processRecord.setCurRawAdj(0);
                                    processRecord.cached = false;
                                    processRecord.adjType = "ext-provider";
                                    processRecord.adjTarget = cpr.name;
                                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid4 == i3) {
                                        reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise adj to external provider: " + processRecord);
                                    }
                                    schedGroup7 = 2;
                                }
                                if (procState3 > 7) {
                                    processRecord.setCurRawProcState(7);
                                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid4 == i3) {
                                        reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise procstate to external provider: " + processRecord);
                                    }
                                    procState2 = 7;
                                    adj5 = adj16;
                                } else {
                                    adj5 = adj16;
                                    procState2 = procState3;
                                }
                            } else {
                                adj5 = adj16;
                                procState2 = procState3;
                            }
                            schedGroup5 = schedGroup7;
                            provi = provi2 - 1;
                            logUid2 = logUid4;
                            j = j3;
                            str15 = str4;
                            str2 = str3;
                            appUid = i3;
                        } else {
                            logUid3 = logUid2;
                            appUid2 = appUid;
                            j2 = j;
                            break;
                        }
                    }
                    if (processRecord.lastProviderTime > 0 && processRecord.lastProviderTime + this.mConstants.CONTENT_PROVIDER_RETAIN_TIME > j2) {
                        if (adj5 > 700) {
                            adj5 = CompatibilityHelper.FORCE_DELAY_TO_USE_POST;
                            schedGroup5 = 0;
                            processRecord.cached = false;
                            processRecord.adjType = "recent-provider";
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise adj to recent provider: " + processRecord);
                            }
                        }
                        if (procState2 > 16) {
                            procState2 = 16;
                            processRecord.adjType = "recent-provider";
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise procstate to recent provider: " + processRecord);
                            }
                        }
                    }
                    if (procState2 >= 20) {
                        if (app.hasClientActivities()) {
                            procState2 = 18;
                            processRecord.adjType = "cch-client-act";
                        } else if (processRecord.treatLikeActivity) {
                            procState2 = 17;
                            processRecord.adjType = "cch-as-act";
                        }
                    }
                    if (adj5 == 500) {
                        if (doingAll) {
                            processRecord.serviceb = this.mNewNumAServiceProcs > this.mNumServiceProcs / 3;
                            this.mNewNumServiceProcs++;
                            if (!processRecord.serviceb) {
                                if (this.mService.mLastMemoryLevel <= 0) {
                                    i2 = 1;
                                } else if (processRecord.lastPss >= this.mProcessList.getCachedRestoreThresholdKb()) {
                                    processRecord.serviceHighRam = true;
                                    processRecord.serviceb = true;
                                } else {
                                    i2 = 1;
                                }
                                this.mNewNumAServiceProcs += i2;
                            } else {
                                processRecord.serviceHighRam = false;
                            }
                        }
                        if (processRecord.serviceb) {
                            adj5 = 800;
                        }
                    }
                    if (processRecord != TOP_APP || processRecord.processName == null || !OppoOomAjusterHelper.isOppoImportantApp(processRecord.processName)) {
                        z = false;
                    } else if (adj5 > 700) {
                        adj5 = CompatibilityHelper.FORCE_DELAY_TO_USE_POST;
                        processRecord.adjType = "previous";
                        schedGroup5 = 0;
                        z = false;
                        processRecord.cached = false;
                    } else {
                        z = false;
                    }
                    if (processRecord == TOP_APP && this.mLastTopUid != TOP_APP.uid) {
                        OppoOomAjusterHelper.updataKernelTopUid(TOP_APP);
                        this.mLastTopUid = TOP_APP.uid;
                    }
                    if (processRecord.info.uid == 1000 && processRecord.processName != null && OppoOomAjusterHelper.isAgingTestTool(processRecord.processName)) {
                        adj5 = 0;
                    }
                    processRecord.setCurRawAdj(adj5);
                    if (adj5 > processRecord.maxAdj) {
                        adj5 = processRecord.maxAdj;
                        if (processRecord.maxAdj <= 250) {
                            schedGroup6 = 2;
                        } else {
                            schedGroup6 = schedGroup5;
                        }
                    } else {
                        schedGroup6 = schedGroup5;
                    }
                    if (procState2 >= 6) {
                        z2 = true;
                        if (this.mService.mWakefulness != 1 && schedGroup6 > 1) {
                            schedGroup6 = 1;
                        }
                    } else {
                        z2 = true;
                    }
                    processRecord.curAdj = processRecord.modifyRawOomAdj(adj5);
                    processRecord.setCurrentSchedulingGroup(schedGroup6);
                    processRecord.setCurProcState(procState2);
                    processRecord.setCurRawProcState(procState2);
                    processRecord.setHasForegroundActivities(foregroundActivities2);
                    processRecord.completedAdjSeq = this.mAdjSeq;
                    if (processRecord.curAdj >= prevAppAdj) {
                        return app.getCurProcState() < prevProcState ? z2 : z;
                    }
                    return z2;
                }
                adj3 = adj9;
                if (adj3 <= 200) {
                }
                adj3 = 200;
                procState = 9;
                processRecord3.cached = false;
                processRecord3.adjType = "force-imp";
                processRecord3.adjSource = processRecord3.forcingToImportant;
                schedGroup4 = 2;
                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise to force imp: " + processRecord3);
                if (this.mService.mAtmInternal.isHeavyWeightProcess(app.getWindowProcessController())) {
                }
                if (wpc3.isHomeProcess()) {
                }
                if (adj3 > 700) {
                }
                if (procState > 16) {
                }
                processRecord3.setCurRawAdj(!cycleReEval ? adj3 : Math.min(adj3, app.getCurRawAdj()));
                if (!cycleReEval) {
                }
                processRecord3.setCurRawProcState(i);
                processRecord3.hasStartedServices = false;
                processRecord3.adjSeq = this.mAdjSeq;
                backupTarget = this.mService.mBackupTargets.get(processRecord3.userId);
                if (backupTarget == null) {
                }
                schedGroup5 = schedGroup4;
                is = processRecord3.services.size() - 1;
                procState2 = procState;
                while (true) {
                    if (is >= 0) {
                    }
                    adjType = adjType;
                    j6 = j6;
                    processRecord3 = processRecord3;
                    appUid7 = appUid5;
                    PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP;
                    backupTarget = backupTarget2;
                    is = is2 - 1;
                    adj3 = adj7;
                    foregroundActivities = foregroundActivities3;
                    wpc3 = wpc2;
                }
                provi = processRecord.pubProviders.size() - 1;
                while (true) {
                    if (provi >= 0) {
                    }
                    schedGroup5 = schedGroup7;
                    provi = provi2 - 1;
                    logUid2 = logUid4;
                    j = j3;
                    str15 = str4;
                    str2 = str3;
                    appUid = i3;
                }
                if (adj5 > 700) {
                }
                if (procState2 > 16) {
                }
                if (procState2 >= 20) {
                }
                if (adj5 == 500) {
                }
                if (processRecord != TOP_APP) {
                }
                z = false;
                OppoOomAjusterHelper.updataKernelTopUid(TOP_APP);
                this.mLastTopUid = TOP_APP.uid;
                adj5 = 0;
                processRecord.setCurRawAdj(adj5);
                if (adj5 > processRecord.maxAdj) {
                }
                if (procState2 >= 6) {
                }
                processRecord.curAdj = processRecord.modifyRawOomAdj(adj5);
                processRecord.setCurrentSchedulingGroup(schedGroup6);
                processRecord.setCurProcState(procState2);
                processRecord.setCurRawProcState(procState2);
                processRecord.setHasForegroundActivities(foregroundActivities2);
                processRecord.completedAdjSeq = this.mAdjSeq;
                if (processRecord.curAdj >= prevAppAdj) {
                }
            }
            adj2 = adj10;
            if (app.hasForegroundServices()) {
            }
            adj9 = adj2;
            PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP3;
            schedGroup3 = schedGroup2;
            adjType = ": ";
            adj3 = adj9;
            if (adj3 <= 200) {
            }
            adj3 = 200;
            procState = 9;
            processRecord3.cached = false;
            processRecord3.adjType = "force-imp";
            processRecord3.adjSource = processRecord3.forcingToImportant;
            schedGroup4 = 2;
            reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Raise to force imp: " + processRecord3);
            if (this.mService.mAtmInternal.isHeavyWeightProcess(app.getWindowProcessController())) {
            }
            if (wpc3.isHomeProcess()) {
            }
            if (adj3 > 700) {
            }
            if (procState > 16) {
            }
            processRecord3.setCurRawAdj(!cycleReEval ? adj3 : Math.min(adj3, app.getCurRawAdj()));
            if (!cycleReEval) {
            }
            processRecord3.setCurRawProcState(i);
            processRecord3.hasStartedServices = false;
            processRecord3.adjSeq = this.mAdjSeq;
            backupTarget = this.mService.mBackupTargets.get(processRecord3.userId);
            if (backupTarget == null) {
            }
            schedGroup5 = schedGroup4;
            is = processRecord3.services.size() - 1;
            procState2 = procState;
            while (true) {
                if (is >= 0) {
                }
                adjType = adjType;
                j6 = j6;
                processRecord3 = processRecord3;
                appUid7 = appUid5;
                PROCESS_STATE_CUR_TOP = PROCESS_STATE_CUR_TOP;
                backupTarget = backupTarget2;
                is = is2 - 1;
                adj3 = adj7;
                foregroundActivities = foregroundActivities3;
                wpc3 = wpc2;
            }
            provi = processRecord.pubProviders.size() - 1;
            while (true) {
                if (provi >= 0) {
                }
                schedGroup5 = schedGroup7;
                provi = provi2 - 1;
                logUid2 = logUid4;
                j = j3;
                str15 = str4;
                str2 = str3;
                appUid = i3;
            }
            if (adj5 > 700) {
            }
            if (procState2 > 16) {
            }
            if (procState2 >= 20) {
            }
            if (adj5 == 500) {
            }
            if (processRecord != TOP_APP) {
            }
            z = false;
            OppoOomAjusterHelper.updataKernelTopUid(TOP_APP);
            this.mLastTopUid = TOP_APP.uid;
            adj5 = 0;
            processRecord.setCurRawAdj(adj5);
            if (adj5 > processRecord.maxAdj) {
            }
            if (procState2 >= 6) {
            }
            processRecord.curAdj = processRecord.modifyRawOomAdj(adj5);
            processRecord.setCurrentSchedulingGroup(schedGroup6);
            processRecord.setCurProcState(procState2);
            processRecord.setCurRawProcState(procState2);
            processRecord.setHasForegroundActivities(foregroundActivities2);
            processRecord.completedAdjSeq = this.mAdjSeq;
            if (processRecord.curAdj >= prevAppAdj) {
            }
        }
    }

    private boolean shouldSkipDueToCycle(ProcessRecord app, ProcessRecord client, int procState, int adj, boolean cycleReEval) {
        if (!client.containsCycle) {
            return false;
        }
        app.containsCycle = true;
        if (client.completedAdjSeq >= this.mAdjSeq) {
            return false;
        }
        if (!cycleReEval) {
            return true;
        }
        if (client.getCurRawProcState() < procState || client.getCurRawAdj() < adj) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void reportOomAdjMessageLocked(String tag, String msg) {
        Slog.d(tag, msg);
        if (this.mService.mCurOomAdjObserver != null) {
            this.mService.mUiHandler.obtainMessage(70, msg).sendToTarget();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:123:0x0264  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0286  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0290  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x029b  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x02b7  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0329  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x034e  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x0391  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0421  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x044e  */
    @GuardedBy({"mService"})
    private final boolean applyOomAdjLocked(ProcessRecord app, boolean doingAll, long now, long nowElapsed) {
        boolean success;
        int changes;
        String str;
        String str2;
        int processGroup;
        if (app.getCurRawAdj() != app.setRawAdj) {
            app.setRawAdj = app.getCurRawAdj();
        }
        if (this.mAppCompact.useCompaction() && this.mService.mBooted) {
            if (app.curAdj != app.setAdj) {
                if (app.setAdj <= 200 && (app.curAdj == 700 || app.curAdj == 600)) {
                    this.mAppCompact.compactAppSome(app);
                } else if ((app.setAdj < 900 || app.setAdj > 999) && app.curAdj >= 900 && app.curAdj <= 999) {
                    this.mAppCompact.compactAppFull(app);
                }
            } else if (this.mService.mWakefulness != 1 && app.setAdj < 0 && this.mAppCompact.shouldCompactPersistent(app, now)) {
                this.mAppCompact.compactAppPersistent(app);
            } else if (this.mService.mWakefulness != 1 && app.getCurProcState() == 6 && this.mAppCompact.shouldCompactBFGS(app, now)) {
                this.mAppCompact.compactAppBfgs(app);
            }
        }
        if (app.curAdj != app.setAdj) {
            ProcessList.setOomAdj(app.pid, app.uid, app.curAdj);
            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ || this.mService.mCurOomAdjUid == app.info.uid) {
                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Set " + app.pid + StringUtils.SPACE + app.processName + " adj " + app.curAdj + ": " + app.adjType);
            }
            app.setAdj = app.curAdj;
            app.verifiedAdj = -10000;
        }
        int curSchedGroup = app.getCurrentSchedulingGroup();
        if (app.setSchedGroup != curSchedGroup) {
            int oldSchedGroup = app.setSchedGroup;
            app.setSchedGroup = curSchedGroup;
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ || this.mService.mCurOomAdjUid == app.uid) {
                reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Setting sched group of " + app.processName + " to " + curSchedGroup + ": " + app.adjType);
            }
            if (app.waitingToKill == null || !app.curReceivers.isEmpty() || app.setSchedGroup != 0) {
                if (curSchedGroup == 0) {
                    processGroup = 0;
                } else if (curSchedGroup == 1) {
                    processGroup = 7;
                } else if (curSchedGroup == 3 || curSchedGroup == 4) {
                    processGroup = 5;
                } else {
                    processGroup = -1;
                }
                Handler handler = this.mProcessGroupHandler;
                success = true;
                handler.sendMessage(handler.obtainMessage(0, app.pid, processGroup));
                if (curSchedGroup == 3) {
                    if (oldSchedGroup != 3) {
                        try {
                            app.getWindowProcessController().onTopProcChanged();
                            if (this.mService.mUseFifoUiScheduling) {
                                app.savedPriority = Process.getThreadPriority(app.pid);
                                ActivityManagerService activityManagerService = this.mService;
                                ActivityManagerService.scheduleAsFifoPriority(app.pid, true);
                                if (app.renderThreadTid != 0) {
                                    ActivityManagerService activityManagerService2 = this.mService;
                                    try {
                                        ActivityManagerService.scheduleAsFifoPriority(app.renderThreadTid, true);
                                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                            Slog.d("UI_FIFO", "Set RenderThread (TID " + app.renderThreadTid + ") to FIFO");
                                        }
                                    } catch (Exception e) {
                                        e = e;
                                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                        }
                                        if (app.repForegroundActivities == app.hasForegroundActivities()) {
                                        }
                                        if (app.getReportedProcState() != app.getCurProcState()) {
                                        }
                                        if (app.setProcState != 21) {
                                        }
                                        app.lastStateTime = now;
                                        app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
                                        if (ActivityManagerDebugConfig.DEBUG_PSS) {
                                        }
                                        if (app.setProcState == app.getCurProcState()) {
                                        }
                                        if (changes != 0) {
                                        }
                                        return success;
                                    }
                                } else if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                    Slog.d("UI_FIFO", "Not setting RenderThread TID");
                                }
                            } else {
                                Process.setThreadPriority(app.pid, -10);
                                if (app.renderThreadTid != 0) {
                                    try {
                                        Process.setThreadPriority(app.renderThreadTid, -10);
                                    } catch (IllegalArgumentException e2) {
                                    }
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                Slog.w(TAG, "Failed setting thread priority of " + app.pid, e);
                            }
                            if (app.repForegroundActivities == app.hasForegroundActivities()) {
                            }
                            if (app.getReportedProcState() != app.getCurProcState()) {
                            }
                            if (app.setProcState != 21) {
                            }
                            app.lastStateTime = now;
                            app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
                            if (ActivityManagerDebugConfig.DEBUG_PSS) {
                            }
                            if (app.setProcState == app.getCurProcState()) {
                            }
                            if (changes != 0) {
                            }
                            return success;
                        }
                    }
                } else if (oldSchedGroup == 3 && curSchedGroup != 3) {
                    app.getWindowProcessController().onTopProcChanged();
                    if (this.mService.mUseFifoUiScheduling) {
                        try {
                            try {
                                Process.setThreadScheduler(app.pid, 0, 0);
                                Process.setThreadPriority(app.pid, app.savedPriority);
                                if (app.renderThreadTid != 0) {
                                    Process.setThreadScheduler(app.renderThreadTid, 0, 0);
                                    Process.setThreadPriority(app.renderThreadTid, -4);
                                }
                            } catch (Exception e4) {
                                e = e4;
                                if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                }
                                if (app.repForegroundActivities == app.hasForegroundActivities()) {
                                }
                                if (app.getReportedProcState() != app.getCurProcState()) {
                                }
                                if (app.setProcState != 21) {
                                }
                                app.lastStateTime = now;
                                app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
                                if (ActivityManagerDebugConfig.DEBUG_PSS) {
                                }
                                if (app.setProcState == app.getCurProcState()) {
                                }
                                if (changes != 0) {
                                }
                                return success;
                            }
                        } catch (IllegalArgumentException e5) {
                            Slog.w(TAG, "Failed to set scheduling policy, thread does not exist:\n" + e5);
                        } catch (SecurityException e6) {
                            Slog.w(TAG, "Failed to set scheduling policy, not allowed:\n" + e6);
                        }
                    } else {
                        Process.setThreadPriority(app.pid, 0);
                        if (app.renderThreadTid != 0) {
                            Process.setThreadPriority(app.renderThreadTid, 0);
                        }
                    }
                }
            } else {
                app.kill(app.waitingToKill, true);
                success = false;
            }
        } else {
            success = true;
        }
        if (app.repForegroundActivities == app.hasForegroundActivities()) {
            app.repForegroundActivities = app.hasForegroundActivities();
            changes = 0 | 1;
        } else {
            changes = 0;
        }
        if (app.getReportedProcState() != app.getCurProcState()) {
            app.setReportedProcState(app.getCurProcState());
            if (app.thread != null) {
                try {
                    app.thread.setProcessState(app.getReportedProcState());
                } catch (RemoteException e7) {
                }
            }
        }
        if (app.setProcState != 21) {
            str = StringUtils.SPACE;
            str2 = " to ";
        } else if (ProcessList.procStatesDifferForMem(app.getCurProcState(), app.setProcState)) {
            str = StringUtils.SPACE;
            str2 = " to ";
        } else {
            if (now <= app.nextPssTime) {
                if (now <= app.lastPssTime + AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT) {
                    str = StringUtils.SPACE;
                    str2 = " to ";
                } else if (now <= app.lastStateTime + ProcessList.minTimeFromStateChange(this.mService.mTestPssMode)) {
                    str = StringUtils.SPACE;
                    str2 = " to ";
                }
                if (app.setProcState == app.getCurProcState()) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ || this.mService.mCurOomAdjUid == app.uid) {
                        reportOomAdjMessageLocked(ActivityManagerService.TAG_OOM_ADJ, "Proc state change of " + app.processName + str2 + ProcessList.makeProcStateString(app.getCurProcState()) + " (" + app.getCurProcState() + "): " + app.adjType);
                    }
                    boolean setImportant = app.setProcState < 11;
                    boolean curImportant = app.getCurProcState() < 11;
                    if (setImportant && !curImportant) {
                        app.setWhenUnimportant(now);
                        app.lastCpuTime = 0;
                    }
                    maybeUpdateUsageStatsLocked(app, nowElapsed);
                    maybeUpdateLastTopTime(app, now);
                    app.setProcState = app.getCurProcState();
                    if (app.setProcState >= 15) {
                        app.notCachedSinceIdle = false;
                    }
                    if (!doingAll) {
                        ActivityManagerService activityManagerService3 = this.mService;
                        activityManagerService3.setProcessTrackerStateLocked(app, activityManagerService3.mProcessStats.getMemFactorLocked(), now);
                    } else {
                        app.procStateChanged = true;
                    }
                } else if (app.reportedInteraction && nowElapsed - app.getInteractionEventTime() > this.mConstants.USAGE_STATS_INTERACTION_INTERVAL) {
                    maybeUpdateUsageStatsLocked(app, nowElapsed);
                } else if (!app.reportedInteraction && nowElapsed - app.getFgInteractionTime() > this.mConstants.SERVICE_USAGE_INTERACTION_TIME) {
                    maybeUpdateUsageStatsLocked(app, nowElapsed);
                }
                if (changes != 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(ActivityManagerService.TAG_PROCESS_OBSERVERS, "Changes in " + app + ": " + changes);
                    }
                    ActivityManagerService.ProcessChangeItem item = this.mService.enqueueProcessChangeItemLocked(app.pid, app.info.uid);
                    item.changes = changes;
                    item.foregroundActivities = app.repForegroundActivities;
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(ActivityManagerService.TAG_PROCESS_OBSERVERS, "Item " + Integer.toHexString(System.identityHashCode(item)) + str + app.toShortString() + ": changes=" + item.changes + " foreground=" + item.foregroundActivities + " type=" + app.adjType + " source=" + app.adjSource + " target=" + app.adjTarget);
                    }
                }
                return success;
            }
            if (this.mService.requestPssLocked(app, app.setProcState)) {
                int curProcState = app.getCurProcState();
                ProcessList.ProcStateMemTracker procStateMemTracker = app.procStateMemTracker;
                boolean z = this.mService.mTestPssMode;
                boolean isSleeping = this.mService.mAtmInternal.isSleeping();
                str = StringUtils.SPACE;
                str2 = " to ";
                app.nextPssTime = ProcessList.computeNextPssTime(curProcState, procStateMemTracker, z, isSleeping, now);
            } else {
                str = StringUtils.SPACE;
                str2 = " to ";
            }
            if (app.setProcState == app.getCurProcState()) {
            }
            if (changes != 0) {
            }
            return success;
        }
        app.lastStateTime = now;
        app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
        if (ActivityManagerDebugConfig.DEBUG_PSS) {
            Slog.d(ActivityManagerService.TAG_PSS, "Process state change from " + ProcessList.makeProcStateString(app.setProcState) + str2 + ProcessList.makeProcStateString(app.getCurProcState()) + " next pss in " + (app.nextPssTime - now) + ": " + app);
        }
        if (app.setProcState == app.getCurProcState()) {
        }
        if (changes != 0) {
        }
        return success;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void maybeUpdateUsageStats(ProcessRecord app, long nowElapsed) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                maybeUpdateUsageStatsLocked(app, nowElapsed);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    @GuardedBy({"mService"})
    private void maybeUpdateUsageStatsLocked(ProcessRecord app, long nowElapsed) {
        boolean isInteraction;
        if (ActivityManagerDebugConfig.DEBUG_USAGE_STATS) {
            Slog.d(TAG, "Checking proc [" + Arrays.toString(app.getPackageList()) + "] state changes: old = " + app.setProcState + ", new = " + app.getCurProcState());
        }
        if (this.mService.mUsageStatsService != null) {
            if (app.getCurProcState() <= 2 || app.getCurProcState() == 4) {
                isInteraction = true;
                app.setFgInteractionTime(0);
            } else {
                boolean z = false;
                if (app.getCurProcState() > 5) {
                    if (app.getCurProcState() <= 7) {
                        z = true;
                    }
                    isInteraction = z;
                    app.setFgInteractionTime(0);
                } else if (app.getFgInteractionTime() == 0) {
                    app.setFgInteractionTime(nowElapsed);
                    isInteraction = false;
                } else {
                    if (nowElapsed > app.getFgInteractionTime() + this.mConstants.SERVICE_USAGE_INTERACTION_TIME) {
                        z = true;
                    }
                    isInteraction = z;
                }
            }
            if (isInteraction && (!app.reportedInteraction || nowElapsed - app.getInteractionEventTime() > this.mConstants.USAGE_STATS_INTERACTION_INTERVAL)) {
                app.setInteractionEventTime(nowElapsed);
                String[] packages = app.getPackageList();
                if (packages != null) {
                    for (String str : packages) {
                        this.mService.mUsageStatsService.reportEvent(str, app.userId, 6);
                    }
                }
            }
            app.reportedInteraction = isInteraction;
            if (!isInteraction) {
                app.setInteractionEventTime(0);
            }
        }
    }

    private void maybeUpdateLastTopTime(ProcessRecord app, long nowUptime) {
        if (app.setProcState <= 2 && app.getCurProcState() > 2) {
            app.lastTopTime = nowUptime;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void idleUidsLocked() {
        int N = this.mActiveUids.size();
        if (N > 0) {
            long nowElapsed = SystemClock.elapsedRealtime();
            long maxBgTime = nowElapsed - this.mConstants.BACKGROUND_SETTLE_TIME;
            long nextTime = 0;
            PowerManagerInternal powerManagerInternal = this.mLocalPowerManager;
            if (powerManagerInternal != null) {
                powerManagerInternal.startUidChanges();
            }
            for (int i = N - 1; i >= 0; i--) {
                UidRecord uidRec = this.mActiveUids.valueAt(i);
                long bgTime = uidRec.lastBackgroundTime;
                if (bgTime > 0 && !uidRec.idle) {
                    if (bgTime <= maxBgTime) {
                        EventLogTags.writeAmUidIdle(uidRec.uid);
                        uidRec.idle = true;
                        uidRec.setIdle = true;
                        this.mService.doStopUidLocked(uidRec.uid, uidRec);
                    } else if (nextTime == 0 || nextTime > bgTime) {
                        nextTime = bgTime;
                    }
                }
            }
            PowerManagerInternal powerManagerInternal2 = this.mLocalPowerManager;
            if (powerManagerInternal2 != null) {
                powerManagerInternal2.finishUidChanges();
            }
            if (nextTime > 0) {
                this.mService.mHandler.removeMessages(58);
                this.mService.mHandler.sendEmptyMessageDelayed(58, (this.mConstants.BACKGROUND_SETTLE_TIME + nextTime) - nowElapsed);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final void setAppIdTempWhitelistStateLocked(int appId, boolean onWhitelist) {
        boolean changed = false;
        for (int i = this.mActiveUids.size() - 1; i >= 0; i--) {
            UidRecord uidRec = this.mActiveUids.valueAt(i);
            if (UserHandle.getAppId(uidRec.uid) == appId && uidRec.curWhitelist != onWhitelist) {
                uidRec.curWhitelist = onWhitelist;
                changed = true;
            }
        }
        if (changed) {
            updateOomAdjLocked(OOM_ADJ_REASON_WHITELIST);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final void setUidTempWhitelistStateLocked(int uid, boolean onWhitelist) {
        UidRecord uidRec = this.mActiveUids.get(uid);
        if (uidRec != null && uidRec.curWhitelist != onWhitelist) {
            uidRec.curWhitelist = onWhitelist;
            updateOomAdjLocked(OOM_ADJ_REASON_WHITELIST);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpProcessListVariablesLocked(ProtoOutputStream proto) {
        proto.write(1120986464305L, this.mAdjSeq);
        proto.write(1120986464306L, this.mProcessList.mLruSeq);
        proto.write(1120986464307L, this.mNumNonCachedProcs);
        proto.write(1120986464309L, this.mNumServiceProcs);
        proto.write(1120986464310L, this.mNewNumServiceProcs);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpSequenceNumbersLocked(PrintWriter pw) {
        pw.println("  mAdjSeq=" + this.mAdjSeq + " mLruSeq=" + this.mProcessList.mLruSeq);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpProcCountsLocked(PrintWriter pw) {
        pw.println("  mNumNonCachedProcs=" + this.mNumNonCachedProcs + " (" + this.mProcessList.getLruSizeLocked() + " total) mNumCachedHiddenProcs=" + this.mNumCachedHiddenProcs + " mNumServiceProcs=" + this.mNumServiceProcs + " mNewNumServiceProcs=" + this.mNewNumServiceProcs);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpAppCompactorSettings(PrintWriter pw) {
        this.mAppCompact.dump(pw);
    }
}
