package com.android.server.wm;

import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.WindowConfiguration;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IApplicationToken;
import android.view.IWindow;
import android.view.IWindowFocusObserver;
import android.view.IWindowId;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputWindowHandle;
import android.view.OppoScreenDragUtil;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowInfo;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IColorCommonListManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.wm.LocalAnimationAdapter;
import com.android.server.wm.utils.InsetUtils;
import com.android.server.wm.utils.WmDisplayCutout;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class WindowState extends OppoBaseWindowState implements WindowManagerPolicy.WindowState {
    static final int ATTACH_STATE_BEGIN = 1;
    static final int ATTACH_STATE_END = 2;
    static final int ATTACH_STATE_NOTDEFINE = 0;
    private static final float DEFAULT_DIM_AMOUNT_DEAD_WINDOW = 0.5f;
    static final int LEGACY_POLICY_VISIBILITY = 1;
    static final int MINIMUM_VISIBLE_HEIGHT_IN_DP = 32;
    static final int MINIMUM_VISIBLE_WIDTH_IN_DP = 48;
    private static final String OPPO_INCALL_ACTIVITY = "com.android.incallui/com.android.incallui.OppoInCallActivity";
    private static final int POLICY_VISIBILITY_ALL = 3;
    static final int RESIZE_HANDLE_WIDTH_IN_DP = 30;
    static final String TAG = "WindowManager";
    private static final int VISIBLE_FOR_USER = 2;
    private static final String WAKEUP_REAMSON_WMS_TURN_ON = "android.server.wm:SCREEN_ON_FLAG";
    private static final StringBuilder sTmpSB = new StringBuilder();
    private static final Comparator<WindowState> sWindowSubLayerComparator = new Comparator<WindowState>() {
        /* class com.android.server.wm.WindowState.AnonymousClass1 */

        public int compare(WindowState w1, WindowState w2) {
            int layer1 = w1.mSubLayer;
            int layer2 = w2.mSubLayer;
            if (layer1 < layer2) {
                return -1;
            }
            if (layer1 != layer2 || layer2 >= 0) {
                return 1;
            }
            return -1;
        }
    };
    private boolean mAnimateReplacingWindow;
    boolean mAnimatingExit;
    boolean mAppDied;
    boolean mAppFreezing;
    final int mAppOp;
    private boolean mAppOpVisibility;
    AppWindowToken mAppToken;
    int mAttachState;
    boolean mAttachSuccess;
    final WindowManager.LayoutParams mAttrs;
    final int mBaseLayer;
    final IWindow mClient;
    private InputChannel mClientChannel;
    final Context mContext;
    private DeadWindowEventReceiver mDeadWindowEventReceiver;
    final DeathRecipient mDeathRecipient;
    boolean mDestroying;
    private boolean mDragResizing;
    private boolean mDragResizingChangeReported;
    private PowerManager.WakeLock mDrawLock;
    private boolean mDrawnStateEvaluated;
    private final List<Rect> mExclusionRects;
    long mFinishSeamlessRotateFrameNumber;
    boolean mFloatWindowVisiblility;
    private RemoteCallbackList<IWindowFocusObserver> mFocusCallbacks;
    private boolean mForceHideNonSystemOverlayWindow;
    final boolean mForceSeamlesslyRotate;
    private long mFrameNumber;
    final Rect mGivenContentInsets;
    boolean mGivenInsetsPending;
    final Region mGivenTouchableRegion;
    final Rect mGivenVisibleInsets;
    float mGlobalScale;
    float mHScale;
    public float mHWScale;
    boolean mHasSurface;
    boolean mHaveFrame;
    boolean mHidden;
    private boolean mHiddenWhileSuspended;
    boolean mInRelayout;
    InputChannel mInputChannel;
    final InputWindowHandle mInputWindowHandle;
    private final Rect mInsetFrame;
    private InsetsSourceProvider mInsetProvider;
    float mInvGlobalScale;
    private boolean mIsChildWindow;
    private boolean mIsDimming;
    private final boolean mIsFloatingLayer;
    final boolean mIsImWindow;
    final boolean mIsWallpaper;
    private boolean mLastConfigReportedToClient;
    int mLastFreezeDuration;
    float mLastHScale;
    final Rect mLastRelayoutContentInsets;
    private final MergedConfiguration mLastReportedConfiguration;
    private int mLastRequestedHeight;
    private int mLastRequestedWidth;
    final Rect mLastSurfaceInsets;
    private CharSequence mLastTitle;
    float mLastVScale;
    int mLastVisibleLayoutRotation;
    int mLayer;
    final boolean mLayoutAttached;
    boolean mLayoutNeeded;
    int mLayoutSeq;
    boolean mLegacyPolicyVisibilityAfterAnim;
    private boolean mMovedByResize;
    public boolean mNeedHWResizer;
    boolean mObscured;
    private boolean mOrientationChangeTimedOut;
    private boolean mOrientationChanging;
    final boolean mOwnerCanAddInternalSystemWindow;
    final int mOwnerUid;
    SeamlessRotator mPendingSeamlessRotate;
    boolean mPermanentlyHidden;
    final WindowManagerPolicy mPolicy;
    public int mPolicyVisibility;
    private PowerManagerWrapper mPowerManagerWrapper;
    OppoRefreshRateData mRefreshRateData;
    boolean mRelayoutCalled;
    boolean mRemoveOnExit;
    boolean mRemoved;
    private WindowState mReplacementWindow;
    private boolean mReplacingRemoveRequested;
    boolean mReportOrientationChanged;
    int mRequestedHeight;
    int mRequestedWidth;
    private int mResizeMode;
    boolean mResizedWhileGone;
    public float mScale;
    boolean mSeamlesslyRotated;
    int mSeq;
    final Session mSession;
    private boolean mShowToOwnerOnly;
    boolean mSkipEnterAnimationForSeamlessReplacement;
    private String mStringNameCache;
    final int mSubLayer;
    private final Point mSurfacePosition;
    int mSystemUiVisibility;
    private TapExcludeRegionHolder mTapExcludeRegionHolder;
    private final Configuration mTempConfiguration;
    final Matrix mTmpMatrix;
    private final Point mTmpPoint;
    private final Rect mTmpRect;
    WindowToken mToken;
    int mTouchableInsets;
    float mVScale;
    int mViewVisibility;
    int mWallpaperDisplayOffsetX;
    int mWallpaperDisplayOffsetY;
    boolean mWallpaperVisible;
    float mWallpaperX;
    float mWallpaperXStep;
    float mWallpaperY;
    float mWallpaperYStep;
    private boolean mWasExiting;
    private boolean mWasVisibleBeforeClientHidden;
    boolean mWillReplaceWindow;
    final WindowStateAnimator mWinAnimator;
    private final WindowFrames mWindowFrames;
    final WindowId mWindowId;
    boolean mWindowRemovalAllowed;
    int mYoffset;

    /* access modifiers changed from: package-private */
    public interface PowerManagerWrapper {
        boolean isInteractive();

        void wakeUp(long j, int i, String str);
    }

    /* access modifiers changed from: package-private */
    public void seamlesslyRotateIfAllowed(SurfaceControl.Transaction transaction, int oldRotation, int rotation, boolean requested) {
        if (isVisibleNow() && !this.mIsWallpaper) {
            SeamlessRotator seamlessRotator = this.mPendingSeamlessRotate;
            if (seamlessRotator != null) {
                oldRotation = seamlessRotator.getOldRotation();
            }
            if (this.mForceSeamlesslyRotate || requested) {
                this.mPendingSeamlessRotate = new SeamlessRotator(oldRotation, rotation, getDisplayInfo());
                this.mPendingSeamlessRotate.unrotate(transaction, this);
                this.mWmService.markForSeamlessRotation(this, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void finishSeamlessRotation(boolean timeout) {
        SeamlessRotator seamlessRotator = this.mPendingSeamlessRotate;
        if (seamlessRotator != null) {
            seamlessRotator.finish(this, timeout);
            this.mFinishSeamlessRotateFrameNumber = getFrameNumber();
            this.mPendingSeamlessRotate = null;
            this.mWmService.markForSeamlessRotation(this, false);
        }
    }

    /* access modifiers changed from: package-private */
    public List<Rect> getSystemGestureExclusion() {
        return this.mExclusionRects;
    }

    /* access modifiers changed from: package-private */
    public boolean setSystemGestureExclusion(List<Rect> exclusionRects) {
        if (this.mExclusionRects.equals(exclusionRects)) {
            return false;
        }
        this.mExclusionRects.clear();
        this.mExclusionRects.addAll(exclusionRects);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isImplicitlyExcludingAllSystemGestures() {
        AppWindowToken appWindowToken;
        return ((this.mSystemUiVisibility & UsbACInterface.FORMAT_II_AC3) == 4098) && this.mWmService.mSystemGestureExcludedByPreQStickyImmersive && (appWindowToken = this.mAppToken) != null && appWindowToken.mTargetSdk < 29;
    }

    WindowState(final WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow) {
        this(service, s, c, token, parentWindow, appOp, seq, a, viewVisibility, ownerId, ownerCanAddInternalSystemWindow, new PowerManagerWrapper() {
            /* class com.android.server.wm.WindowState.AnonymousClass2 */

            @Override // com.android.server.wm.WindowState.PowerManagerWrapper
            public void wakeUp(long time, int reason, String details) {
                WindowManagerService.this.mPowerManager.wakeUp(time, reason, details);
            }

            @Override // com.android.server.wm.WindowState.PowerManagerWrapper
            public boolean isInteractive() {
                return WindowManagerService.this.mPowerManager.isInteractive();
            }
        });
    }

    WindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow, PowerManagerWrapper powerManagerWrapper) {
        super(service);
        this.mAttrs = new WindowManager.LayoutParams();
        this.mPolicyVisibility = 3;
        this.mLegacyPolicyVisibilityAfterAnim = true;
        this.mAppOpVisibility = true;
        this.mHidden = true;
        this.mDragResizingChangeReported = true;
        this.mYoffset = 0;
        this.mFloatWindowVisiblility = true;
        this.mLayoutSeq = -1;
        this.mLastReportedConfiguration = new MergedConfiguration();
        this.mTempConfiguration = new Configuration();
        this.mLastRelayoutContentInsets = new Rect();
        this.mGivenContentInsets = new Rect();
        this.mGivenVisibleInsets = new Rect();
        this.mGivenTouchableRegion = new Region();
        this.mTouchableInsets = 0;
        this.mGlobalScale = 1.0f;
        this.mInvGlobalScale = 1.0f;
        this.mHScale = 1.0f;
        this.mVScale = 1.0f;
        this.mLastHScale = 1.0f;
        this.mLastVScale = 1.0f;
        this.mTmpMatrix = new Matrix();
        this.mWindowFrames = new WindowFrames();
        this.mInsetFrame = new Rect();
        this.mExclusionRects = new ArrayList();
        this.mWallpaperX = -1.0f;
        this.mWallpaperY = -1.0f;
        this.mWallpaperXStep = -1.0f;
        this.mWallpaperYStep = -1.0f;
        this.mWallpaperDisplayOffsetX = Integer.MIN_VALUE;
        this.mWallpaperDisplayOffsetY = Integer.MIN_VALUE;
        this.mLastVisibleLayoutRotation = -1;
        this.mHasSurface = false;
        this.mWillReplaceWindow = false;
        this.mReplacingRemoveRequested = false;
        this.mAnimateReplacingWindow = false;
        this.mReplacementWindow = null;
        this.mSkipEnterAnimationForSeamlessReplacement = false;
        this.mTmpRect = new Rect();
        this.mTmpPoint = new Point();
        this.mResizedWhileGone = false;
        this.mSeamlesslyRotated = false;
        this.mLastSurfaceInsets = new Rect();
        this.mSurfacePosition = new Point();
        float f = this.mGlobalScale;
        this.mScale = f;
        this.mNeedHWResizer = false;
        this.mHWScale = f;
        this.mRefreshRateData = new OppoRefreshRateData();
        this.mFrameNumber = -1;
        this.mIsDimming = false;
        this.mAttachSuccess = true;
        this.mAttachState = 0;
        this.mSession = s;
        this.mClient = c;
        this.mAppOp = appOp;
        this.mToken = token;
        this.mAppToken = this.mToken.asAppWindowToken();
        this.mOwnerUid = ownerId;
        this.mOwnerCanAddInternalSystemWindow = ownerCanAddInternalSystemWindow;
        this.mWindowId = new WindowId();
        this.mAttrs.copyFrom(a);
        this.mLastSurfaceInsets.set(this.mAttrs.surfaceInsets);
        this.mViewVisibility = viewVisibility;
        this.mPolicy = this.mWmService.mPolicy;
        this.mContext = this.mWmService.mContext;
        DeathRecipient deathRecipient = new DeathRecipient();
        this.mSeq = seq;
        this.mPowerManagerWrapper = powerManagerWrapper;
        this.mForceSeamlesslyRotate = token.mRoundedCornerOverlay;
        if (WindowManagerService.localLOGV) {
            Slog.v("WindowManager", "Window " + this + " client=" + c.asBinder() + " token=" + token + " (" + this.mAttrs.token + ") params=" + a);
        }
        try {
            c.asBinder().linkToDeath(deathRecipient, 0);
            this.mDeathRecipient = deathRecipient;
            if (this.mAttrs.type < 1000 || this.mAttrs.type > 1999) {
                this.mBaseLayer = (this.mPolicy.getWindowLayerLw(this) * 10000) + 1000;
                this.mSubLayer = 0;
                this.mIsChildWindow = false;
                this.mLayoutAttached = false;
                this.mIsImWindow = this.mAttrs.type == 2011 || this.mAttrs.type == 2012;
                this.mIsWallpaper = this.mAttrs.type == 2013;
            } else {
                this.mBaseLayer = (this.mPolicy.getWindowLayerLw(parentWindow) * 10000) + 1000;
                this.mSubLayer = this.mPolicy.getSubWindowLayerFromTypeLw(a.type);
                this.mIsChildWindow = true;
                if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.v("WindowManager", "Adding " + this + " to " + parentWindow);
                }
                parentWindow.addChild(this, sWindowSubLayerComparator);
                this.mLayoutAttached = this.mAttrs.type != 1003;
                this.mIsImWindow = parentWindow.mAttrs.type == 2011 || parentWindow.mAttrs.type == 2012;
                this.mIsWallpaper = parentWindow.mAttrs.type == 2013;
            }
            this.mIsFloatingLayer = this.mIsImWindow || this.mIsWallpaper;
            AppWindowToken appWindowToken = this.mAppToken;
            if (appWindowToken != null && appWindowToken.mShowForAllUsers) {
                this.mAttrs.flags |= DumpState.DUMP_FROZEN;
            }
            this.mWinAnimator = new WindowStateAnimator(this);
            this.mWinAnimator.mAlpha = a.alpha;
            this.mRequestedWidth = 0;
            this.mRequestedHeight = 0;
            this.mLastRequestedWidth = 0;
            this.mLastRequestedHeight = 0;
            this.mLayer = 0;
            AppWindowToken appWindowToken2 = this.mAppToken;
            this.mInputWindowHandle = new InputWindowHandle(appWindowToken2 != null ? appWindowToken2.mInputApplicationHandle : null, c, getDisplayId());
            OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).initColorDisplayCompat(this.mAttrs.packageName, this);
            this.mColorWindowStateInner = new ColorWindowStateInner();
            this.mColorWindowStateInner = new ColorWindowStateInner();
        } catch (RemoteException e) {
            this.mDeathRecipient = null;
            this.mIsChildWindow = false;
            this.mLayoutAttached = false;
            this.mIsImWindow = false;
            this.mIsWallpaper = false;
            this.mIsFloatingLayer = false;
            this.mBaseLayer = 0;
            this.mSubLayer = 0;
            this.mInputWindowHandle = null;
            this.mWinAnimator = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void attach() {
        if (WindowManagerService.localLOGV) {
            Slog.v("WindowManager", "Attaching " + this + " token=" + this.mToken);
        }
        this.mAttachState = 1;
        this.mAttachSuccess = true;
        this.mSession.windowAddedLocked(this.mAttrs.packageName);
        this.mAttachState = 2;
    }

    /* access modifiers changed from: package-private */
    public boolean inSizeCompatMode() {
        AppWindowToken appWindowToken;
        if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
            return false;
        }
        if ((this.mAttrs.privateFlags & 128) == 0 && ((appWindowToken = this.mAppToken) == null || !appWindowToken.inSizeCompatMode() || this.mAttrs.type == 3)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean getDrawnStateEvaluated() {
        return this.mDrawnStateEvaluated;
    }

    /* access modifiers changed from: package-private */
    public void setDrawnStateEvaluated(boolean evaluated) {
        this.mDrawnStateEvaluated = evaluated;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        super.onParentChanged();
        setDrawnStateEvaluated(false);
        getDisplayContent().reapplyMagnificationSpec();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public int getOwningUid() {
        return this.mOwnerUid;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public String getOwningPackage() {
        return this.mAttrs.packageName;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean canAddInternalSystemWindow() {
        return this.mOwnerCanAddInternalSystemWindow;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean canAcquireSleepToken() {
        return this.mSession.mCanAcquireSleepToken;
    }

    private void subtractInsets(Rect frame, Rect layoutFrame, Rect insetFrame, Rect displayFrame) {
        frame.inset(Math.max(0, insetFrame.left - Math.max(layoutFrame.left, displayFrame.left)), Math.max(0, insetFrame.top - Math.max(layoutFrame.top, displayFrame.top)), Math.max(0, Math.min(layoutFrame.right, displayFrame.right) - insetFrame.right), Math.max(0, Math.min(layoutFrame.bottom, displayFrame.bottom) - insetFrame.bottom));
    }

    @Override // com.android.server.wm.WindowContainer
    public Rect getDisplayedBounds() {
        Task task = getTask();
        if (task != null) {
            Rect bounds = task.getOverrideDisplayedBounds();
            if (!bounds.isEmpty()) {
                return bounds;
            }
        }
        return super.getDisplayedBounds();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public void computeFrameLw() {
        Rect layoutDisplayFrame;
        Rect layoutContainingFrame;
        int layoutYDiff;
        int layoutXDiff;
        String str;
        DisplayContent displayContent;
        if (!this.mWillReplaceWindow || (!this.mAnimatingExit && this.mReplacingRemoveRequested)) {
            this.mHaveFrame = true;
            Task task = getTask();
            boolean isFullscreenAndFillsDisplay = !inMultiWindowMode() && matchesDisplayBounds();
            boolean windowsAreFloating = task != null && task.isFloating();
            DisplayContent dc = getDisplayContent();
            this.mInsetFrame.set(getBounds());
            WindowState imeWin = this.mWmService.mRoot.getCurrentInputMethodWindow();
            boolean isImeTarget = imeWin != null && imeWin.isVisibleNow() && isInputMethodTarget();
            OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).adjustWindowFrame(this, this.mWindowFrames);
            if (isFullscreenAndFillsDisplay || layoutInParentFrame()) {
                this.mWindowFrames.mContainingFrame.set(this.mWindowFrames.mParentFrame);
                layoutDisplayFrame = this.mWindowFrames.mDisplayFrame;
                layoutYDiff = 0;
                layoutContainingFrame = this.mWindowFrames.mParentFrame;
                layoutXDiff = 0;
            } else {
                this.mWindowFrames.mContainingFrame.set(getDisplayedBounds());
                AppWindowToken appWindowToken = this.mAppToken;
                if (appWindowToken != null && !appWindowToken.mFrozenBounds.isEmpty()) {
                    Rect frozen = this.mAppToken.mFrozenBounds.peek();
                    this.mWindowFrames.mContainingFrame.right = this.mWindowFrames.mContainingFrame.left + frozen.width();
                    this.mWindowFrames.mContainingFrame.bottom = this.mWindowFrames.mContainingFrame.top + frozen.height();
                }
                if (isImeTarget) {
                    if (inFreeformWindowingMode()) {
                        int bottomOverlap = this.mWindowFrames.mContainingFrame.bottom - this.mWindowFrames.mVisibleFrame.bottom;
                        if (bottomOverlap > 0) {
                            this.mWindowFrames.mContainingFrame.top -= Math.min(bottomOverlap, Math.max(this.mWindowFrames.mContainingFrame.top - this.mWindowFrames.mDisplayFrame.top, 0));
                        }
                    } else if (!inPinnedWindowingMode() && this.mWindowFrames.mContainingFrame.bottom > this.mWindowFrames.mParentFrame.bottom) {
                        this.mWindowFrames.mContainingFrame.bottom = this.mWindowFrames.mParentFrame.bottom;
                    }
                }
                if (windowsAreFloating && this.mWindowFrames.mContainingFrame.isEmpty()) {
                    this.mWindowFrames.mContainingFrame.set(this.mWindowFrames.mContentFrame);
                }
                TaskStack stack = getStack();
                if (inPinnedWindowingMode() && stack != null && stack.lastAnimatingBoundsWasToFullscreen()) {
                    this.mInsetFrame.intersectUnchecked(this.mWindowFrames.mParentFrame);
                    this.mWindowFrames.mContainingFrame.intersectUnchecked(this.mWindowFrames.mParentFrame);
                }
                Rect layoutDisplayFrame2 = new Rect(this.mWindowFrames.mDisplayFrame);
                this.mWindowFrames.mDisplayFrame.set(this.mWindowFrames.mContainingFrame);
                int layoutXDiff2 = this.mInsetFrame.left - this.mWindowFrames.mContainingFrame.left;
                int layoutYDiff2 = this.mInsetFrame.top - this.mWindowFrames.mContainingFrame.top;
                Rect layoutContainingFrame2 = this.mInsetFrame;
                this.mTmpRect.set(0, 0, dc.getDisplayInfo().logicalWidth, dc.getDisplayInfo().logicalHeight);
                subtractInsets(this.mWindowFrames.mDisplayFrame, layoutContainingFrame2, layoutDisplayFrame2, this.mTmpRect);
                if (!layoutInParentFrame()) {
                    subtractInsets(this.mWindowFrames.mContainingFrame, layoutContainingFrame2, this.mWindowFrames.mParentFrame, this.mTmpRect);
                    subtractInsets(this.mInsetFrame, layoutContainingFrame2, this.mWindowFrames.mParentFrame, this.mTmpRect);
                }
                layoutDisplayFrame2.intersect(layoutContainingFrame2);
                layoutDisplayFrame = layoutDisplayFrame2;
                layoutYDiff = layoutYDiff2;
                layoutContainingFrame = layoutContainingFrame2;
                layoutXDiff = layoutXDiff2;
            }
            OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).windowZoomFrame(this.mWindowFrames.mParentFrame, this.mWindowFrames.mDisplayFrame, this.mWindowFrames.mContentFrame, this.mWindowFrames.mVisibleFrame, this);
            int pw = this.mWindowFrames.mContainingFrame.width();
            int ph = this.mWindowFrames.mContainingFrame.height();
            if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
                this.mLastRequestedWidth = this.mRequestedWidth;
                this.mLastRequestedHeight = this.mRequestedHeight;
                this.mWindowFrames.setContentChanged(true);
            }
            int fw = this.mWindowFrames.mFrame.width();
            int fh = this.mWindowFrames.mFrame.height();
            applyGravityAndUpdateFrame(layoutContainingFrame, layoutDisplayFrame);
            if (fw == 0 && fh == 0 && this.mAttrs.packageName != null) {
                if (ActivityThread.inCptWhiteList((int) CompatibilityHelper.FORCE_RESET_WRONG_FRAME_SIZE, this.mAttrs.packageName)) {
                    Slog.i("WindowManager", "FORCE_RESET_WRONG_FRAME_SIZE " + this.mAttrs.packageName + " mFrame: pw = " + pw + " ,ph = " + ph);
                    this.mWindowFrames.mFrame.set(0, 0, pw, ph);
                }
            }
            this.mWindowFrames.calculateOutsets();
            if (!windowsAreFloating || this.mWindowFrames.mFrame.isEmpty()) {
                str = "WindowManager";
                if (this.mAttrs.type == 2034) {
                    dc.getDockedDividerController().positionDockedStackedDivider(this.mWindowFrames.mFrame);
                    this.mWindowFrames.mContentFrame.set(this.mWindowFrames.mFrame);
                    if (!this.mWindowFrames.mFrame.equals(this.mWindowFrames.mLastFrame)) {
                        this.mMovedByResize = true;
                    }
                } else {
                    this.mWindowFrames.mContentFrame.set(Math.max(this.mWindowFrames.mContentFrame.left, this.mWindowFrames.mFrame.left), Math.max(this.mWindowFrames.mContentFrame.top, this.mWindowFrames.mFrame.top), Math.min(this.mWindowFrames.mContentFrame.right, this.mWindowFrames.mFrame.right), Math.min(this.mWindowFrames.mContentFrame.bottom, this.mWindowFrames.mFrame.bottom));
                    this.mWindowFrames.mVisibleFrame.set(Math.max(this.mWindowFrames.mVisibleFrame.left, this.mWindowFrames.mFrame.left), Math.max(this.mWindowFrames.mVisibleFrame.top, this.mWindowFrames.mFrame.top), Math.min(this.mWindowFrames.mVisibleFrame.right, this.mWindowFrames.mFrame.right), Math.min(this.mWindowFrames.mVisibleFrame.bottom, this.mWindowFrames.mFrame.bottom));
                    this.mWindowFrames.mStableFrame.set(Math.max(this.mWindowFrames.mStableFrame.left, this.mWindowFrames.mFrame.left), Math.max(this.mWindowFrames.mStableFrame.top, this.mWindowFrames.mFrame.top), Math.min(this.mWindowFrames.mStableFrame.right, this.mWindowFrames.mFrame.right), Math.min(this.mWindowFrames.mStableFrame.bottom, this.mWindowFrames.mFrame.bottom));
                }
            } else {
                int visBottom = this.mWindowFrames.mVisibleFrame.bottom;
                int contentBottom = this.mWindowFrames.mContentFrame.bottom;
                str = "WindowManager";
                this.mWindowFrames.mContentFrame.set(this.mWindowFrames.mFrame);
                this.mWindowFrames.mVisibleFrame.set(this.mWindowFrames.mContentFrame);
                this.mWindowFrames.mStableFrame.set(this.mWindowFrames.mContentFrame);
                if (isImeTarget && inFreeformWindowingMode()) {
                    if (contentBottom + layoutYDiff < this.mWindowFrames.mContentFrame.bottom) {
                        this.mWindowFrames.mContentFrame.bottom = contentBottom + layoutYDiff;
                    }
                    if (visBottom + layoutYDiff < this.mWindowFrames.mVisibleFrame.bottom) {
                        this.mWindowFrames.mVisibleFrame.bottom = visBottom + layoutYDiff;
                    }
                }
            }
            if (isFullscreenAndFillsDisplay && !windowsAreFloating) {
                InsetUtils.insetsBetweenFrames(layoutContainingFrame, this.mWindowFrames.mOverscanFrame, this.mWindowFrames.mOverscanInsets);
            }
            if (this.mAttrs.type == 2034) {
                this.mWindowFrames.calculateDockedDividerInsets(this.mWindowFrames.mDisplayCutout.calculateRelativeTo(this.mWindowFrames.mDisplayFrame).getDisplayCutout().getSafeInsets());
            } else {
                getDisplayContent().getBounds(this.mTmpRect);
                this.mWindowFrames.calculateInsets(windowsAreFloating, isFullscreenAndFillsDisplay, this.mTmpRect);
            }
            WindowFrames windowFrames = this.mWindowFrames;
            windowFrames.setDisplayCutout(windowFrames.mDisplayCutout.calculateRelativeTo(this.mWindowFrames.mFrame));
            this.mWindowFrames.offsetFrames(-layoutXDiff, -layoutYDiff);
            this.mWindowFrames.mCompatFrame.set(this.mWindowFrames.mFrame);
            if (inSizeCompatMode() || this.mNeedHWResizer) {
                this.mWindowFrames.scaleInsets(this.mInvGlobalScale);
                this.mWindowFrames.mCompatFrame.scale(this.mInvGlobalScale);
            }
            if (this.mIsWallpaper && !((fw == this.mWindowFrames.mFrame.width() && fh == this.mWindowFrames.mFrame.height()) || (displayContent = getDisplayContent()) == null)) {
                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                getDisplayContent().mWallpaperController.updateWallpaperOffset(this, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
            }
            if (WindowManagerDebugConfig.DEBUG_LAYOUT || WindowManagerService.localLOGV) {
                Slog.v(str, "Resolving (mRequestedWidth=" + this.mRequestedWidth + ", mRequestedheight=" + this.mRequestedHeight + ") to (pw=" + pw + ", ph=" + ph + "): frame=" + this.mWindowFrames.mFrame.toShortString() + StringUtils.SPACE + this.mWindowFrames.getInsetsInfo() + StringUtils.SPACE + ((Object) this.mAttrs.getTitle()));
            }
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public Rect getBounds() {
        AppWindowToken appWindowToken = this.mAppToken;
        if (appWindowToken != null) {
            return appWindowToken.getBounds();
        }
        return super.getBounds();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public Rect getFrameLw() {
        return this.mWindowFrames.mFrame;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public Rect getDisplayFrameLw() {
        return this.mWindowFrames.mDisplayFrame;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public Rect getOverscanFrameLw() {
        return this.mWindowFrames.mOverscanFrame;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public Rect getContentFrameLw() {
        return this.mWindowFrames.mContentFrame;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public Rect getVisibleFrameLw() {
        return this.mWindowFrames.mVisibleFrame;
    }

    /* access modifiers changed from: package-private */
    public Rect getStableFrameLw() {
        return this.mWindowFrames.mStableFrame;
    }

    /* access modifiers changed from: package-private */
    public Rect getDecorFrame() {
        return this.mWindowFrames.mDecorFrame;
    }

    /* access modifiers changed from: package-private */
    public Rect getParentFrame() {
        return this.mWindowFrames.mParentFrame;
    }

    /* access modifiers changed from: package-private */
    public Rect getContainingFrame() {
        return this.mWindowFrames.mContainingFrame;
    }

    /* access modifiers changed from: package-private */
    public WmDisplayCutout getWmDisplayCutout() {
        return this.mWindowFrames.mDisplayCutout;
    }

    /* access modifiers changed from: package-private */
    public void getCompatFrame(Rect outFrame) {
        outFrame.set(this.mWindowFrames.mCompatFrame);
    }

    /* access modifiers changed from: package-private */
    public void getCompatFrameSize(Rect outFrame) {
        outFrame.set(0, 0, this.mWindowFrames.mCompatFrame.width(), this.mWindowFrames.mCompatFrame.height());
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean getGivenInsetsPendingLw() {
        return this.mGivenInsetsPending;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public Rect getGivenContentInsetsLw() {
        return this.mGivenContentInsets;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public Rect getGivenVisibleInsetsLw() {
        return this.mGivenVisibleInsets;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public WindowManager.LayoutParams getAttrs() {
        return this.mAttrs;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean getNeedsMenuLw(WindowManagerPolicy.WindowState bottom) {
        return getDisplayContent().getNeedsMenu(this, bottom);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public int getSystemUiVisibility() {
        return this.mSystemUiVisibility;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public int getSurfaceLayer() {
        return this.mLayer;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public int getBaseType() {
        return getTopParentWindow().mAttrs.type;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public IApplicationToken getAppToken() {
        AppWindowToken appWindowToken = this.mAppToken;
        if (appWindowToken != null) {
            return appWindowToken.appToken;
        }
        return null;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isVoiceInteraction() {
        AppWindowToken appWindowToken = this.mAppToken;
        return appWindowToken != null && appWindowToken.mVoiceInteraction;
    }

    /* access modifiers changed from: package-private */
    public boolean setReportResizeHints() {
        return this.mWindowFrames.setReportResizeHints();
    }

    /* access modifiers changed from: package-private */
    public void updateResizingWindowIfNeeded() {
        WindowStateAnimator winAnimator = this.mWinAnimator;
        if (this.mHasSurface && getDisplayContent().mLayoutSeq == this.mLayoutSeq && !isGoneForLayoutLw()) {
            Task task = getTask();
            if (task == null || !task.mStack.isAnimatingBounds()) {
                boolean didFrameInsetsChange = setReportResizeHints();
                boolean configChanged = !isLastConfigReportedToClient();
                if (WindowManagerDebugConfig.DEBUG_CONFIGURATION && configChanged) {
                    Slog.v("WindowManager", "Win " + this + " config changed: " + getConfiguration());
                }
                boolean dragResizingChanged = isDragResizeChanged() && !isDragResizingChangeReported();
                if (WindowManagerService.localLOGV) {
                    Slog.v("WindowManager", "Resizing " + this + ": configChanged=" + configChanged + " dragResizingChanged=" + dragResizingChanged + " last=" + this.mWindowFrames.mLastFrame + " frame=" + this.mWindowFrames.mFrame);
                }
                this.mWindowFrames.mLastFrame.set(this.mWindowFrames.mFrame);
                if (didFrameInsetsChange || winAnimator.mSurfaceResized || configChanged || dragResizingChanged || this.mReportOrientationChanged) {
                    if (WindowManagerDebugConfig.DEBUG_RESIZE || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Resize reasons for w=" + this + ":  " + this.mWindowFrames.getInsetsChangedInfo() + " surfaceResized=" + winAnimator.mSurfaceResized + " configChanged=" + configChanged + " dragResizingChanged=" + dragResizingChanged + " reportOrientationChanged=" + this.mReportOrientationChanged + " contentInsets=" + getContentInsets() + " visibleInsets=" + getVisibleInsets());
                    }
                    AppWindowToken appWindowToken = this.mAppToken;
                    if (appWindowToken == null || !this.mAppDied) {
                        updateLastInsetValues();
                        this.mWmService.makeWindowFreezingScreenIfNeededLocked(this);
                        if (getOrientationChanging() || dragResizingChanged) {
                            if (WindowManagerDebugConfig.DEBUG_ANIM || WindowManagerDebugConfig.DEBUG_ORIENTATION || WindowManagerDebugConfig.DEBUG_RESIZE) {
                                Slog.v("WindowManager", "Orientation or resize start waiting for draw, mDrawState=DRAW_PENDING in " + this + ", surfaceController " + winAnimator.mSurfaceController);
                            }
                            winAnimator.mDrawState = 1;
                            AppWindowToken appWindowToken2 = this.mAppToken;
                            if (appWindowToken2 != null) {
                                appWindowToken2.clearAllDrawn();
                            }
                        }
                        if (!this.mWmService.mResizingWindows.contains(this)) {
                            if (WindowManagerDebugConfig.DEBUG_RESIZE || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v("WindowManager", "Resizing window " + this);
                            }
                            this.mWmService.mResizingWindows.add(this);
                            return;
                        }
                        return;
                    }
                    appWindowToken.removeDeadWindows();
                } else if (getOrientationChanging() && isDrawnLw()) {
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Orientation not waiting for draw in " + this + ", surfaceController " + winAnimator.mSurfaceController);
                    }
                    setOrientationChanging(false);
                    this.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mWmService.mDisplayFreezeTime);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getOrientationChanging() {
        return (this.mOrientationChanging || (isVisible() && getConfiguration().orientation != getLastReportedConfiguration().orientation)) && !this.mSeamlesslyRotated && !this.mOrientationChangeTimedOut;
    }

    /* access modifiers changed from: package-private */
    public void setOrientationChanging(boolean changing) {
        this.mOrientationChanging = changing;
        this.mOrientationChangeTimedOut = false;
    }

    /* access modifiers changed from: package-private */
    public void orientationChangeTimedOut() {
        this.mOrientationChangeTimedOut = true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public DisplayContent getDisplayContent() {
        return this.mToken.getDisplayContent();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDisplayChanged(DisplayContent dc) {
        super.onDisplayChanged(dc);
        if (dc != null && this.mInputWindowHandle.displayId != dc.getDisplayId()) {
            this.mLayoutSeq = dc.mLayoutSeq - 1;
            this.mInputWindowHandle.displayId = dc.getDisplayId();
        }
    }

    /* access modifiers changed from: package-private */
    public DisplayInfo getDisplayInfo() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent != null) {
            return displayContent.getDisplayInfo();
        }
        return null;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public int getDisplayId() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return -1;
        }
        return displayContent.getDisplayId();
    }

    /* access modifiers changed from: package-private */
    public Task getTask() {
        AppWindowToken appWindowToken = this.mAppToken;
        if (appWindowToken != null) {
            return appWindowToken.getTask();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack() {
        Task task = getTask();
        if (task != null && task.mStack != null) {
            return task.mStack;
        }
        DisplayContent dc = getDisplayContent();
        if (this.mAttrs.type < 2000 || dc == null) {
            return null;
        }
        return dc.getHomeStack();
    }

    /* access modifiers changed from: package-private */
    public void getVisibleBounds(Rect bounds) {
        Task task = getTask();
        boolean intersectWithStackBounds = task != null && task.cropWindowsToStackBounds();
        bounds.setEmpty();
        this.mTmpRect.setEmpty();
        if (intersectWithStackBounds) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
            } else {
                intersectWithStackBounds = false;
            }
        }
        bounds.set(this.mWindowFrames.mVisibleFrame);
        if (intersectWithStackBounds) {
            bounds.intersect(this.mTmpRect);
        }
        if (bounds.isEmpty()) {
            bounds.set(this.mWindowFrames.mFrame);
            if (intersectWithStackBounds) {
                bounds.intersect(this.mTmpRect);
            }
        }
    }

    public long getInputDispatchingTimeoutNanos() {
        AppWindowToken appWindowToken = this.mAppToken;
        if (appWindowToken != null) {
            return appWindowToken.mInputDispatchingTimeoutNanos;
        }
        return 5000000000L;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean hasAppShownWindows() {
        AppWindowToken appWindowToken = this.mAppToken;
        return appWindowToken != null && (appWindowToken.firstWindowDrawn || this.mAppToken.startingDisplayed);
    }

    /* access modifiers changed from: package-private */
    public boolean isIdentityMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        if (dsdx < 0.99999f || dsdx > 1.00001f || dtdy < 0.99999f || dtdy > 1.00001f || dtdx < -1.0E-6f || dtdx > 1.0E-6f || dsdy < -1.0E-6f || dsdy > 1.0E-6f) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void prelayout() {
        if (this.mNeedHWResizer) {
            this.mGlobalScale = this.mHWScale;
            this.mInvGlobalScale = 1.0f / this.mGlobalScale;
            Slog.v("AppResolutionTuner", "windowstate prelayout() Need HWResizer, mGlobalScale = " + this.mGlobalScale + " , this = " + this);
        } else if (inSizeCompatMode()) {
            this.mGlobalScale = this.mToken.getSizeCompatScale();
            if (this.mAttrs.type == 3) {
                this.mGlobalScale = 1.0f;
            } else {
                this.mGlobalScale = this.mScale;
            }
            this.mInvGlobalScale = 1.0f / this.mGlobalScale;
        } else {
            this.mInvGlobalScale = 1.0f;
            this.mGlobalScale = 1.0f;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean hasContentToDisplay() {
        if (!this.mAppFreezing && isDrawnLw()) {
            if (this.mViewVisibility == 0) {
                return true;
            }
            if (isAnimating() && !getDisplayContent().mAppTransition.isTransitionSet()) {
                return true;
            }
        }
        return super.hasContentToDisplay();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isVisible() {
        InsetsSourceProvider insetsSourceProvider;
        return wouldBeVisibleIfPolicyIgnored() && isVisibleByPolicy() && ((insetsSourceProvider = this.mInsetProvider) == null || insetsSourceProvider.isClientVisible());
    }

    /* access modifiers changed from: package-private */
    public boolean isVisibleByPolicy() {
        return (this.mPolicyVisibility & 3) == 3;
    }

    /* access modifiers changed from: package-private */
    public void clearPolicyVisibilityFlag(int policyVisibilityFlag) {
        this.mPolicyVisibility &= ~policyVisibilityFlag;
        this.mWmService.scheduleAnimationLocked();
    }

    /* access modifiers changed from: package-private */
    public void setPolicyVisibilityFlag(int policyVisibilityFlag) {
        this.mPolicyVisibility |= policyVisibilityFlag;
        this.mWmService.scheduleAnimationLocked();
    }

    private boolean isLegacyPolicyVisibility() {
        return (this.mPolicyVisibility & 1) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean wouldBeVisibleIfPolicyIgnored() {
        return this.mHasSurface && !isParentWindowHidden() && !this.mAnimatingExit && !this.mDestroying && (!this.mIsWallpaper || this.mWallpaperVisible);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isVisibleLw() {
        return isVisible();
    }

    /* access modifiers changed from: package-private */
    public boolean isWinVisibleLw() {
        AppWindowToken appWindowToken = this.mAppToken;
        return (appWindowToken == null || !appWindowToken.hiddenRequested || this.mAppToken.isSelfAnimating()) && isVisible();
    }

    /* access modifiers changed from: package-private */
    public boolean isVisibleNow() {
        return (!this.mToken.isHidden() || this.mAttrs.type == 3) && isVisible();
    }

    /* access modifiers changed from: package-private */
    public boolean isPotentialDragTarget() {
        return isVisibleNow() && !this.mRemoved && this.mInputChannel != null && this.mInputWindowHandle != null;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisibleOrAdding() {
        AppWindowToken atoken = this.mAppToken;
        return (this.mHasSurface || (!this.mRelayoutCalled && this.mViewVisibility == 0)) && isVisibleByPolicy() && !isParentWindowHidden() && (atoken == null || !atoken.hiddenRequested) && !this.mAnimatingExit && !this.mDestroying;
    }

    /* access modifiers changed from: package-private */
    public boolean isOnScreen() {
        if (!this.mHasSurface || this.mDestroying || !isVisibleByPolicy() || !OppoFeatureCache.get(IColorLockTaskController.DEFAULT).canShowInLockDeviceMode(this.mAttrs.type)) {
            return false;
        }
        AppWindowToken atoken = this.mAppToken;
        if (atoken != null) {
            if ((isParentWindowHidden() || atoken.hiddenRequested) && !isAnimating()) {
                return false;
            }
            return true;
        } else if (!isParentWindowHidden() || isAnimating()) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean mightAffectAllDrawn() {
        return (isOnScreen() || (this.mWinAnimator.mAttrType == 1 || this.mWinAnimator.mAttrType == 4)) && !this.mAnimatingExit && !this.mDestroying;
    }

    /* access modifiers changed from: package-private */
    public boolean isInteresting() {
        AppWindowToken appWindowToken = this.mAppToken;
        return appWindowToken != null && !this.mAppDied && (!appWindowToken.isFreezingScreen() || !this.mAppFreezing) && this.mViewVisibility == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isReadyForDisplay() {
        if (this.mToken.waitingToShow && getDisplayContent().mAppTransition.isTransitionSet()) {
            return false;
        }
        boolean parentAndClientVisible = !isParentWindowHidden() && this.mViewVisibility == 0 && !this.mToken.isHidden();
        if (!this.mHasSurface || !isVisibleByPolicy() || this.mDestroying) {
            return false;
        }
        if (parentAndClientVisible || isAnimating()) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean canAffectSystemUiFlags() {
        if (this.mAttrs.alpha == OppoBrightUtils.MIN_LUX_LIMITI) {
            return false;
        }
        if (this.mAppToken == null) {
            return this.mWinAnimator.getShown() && !(this.mAnimatingExit || this.mDestroying);
        }
        Task task = getTask();
        return (task != null && task.canAffectSystemUiFlags()) && !this.mAppToken.isHidden();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isDisplayedLw() {
        AppWindowToken atoken = this.mAppToken;
        return isDrawnLw() && isVisibleByPolicy() && ((!isParentWindowHidden() && (atoken == null || !atoken.hiddenRequested)) || isAnimating());
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isAnimatingLw() {
        return isAnimating();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isGoneForLayoutLw() {
        AppWindowToken atoken = this.mAppToken;
        return this.mViewVisibility == 8 || !this.mRelayoutCalled || (atoken == null && this.mToken.isHidden()) || ((atoken != null && atoken.hiddenRequested) || isParentWindowGoneForLayout() || ((this.mAnimatingExit && !isAnimatingLw()) || this.mDestroying));
    }

    public boolean isDrawFinishedLw() {
        return this.mHasSurface && !this.mDestroying && (this.mWinAnimator.mDrawState == 2 || this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isDrawnLw() {
        return this.mHasSurface && !this.mDestroying && (this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4);
    }

    private boolean isOpaqueDrawn() {
        return ((!this.mIsWallpaper && this.mAttrs.format == -1) || (this.mIsWallpaper && this.mWallpaperVisible)) && isDrawnLw() && !isAnimating();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onMovedByResize() {
        if (WindowManagerDebugConfig.DEBUG_RESIZE) {
            Slog.d("WindowManager", "onMovedByResize: Moving " + this);
        }
        this.mMovedByResize = true;
        super.onMovedByResize();
    }

    /* access modifiers changed from: package-private */
    public boolean onAppVisibilityChanged(boolean visible, boolean runningAppAnimation) {
        boolean changed = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            changed |= ((WindowState) this.mChildren.get(i)).onAppVisibilityChanged(visible, runningAppAnimation);
        }
        if (this.mAttrs.type == 3) {
            if (!visible && isVisibleNow() && this.mAppToken.isSelfAnimating()) {
                this.mAnimatingExit = true;
                this.mRemoveOnExit = true;
                this.mWindowRemovalAllowed = true;
            }
            return changed;
        }
        boolean isVisibleNow = isVisibleNow();
        if (visible == isVisibleNow) {
            return changed;
        }
        if (!runningAppAnimation && isVisibleNow) {
            AccessibilityController accessibilityController = this.mWmService.mAccessibilityController;
            this.mWinAnimator.applyAnimationLocked(2, false);
            if (accessibilityController != null) {
                accessibilityController.onWindowTransitionLocked(this, 2);
            }
        }
        setDisplayLayoutNeeded();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean onSetAppExiting() {
        DisplayContent displayContent = getDisplayContent();
        boolean changed = false;
        if (isVisibleNow()) {
            this.mWinAnimator.applyAnimationLocked(2, false);
            if (this.mWmService.mAccessibilityController != null) {
                this.mWmService.mAccessibilityController.onWindowTransitionLocked(this, 2);
            }
            changed = true;
            if (displayContent != null) {
                displayContent.setLayoutNeeded();
            }
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            changed |= ((WindowState) this.mChildren.get(i)).onSetAppExiting();
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onResize() {
        ArrayList<WindowState> resizingWindows = this.mWmService.mResizingWindows;
        if (this.mHasSurface && !isGoneForLayoutLw() && !resizingWindows.contains(this)) {
            if (WindowManagerDebugConfig.DEBUG_RESIZE) {
                Slog.d("WindowManager", "onResize: Resizing " + this);
            }
            resizingWindows.add(this);
        }
        if (isGoneForLayoutLw()) {
            this.mResizedWhileGone = true;
        }
        super.onResize();
    }

    /* access modifiers changed from: package-private */
    public void onUnfreezeBounds() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onUnfreezeBounds();
        }
        if (this.mHasSurface) {
            this.mLayoutNeeded = true;
            setDisplayLayoutNeeded();
            if (!this.mWmService.mResizingWindows.contains(this)) {
                this.mWmService.mResizingWindows.add(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleWindowMovedIfNeeded() {
        if (hasMoved()) {
            int left = this.mWindowFrames.mFrame.left;
            int top = this.mWindowFrames.mFrame.top;
            Task task = getTask();
            boolean adjustedForMinimizedDockOrIme = task != null && (task.mStack.isAdjustedForMinimizedDockedStack() || task.mStack.isAdjustedForIme());
            if (this.mToken.okToAnimate() && (this.mAttrs.privateFlags & 64) == 0 && !isDragResizing() && !adjustedForMinimizedDockOrIme && getWindowConfiguration().hasMovementAnimations() && !this.mWinAnimator.mLastHidden && !this.mSeamlesslyRotated) {
                startMoveAnimation(left, top);
            }
            if (this.mWmService.mAccessibilityController != null && getDisplayContent().getDisplayId() == 0) {
                this.mWmService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            try {
                this.mClient.moved(left, top);
            } catch (RemoteException e) {
            }
            this.mMovedByResize = false;
        }
    }

    private boolean hasMoved() {
        return this.mHasSurface && (this.mWindowFrames.hasContentChanged() || this.mMovedByResize) && !this.mAnimatingExit && (!(this.mWindowFrames.mFrame.top == this.mWindowFrames.mLastFrame.top && this.mWindowFrames.mFrame.left == this.mWindowFrames.mLastFrame.left) && (!this.mIsChildWindow || !getParentWindow().hasMoved()));
    }

    /* access modifiers changed from: package-private */
    public boolean isObscuringDisplay() {
        Task task = getTask();
        if ((task == null || task.mStack == null || task.mStack.fillsParent()) && isOpaqueDrawn() && fillsDisplay()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean fillsDisplay() {
        DisplayInfo displayInfo = getDisplayInfo();
        return this.mWindowFrames.mFrame.left <= 0 && this.mWindowFrames.mFrame.top <= 0 && this.mWindowFrames.mFrame.right >= displayInfo.appWidth && this.mWindowFrames.mFrame.bottom >= displayInfo.appHeight;
    }

    private boolean matchesDisplayBounds() {
        return getDisplayContent().getBounds().equals(getBounds());
    }

    /* access modifiers changed from: package-private */
    public boolean isLastConfigReportedToClient() {
        return this.mLastConfigReportedToClient;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public void onMergedOverrideConfigurationChanged() {
        super.onMergedOverrideConfigurationChanged();
        this.mLastConfigReportedToClient = false;
    }

    /* access modifiers changed from: package-private */
    public void onWindowReplacementTimeout() {
        if (this.mWillReplaceWindow) {
            removeImmediately();
            return;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onWindowReplacementTimeout();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void forceWindowsScaleableInTransaction(boolean force) {
        WindowStateAnimator windowStateAnimator = this.mWinAnimator;
        if (windowStateAnimator != null && windowStateAnimator.hasSurface()) {
            this.mWinAnimator.mSurfaceController.forceScaleableInTransaction(force);
        }
        super.forceWindowsScaleableInTransaction(force);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeImmediately() {
        super.removeImmediately();
        if (!this.mRemoved) {
            this.mRemoved = true;
            this.mWillReplaceWindow = false;
            WindowState windowState = this.mReplacementWindow;
            if (windowState != null) {
                windowState.mSkipEnterAnimationForSeamlessReplacement = false;
            }
            DisplayContent dc = getDisplayContent();
            if (isInputMethodTarget()) {
                dc.computeImeTarget(true);
            }
            if (WindowManagerService.excludeWindowTypeFromTapOutTask(this.mAttrs.type)) {
                dc.mTapExcludedWindows.remove(this);
            }
            if (this.mTapExcludeRegionHolder != null) {
                dc.mTapExcludeProvidingWindows.remove(this);
            }
            dc.getDisplayPolicy().removeWindowLw(this);
            disposeInputChannel();
            this.mWinAnimator.destroyDeferredSurfaceLocked();
            this.mWinAnimator.destroySurfaceLocked();
            this.mSession.windowRemovedLocked();
            try {
                this.mClient.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
            } catch (RuntimeException e) {
            }
            this.mWmService.postWindowRemoveCleanupLocked(this);
        } else if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "WS.removeImmediately: " + this + " Already removed...");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeIfPossible() {
        super.removeIfPossible();
        removeIfPossible(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeIfPossible(boolean keepVisibleDeadWindow) {
        this.mWindowRemovalAllowed = true;
        int transit = 5;
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.v("WindowManager", "removeIfPossible: " + this + " callers=" + Debug.getCallers(5));
        }
        int i = 0;
        boolean startingWindow = this.mAttrs.type == 3;
        if (startingWindow && WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.d("WindowManager", "Starting window removed " + this);
        }
        if (WindowManagerService.localLOGV || WindowManagerDebugConfig.DEBUG_FOCUS || (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT && isFocused())) {
            Slog.v("WindowManager", "Remove " + this + " client=" + Integer.toHexString(System.identityHashCode(this.mClient.asBinder())) + ", surfaceController=" + this.mWinAnimator.mSurfaceController + " Callers=" + Debug.getCallers(5));
        }
        long origId = Binder.clearCallingIdentity();
        try {
            disposeInputChannel();
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                StringBuilder sb = new StringBuilder();
                sb.append("Remove ");
                sb.append(this);
                sb.append(": mSurfaceController=");
                sb.append(this.mWinAnimator.mSurfaceController);
                sb.append(" mAnimatingExit=");
                sb.append(this.mAnimatingExit);
                sb.append(" mRemoveOnExit=");
                sb.append(this.mRemoveOnExit);
                sb.append(" mHasSurface=");
                sb.append(this.mHasSurface);
                sb.append(" surfaceShowing=");
                sb.append(this.mWinAnimator.getShown());
                sb.append(" animating=");
                sb.append(isAnimating());
                sb.append(" app-animation=");
                sb.append(this.mAppToken != null ? Boolean.valueOf(this.mAppToken.isSelfAnimating()) : TemperatureProvider.SWITCH_OFF);
                sb.append(" mWillReplaceWindow=");
                sb.append(this.mWillReplaceWindow);
                sb.append(" inPendingTransaction=");
                sb.append(this.mAppToken != null ? this.mAppToken.inPendingTransaction : false);
                sb.append(" mDisplayFrozen=");
                sb.append(this.mWmService.mDisplayFrozen);
                sb.append(" callers=");
                sb.append(Debug.getCallers(6));
                Slog.v("WindowManager", sb.toString());
            }
            boolean wasVisible = false;
            getDisplayId();
            if (this.mHasSurface && this.mToken.okToAnimate()) {
                if (this.mWillReplaceWindow) {
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "Preserving " + this + " until the new one is added");
                    }
                    this.mAnimatingExit = true;
                    this.mReplacingRemoveRequested = true;
                    return;
                }
                wasVisible = isWinVisibleLw();
                if (keepVisibleDeadWindow) {
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "Not removing " + this + " because app died while it's visible");
                    }
                    this.mAppDied = true;
                    setDisplayLayoutNeeded();
                    this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                    openInputChannel(null);
                    getDisplayContent().getInputMonitor().updateInputWindowsLw(true);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
                if (wasVisible) {
                    if (!startingWindow) {
                        transit = 2;
                    }
                    if (this.mWinAnimator.applyAnimationLocked(transit, false)) {
                        this.mAnimatingExit = true;
                        setDisplayLayoutNeeded();
                        this.mWmService.requestTraversal();
                    }
                    if (this.mWmService.mAccessibilityController != null) {
                        this.mWmService.mAccessibilityController.onWindowTransitionLocked(this, transit);
                    }
                }
                boolean isAnimating = isAnimating() && (this.mAppToken == null || !this.mAppToken.isWaitingForTransitionStart());
                boolean lastWindowIsStartingWindow = startingWindow && this.mAppToken != null && this.mAppToken.isLastWindow(this);
                if (!this.mWinAnimator.getShown() || !this.mAnimatingExit || (lastWindowIsStartingWindow && !isAnimating)) {
                    this.mAnimatingExit = false;
                } else {
                    if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                        Slog.v("WindowManager", "Not removing " + this + " due to exit animation ");
                    }
                    setupWindowForRemoveOnExit();
                    if (this.mAppToken != null) {
                        this.mAppToken.updateReportedVisibilityLocked();
                    }
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            removeImmediately();
            if (wasVisible) {
                DisplayContent displayContent = getDisplayContent();
                if (displayContent.updateOrientationFromAppTokens()) {
                    displayContent.sendNewConfiguration();
                }
            }
            WindowManagerService windowManagerService = this.mWmService;
            if (isFocused()) {
                i = 4;
            }
            windowManagerService.updateFocusedWindowLocked(i, true);
            Binder.restoreCallingIdentity(origId);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private void setupWindowForRemoveOnExit() {
        this.mRemoveOnExit = true;
        setDisplayLayoutNeeded();
        boolean focusChanged = this.mWmService.updateFocusedWindowLocked(3, false);
        this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
        if (focusChanged) {
            getDisplayContent().getInputMonitor().updateInputWindowsLw(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void setHasSurface(boolean hasSurface) {
        this.mHasSurface = hasSurface;
        Session session = this.mSession;
        if (session != null && session.mUid > 10000 && this.mWinAnimator != null) {
            OppoFeatureCache.get(IColorCommonListManager.DEFAULT).updateWindowState(this.mSession.mUid, this.mSession.mPid, hashCode(), this.mAttrs.type, hasSurface, this.mWinAnimator.getShown());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getHasSurface() {
        return this.mHasSurface;
    }

    /* access modifiers changed from: package-private */
    public boolean canBeImeTarget() {
        if (this.mIsImWindow) {
            return false;
        }
        AppWindowToken appWindowToken = this.mAppToken;
        if (!(appWindowToken == null || appWindowToken.windowsAreFocusable())) {
            return false;
        }
        int fl = this.mAttrs.flags & 131080;
        int type = this.mAttrs.type;
        if (fl != 0 && fl != 131080 && type != 3) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i("WindowManager", "isVisibleOrAdding " + this + ": " + isVisibleOrAdding());
            if (!isVisibleOrAdding()) {
                Slog.i("WindowManager", "  mSurfaceController=" + this.mWinAnimator.mSurfaceController + " relayoutCalled=" + this.mRelayoutCalled + " viewVis=" + this.mViewVisibility + " policyVis=" + isVisibleByPolicy() + " policyVisAfterAnim=" + this.mLegacyPolicyVisibilityAfterAnim + " parentHidden=" + isParentWindowHidden() + " exiting=" + this.mAnimatingExit + " destroying=" + this.mDestroying);
                if (this.mAppToken != null) {
                    Slog.i("WindowManager", "  mAppToken.hiddenRequested=" + this.mAppToken.hiddenRequested);
                }
            }
        }
        return isVisibleOrAdding();
    }

    /* access modifiers changed from: private */
    public final class DeadWindowEventReceiver extends InputEventReceiver {
        DeadWindowEventReceiver(InputChannel inputChannel) {
            super(inputChannel, WindowState.this.mWmService.mH.getLooper());
        }

        public void onInputEvent(InputEvent event) {
            finishInputEvent(event, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void openInputChannel(InputChannel outInputChannel) {
        if (this.mInputChannel == null) {
            InputChannel[] inputChannels = InputChannel.openInputChannelPair(getName());
            this.mInputChannel = inputChannels[0];
            this.mClientChannel = inputChannels[1];
            this.mInputWindowHandle.token = this.mClient.asBinder();
            if (outInputChannel != null) {
                this.mClientChannel.transferTo(outInputChannel);
                this.mClientChannel.dispose();
                this.mClientChannel = null;
            } else {
                this.mDeadWindowEventReceiver = new DeadWindowEventReceiver(this.mClientChannel);
            }
            this.mWmService.mInputManager.registerInputChannel(this.mInputChannel, this.mClient.asBinder());
            return;
        }
        throw new IllegalStateException("Window already has an input channel.");
    }

    /* access modifiers changed from: package-private */
    public void disposeInputChannel() {
        DeadWindowEventReceiver deadWindowEventReceiver = this.mDeadWindowEventReceiver;
        if (deadWindowEventReceiver != null) {
            deadWindowEventReceiver.dispose();
            this.mDeadWindowEventReceiver = null;
        }
        if (this.mInputChannel != null) {
            this.mWmService.mInputManager.unregisterInputChannel(this.mInputChannel);
            this.mInputChannel.dispose();
            this.mInputChannel = null;
        }
        InputChannel inputChannel = this.mClientChannel;
        if (inputChannel != null) {
            inputChannel.dispose();
            this.mClientChannel = null;
        }
        this.mInputWindowHandle.token = null;
    }

    /* access modifiers changed from: package-private */
    public boolean removeReplacedWindowIfNeeded(WindowState replacement) {
        if (!this.mWillReplaceWindow || this.mReplacementWindow != replacement || !replacement.hasDrawnLw()) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                if (((WindowState) this.mChildren.get(i)).removeReplacedWindowIfNeeded(replacement)) {
                    return true;
                }
            }
            return false;
        }
        replacement.mSkipEnterAnimationForSeamlessReplacement = false;
        removeReplacedWindow();
        return true;
    }

    private void removeReplacedWindow() {
        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.d("WindowManager", "Removing replaced window: " + this);
        }
        this.mWillReplaceWindow = false;
        this.mAnimateReplacingWindow = false;
        this.mReplacingRemoveRequested = false;
        this.mReplacementWindow = null;
        if (this.mAnimatingExit || !this.mAnimateReplacingWindow) {
            removeImmediately();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setReplacementWindowIfNeeded(WindowState replacementCandidate) {
        boolean replacementSet = false;
        if (this.mWillReplaceWindow && this.mReplacementWindow == null && getWindowTag().toString().equals(replacementCandidate.getWindowTag().toString())) {
            this.mReplacementWindow = replacementCandidate;
            replacementCandidate.mSkipEnterAnimationForSeamlessReplacement = !this.mAnimateReplacingWindow;
            replacementSet = true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            replacementSet |= ((WindowState) this.mChildren.get(i)).setReplacementWindowIfNeeded(replacementCandidate);
        }
        return replacementSet;
    }

    /* access modifiers changed from: package-private */
    public void setDisplayLayoutNeeded() {
        DisplayContent dc = getDisplayContent();
        if (dc != null) {
            dc.setLayoutNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void applyAdjustForImeIfNeeded() {
        Task task = getTask();
        if (task != null && task.mStack != null && task.mStack.isAdjustedForIme()) {
            task.mStack.applyAdjustForImeIfNeeded(task);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void switchUser() {
        super.switchUser();
        if (isHiddenFromUserLocked()) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.w("WindowManager", "user changing, hiding " + this + ", attrs=" + this.mAttrs.type + ", belonging to " + this.mOwnerUid);
            }
            clearPolicyVisibilityFlag(2);
            return;
        }
        setPolicyVisibilityFlag(2);
    }

    /* access modifiers changed from: package-private */
    public int getSurfaceTouchableRegion(InputWindowHandle inputWindowHandle, int flags) {
        AppWindowToken appWindowToken;
        int surfaceOffsetX = 0;
        boolean modal = (flags & 40) == 0;
        Region region = inputWindowHandle.touchableRegion;
        setTouchableRegionCropIfNeeded(inputWindowHandle);
        AppWindowToken appWindowToken2 = this.mAppToken;
        Rect appOverrideBounds = appWindowToken2 != null ? appWindowToken2.getResolvedOverrideBounds() : null;
        if (appOverrideBounds == null || appOverrideBounds.isEmpty()) {
            if (modal && (appWindowToken = this.mAppToken) != null) {
                flags |= 32;
                appWindowToken.getLetterboxInnerBounds(this.mTmpRect);
                if (this.mTmpRect.isEmpty()) {
                    Task task = getTask();
                    if (task != null) {
                        task.getDimBounds(this.mTmpRect);
                    } else {
                        getStack().getDimBounds(this.mTmpRect);
                    }
                }
                if (inFreeformWindowingMode()) {
                    int delta = WindowManagerService.dipToPixel(30, getDisplayContent().getDisplayMetrics());
                    this.mTmpRect.inset(-delta, -delta);
                }
                region.set(this.mTmpRect);
                cropRegionToStackBoundsIfNeeded(region);
                subtractTouchExcludeRegionIfNeeded(region);
            } else if (!modal || this.mTapExcludeRegionHolder == null) {
                getTouchableRegion(region);
            } else {
                Region touchExcludeRegion = Region.obtain();
                amendTapExcludeRegion(touchExcludeRegion);
                if (!touchExcludeRegion.isEmpty()) {
                    flags |= 32;
                    getDisplayContent().getBounds(this.mTmpRect);
                    int dw = this.mTmpRect.width();
                    int dh = this.mTmpRect.height();
                    region.set(-dw, -dh, dw + dw, dh + dh);
                    region.op(touchExcludeRegion, Region.Op.DIFFERENCE);
                    inputWindowHandle.setTouchableRegionCrop((SurfaceControl) null);
                }
                touchExcludeRegion.recycle();
            }
            OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).injectorGetSurfaceTouchableRegion(this, region);
            region.translate(-this.mWindowFrames.mFrame.left, -this.mWindowFrames.mFrame.top);
            return OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).clearWindowFlagsIfNeed(this, flags);
        }
        if (modal) {
            flags |= 32;
            this.mTmpRect.set(0, 0, appOverrideBounds.width(), appOverrideBounds.height());
        } else {
            this.mTmpRect.set(this.mWindowFrames.mCompatFrame);
        }
        if (this.mAppToken.inSizeCompatMode()) {
            surfaceOffsetX = this.mAppToken.getBounds().left;
        }
        this.mTmpRect.offset(surfaceOffsetX - this.mWindowFrames.mFrame.left, -this.mWindowFrames.mFrame.top);
        region.set(this.mTmpRect);
        if (!modal) {
            getTouchableRegion(region);
            this.mTmpRect.set(region.getBounds());
            this.mTmpRect.offset(surfaceOffsetX - this.mWindowFrames.mFrame.left, -this.mWindowFrames.mFrame.top);
            region.set(this.mTmpRect);
        }
        return flags;
    }

    /* access modifiers changed from: package-private */
    public void checkPolicyVisibilityChange() {
        if (isLegacyPolicyVisibility() != this.mLegacyPolicyVisibilityAfterAnim) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "Policy visibility changing after anim in " + this.mWinAnimator + ": " + this.mLegacyPolicyVisibilityAfterAnim);
            }
            if (this.mLegacyPolicyVisibilityAfterAnim) {
                setPolicyVisibilityFlag(1);
            } else {
                clearPolicyVisibilityFlag(1);
            }
            if (!isVisibleByPolicy()) {
                this.mWinAnimator.hide("checkPolicyVisibilityChange");
                if (isFocused()) {
                    if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                        Slog.i("WindowManager", "setAnimationLocked: setting mFocusMayChange true");
                    }
                    this.mWmService.mFocusMayChange = true;
                }
                setDisplayLayoutNeeded();
                this.mWmService.enableScreenIfNeededLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRequestedSize(int requestedWidth, int requestedHeight) {
        if (this.mRequestedWidth != requestedWidth || this.mRequestedHeight != requestedHeight) {
            this.mLayoutNeeded = true;
            this.mRequestedWidth = requestedWidth;
            this.mRequestedHeight = requestedHeight;
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareWindowToDisplayDuringRelayout(boolean wasVisible) {
        if ((this.mAttrs.flags & DumpState.DUMP_COMPILER_STATS) != 0) {
            boolean allowTheaterMode = this.mWmService.mAllowTheaterModeWakeFromLayout || Settings.Global.getInt(this.mWmService.mContext.getContentResolver(), "theater_mode_on", 0) == 0;
            AppWindowToken appWindowToken = this.mAppToken;
            boolean canTurnScreenOn = appWindowToken == null || appWindowToken.canTurnScreenOn();
            if (allowTheaterMode && canTurnScreenOn && !this.mPowerManagerWrapper.isInteractive()) {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_POWER) {
                    Slog.v("WindowManager", "Relayout window turning screen on: " + this);
                }
                String appTitle = "";
                String wakeUpReason = WAKEUP_REAMSON_WMS_TURN_ON;
                if (this.mAttrs.getTitle() != null) {
                    appTitle = this.mAttrs.getTitle().toString();
                }
                if (appTitle != null && OPPO_INCALL_ACTIVITY.equals(appTitle)) {
                    wakeUpReason = "android.server.wm:SCREEN_ON_FLAG:oppoincall";
                }
                this.mPowerManagerWrapper.wakeUp(SystemClock.uptimeMillis(), 2, wakeUpReason);
            }
            AppWindowToken appWindowToken2 = this.mAppToken;
            if (appWindowToken2 != null) {
                appWindowToken2.setCanTurnScreenOn(false);
            }
        }
        if (!wasVisible) {
            if ((this.mAttrs.softInputMode & 240) == 16) {
                this.mLayoutNeeded = true;
            }
            if (isDrawnLw() && this.mToken.okToAnimate()) {
                this.mWinAnimator.applyEnterAnimationLocked();
            }
        } else if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v("WindowManager", "Already visible and does not turn on screen, skip preparing: " + this);
        }
    }

    private Configuration getProcessGlobalConfiguration() {
        WindowState parentWindow = getParentWindow();
        return this.mWmService.mAtmService.getGlobalConfigurationForPid((parentWindow != null ? parentWindow.mSession : this.mSession).mPid);
    }

    /* access modifiers changed from: package-private */
    public void getMergedConfiguration(MergedConfiguration outConfiguration) {
        outConfiguration.setConfiguration(getProcessGlobalConfiguration(), getMergedOverrideConfiguration());
    }

    /* access modifiers changed from: package-private */
    public void setLastReportedMergedConfiguration(MergedConfiguration config) {
        this.mLastReportedConfiguration.setTo(config);
        this.mLastConfigReportedToClient = true;
    }

    /* access modifiers changed from: package-private */
    public void getLastReportedMergedConfiguration(MergedConfiguration config) {
        config.setTo(this.mLastReportedConfiguration);
    }

    private Configuration getLastReportedConfiguration() {
        return this.mLastReportedConfiguration.getMergedConfiguration();
    }

    /* access modifiers changed from: package-private */
    public void adjustStartingWindowFlags() {
        AppWindowToken appWindowToken;
        if (this.mAttrs.type == 1 && (appWindowToken = this.mAppToken) != null && appWindowToken.startingWindow != null) {
            WindowManager.LayoutParams sa = this.mAppToken.startingWindow.mAttrs;
            sa.flags = (sa.flags & -4718594) | (this.mAttrs.flags & 4718593);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowScale(int requestedWidth, int requestedHeight) {
        float f = 1.0f;
        if ((this.mAttrs.flags & 16384) != 0) {
            this.mHScale = this.mAttrs.width != requestedWidth ? ((float) this.mAttrs.width) / ((float) requestedWidth) : 1.0f;
            if (this.mAttrs.height != requestedHeight) {
                f = ((float) this.mAttrs.height) / ((float) requestedHeight);
            }
            this.mVScale = f;
            return;
        }
        this.mVScale = 1.0f;
        this.mHScale = 1.0f;
    }

    /* access modifiers changed from: private */
    public class DeathRecipient implements IBinder.DeathRecipient {
        private DeathRecipient() {
        }

        public void binderDied() {
            boolean resetSplitScreenResizing = false;
            try {
                synchronized (WindowState.this.mWmService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        WindowState win = WindowState.this.mWmService.windowForClientLocked(WindowState.this.mSession, WindowState.this.mClient, false);
                        Slog.i("WindowManager", "WIN DEATH: " + win);
                        if (win != null) {
                            DisplayContent dc = WindowState.this.getDisplayContent();
                            if (win.mAppToken != null && win.mAppToken.findMainWindow() == win) {
                                WindowState.this.mWmService.mTaskSnapshotController.onAppDied(win.mAppToken);
                            }
                            win.removeIfPossible(WindowState.this.shouldKeepVisibleDeadAppWindow());
                            if (win.mAttrs.type == 2034) {
                                TaskStack stack = dc.getSplitScreenPrimaryStackIgnoringVisibility();
                                if (stack != null) {
                                    stack.resetDockedStackToMiddle();
                                }
                                resetSplitScreenResizing = true;
                            }
                        } else if (WindowState.this.mHasSurface) {
                            Slog.e("WindowManager", "!!! LEAK !!! Window removed but surface still valid.");
                            WindowState.this.removeIfPossible();
                        } else if (1 == WindowState.this.mAttachState) {
                            Slog.e("WindowManager", "This win has not Add for map yet and without surface, should not add later.");
                            WindowState.this.mAttachSuccess = false;
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
                if (resetSplitScreenResizing) {
                    try {
                        WindowState.this.mWmService.mActivityTaskManager.setSplitScreenResizing(false);
                    } catch (RemoteException e) {
                        throw e.rethrowAsRuntimeException();
                    }
                }
            } catch (IllegalArgumentException e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldKeepVisibleDeadAppWindow() {
        AppWindowToken appWindowToken;
        if (!isWinVisibleLw() || (appWindowToken = this.mAppToken) == null || appWindowToken.isClientHidden() || this.mAttrs.token != this.mClient.asBinder() || this.mAttrs.type == 3) {
            return false;
        }
        return getWindowConfiguration().keepVisibleDeadAppWindowOnScreen();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean canReceiveKeys() {
        if (!this.mFloatWindowVisiblility || !isVisibleOrAdding() || this.mViewVisibility != 0 || this.mRemoveOnExit || (this.mAttrs.flags & 8) != 0) {
            return false;
        }
        AppWindowToken appWindowToken = this.mAppToken;
        if ((appWindowToken == null || appWindowToken.windowsAreFocusable()) && !cantReceiveTouchInput()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setFloatWindowVisiblility(boolean visible) {
        this.mFloatWindowVisiblility = visible;
    }

    /* access modifiers changed from: package-private */
    public boolean getAppOpVisibility() {
        return this.mAppOpVisibility;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean canShowWhenLocked() {
        AppWindowToken appWindowToken = this.mAppToken;
        return (appWindowToken != null && appWindowToken.mActivityRecord.canShowWhenLocked()) || ((getAttrs().flags & DumpState.DUMP_FROZEN) != 0);
    }

    /* access modifiers changed from: package-private */
    public boolean cantReceiveTouchInput() {
        AppWindowToken appWindowToken = this.mAppToken;
        return (appWindowToken == null || appWindowToken.getTask() == null || (!this.mAppToken.getTask().mStack.shouldIgnoreInput() && !this.mAppToken.hiddenRequested)) ? false : true;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean hasDrawnLw() {
        return this.mWinAnimator.mDrawState == 4;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean showLw(boolean doAnimation) {
        return showLw(doAnimation, true);
    }

    /* access modifiers changed from: package-private */
    public boolean showLw(boolean doAnimation, boolean requestAnim) {
        if ((isLegacyPolicyVisibility() && this.mLegacyPolicyVisibilityAfterAnim) || isHiddenFromUserLocked() || !this.mAppOpVisibility || this.mPermanentlyHidden || this.mHiddenWhileSuspended || this.mForceHideNonSystemOverlayWindow) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v("WindowManager", "Policy visibility true: " + this);
        }
        if (doAnimation) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "doAnimation: mPolicyVisibility=" + isLegacyPolicyVisibility() + " animating=" + isAnimating());
            }
            if (!this.mToken.okToAnimate()) {
                doAnimation = false;
            } else if (isLegacyPolicyVisibility() && !isAnimating()) {
                doAnimation = false;
            }
        }
        setPolicyVisibilityFlag(1);
        this.mLegacyPolicyVisibilityAfterAnim = true;
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(1, true);
        }
        if (requestAnim) {
            this.mWmService.scheduleAnimationLocked();
        }
        if ((this.mAttrs.flags & 8) == 0) {
            this.mWmService.updateFocusedWindowLocked(0, false);
        }
        return true;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean hideLw(boolean doAnimation) {
        return hideLw(doAnimation, true);
    }

    /* access modifiers changed from: package-private */
    public boolean hideLw(boolean doAnimation, boolean requestAnim) {
        if (doAnimation && !this.mToken.okToAnimate()) {
            doAnimation = false;
        }
        if (!(doAnimation ? this.mLegacyPolicyVisibilityAfterAnim : isLegacyPolicyVisibility())) {
            return false;
        }
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(2, false);
            if (!isAnimating()) {
                doAnimation = false;
            }
        }
        this.mLegacyPolicyVisibilityAfterAnim = false;
        boolean isFocused = isFocused();
        if (!doAnimation) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "Policy visibility false: " + this);
            }
            clearPolicyVisibilityFlag(1);
            this.mWmService.enableScreenIfNeededLocked();
            if (isFocused) {
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.i("WindowManager", "WindowState.hideLw: setting mFocusMayChange true");
                }
                this.mWmService.mFocusMayChange = true;
            }
        }
        if (requestAnim) {
            this.mWmService.scheduleAnimationLocked();
        }
        if (isFocused) {
            this.mWmService.updateFocusedWindowLocked(0, false);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setForceHideNonSystemOverlayWindowIfNeeded(boolean forceHide) {
        if (this.mOwnerCanAddInternalSystemWindow) {
            return;
        }
        if ((WindowManager.LayoutParams.isSystemAlertWindowType(this.mAttrs.type) || this.mAttrs.type == 2005) && this.mForceHideNonSystemOverlayWindow != forceHide) {
            this.mForceHideNonSystemOverlayWindow = forceHide;
            if (forceHide) {
                hideLw(true, true);
            } else {
                showLw(true, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setHiddenWhileSuspended(boolean hide) {
        if (this.mOwnerCanAddInternalSystemWindow) {
            return;
        }
        if ((WindowManager.LayoutParams.isSystemAlertWindowType(this.mAttrs.type) || this.mAttrs.type == 2005) && this.mHiddenWhileSuspended != hide) {
            this.mHiddenWhileSuspended = hide;
            if (hide) {
                hideLw(true, true);
            } else {
                showLw(true, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppOpVisibilityLw(boolean state) {
        if (this.mAppOpVisibility != state) {
            this.mAppOpVisibility = state;
            if (state) {
                showLw(true, true);
                setFloatWindowVisiblility(true);
                return;
            }
            hideLw(true, true);
            setFloatWindowVisiblility(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void initAppOpsState() {
        int mode;
        if (this.mAppOp != -1 && this.mAppOpVisibility && (mode = this.mWmService.mAppOps.startOpNoThrow(this.mAppOp, getOwningUid(), getOwningPackage(), true)) != 0 && mode != 3) {
            setAppOpVisibilityLw(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void resetAppOpsState() {
        if (this.mAppOp != -1 && this.mAppOpVisibility) {
            this.mWmService.mAppOps.finishOp(this.mAppOp, getOwningUid(), getOwningPackage());
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAppOpsState() {
        if (this.mAppOp != -1) {
            int uid = getOwningUid();
            String packageName = getOwningPackage();
            if (this.mAppOpVisibility) {
                int mode = this.mWmService.mAppOps.checkOpNoThrow(this.mAppOp, uid, packageName);
                if (mode != 0 && mode != 3) {
                    this.mWmService.mAppOps.finishOp(this.mAppOp, uid, packageName);
                    setAppOpVisibilityLw(false);
                    return;
                }
                return;
            }
            int mode2 = this.mWmService.mAppOps.startOpNoThrow(this.mAppOp, uid, packageName, true);
            if (mode2 == 0 || mode2 == 3) {
                setAppOpVisibilityLw(true);
            }
        }
    }

    public void hidePermanentlyLw() {
        if (!this.mPermanentlyHidden) {
            this.mPermanentlyHidden = true;
            hideLw(true, true);
        }
    }

    public void pokeDrawLockLw(long timeout) {
        if (isVisibleOrAdding()) {
            if (this.mDrawLock == null) {
                CharSequence tag = getWindowTag();
                PowerManager powerManager = this.mWmService.mPowerManager;
                this.mDrawLock = powerManager.newWakeLock(128, "Window:" + ((Object) tag));
                this.mDrawLock.setReferenceCounted(false);
                this.mDrawLock.setWorkSource(new WorkSource(this.mOwnerUid, this.mAttrs.packageName));
            }
            if (WindowManagerDebugConfig.DEBUG_POWER) {
                Slog.d("WindowManager", "pokeDrawLock: poking draw lock on behalf of visible window owned by " + this.mAttrs.packageName);
            }
            this.mDrawLock.acquire(timeout);
        } else if (WindowManagerDebugConfig.DEBUG_POWER) {
            Slog.d("WindowManager", "pokeDrawLock: suppressed draw lock request for invisible window owned by " + this.mAttrs.packageName);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isAlive() {
        return this.mClient.asBinder().isBinderAlive();
    }

    /* access modifiers changed from: package-private */
    public boolean isClosing() {
        AppWindowToken appWindowToken;
        return this.mAnimatingExit || ((appWindowToken = this.mAppToken) != null && appWindowToken.isClosingOrEnteringPip());
    }

    /* access modifiers changed from: package-private */
    public void addWinAnimatorToList(ArrayList<WindowStateAnimator> animators) {
        animators.add(this.mWinAnimator);
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).addWinAnimatorToList(animators);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void sendAppVisibilityToClients() {
        super.sendAppVisibilityToClients();
        boolean clientHidden = this.mAppToken.isClientHidden();
        if (this.mAttrs.type != 3 || !clientHidden) {
            boolean z = true;
            if (clientHidden) {
                for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                    ((WindowState) this.mChildren.get(i)).mWinAnimator.detachChildren();
                }
                this.mWinAnimator.detachChildren();
            }
            try {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_WMS || WindowManagerDebugConfig.DEBUG_FOCUS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Setting visibility of ");
                    sb.append(this);
                    sb.append(": ");
                    sb.append(!clientHidden);
                    Slog.v("WindowManager", sb.toString());
                }
                IWindow iWindow = this.mClient;
                if (clientHidden) {
                    z = false;
                }
                iWindow.dispatchAppVisibility(z);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onStartFreezingScreen() {
        this.mAppFreezing = true;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onStartFreezingScreen();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onStopFreezingScreen() {
        boolean unfrozeWindows = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            unfrozeWindows |= ((WindowState) this.mChildren.get(i)).onStopFreezingScreen();
        }
        if (!this.mAppFreezing) {
            return unfrozeWindows;
        }
        this.mAppFreezing = false;
        if (this.mHasSurface && !getOrientationChanging() && this.mWmService.mWindowsFreezingScreen != 2) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "set mOrientationChanging of " + this);
            }
            setOrientationChanging(true);
            this.mWmService.mRoot.mOrientationChangeComplete = false;
        }
        this.mLastFreezeDuration = 0;
        setDisplayLayoutNeeded();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean destroySurface(boolean cleanupOnResume, boolean appStopped) {
        boolean destroyedSomething = false;
        ArrayList<WindowState> childWindows = new ArrayList<>(this.mChildren);
        for (int i = childWindows.size() - 1; i >= 0; i--) {
            destroyedSomething |= childWindows.get(i).destroySurface(cleanupOnResume, appStopped);
        }
        if (!(appStopped || this.mWindowRemovalAllowed || cleanupOnResume)) {
            return destroyedSomething;
        }
        if (appStopped || this.mWindowRemovalAllowed) {
            this.mWinAnimator.destroyPreservedSurfaceLocked();
        }
        if (this.mDestroying) {
            if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                Slog.e("WindowManager", "win=" + this + " destroySurfaces: appStopped=" + appStopped + " win.mWindowRemovalAllowed=" + this.mWindowRemovalAllowed + " win.mRemoveOnExit=" + this.mRemoveOnExit);
            }
            if (!cleanupOnResume || this.mRemoveOnExit) {
                destroySurfaceUnchecked();
            }
            if (this.mRemoveOnExit) {
                removeImmediately();
            }
            if (cleanupOnResume) {
                requestUpdateWallpaperIfNeeded();
            }
            this.mDestroying = false;
            destroyedSomething = true;
            if (getDisplayContent().mAppTransition.isTransitionSet() && getDisplayContent().mOpeningApps.contains(this.mAppToken)) {
                this.mWmService.mWindowPlacerLocked.requestTraversal();
            }
        }
        return destroyedSomething;
    }

    /* access modifiers changed from: package-private */
    public void destroySurfaceUnchecked() {
        this.mWinAnimator.destroySurfaceLocked();
        this.mAnimatingExit = false;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isDefaultDisplay() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return false;
        }
        return displayContent.isDefaultDisplay;
    }

    /* access modifiers changed from: package-private */
    public void setShowToOwnerOnlyLocked(boolean showToOwnerOnly) {
        this.mShowToOwnerOnly = showToOwnerOnly;
    }

    private boolean isHiddenFromUserLocked() {
        AppWindowToken appWindowToken;
        WindowState win = getTopParentWindow();
        if ((win.mAttrs.type >= 2000 || (appWindowToken = win.mAppToken) == null || !appWindowToken.mShowForAllUsers || win.getFrameLw().left > win.getDisplayFrameLw().left || win.getFrameLw().top > win.getDisplayFrameLw().top || win.getFrameLw().right < win.getStableFrameLw().right || win.getFrameLw().bottom < win.getStableFrameLw().bottom) && win.mShowToOwnerOnly && !this.mWmService.isCurrentProfileLocked(UserHandle.getUserId(win.mOwnerUid))) {
            return true;
        }
        return false;
    }

    private static void applyInsets(Region outRegion, Rect frame, Rect inset) {
        outRegion.set(frame.left + inset.left, frame.top + inset.top, frame.right - inset.right, frame.bottom - inset.bottom);
    }

    /* access modifiers changed from: package-private */
    public void getTouchableRegion(Region outRegion) {
        Rect frame = this.mWindowFrames.mFrame;
        int i = this.mTouchableInsets;
        if (i == 1) {
            applyInsets(outRegion, frame, this.mGivenContentInsets);
        } else if (i == 2) {
            applyInsets(outRegion, frame, this.mGivenVisibleInsets);
        } else if (i != 3) {
            outRegion.set(frame);
        } else {
            outRegion.set(this.mGivenTouchableRegion);
            outRegion.translate(frame.left, frame.top);
        }
        cropRegionToStackBoundsIfNeeded(outRegion);
        subtractTouchExcludeRegionIfNeeded(outRegion);
    }

    /* access modifiers changed from: package-private */
    public void getEffectiveTouchableRegion(Region outRegion) {
        boolean modal = (this.mAttrs.flags & 40) == 0;
        DisplayContent dc = getDisplayContent();
        if (!modal || dc == null) {
            getTouchableRegion(outRegion);
            return;
        }
        outRegion.set(dc.getBounds());
        cropRegionToStackBoundsIfNeeded(outRegion);
        subtractTouchExcludeRegionIfNeeded(outRegion);
    }

    private void setTouchableRegionCropIfNeeded(InputWindowHandle handle) {
        TaskStack stack;
        Task task = getTask();
        if (task != null && task.cropWindowsToStackBounds() && (stack = task.mStack) != null) {
            handle.setTouchableRegionCrop(stack.getSurfaceControl());
        }
    }

    private void cropRegionToStackBoundsIfNeeded(Region region) {
        TaskStack stack;
        Task task = getTask();
        if (task != null && task.cropWindowsToStackBounds() && (stack = task.mStack) != null) {
            stack.getDimBounds(this.mTmpRect);
            if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM) {
                task.getDimBounds(this.mTmpRect);
            }
            region.op(this.mTmpRect, Region.Op.INTERSECT);
        }
    }

    private void subtractTouchExcludeRegionIfNeeded(Region touchableRegion) {
        if (this.mTapExcludeRegionHolder != null) {
            Region touchExcludeRegion = Region.obtain();
            amendTapExcludeRegion(touchExcludeRegion);
            if (!touchExcludeRegion.isEmpty()) {
                touchableRegion.op(touchExcludeRegion, Region.Op.DIFFERENCE);
            }
            touchExcludeRegion.recycle();
        }
    }

    /* access modifiers changed from: package-private */
    public void reportFocusChangedSerialized(boolean focused, boolean inTouchMode) {
        try {
            this.mClient.windowFocusChanged(focused, inTouchMode);
        } catch (RemoteException e) {
        }
        RemoteCallbackList<IWindowFocusObserver> remoteCallbackList = this.mFocusCallbacks;
        if (remoteCallbackList != null) {
            int N = remoteCallbackList.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IWindowFocusObserver obs = this.mFocusCallbacks.getBroadcastItem(i);
                if (focused) {
                    try {
                        obs.focusGained(this.mWindowId.asBinder());
                    } catch (RemoteException e2) {
                    }
                } else {
                    obs.focusLost(this.mWindowId.asBinder());
                }
            }
            this.mFocusCallbacks.finishBroadcast();
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public Configuration getConfiguration() {
        AppWindowToken appWindowToken = this.mAppToken;
        if (appWindowToken != null && appWindowToken.mFrozenMergedConfig.size() > 0) {
            return this.mAppToken.mFrozenMergedConfig.peek();
        }
        if (!registeredForDisplayConfigChanges()) {
            return super.getConfiguration();
        }
        this.mTempConfiguration.setTo(getProcessGlobalConfiguration());
        this.mTempConfiguration.updateFrom(getMergedOverrideConfiguration());
        return this.mTempConfiguration;
    }

    private boolean registeredForDisplayConfigChanges() {
        WindowProcessController app;
        WindowState parentWindow = getParentWindow();
        Session session = parentWindow != null ? parentWindow.mSession : this.mSession;
        if (session.mPid == ActivityManagerService.MY_PID || session.mPid < 0 || (app = this.mWmService.mAtmService.getProcessController(session.mPid, session.mUid)) == null || !app.registeredForDisplayConfigChanges()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void reportResized() {
        long j;
        boolean z;
        WindowState windowState;
        String str;
        Trace.traceBegin(32, "wm.reportResized_" + ((Object) getWindowTag()));
        try {
            if (WindowManagerDebugConfig.DEBUG_RESIZE || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Reporting new frame to " + this + ": " + this.mWindowFrames.mCompatFrame);
            }
            final MergedConfiguration mergedConfiguration = new MergedConfiguration(this.mWmService.mRoot.getConfiguration(), getMergedOverrideConfiguration());
            setLastReportedMergedConfiguration(mergedConfiguration);
            final boolean reportDraw = true;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION && this.mWinAnimator.mDrawState == 1) {
                Slog.i("WindowManager", "Resizing " + this + " WITH DRAW PENDING");
            }
            final Rect frame = this.mWindowFrames.mCompatFrame;
            final Rect overscanInsets = this.mWindowFrames.mLastOverscanInsets;
            final Rect contentInsets = this.mWindowFrames.mLastContentInsets;
            final Rect visibleInsets = this.mWindowFrames.mLastVisibleInsets;
            final Rect stableInsets = this.mWindowFrames.mLastStableInsets;
            final Rect outsets = this.mWindowFrames.mLastOutsets;
            if (this.mWinAnimator.mDrawState != 1) {
                reportDraw = false;
            }
            final boolean reportOrientation = this.mReportOrientationChanged;
            final int displayId = getDisplayId();
            final DisplayCutout displayCutout = getWmDisplayCutout().getDisplayCutout();
            try {
                if (this.mAttrs.type != 3) {
                    try {
                        if (this.mClient instanceof IWindow.Stub) {
                            j = 32;
                            try {
                                this.mWmService.mH.post(new Runnable() {
                                    /* class com.android.server.wm.WindowState.AnonymousClass3 */

                                    public void run() {
                                        try {
                                            WindowState.this.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, reportOrientation, displayId, displayCutout);
                                        } catch (RemoteException e) {
                                        }
                                    }
                                });
                                windowState = this;
                                str = "WindowManager";
                                if (windowState.mWmService.mAccessibilityController != null && (getDisplayId() == 0 || getDisplayContent().getParentWindow() != null)) {
                                    windowState.mWmService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                                }
                                windowState.mWindowFrames.resetInsetsChanged();
                                z = false;
                                windowState.mWinAnimator.mSurfaceResized = false;
                                windowState.mReportOrientationChanged = false;
                            } catch (RemoteException e) {
                                windowState = this;
                                str = "WindowManager";
                                z = false;
                                windowState.setOrientationChanging(z);
                                windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mWmService.mDisplayFreezeTime);
                                Slog.w(str, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
                                windowState.mWmService.mPendingRemove.add(windowState);
                                windowState.mWmService.mWindowPlacerLocked.requestTraversal();
                                Trace.traceEnd(j);
                            }
                            Trace.traceEnd(j);
                        }
                    } catch (RemoteException e2) {
                        j = 32;
                        str = "WindowManager";
                        windowState = this;
                        z = false;
                        windowState.setOrientationChanging(z);
                        windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mWmService.mDisplayFreezeTime);
                        Slog.w(str, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
                        windowState.mWmService.mPendingRemove.add(windowState);
                        windowState.mWmService.mWindowPlacerLocked.requestTraversal();
                        Trace.traceEnd(j);
                    }
                }
                j = 32;
                str = "WindowManager";
                windowState = this;
                try {
                    dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, reportOrientation, displayId, displayCutout);
                    windowState.mWmService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                    windowState.mWindowFrames.resetInsetsChanged();
                    z = false;
                    try {
                        windowState.mWinAnimator.mSurfaceResized = false;
                        windowState.mReportOrientationChanged = false;
                    } catch (RemoteException e3) {
                    }
                } catch (RemoteException e4) {
                    z = false;
                    windowState.setOrientationChanging(z);
                    windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mWmService.mDisplayFreezeTime);
                    Slog.w(str, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
                    windowState.mWmService.mPendingRemove.add(windowState);
                    windowState.mWmService.mWindowPlacerLocked.requestTraversal();
                    Trace.traceEnd(j);
                }
            } catch (RemoteException e5) {
                str = "WindowManager";
                windowState = this;
                j = 32;
                z = false;
                windowState.setOrientationChanging(z);
                windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mWmService.mDisplayFreezeTime);
                Slog.w(str, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
                windowState.mWmService.mPendingRemove.add(windowState);
                windowState.mWmService.mWindowPlacerLocked.requestTraversal();
                Trace.traceEnd(j);
            }
        } catch (RemoteException e6) {
            j = 32;
            str = "WindowManager";
            windowState = this;
            z = false;
            windowState.setOrientationChanging(z);
            windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mWmService.mDisplayFreezeTime);
            Slog.w(str, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
            windowState.mWmService.mPendingRemove.add(windowState);
            windowState.mWmService.mWindowPlacerLocked.requestTraversal();
            Trace.traceEnd(j);
        }
        Trace.traceEnd(j);
    }

    /* access modifiers changed from: package-private */
    public void notifyInsetsChanged() {
        try {
            this.mClient.insetsChanged(getDisplayContent().getInsetsStateController().getInsetsForDispatch(this));
        } catch (RemoteException e) {
            Slog.w("WindowManager", "Failed to deliver inset state change", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyInsetsControlChanged() {
        InsetsStateController stateController = getDisplayContent().getInsetsStateController();
        try {
            this.mClient.insetsControlChanged(stateController.getInsetsForDispatch(this), stateController.getControlsForDispatch(this));
        } catch (RemoteException e) {
            Slog.w("WindowManager", "Failed to deliver inset state change", e);
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getBackdropFrame(Rect frame) {
        boolean resizing = isDragResizing() || isDragResizeChanged();
        if (getWindowConfiguration().useWindowFrameForBackdrop() || !resizing) {
            this.mTmpRect.set(frame);
            this.mTmpRect.offsetTo(0, 0);
            return this.mTmpRect;
        }
        DisplayInfo displayInfo = getDisplayInfo();
        this.mTmpRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        return this.mTmpRect;
    }

    public int getStackId() {
        TaskStack stack = getStack();
        if (stack == null) {
            return -1;
        }
        return stack.mStackId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, boolean reportOrientation, int displayId, DisplayCutout displayCutout) throws RemoteException {
        this.mClient.resized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, getBackdropFrame(frame), isDragResizeChanged() || reportOrientation, getDisplayContent().getDisplayPolicy().areSystemBarsForcedShownLw(this), displayId, new DisplayCutout.ParcelableWrapper(displayCutout));
        this.mDragResizingChangeReported = true;
    }

    public void registerFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mFocusCallbacks == null) {
                    this.mFocusCallbacks = new RemoteCallbackList<>();
                }
                this.mFocusCallbacks.register(observer);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void unregisterFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mFocusCallbacks != null) {
                    this.mFocusCallbacks.unregister(observer);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFocused() {
        return getDisplayContent().mCurrentFocus == this;
    }

    private boolean inAppWindowThatMatchesParentBounds() {
        AppWindowToken appWindowToken = this.mAppToken;
        return appWindowToken == null || (appWindowToken.matchParentBounds() && !inMultiWindowMode());
    }

    /* access modifiers changed from: package-private */
    public boolean isLetterboxedAppWindow() {
        return (!inMultiWindowMode() && !inZoomWindowingMode() && !matchesDisplayBounds()) || isLetterboxedForDisplayCutoutLw();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isLetterboxedForDisplayCutoutLw() {
        if (this.mAppToken != null && this.mWindowFrames.parentFrameWasClippedByDisplayCutout() && this.mAttrs.layoutInDisplayCutoutMode != 1 && this.mAttrs.isFullscreen()) {
            return !frameCoversEntireAppTokenBounds();
        }
        return false;
    }

    private boolean frameCoversEntireAppTokenBounds() {
        this.mTmpRect.set(this.mAppToken.getBounds());
        this.mTmpRect.intersectUnchecked(this.mWindowFrames.mFrame);
        return this.mAppToken.getBounds().equals(this.mTmpRect);
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isLetterboxedOverlappingWith(Rect rect) {
        AppWindowToken appWindowToken = this.mAppToken;
        return appWindowToken != null && appWindowToken.isLetterboxOverlappingWith(rect);
    }

    /* access modifiers changed from: package-private */
    public boolean isDragResizeChanged() {
        return this.mDragResizing != computeDragResizing();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void setWaitingForDrawnIfResizingChanged() {
        if (isDragResizeChanged()) {
            this.mWmService.mWaitingForDrawn.add(this);
        }
        super.setWaitingForDrawnIfResizingChanged();
    }

    private boolean isDragResizingChangeReported() {
        return this.mDragResizingChangeReported;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void resetDragResizingChangeReported() {
        this.mDragResizingChangeReported = false;
        super.resetDragResizingChangeReported();
    }

    /* access modifiers changed from: package-private */
    public int getResizeMode() {
        return this.mResizeMode;
    }

    private boolean computeDragResizing() {
        AppWindowToken appWindowToken;
        Task task = getTask();
        if (task == null) {
            return false;
        }
        if ((!inSplitScreenWindowingMode() && !inFreeformWindowingMode()) || this.mAttrs.width != -1 || this.mAttrs.height != -1) {
            return false;
        }
        if (task.isDragResizing()) {
            return true;
        }
        if ((getDisplayContent().mDividerControllerLocked.isResizing() || ((appWindowToken = this.mAppToken) != null && !appWindowToken.mFrozenBounds.isEmpty())) && !task.inFreeformWindowingMode() && !isGoneForLayoutLw()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setDragResizing() {
        int i;
        boolean resizing = computeDragResizing();
        if (resizing != this.mDragResizing) {
            this.mDragResizing = resizing;
            Task task = getTask();
            if (task == null || !task.isDragResizing()) {
                if (!this.mDragResizing || !getDisplayContent().mDividerControllerLocked.isResizing()) {
                    i = 0;
                } else {
                    i = 1;
                }
                this.mResizeMode = i;
                return;
            }
            this.mResizeMode = task.getDragResizeMode();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDragResizing() {
        return this.mDragResizing;
    }

    /* access modifiers changed from: package-private */
    public boolean isDockedResizing() {
        if (this.mDragResizing && getResizeMode() == 1) {
            return true;
        }
        if (!isChildWindow() || !getParentWindow().isDockedResizing()) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer, com.android.server.wm.OppoBaseWindowState
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        boolean isVisible = isVisible();
        if (logLevel != 2 || isVisible) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            writeIdentifierToProto(proto, 1146756268034L);
            proto.write(1120986464259L, getDisplayId());
            proto.write(1120986464260L, getStackId());
            this.mAttrs.writeToProto(proto, 1146756268037L);
            this.mGivenContentInsets.writeToProto(proto, 1146756268038L);
            this.mWindowFrames.writeToProto(proto, 1146756268073L);
            this.mAttrs.surfaceInsets.writeToProto(proto, 1146756268044L);
            this.mSurfacePosition.writeToProto(proto, 1146756268048L);
            this.mWinAnimator.writeToProto(proto, 1146756268045L);
            proto.write(1133871366158L, this.mAnimatingExit);
            for (int i = 0; i < this.mChildren.size(); i++) {
                ((WindowState) this.mChildren.get(i)).writeToProto(proto, 2246267895823L, logLevel);
            }
            proto.write(1120986464274L, this.mRequestedWidth);
            proto.write(1120986464275L, this.mRequestedHeight);
            proto.write(1120986464276L, this.mViewVisibility);
            proto.write(1120986464277L, this.mSystemUiVisibility);
            proto.write(1133871366166L, this.mHasSurface);
            proto.write(1133871366167L, isReadyForDisplay());
            proto.write(1133871366178L, this.mRemoveOnExit);
            proto.write(1133871366179L, this.mDestroying);
            proto.write(1133871366180L, this.mRemoved);
            proto.write(1133871366181L, isOnScreen());
            proto.write(1133871366182L, isVisible);
            proto.write(1133871366183L, this.mPendingSeamlessRotate != null);
            proto.write(1112396529704L, this.mFinishSeamlessRotateFrameNumber);
            proto.write(1133871366186L, this.mForceSeamlesslyRotate);
            proto.end(token);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public void writeIdentifierToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, System.identityHashCode(this));
        proto.write(1120986464258L, UserHandle.getUserId(this.mOwnerUid));
        CharSequence title = getWindowTag();
        if (title != null) {
            proto.write(1138166333443L, title.toString());
        }
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public boolean isAttachSuccess() {
        return this.mAttachSuccess;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        TaskStack stack = getStack();
        pw.print(prefix + "mDisplayId=" + getDisplayId());
        if (stack != null) {
            pw.print(" stackId=" + stack.mStackId);
        }
        pw.println(" mSession=" + this.mSession + " mClient=" + this.mClient.asBinder());
        pw.println(prefix + "mOwnerUid=" + this.mOwnerUid + " mShowToOwnerOnly=" + this.mShowToOwnerOnly + " package=" + this.mAttrs.packageName + " appop=" + AppOpsManager.opToName(this.mAppOp));
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("mAttrs=");
        sb.append(this.mAttrs.toString(prefix));
        pw.println(sb.toString());
        pw.println(prefix + "Requested w=" + this.mRequestedWidth + " h=" + this.mRequestedHeight + " mLayoutSeq=" + this.mLayoutSeq);
        if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
            pw.println(prefix + "LastRequested w=" + this.mLastRequestedWidth + " h=" + this.mLastRequestedHeight);
        }
        if (this.mIsChildWindow || this.mLayoutAttached) {
            pw.println(prefix + "mParentWindow=" + getParentWindow() + " mLayoutAttached=" + this.mLayoutAttached);
        }
        if (this.mIsImWindow || this.mIsWallpaper || this.mIsFloatingLayer) {
            pw.println(prefix + "mIsImWindow=" + this.mIsImWindow + " mIsWallpaper=" + this.mIsWallpaper + " mIsFloatingLayer=" + this.mIsFloatingLayer + " mWallpaperVisible=" + this.mWallpaperVisible);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mBaseLayer=");
            pw.print(this.mBaseLayer);
            pw.print(" mSubLayer=");
            pw.print(this.mSubLayer);
        }
        if (dumpAll) {
            pw.println(prefix + "mToken=" + this.mToken);
            if (this.mAppToken != null) {
                pw.println(prefix + "mAppToken=" + this.mAppToken);
                pw.print(prefix + "mAppDied=" + this.mAppDied);
                pw.print(prefix + "drawnStateEvaluated=" + getDrawnStateEvaluated());
                pw.println(prefix + "mightAffectAllDrawn=" + mightAffectAllDrawn());
            }
            pw.println(prefix + "mViewVisibility=0x" + Integer.toHexString(this.mViewVisibility) + " mHaveFrame=" + this.mHaveFrame + " mObscured=" + this.mObscured);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(prefix);
            sb2.append("mSeq=");
            sb2.append(this.mSeq);
            sb2.append(" mSystemUiVisibility=0x");
            sb2.append(Integer.toHexString(this.mSystemUiVisibility));
            pw.println(sb2.toString());
        }
        if (!isVisibleByPolicy() || !this.mLegacyPolicyVisibilityAfterAnim || !this.mAppOpVisibility || isParentWindowHidden() || this.mPermanentlyHidden || this.mForceHideNonSystemOverlayWindow || this.mHiddenWhileSuspended) {
            pw.println(prefix + "mPolicyVisibility=" + isVisibleByPolicy() + " mLegacyPolicyVisibilityAfterAnim=" + this.mLegacyPolicyVisibilityAfterAnim + " mAppOpVisibility=" + this.mAppOpVisibility + " parentHidden=" + isParentWindowHidden() + " mPermanentlyHidden=" + this.mPermanentlyHidden + " mHiddenWhileSuspended=" + this.mHiddenWhileSuspended + " mForceHideNonSystemOverlayWindow=" + this.mForceHideNonSystemOverlayWindow);
        }
        if (!this.mRelayoutCalled || this.mLayoutNeeded) {
            pw.println(prefix + "mRelayoutCalled=" + this.mRelayoutCalled + " mLayoutNeeded=" + this.mLayoutNeeded);
        }
        if (dumpAll) {
            pw.println(prefix + "mGivenContentInsets=" + this.mGivenContentInsets.toShortString(sTmpSB) + " mGivenVisibleInsets=" + this.mGivenVisibleInsets.toShortString(sTmpSB));
            if (this.mTouchableInsets != 0 || this.mGivenInsetsPending) {
                pw.println(prefix + "mTouchableInsets=" + this.mTouchableInsets + " mGivenInsetsPending=" + this.mGivenInsetsPending);
                Region region = new Region();
                getTouchableRegion(region);
                pw.println(prefix + "touchable region=" + region);
            }
            pw.println(prefix + "mFullConfiguration=" + getConfiguration());
            pw.println(prefix + "mLastReportedConfiguration=" + getLastReportedConfiguration());
        }
        pw.print(" canReceiveKeys()=");
        pw.print(canReceiveKeys());
        pw.println(prefix + "mHasSurface=" + this.mHasSurface + " isReadyForDisplay()=" + isReadyForDisplay() + " mWindowRemovalAllowed=" + this.mWindowRemovalAllowed);
        if (inSizeCompatMode()) {
            pw.println(prefix + "mCompatFrame=" + this.mWindowFrames.mCompatFrame.toShortString(sTmpSB));
        }
        if (dumpAll) {
            this.mWindowFrames.dump(pw, prefix);
            pw.println(prefix + " surface=" + this.mAttrs.surfaceInsets.toShortString(sTmpSB));
        }
        super.dump(pw, prefix, dumpAll);
        pw.println(prefix + this.mWinAnimator + ":");
        WindowStateAnimator windowStateAnimator = this.mWinAnimator;
        windowStateAnimator.dump(pw, prefix + "  ", dumpAll);
        if (this.mAnimatingExit || this.mRemoveOnExit || this.mDestroying || this.mRemoved) {
            pw.println(prefix + "mAnimatingExit=" + this.mAnimatingExit + " mRemoveOnExit=" + this.mRemoveOnExit + " mDestroying=" + this.mDestroying + " mRemoved=" + this.mRemoved);
        }
        if (getOrientationChanging() || this.mAppFreezing || this.mReportOrientationChanged) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(prefix);
            sb3.append("mOrientationChanging=");
            sb3.append(this.mOrientationChanging);
            sb3.append(" configOrientationChanging=");
            sb3.append(getLastReportedConfiguration().orientation != getConfiguration().orientation);
            sb3.append(" mAppFreezing=");
            sb3.append(this.mAppFreezing);
            sb3.append(" mReportOrientationChanged=");
            sb3.append(this.mReportOrientationChanged);
            pw.println(sb3.toString());
        }
        if (this.mLastFreezeDuration != 0) {
            pw.print(prefix + "mLastFreezeDuration=");
            TimeUtils.formatDuration((long) this.mLastFreezeDuration, pw);
            pw.println();
        }
        pw.print(prefix + "mForceSeamlesslyRotate=" + this.mForceSeamlesslyRotate + " seamlesslyRotate: pending=");
        SeamlessRotator seamlessRotator = this.mPendingSeamlessRotate;
        if (seamlessRotator != null) {
            seamlessRotator.dump(pw);
        } else {
            pw.print("null");
        }
        pw.println(" finishedFrameNumber=" + this.mFinishSeamlessRotateFrameNumber);
        if (!(this.mHScale == 1.0f && this.mVScale == 1.0f)) {
            pw.println(prefix + "mHScale=" + this.mHScale + " mVScale=" + this.mVScale);
        }
        if (!(this.mWallpaperX == -1.0f && this.mWallpaperY == -1.0f)) {
            pw.println(prefix + "mWallpaperX=" + this.mWallpaperX + " mWallpaperY=" + this.mWallpaperY);
        }
        if (!(this.mWallpaperXStep == -1.0f && this.mWallpaperYStep == -1.0f)) {
            pw.println(prefix + "mWallpaperXStep=" + this.mWallpaperXStep + " mWallpaperYStep=" + this.mWallpaperYStep);
        }
        if (!(this.mWallpaperDisplayOffsetX == Integer.MIN_VALUE && this.mWallpaperDisplayOffsetY == Integer.MIN_VALUE)) {
            pw.println(prefix + "mWallpaperDisplayOffsetX=" + this.mWallpaperDisplayOffsetX + " mWallpaperDisplayOffsetY=" + this.mWallpaperDisplayOffsetY);
        }
        if (this.mDrawLock != null) {
            pw.println(prefix + "mDrawLock=" + this.mDrawLock);
        }
        if (isDragResizing()) {
            pw.println(prefix + "isDragResizing=" + isDragResizing());
        }
        if (computeDragResizing()) {
            pw.println(prefix + "computeDragResizing=" + computeDragResizing());
        }
        pw.println(prefix + "isOnScreen=" + isOnScreen());
        pw.println(prefix + "isVisible=" + isVisible());
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public String getName() {
        return Integer.toHexString(System.identityHashCode(this)) + StringUtils.SPACE + ((Object) getWindowTag());
    }

    /* access modifiers changed from: package-private */
    public CharSequence getWindowTag() {
        CharSequence tag = this.mAttrs.getTitle();
        if (tag == null || tag.length() <= 0) {
            return this.mAttrs.packageName;
        }
        return tag;
    }

    public String toString() {
        CharSequence title = getWindowTag();
        if (!(this.mStringNameCache != null && this.mLastTitle == title && this.mWasExiting == this.mAnimatingExit)) {
            this.mLastTitle = title;
            this.mWasExiting = this.mAnimatingExit;
            StringBuilder sb = new StringBuilder();
            sb.append("Window{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" u");
            sb.append(UserHandle.getUserId(this.mOwnerUid));
            sb.append(StringUtils.SPACE);
            sb.append((Object) this.mLastTitle);
            sb.append(this.mAnimatingExit ? " EXITING}" : "}");
            this.mStringNameCache = sb.toString();
        }
        return this.mStringNameCache;
    }

    /* access modifiers changed from: package-private */
    public void transformClipRectFromScreenToSurfaceSpace(Rect clipRect) {
        if (this.mHScale != 1.0f || this.mVScale != 1.0f) {
            if (this.mHScale >= OppoBrightUtils.MIN_LUX_LIMITI) {
                clipRect.left = (int) (((float) clipRect.left) / this.mHScale);
                clipRect.right = (int) Math.ceil((double) (((float) clipRect.right) / this.mHScale));
            }
            if (this.mVScale >= OppoBrightUtils.MIN_LUX_LIMITI) {
                clipRect.top = (int) (((float) clipRect.top) / this.mVScale);
                clipRect.bottom = (int) Math.ceil((double) (((float) clipRect.bottom) / this.mVScale));
            }
        }
    }

    private void applyGravityAndUpdateFrame(Rect containingFrame, Rect displayFrame) {
        int h;
        int w;
        float y;
        float x;
        int pw = containingFrame.width();
        int ph = containingFrame.height();
        Task task = getTask();
        boolean fitToDisplay = true;
        boolean inNonFullscreenContainer = !inAppWindowThatMatchesParentBounds();
        boolean noLimits = (this.mAttrs.flags & 512) != 0;
        if (task != null && inNonFullscreenContainer && (this.mAttrs.type == 1 || noLimits)) {
            fitToDisplay = false;
        }
        boolean inSizeCompatMode = inSizeCompatMode();
        if ((this.mAttrs.flags & 16384) != 0) {
            if (this.mAttrs.width < 0) {
                w = pw;
            } else if (inSizeCompatMode) {
                w = (int) ((((float) this.mAttrs.width) * this.mGlobalScale) + 0.5f);
            } else {
                w = this.mAttrs.width;
            }
            if (this.mAttrs.height < 0) {
                h = ph;
            } else if (inSizeCompatMode) {
                h = (int) ((((float) this.mAttrs.height) * this.mGlobalScale) + 0.5f);
            } else {
                h = this.mAttrs.height;
            }
        } else {
            if (this.mAttrs.width == -1) {
                w = pw;
            } else if (inSizeCompatMode) {
                w = (int) ((((float) this.mRequestedWidth) * this.mGlobalScale) + 0.5f);
            } else {
                w = this.mRequestedWidth;
            }
            if (this.mAttrs.height == -1) {
                h = ph;
            } else if (inSizeCompatMode) {
                h = (int) ((((float) this.mRequestedHeight) * this.mGlobalScale) + 0.5f);
            } else {
                h = this.mRequestedHeight;
            }
        }
        if (inSizeCompatMode) {
            x = ((float) this.mAttrs.x) * this.mGlobalScale;
            y = ((float) this.mAttrs.y) * this.mGlobalScale;
        } else {
            x = (float) this.mAttrs.x;
            y = (float) this.mAttrs.y;
        }
        if (inNonFullscreenContainer && !layoutInParentFrame()) {
            w = Math.min(w, pw);
            h = Math.min(h, ph);
        }
        Gravity.apply(this.mAttrs.gravity, w, h, containingFrame, (int) ((this.mAttrs.horizontalMargin * ((float) pw)) + x), (int) ((this.mAttrs.verticalMargin * ((float) ph)) + y), this.mWindowFrames.mFrame);
        if (fitToDisplay) {
            Gravity.applyDisplay(this.mAttrs.gravity, displayFrame, this.mWindowFrames.mFrame);
        }
        if (getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM || (getParentWindow() != null && getParentWindow().inZoomWindowingMode())) {
            int left = (int) (((float) this.mWindowFrames.mFrame.left) * this.mGlobalScale);
            int top = (int) (((float) this.mWindowFrames.mFrame.top) * this.mGlobalScale);
            if (getParentWindow() != null) {
                left += getParentWindow().getFrameLw().left;
                top += getParentWindow().getFrameLw().top;
            }
            this.mWindowFrames.mFrame.set(left, top, this.mWindowFrames.mFrame.width() + left, this.mWindowFrames.mFrame.height() + top);
        }
        this.mWindowFrames.mCompatFrame.set(this.mWindowFrames.mFrame);
        if (inSizeCompatMode) {
            this.mWindowFrames.mCompatFrame.scale(this.mInvGlobalScale);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isChildWindow() {
        return this.mIsChildWindow;
    }

    /* access modifiers changed from: package-private */
    public boolean layoutInParentFrame() {
        return this.mIsChildWindow && (this.mAttrs.privateFlags & 65536) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hideNonSystemOverlayWindowsWhenVisible() {
        return (this.mAttrs.privateFlags & DumpState.DUMP_FROZEN) != 0 && this.mSession.mCanHideNonSystemOverlayWindows;
    }

    /* access modifiers changed from: package-private */
    public WindowState getParentWindow() {
        if (this.mIsChildWindow) {
            return (WindowState) super.getParent();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState getTopParentWindow() {
        WindowState current = this;
        WindowState topParent = current;
        while (current != null && current.mIsChildWindow) {
            current = current.getParentWindow();
            if (current != null) {
                topParent = current;
            }
        }
        return topParent;
    }

    /* access modifiers changed from: package-private */
    public boolean isParentWindowHidden() {
        WindowState parent = getParentWindow();
        return parent != null && parent.mHidden;
    }

    private boolean isParentWindowGoneForLayout() {
        WindowState parent = getParentWindow();
        return parent != null && parent.isGoneForLayoutLw();
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceWindow(boolean animate) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceWindow(animate);
        }
        if ((this.mAttrs.privateFlags & 32768) == 0 && this.mAttrs.type != 3) {
            this.mWillReplaceWindow = true;
            this.mReplacementWindow = null;
            this.mAnimateReplacingWindow = animate;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWillReplaceWindow() {
        this.mWillReplaceWindow = false;
        this.mReplacementWindow = null;
        this.mAnimateReplacingWindow = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWillReplaceWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean waitingForReplacement() {
        if (this.mWillReplaceWindow) {
            return true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).waitingForReplacement()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void requestUpdateWallpaperIfNeeded() {
        DisplayContent dc = getDisplayContent();
        if (!(dc == null || (this.mAttrs.flags & DumpState.DUMP_DEXOPT) == 0)) {
            dc.pendingLayoutChanges |= 4;
            dc.setLayoutNeeded();
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).requestUpdateWallpaperIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public float translateToWindowX(float x) {
        float winX = x - ((float) this.mWindowFrames.mFrame.left);
        if (inSizeCompatMode()) {
            return winX * this.mGlobalScale;
        }
        return winX;
    }

    /* access modifiers changed from: package-private */
    public float translateToWindowY(float y) {
        float winY = y - ((float) this.mWindowFrames.mFrame.top);
        if (inSizeCompatMode()) {
            return winY * this.mGlobalScale;
        }
        return winY;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBeReplacedWithChildren() {
        return this.mIsChildWindow || this.mAttrs.type == 2 || this.mAttrs.type == 4;
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceChildWindows() {
        if (shouldBeReplacedWithChildren()) {
            setWillReplaceWindow(false);
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceChildWindows();
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState getReplacingWindow() {
        if (this.mAnimatingExit && this.mWillReplaceWindow && this.mAnimateReplacingWindow) {
            return this;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState replacing = ((WindowState) this.mChildren.get(i)).getReplacingWindow();
            if (replacing != null) {
                return replacing;
            }
        }
        return null;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public int getRotationAnimationHint() {
        AppWindowToken appWindowToken = this.mAppToken;
        if (appWindowToken != null) {
            return appWindowToken.mRotationAnimationHint;
        }
        return -1;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isInputMethodWindow() {
        return this.mIsImWindow;
    }

    /* access modifiers changed from: package-private */
    public boolean performShowLocked() {
        AppWindowToken appWindowToken;
        if (isHiddenFromUserLocked()) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.w("WindowManager", "hiding " + this + ", belonging to " + this.mOwnerUid);
            }
            clearPolicyVisibilityFlag(2);
            return false;
        }
        logPerformShow("performShow on ");
        int drawState = this.mWinAnimator.mDrawState;
        if (!(!(drawState == 4 || drawState == 3) || this.mAttrs.type == 3 || (appWindowToken = this.mAppToken) == null)) {
            appWindowToken.onFirstWindowDrawn(this, this.mWinAnimator);
        }
        if (this.mWinAnimator.mDrawState != 3 || !isReadyForDisplay()) {
            return false;
        }
        logPerformShow("Showing ");
        this.mWmService.enableScreenIfNeededLocked();
        this.mWinAnimator.applyEnterAnimationLocked();
        this.mWinAnimator.mLastAlpha = -1.0f;
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v("WindowManager", "performShowLocked: mDrawState=HAS_DRAWN in " + this);
        }
        this.mWinAnimator.mDrawState = 4;
        this.mWmService.scheduleAnimationLocked();
        if (this.mHidden) {
            this.mHidden = false;
            DisplayContent displayContent = getDisplayContent();
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                WindowState c = (WindowState) this.mChildren.get(i);
                if (c.mWinAnimator.mSurfaceController != null) {
                    c.performShowLocked();
                    if (displayContent != null) {
                        displayContent.setLayoutNeeded();
                    }
                }
            }
        }
        if (this.mAttrs.type == 2011) {
            getDisplayContent().mDividerControllerLocked.resetImeHideRequested();
        }
        return true;
    }

    private void logPerformShow(String prefix) {
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY || (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE && this.mAttrs.type == 3)) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            sb.append(this);
            sb.append(": mDrawState=");
            sb.append(this.mWinAnimator.drawStateToString());
            sb.append(" readyForDisplay=");
            sb.append(isReadyForDisplay());
            sb.append(" starting=");
            boolean z = true;
            sb.append(this.mAttrs.type == 3);
            sb.append(" during animation: policyVis=");
            sb.append(isVisibleByPolicy());
            sb.append(" parentHidden=");
            sb.append(isParentWindowHidden());
            sb.append(" tok.hiddenRequested=");
            AppWindowToken appWindowToken = this.mAppToken;
            sb.append(appWindowToken != null && appWindowToken.hiddenRequested);
            sb.append(" tok.hidden=");
            AppWindowToken appWindowToken2 = this.mAppToken;
            sb.append(appWindowToken2 != null && appWindowToken2.isHidden());
            sb.append(" animating=");
            sb.append(isAnimating());
            sb.append(" tok animating=");
            AppWindowToken appWindowToken3 = this.mAppToken;
            if (appWindowToken3 == null || !appWindowToken3.isSelfAnimating()) {
                z = false;
            }
            sb.append(z);
            sb.append(" Callers=");
            sb.append(Debug.getCallers(4));
            Slog.v("WindowManager", sb.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public WindowInfo getWindowInfo() {
        WindowInfo windowInfo = WindowInfo.obtain();
        windowInfo.type = this.mAttrs.type;
        windowInfo.layer = this.mLayer;
        windowInfo.token = this.mClient.asBinder();
        AppWindowToken appWindowToken = this.mAppToken;
        if (appWindowToken != null) {
            windowInfo.activityToken = appWindowToken.appToken.asBinder();
        }
        windowInfo.title = this.mAttrs.accessibilityTitle;
        boolean z = false;
        boolean isPanelWindow = this.mAttrs.type >= 1000 && this.mAttrs.type <= 1999;
        boolean isAccessibilityOverlay = windowInfo.type == 2032;
        if (TextUtils.isEmpty(windowInfo.title) && (isPanelWindow || isAccessibilityOverlay)) {
            CharSequence title = this.mAttrs.getTitle();
            windowInfo.title = TextUtils.isEmpty(title) ? null : title;
        }
        windowInfo.accessibilityIdOfAnchor = this.mAttrs.accessibilityIdOfAnchor;
        windowInfo.focused = isFocused();
        Task task = getTask();
        windowInfo.inPictureInPicture = task != null && task.inPinnedWindowingMode();
        if ((this.mAttrs.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0) {
            z = true;
        }
        windowInfo.hasFlagWatchOutsideTouch = z;
        if (this.mIsChildWindow) {
            windowInfo.parentToken = getParentWindow().mClient.asBinder();
        }
        int childCount = this.mChildren.size();
        if (childCount > 0) {
            if (windowInfo.childTokens == null) {
                windowInfo.childTokens = new ArrayList(childCount);
            }
            for (int j = 0; j < childCount; j++) {
                windowInfo.childTokens.add(((WindowState) this.mChildren.get(j)).mClient.asBinder());
            }
        }
        return windowInfo;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (this.mChildren.isEmpty()) {
            return applyInOrderWithImeWindows(callback, traverseTopToBottom);
        }
        if (traverseTopToBottom) {
            return forAllWindowTopToBottom(callback);
        }
        return forAllWindowBottomToTop(callback);
    }

    private boolean forAllWindowBottomToTop(ToBooleanFunction<WindowState> callback) {
        WindowState child;
        int i = 0;
        int count = this.mChildren.size();
        Object obj = this.mChildren.get(0);
        while (true) {
            child = (WindowState) obj;
            if (i >= count || child.mSubLayer >= 0) {
                break;
            } else if (child.applyInOrderWithImeWindows(callback, false)) {
                return true;
            } else {
                i++;
                if (i >= count) {
                    break;
                }
                obj = this.mChildren.get(i);
            }
        }
        if (applyInOrderWithImeWindows(callback, false)) {
            return true;
        }
        while (i < count) {
            if (child.applyInOrderWithImeWindows(callback, false)) {
                return true;
            }
            i++;
            if (i >= count) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        return false;
    }

    private boolean forAllWindowTopToBottom(ToBooleanFunction<WindowState> callback) {
        int i = this.mChildren.size() - 1;
        WindowState child = (WindowState) this.mChildren.get(i);
        while (i >= 0 && child.mSubLayer >= 0) {
            if (child.applyInOrderWithImeWindows(callback, true)) {
                return true;
            }
            i--;
            if (i < 0) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        if (applyInOrderWithImeWindows(callback, true)) {
            return true;
        }
        while (i >= 0) {
            if (child.applyInOrderWithImeWindows(callback, true)) {
                return true;
            }
            i--;
            if (i < 0) {
                return false;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        return false;
    }

    private boolean applyImeWindowsIfNeeded(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (!isInputMethodTarget() || inSplitScreenWindowingMode() || !getDisplayContent().forAllImeWindows(callback, traverseTopToBottom)) {
            return false;
        }
        return true;
    }

    private boolean applyInOrderWithImeWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (traverseTopToBottom) {
            if (applyImeWindowsIfNeeded(callback, traverseTopToBottom) || callback.apply(this)) {
                return true;
            }
            return false;
        } else if (callback.apply(this) || applyImeWindowsIfNeeded(callback, traverseTopToBottom)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public WindowState getWindow(Predicate<WindowState> callback) {
        if (!this.mChildren.isEmpty()) {
            int i = this.mChildren.size() - 1;
            WindowState child = (WindowState) this.mChildren.get(i);
            while (i >= 0 && child.mSubLayer >= 0) {
                if (callback.test(child)) {
                    return child;
                }
                i--;
                if (i < 0) {
                    break;
                }
                child = (WindowState) this.mChildren.get(i);
            }
            if (callback.test(this)) {
                return this;
            }
            while (i >= 0) {
                if (callback.test(child)) {
                    return child;
                }
                i--;
                if (i < 0) {
                    break;
                }
                child = (WindowState) this.mChildren.get(i);
            }
            return null;
        } else if (callback.test(this)) {
            return this;
        } else {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isSelfOrAncestorWindowAnimatingExit() {
        WindowState window = this;
        while (!window.mAnimatingExit) {
            window = window.getParentWindow();
            if (window == null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onExitAnimationDone() {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v("WindowManager", "onExitAnimationDone in " + this + ": exiting=" + this.mAnimatingExit + " remove=" + this.mRemoveOnExit + " selfAnimating=" + isSelfAnimating());
        }
        if (!this.mChildren.isEmpty()) {
            ArrayList<WindowState> childWindows = new ArrayList<>(this.mChildren);
            for (int i = childWindows.size() - 1; i >= 0; i--) {
                childWindows.get(i).onExitAnimationDone();
            }
        }
        if (this.mWinAnimator.mEnteringAnimation) {
            this.mWinAnimator.mEnteringAnimation = false;
            this.mWmService.requestTraversal();
            if (this.mAppToken == null) {
                try {
                    this.mClient.dispatchWindowShown();
                } catch (RemoteException e) {
                }
            }
        }
        if (!isSelfAnimating()) {
            if (this.mWmService.mAccessibilityController != null && (getDisplayId() == 0 || getDisplayContent().getParentWindow() != null)) {
                this.mWmService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            if (this.mAttrs.type == 3 && this.mRemoveOnExit) {
                OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).putSnapshot(this.mAppToken);
            }
            if (isSelfOrAncestorWindowAnimatingExit()) {
                if (WindowManagerService.localLOGV || WindowManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.v("WindowManager", "Exit animation finished in " + this + ": remove=" + this.mRemoveOnExit);
                }
                this.mDestroying = true;
                boolean hasSurface = this.mWinAnimator.hasSurface();
                this.mWinAnimator.hide(getPendingTransaction(), "onExitAnimationDone");
                AppWindowToken appWindowToken = this.mAppToken;
                if (appWindowToken != null) {
                    appWindowToken.destroySurfaces();
                } else {
                    if (hasSurface) {
                        this.mWmService.mDestroySurface.add(this);
                    }
                    if (this.mRemoveOnExit) {
                        this.mWmService.mPendingRemove.add(this);
                        this.mRemoveOnExit = false;
                    }
                }
                this.mAnimatingExit = false;
                getDisplayContent().mWallpaperController.hideWallpapers(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean clearAnimatingFlags() {
        boolean didSomething = false;
        if (!this.mWillReplaceWindow && !this.mRemoveOnExit) {
            if (this.mAnimatingExit) {
                this.mAnimatingExit = false;
                didSomething = true;
            }
            if (this.mDestroying) {
                this.mDestroying = false;
                this.mWmService.mDestroySurface.remove(this);
                didSomething = true;
            }
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            didSomething |= ((WindowState) this.mChildren.get(i)).clearAnimatingFlags();
        }
        return didSomething;
    }

    public boolean isRtl() {
        return getConfiguration().getLayoutDirection() == 1;
    }

    /* access modifiers changed from: package-private */
    public void hideWallpaperWindow(boolean wasDeferred, String reason) {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            ((WindowState) this.mChildren.get(j)).hideWallpaperWindow(wasDeferred, reason);
        }
        if (!this.mWinAnimator.mLastHidden || wasDeferred) {
            this.mWinAnimator.hide(reason);
            getDisplayContent().mWallpaperController.mDeferredHideWallpaper = null;
            dispatchWallpaperVisibility(false);
            DisplayContent displayContent = getDisplayContent();
            if (displayContent != null) {
                displayContent.pendingLayoutChanges |= 4;
                if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                    this.mWmService.mWindowPlacerLocked.debugLayoutRepeats("hideWallpaperWindow " + this, displayContent.pendingLayoutChanges);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchWallpaperVisibility(boolean visible) {
        boolean hideAllowed = getDisplayContent().mWallpaperController.mDeferredHideWallpaper == null;
        if (this.mWallpaperVisible == visible) {
            return;
        }
        if (hideAllowed || visible) {
            this.mWallpaperVisible = visible;
            try {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v("WindowManager", "Updating vis of wallpaper " + this + ": " + visible + " from:\n" + Debug.getCallers(4, "  "));
                }
                this.mClient.dispatchAppVisibility(visible);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasVisibleNotDrawnWallpaper() {
        if (this.mWallpaperVisible && !isDrawnLw()) {
            return true;
        }
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowState) this.mChildren.get(j)).hasVisibleNotDrawnWallpaper()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateReportedVisibility(UpdateReportedVisibilityResults results) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).updateReportedVisibility(results);
        }
        if (!this.mAppFreezing && this.mViewVisibility == 0 && this.mAttrs.type != 3 && !this.mDestroying) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("WindowManager", "Win " + this + ": isDrawn=" + isDrawnLw() + ", animating=" + isAnimating());
                if (!isDrawnLw()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Not displayed: s=");
                    sb.append(this.mWinAnimator.mSurfaceController);
                    sb.append(" pv=");
                    sb.append(isVisibleByPolicy());
                    sb.append(" mDrawState=");
                    sb.append(this.mWinAnimator.mDrawState);
                    sb.append(" ph=");
                    sb.append(isParentWindowHidden());
                    sb.append(" th=");
                    AppWindowToken appWindowToken = this.mAppToken;
                    sb.append(appWindowToken != null ? appWindowToken.hiddenRequested : false);
                    sb.append(" a=");
                    sb.append(isAnimating());
                    Slog.v("WindowManager", sb.toString());
                }
            }
            results.numInteresting++;
            if (isDrawnLw()) {
                results.numDrawn++;
                if (!isAnimating()) {
                    results.numVisible++;
                }
                results.nowGone = false;
            } else if (isAnimating()) {
                results.nowGone = false;
            }
        }
    }

    private boolean skipDecorCrop() {
        if (this.mWindowFrames.mDecorFrame.isEmpty()) {
            return true;
        }
        if (this.mAppToken != null) {
            return false;
        }
        return this.mToken.canLayerAboveSystemBars();
    }

    /* access modifiers changed from: package-private */
    public void calculatePolicyCrop(Rect policyCrop) {
        DisplayContent displayContent = getDisplayContent();
        if (!displayContent.isDefaultDisplay && !displayContent.supportsSystemDecorations()) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            policyCrop.set(0, 0, this.mWindowFrames.mCompatFrame.width(), this.mWindowFrames.mCompatFrame.height());
            policyCrop.intersect(-this.mWindowFrames.mCompatFrame.left, -this.mWindowFrames.mCompatFrame.top, displayInfo.logicalWidth - this.mWindowFrames.mCompatFrame.left, displayInfo.logicalHeight - this.mWindowFrames.mCompatFrame.top);
        } else if (skipDecorCrop()) {
            policyCrop.set(0, 0, this.mWindowFrames.mCompatFrame.width(), this.mWindowFrames.mCompatFrame.height());
        } else {
            calculateSystemDecorRect(policyCrop);
        }
    }

    private void calculateSystemDecorRect(Rect systemDecorRect) {
        Rect decorRect = this.mWindowFrames.mDecorFrame;
        int width = this.mWindowFrames.mFrame.width();
        int height = this.mWindowFrames.mFrame.height();
        int left = this.mWindowFrames.mFrame.left;
        int top = this.mWindowFrames.mFrame.top;
        boolean cropToDecor = false;
        if (isDockedResizing()) {
            DisplayInfo displayInfo = getDisplayContent().getDisplayInfo();
            systemDecorRect.set(0, 0, Math.max(width, displayInfo.logicalWidth), Math.max(height, displayInfo.logicalHeight));
        } else {
            systemDecorRect.set(0, 0, width, height);
        }
        if ((!inFreeformWindowingMode() || !isAnimatingLw()) && !isDockedResizing()) {
            cropToDecor = true;
        }
        if (cropToDecor) {
            systemDecorRect.intersect(decorRect.left - left, decorRect.top - top, decorRect.right - left, decorRect.bottom - top);
        }
        if (this.mInvGlobalScale != 1.0f && inSizeCompatMode()) {
            float scale = this.mInvGlobalScale;
            systemDecorRect.left = (int) ((((float) systemDecorRect.left) * scale) - 0.5f);
            systemDecorRect.top = (int) ((((float) systemDecorRect.top) * scale) - 0.5f);
            systemDecorRect.right = (int) ((((float) (systemDecorRect.right + 1)) * scale) - 0.5f);
            systemDecorRect.bottom = (int) ((((float) (systemDecorRect.bottom + 1)) * scale) - 0.5f);
        }
    }

    /* access modifiers changed from: package-private */
    public void expandForSurfaceInsets(Rect r) {
        r.inset(-this.mAttrs.surfaceInsets.left, -this.mAttrs.surfaceInsets.top, -this.mAttrs.surfaceInsets.right, -this.mAttrs.surfaceInsets.bottom);
    }

    /* access modifiers changed from: package-private */
    public boolean surfaceInsetsChanging() {
        return !this.mLastSurfaceInsets.equals(this.mAttrs.surfaceInsets);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public int relayoutVisibleWindow(int result, int attrChanges) {
        boolean wasVisible = isVisibleLw();
        int i = 0;
        int result2 = result | ((!wasVisible || !isDrawnLw()) ? 2 : 0);
        if (this.mAnimatingExit) {
            Slog.d("WindowManager", "relayoutVisibleWindow: " + this + " mAnimatingExit=true, mRemoveOnExit=" + this.mRemoveOnExit + ", mDestroying=" + this.mDestroying);
            if (isSelfAnimating()) {
                cancelAnimation();
                destroySurfaceUnchecked();
            } else {
                this.mWinAnimator.resetDrawState();
            }
            this.mAnimatingExit = false;
        }
        if (this.mDestroying) {
            this.mDestroying = false;
            this.mWmService.mDestroySurface.remove(this);
        }
        boolean dockedResizing = true;
        if (!wasVisible) {
            this.mWinAnimator.mEnterAnimationPending = true;
        }
        this.mLastVisibleLayoutRotation = getDisplayContent().getRotation();
        this.mWinAnimator.mEnteringAnimation = true;
        Trace.traceBegin(32, "prepareToDisplay");
        try {
            prepareWindowToDisplayDuringRelayout(wasVisible);
            Trace.traceEnd(32);
            if ((attrChanges & 8) != 0 && !this.mWinAnimator.tryChangeFormatInPlaceLocked()) {
                this.mWinAnimator.preserveSurfaceLocked();
                result2 |= 6;
            }
            if (isDragResizeChanged()) {
                setDragResizing();
                if (this.mHasSurface && !isChildWindow()) {
                    this.mWinAnimator.preserveSurfaceLocked();
                    result2 |= 6;
                }
            }
            boolean freeformResizing = isDragResizing() && getResizeMode() == 0;
            if (!isDragResizing() || getResizeMode() != 1) {
                dockedResizing = false;
            }
            int result3 = result2 | (freeformResizing ? 16 : 0);
            if (dockedResizing) {
                i = 8;
            }
            return result3 | i;
        } catch (Throwable th) {
            Trace.traceEnd(32);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLaidOut() {
        return this.mLayoutSeq != -1;
    }

    /* access modifiers changed from: package-private */
    public void updateLastInsetValues() {
        this.mWindowFrames.updateLastInsetValues();
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(Animation anim) {
        InsetsSourceProvider insetsSourceProvider = this.mInsetProvider;
        if (insetsSourceProvider == null || !insetsSourceProvider.isControllable()) {
            DisplayInfo displayInfo = getDisplayContent().getDisplayInfo();
            anim.initialize(this.mWindowFrames.mFrame.width(), this.mWindowFrames.mFrame.height(), displayInfo.appWidth, displayInfo.appHeight);
            anim.restrictDuration(10000);
            anim.scaleCurrentDuration(this.mWmService.getWindowAnimationScaleLocked());
            startAnimation(getPendingTransaction(), new LocalAnimationAdapter(new WindowAnimationSpec(anim, this.mSurfacePosition, false, OppoBrightUtils.MIN_LUX_LIMITI), this.mWmService.mSurfaceAnimationRunner));
            commitPendingTransaction();
        }
    }

    private void startMoveAnimation(int left, int top) {
        InsetsSourceProvider insetsSourceProvider = this.mInsetProvider;
        if (insetsSourceProvider == null || !insetsSourceProvider.isControllable()) {
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v("WindowManager", "Setting move animation on " + this);
            }
            Point oldPosition = new Point();
            Point newPosition = new Point();
            transformFrameToSurfacePosition(this.mWindowFrames.mLastFrame.left, this.mWindowFrames.mLastFrame.top, oldPosition);
            transformFrameToSurfacePosition(left, top, newPosition);
            startAnimation(getPendingTransaction(), new LocalAnimationAdapter(new MoveAnimationSpec(oldPosition.x, oldPosition.y, newPosition.x, newPosition.y), this.mWmService.mSurfaceAnimationRunner));
        }
    }

    private void startAnimation(SurfaceControl.Transaction t, AnimationAdapter adapter) {
        startAnimation(t, adapter, this.mWinAnimator.mLastHidden);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void onAnimationFinished() {
        super.onAnimationFinished();
        this.mWinAnimator.onAnimationFinished();
    }

    /* access modifiers changed from: package-private */
    public void getTransformationMatrix(float[] float9, Matrix outMatrix) {
        float9[0] = this.mWinAnimator.mDsDx;
        float9[3] = this.mWinAnimator.mDtDx;
        float9[1] = this.mWinAnimator.mDtDy;
        float9[4] = this.mWinAnimator.mDsDy;
        int x = this.mSurfacePosition.x;
        int y = this.mSurfacePosition.y;
        DisplayContent dc = getDisplayContent();
        while (dc != null && dc.getParentWindow() != null) {
            WindowState displayParent = dc.getParentWindow();
            x = (int) (((float) x) + ((float) (displayParent.mWindowFrames.mFrame.left - displayParent.mAttrs.surfaceInsets.left)) + (((float) dc.getLocationInParentWindow().x) * displayParent.mGlobalScale) + 0.5f);
            y = (int) (((float) y) + ((float) (displayParent.mWindowFrames.mFrame.top - displayParent.mAttrs.surfaceInsets.top)) + (((float) dc.getLocationInParentWindow().y) * displayParent.mGlobalScale) + 0.5f);
            dc = displayParent.getDisplayContent();
        }
        WindowContainer parent = getParent();
        if (isChildWindow()) {
            WindowState parentWindow = getParentWindow();
            x += parentWindow.mWindowFrames.mFrame.left - parentWindow.mAttrs.surfaceInsets.left;
            y += parentWindow.mWindowFrames.mFrame.top - parentWindow.mAttrs.surfaceInsets.top;
        } else if (parent != null) {
            Rect parentBounds = parent.getBounds();
            x += parentBounds.left;
            y += parentBounds.top;
        }
        float9[2] = (float) x;
        float9[5] = (float) y;
        float9[6] = 0.0f;
        float9[7] = 0.0f;
        float9[8] = 1.0f;
        outMatrix.setValues(float9);
    }

    /* access modifiers changed from: package-private */
    public static final class UpdateReportedVisibilityResults {
        boolean nowGone = true;
        int numDrawn;
        int numInteresting;
        int numVisible;

        UpdateReportedVisibilityResults() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.numInteresting = 0;
            this.numVisible = 0;
            this.numDrawn = 0;
            this.nowGone = true;
        }
    }

    /* access modifiers changed from: private */
    public static final class WindowId extends IWindowId.Stub {
        private final WeakReference<WindowState> mOuter;

        private WindowId(WindowState outer) {
            this.mOuter = new WeakReference<>(outer);
        }

        public void registerFocusObserver(IWindowFocusObserver observer) {
            WindowState outer = this.mOuter.get();
            if (outer != null) {
                outer.registerFocusObserver(observer);
            }
        }

        public void unregisterFocusObserver(IWindowFocusObserver observer) {
            WindowState outer = this.mOuter.get();
            if (outer != null) {
                outer.unregisterFocusObserver(observer);
            }
        }

        public boolean isFocused() {
            boolean isFocused;
            WindowState outer = this.mOuter.get();
            if (outer == null) {
                return false;
            }
            synchronized (outer.mWmService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isFocused = outer.isFocused();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isFocused;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean shouldMagnify() {
        if (!OppoScreenDragUtil.isDragState()) {
            this.mYoffset = 0;
            if (!OppoFeatureCache.get(IColorBreenoManager.DEFAULT).inDragWindowing()) {
                return (this.mAttrs.type == 2011 || this.mAttrs.type == 2012 || this.mAttrs.type == 2027 || this.mAttrs.type == 2019 || this.mAttrs.type == 2024) ? false : true;
            }
            if (!OppoFeatureCache.get(IColorBreenoManager.DEFAULT).canMagnificationSpec(this)) {
                return false;
            }
            this.mYoffset = this.mTmpOffset;
            return true;
        } else if (this.mAttrs.type == 2011 || this.mAttrs.type == 2012 || this.mAttrs.type == 2019) {
            return true;
        } else {
            return (this.mAttrs.type == 2315 || (this.mAttrs.privateFlags & DumpState.DUMP_DEXOPT) != 0 || this.mAttrs.type == 2302 || this.mAttrs.type == 2301) ? false : true;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public SurfaceSession getSession() {
        if (this.mSession.mSurfaceSession != null) {
            return this.mSession.mSurfaceSession;
        }
        return getParent().getSession();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean needsZBoost() {
        AppWindowToken appToken;
        WindowState inputMethodTarget = getDisplayContent().mInputMethodTarget;
        if (!this.mIsImWindow || inputMethodTarget == null || (appToken = inputMethodTarget.mAppToken) == null) {
            return this.mWillReplaceWindow;
        }
        return appToken.needsZBoost();
    }

    private void applyDims(Dimmer dimmer) {
        if (!this.mAnimatingExit && this.mAppDied) {
            this.mIsDimming = true;
            dimmer.dimAbove(getPendingTransaction(), this, 0.5f);
        } else if ((this.mAttrs.flags & 2) != 0 && isVisibleNow() && !this.mHidden) {
            this.mIsDimming = true;
            dimmer.dimBelow(getPendingTransaction(), this, this.mAttrs.dimAmount);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void prepareSurfaces() {
        Dimmer dimmer = getDimmer();
        this.mIsDimming = false;
        if (dimmer != null) {
            applyDims(dimmer);
        }
        updateSurfacePosition();
        this.mWinAnimator.prepareSurfaceLocked(true);
        super.prepareSurfaces();
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer, com.android.server.wm.OppoBaseWindowState
    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        super.onAnimationLeashCreated(t, leash);
        t.setPosition(this.mSurfaceControl, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
        this.mLastSurfacePosition.set(0, 0);
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable, com.android.server.wm.WindowContainer, com.android.server.wm.OppoBaseWindowState
    public void onAnimationLeashLost(SurfaceControl.Transaction t) {
        super.onAnimationLeashLost(t);
        updateSurfacePosition(t);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void updateSurfacePosition() {
        updateSurfacePosition(getPendingTransaction());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateSurfacePosition(SurfaceControl.Transaction t) {
        if (this.mSurfaceControl != null) {
            transformFrameToSurfacePosition(this.mWindowFrames.mFrame.left, this.mWindowFrames.mFrame.top, this.mSurfacePosition);
            if (!this.mSurfaceAnimator.hasLeash() && this.mPendingSeamlessRotate == null && !this.mLastSurfacePosition.equals(this.mSurfacePosition)) {
                t.setPosition(this.mSurfaceControl, (float) this.mSurfacePosition.x, (float) this.mSurfacePosition.y);
                this.mLastSurfacePosition.set(this.mSurfacePosition.x, this.mSurfacePosition.y);
                if (surfaceInsetsChanging() && this.mWinAnimator.hasSurface()) {
                    this.mLastSurfaceInsets.set(this.mAttrs.surfaceInsets);
                    t.deferTransactionUntil(this.mSurfaceControl, this.mWinAnimator.mSurfaceController.mSurfaceControl.getHandle(), getFrameNumber());
                }
            }
        }
    }

    private void transformFrameToSurfacePosition(int left, int top, Point outPoint) {
        outPoint.set(left, top);
        WindowContainer parentWindowContainer = getParent();
        if (isChildWindow()) {
            WindowState parent = getParentWindow();
            transformSurfaceInsetsPosition(this.mTmpPoint, parent.mAttrs.surfaceInsets);
            outPoint.offset((-parent.mWindowFrames.mFrame.left) + this.mTmpPoint.x, (-parent.mWindowFrames.mFrame.top) + this.mTmpPoint.y);
        } else if (parentWindowContainer != null) {
            Rect parentBounds = parentWindowContainer.getDisplayedBounds();
            outPoint.offset(-parentBounds.left, -parentBounds.top);
        }
        TaskStack stack = getStack();
        if (stack != null) {
            int outset = stack.getStackOutset();
            outPoint.offset(outset, outset);
        }
        transformSurfaceInsetsPosition(this.mTmpPoint, this.mAttrs.surfaceInsets);
        outPoint.offset(-this.mTmpPoint.x, -this.mTmpPoint.y);
    }

    private void transformSurfaceInsetsPosition(Point outPos, Rect surfaceInsets) {
        if (!inSizeCompatMode()) {
            outPos.x = surfaceInsets.left;
            outPos.y = surfaceInsets.top;
            return;
        }
        outPos.x = (int) ((((float) surfaceInsets.left) * this.mGlobalScale) + 0.5f);
        outPos.y = (int) ((((float) surfaceInsets.top) * this.mGlobalScale) + 0.5f);
    }

    /* access modifiers changed from: package-private */
    public boolean needsRelativeLayeringToIme() {
        WindowState imeTarget;
        if (!inSplitScreenWindowingMode()) {
            return false;
        }
        if (isChildWindow()) {
            if (getParentWindow().isInputMethodTarget()) {
                return true;
            }
        } else if (this.mAppToken == null || (imeTarget = getDisplayContent().mInputMethodTarget) == null || imeTarget == this || imeTarget.mToken != this.mToken || imeTarget.compareTo((WindowContainer) this) > 0) {
            return false;
        } else {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void assignLayer(SurfaceControl.Transaction t, int layer) {
        if (needsRelativeLayeringToIme()) {
            getDisplayContent().assignRelativeLayerForImeTargetChild(t, this);
        } else {
            super.assignLayer(t, layer);
        }
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isDimming() {
        return this.mIsDimming;
    }

    @Override // com.android.server.wm.WindowContainer
    public void assignChildLayers(SurfaceControl.Transaction t) {
        int layer = 2;
        for (int i = 0; i < this.mChildren.size(); i++) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (w.mAttrs.type == 1001) {
                w.assignLayer(t, -2);
            } else if (w.mAttrs.type == 1004) {
                w.assignLayer(t, -1);
            } else {
                w.assignLayer(t, layer);
            }
            w.assignChildLayers(t);
            layer++;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateTapExcludeRegion(int regionId, Region region) {
        DisplayContent currentDisplay = getDisplayContent();
        if (currentDisplay != null) {
            if (this.mTapExcludeRegionHolder == null) {
                this.mTapExcludeRegionHolder = new TapExcludeRegionHolder();
                currentDisplay.mTapExcludeProvidingWindows.add(this);
            }
            this.mTapExcludeRegionHolder.updateRegion(regionId, region);
            currentDisplay.updateTouchExcludeRegion();
            currentDisplay.getInputMonitor().updateInputWindowsLw(true);
            return;
        }
        throw new IllegalStateException("Trying to update window not attached to any display.");
    }

    /* access modifiers changed from: package-private */
    public void amendTapExcludeRegion(Region region) {
        Region tempRegion = Region.obtain();
        this.mTmpRect.set(this.mWindowFrames.mFrame);
        this.mTmpRect.offsetTo(0, 0);
        this.mTapExcludeRegionHolder.amendRegion(tempRegion, this.mTmpRect);
        tempRegion.translate(this.mWindowFrames.mFrame.left, this.mWindowFrames.mFrame.top);
        region.op(tempRegion, Region.Op.UNION);
        tempRegion.recycle();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public boolean isInputMethodTarget() {
        return getDisplayContent().mInputMethodTarget == this;
    }

    /* access modifiers changed from: package-private */
    public long getFrameNumber() {
        return this.mFrameNumber;
    }

    /* access modifiers changed from: package-private */
    public void setFrameNumber(long frameNumber) {
        this.mFrameNumber = frameNumber;
    }

    public void getMaxVisibleBounds(Rect out) {
        if (out.isEmpty()) {
            out.set(this.mWindowFrames.mVisibleFrame);
            return;
        }
        if (this.mWindowFrames.mVisibleFrame.left < out.left) {
            out.left = this.mWindowFrames.mVisibleFrame.left;
        }
        if (this.mWindowFrames.mVisibleFrame.top < out.top) {
            out.top = this.mWindowFrames.mVisibleFrame.top;
        }
        if (this.mWindowFrames.mVisibleFrame.right > out.right) {
            out.right = this.mWindowFrames.mVisibleFrame.right;
        }
        if (this.mWindowFrames.mVisibleFrame.bottom > out.bottom) {
            out.bottom = this.mWindowFrames.mVisibleFrame.bottom;
        }
    }

    /* access modifiers changed from: package-private */
    public void getInsetsForRelayout(Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets) {
        outOverscanInsets.set(this.mWindowFrames.mOverscanInsets);
        outContentInsets.set(this.mWindowFrames.mContentInsets);
        outVisibleInsets.set(this.mWindowFrames.mVisibleInsets);
        outStableInsets.set(this.mWindowFrames.mStableInsets);
        outOutsets.set(this.mWindowFrames.mOutsets);
        this.mLastRelayoutContentInsets.set(this.mWindowFrames.mContentInsets);
    }

    /* access modifiers changed from: package-private */
    public void getContentInsets(Rect outContentInsets) {
        outContentInsets.set(this.mWindowFrames.mContentInsets);
    }

    /* access modifiers changed from: package-private */
    public Rect getContentInsets() {
        return this.mWindowFrames.mContentInsets;
    }

    /* access modifiers changed from: package-private */
    public void getStableInsets(Rect outStableInsets) {
        outStableInsets.set(this.mWindowFrames.mStableInsets);
    }

    /* access modifiers changed from: package-private */
    public Rect getStableInsets() {
        return this.mWindowFrames.mStableInsets;
    }

    /* access modifiers changed from: package-private */
    public void resetLastContentInsets() {
        this.mWindowFrames.resetLastContentInsets();
    }

    /* access modifiers changed from: package-private */
    public Rect getVisibleInsets() {
        return this.mWindowFrames.mVisibleInsets;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.WindowState
    public WindowFrames getWindowFrames() {
        return this.mWindowFrames;
    }

    /* access modifiers changed from: package-private */
    public void resetContentChanged() {
        this.mWindowFrames.setContentChanged(false);
    }

    /* access modifiers changed from: package-private */
    public void setInsetProvider(InsetsSourceProvider insetProvider) {
        this.mInsetProvider = insetProvider;
    }

    /* access modifiers changed from: package-private */
    public InsetsSourceProvider getInsetProvider() {
        return this.mInsetProvider;
    }

    /* access modifiers changed from: private */
    public final class MoveAnimationSpec implements LocalAnimationAdapter.AnimationSpec {
        private final long mDuration;
        private Point mFrom;
        private Interpolator mInterpolator;
        private Point mTo;

        private MoveAnimationSpec(int fromX, int fromY, int toX, int toY) {
            this.mFrom = new Point();
            this.mTo = new Point();
            Animation anim = AnimationUtils.loadAnimation(WindowState.this.mContext, 17432916);
            this.mDuration = (long) (((float) anim.computeDurationHint()) * WindowState.this.mWmService.getWindowAnimationScaleLocked());
            this.mInterpolator = anim.getInterpolator();
            this.mFrom.set(fromX, fromY);
            this.mTo.set(toX, toY);
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public long getDuration() {
            return this.mDuration;
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
            float v = this.mInterpolator.getInterpolation(((float) currentPlayTime) / ((float) getDuration()));
            t.setPosition(leash, ((float) this.mFrom.x) + (((float) (this.mTo.x - this.mFrom.x)) * v), ((float) this.mFrom.y) + (((float) (this.mTo.y - this.mFrom.y)) * v));
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + "from=" + this.mFrom + " to=" + this.mTo + " duration=" + this.mDuration);
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public void writeToProtoInner(ProtoOutputStream proto) {
            long token = proto.start(1146756268034L);
            this.mFrom.writeToProto(proto, 1146756268033L);
            this.mTo.writeToProto(proto, 1146756268034L);
            proto.write(1112396529667L, this.mDuration);
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyImeWindowStateChange(boolean hasShow) {
        WindowManager.LayoutParams layoutParams;
        if (this.mLastInputmethodShow != hasShow && (layoutParams = this.mAttrs) != null && layoutParams.type == 2011) {
            this.mLastInputmethodShow = hasShow;
            if (this.mOppoWindowManagerInternal == null) {
                this.mOppoWindowManagerInternal = (OppoWindowManagerInternal) LocalServices.getService(OppoWindowManagerInternal.class);
            }
            Bundle result = new Bundle();
            result.putBoolean("input", hasShow);
            this.mOppoWindowManagerInternal.notifyWindowStateChange(result);
            Slog.v("WindowManager", "notifyWindowStateChange input show");
        }
    }

    class ColorWindowStateInner implements IColorWindowStateInner {
        ColorWindowStateInner() {
        }

        @Override // com.android.server.wm.IColorWindowStateInner
        public boolean getAppOpVisibility() {
            return WindowState.this.mAppOpVisibility;
        }

        @Override // com.android.server.wm.IColorWindowStateInner
        public void setAppOpVisibilityLw(boolean state) {
            WindowState.this.setAppOpVisibilityLw(state);
        }
    }
}
