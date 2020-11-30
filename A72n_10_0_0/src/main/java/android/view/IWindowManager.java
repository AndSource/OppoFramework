package android.view;

import android.annotation.UnsupportedAppUsage;
import android.app.IAssistDataReceiver;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IDisplayFoldListener;
import android.view.IDockedStackListener;
import android.view.IOnKeyguardExitResult;
import android.view.IOppoWindowStateObserver;
import android.view.IPinnedStackListener;
import android.view.IRotationWatcher;
import android.view.ISystemGestureExclusionListener;
import android.view.IWallpaperVisibilityListener;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;

public interface IWindowManager extends IInterface {
    void addWindowToken(IBinder iBinder, int i, int i2) throws RemoteException;

    Bitmap captureLayersForKgd() throws RemoteException;

    void clearForcedDisplayDensityForUser(int i, int i2) throws RemoteException;

    void clearForcedDisplaySize(int i) throws RemoteException;

    boolean clearWindowContentFrameStats(IBinder iBinder) throws RemoteException;

    void closeSystemDialogs(String str) throws RemoteException;

    @UnsupportedAppUsage
    void createInputConsumer(IBinder iBinder, String str, int i, InputChannel inputChannel) throws RemoteException;

    @UnsupportedAppUsage
    boolean destroyInputConsumer(String str, int i) throws RemoteException;

    void disableKeyguard(IBinder iBinder, String str, int i) throws RemoteException;

    void dismissKeyguard(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) throws RemoteException;

    void dontOverrideDisplayInfo(int i) throws RemoteException;

    void enableScreenIfNeeded() throws RemoteException;

    @UnsupportedAppUsage
    void endProlongedAnimations() throws RemoteException;

    @UnsupportedAppUsage
    void executeAppTransition() throws RemoteException;

    void exitKeyguardSecurely(IOnKeyguardExitResult iOnKeyguardExitResult) throws RemoteException;

    void freezeDisplayRotation(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void freezeRotation(int i) throws RemoteException;

    @UnsupportedAppUsage
    float getAnimationScale(int i) throws RemoteException;

    @UnsupportedAppUsage
    float[] getAnimationScales() throws RemoteException;

    int getBaseDisplayDensity(int i) throws RemoteException;

    @UnsupportedAppUsage
    void getBaseDisplaySize(int i, Point point) throws RemoteException;

    float getCurrentAnimatorScale() throws RemoteException;

    Region getCurrentImeTouchRegion() throws RemoteException;

    int getDefaultDisplayRotation() throws RemoteException;

    @UnsupportedAppUsage
    int getDockedStackSide() throws RemoteException;

    int getFocusedWindowIgnoreHomeMenuKey() throws RemoteException;

    void getFreeformStackBounds(Rect rect) throws RemoteException;

    int getImeBgColorFromAdaptation(String str) throws RemoteException;

    @UnsupportedAppUsage
    int getInitialDisplayDensity(int i) throws RemoteException;

    @UnsupportedAppUsage
    void getInitialDisplaySize(int i, Point point) throws RemoteException;

    int getNavBarColorFromAdaptation(String str, String str2) throws RemoteException;

    int getNavBarPosition(int i) throws RemoteException;

    int getPreferredOptionsPanelGravity(int i) throws RemoteException;

    int getRemoveContentMode(int i) throws RemoteException;

    @UnsupportedAppUsage
    void getStableInsets(int i, Rect rect) throws RemoteException;

    int getStatusBarColorFromAdaptation(String str, String str2) throws RemoteException;

    int getTypedWindowLayer(int i) throws RemoteException;

    WindowContentFrameStats getWindowContentFrameStats(IBinder iBinder) throws RemoteException;

    int getWindowingMode(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean hasNavigationBar(int i) throws RemoteException;

    boolean injectInputAfterTransactionsApplied(InputEvent inputEvent, int i) throws RemoteException;

    boolean isActivityNeedPalette(String str, String str2) throws RemoteException;

    boolean isDisplayRotationFrozen(int i) throws RemoteException;

    boolean isInFreeformMode() throws RemoteException;

    @UnsupportedAppUsage
    boolean isKeyguardLocked() throws RemoteException;

    @UnsupportedAppUsage
    boolean isKeyguardSecure(int i) throws RemoteException;

    boolean isRotationFrozen() throws RemoteException;

    @UnsupportedAppUsage
    boolean isSafeModeEnabled() throws RemoteException;

    boolean isViewServerRunning() throws RemoteException;

    boolean isWindowTraceEnabled() throws RemoteException;

    @UnsupportedAppUsage
    void lockNow(Bundle bundle) throws RemoteException;

    IWindowSession openSession(IWindowSessionCallback iWindowSessionCallback) throws RemoteException;

    @UnsupportedAppUsage
    void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture, IRemoteCallback iRemoteCallback, boolean z, int i) throws RemoteException;

    @UnsupportedAppUsage
    void overridePendingAppTransitionRemote(RemoteAnimationAdapter remoteAnimationAdapter, int i) throws RemoteException;

    void prepareAppTransition(int i, boolean z) throws RemoteException;

    void reenableKeyguard(IBinder iBinder, int i) throws RemoteException;

    void refreshScreenCaptureDisabled(int i) throws RemoteException;

    void registerDisplayFoldListener(IDisplayFoldListener iDisplayFoldListener) throws RemoteException;

    @UnsupportedAppUsage
    void registerDockedStackListener(IDockedStackListener iDockedStackListener) throws RemoteException;

    void registerOppoWindowStateObserver(IOppoWindowStateObserver iOppoWindowStateObserver) throws RemoteException;

    void registerPinnedStackListener(int i, IPinnedStackListener iPinnedStackListener) throws RemoteException;

    void registerShortcutKey(long j, IShortcutService iShortcutService) throws RemoteException;

    void registerSystemGestureExclusionListener(ISystemGestureExclusionListener iSystemGestureExclusionListener, int i) throws RemoteException;

    boolean registerWallpaperVisibilityListener(IWallpaperVisibilityListener iWallpaperVisibilityListener, int i) throws RemoteException;

    @UnsupportedAppUsage
    void removeRotationWatcher(IRotationWatcher iRotationWatcher) throws RemoteException;

    void removeWindowToken(IBinder iBinder, int i) throws RemoteException;

    void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) throws RemoteException;

    boolean requestAssistScreenshot(IAssistDataReceiver iAssistDataReceiver) throws RemoteException;

    void requestUserActivityNotification() throws RemoteException;

    Bitmap screenshotWallpaper() throws RemoteException;

    @UnsupportedAppUsage
    void setAnimationScale(int i, float f) throws RemoteException;

    @UnsupportedAppUsage
    void setAnimationScales(float[] fArr) throws RemoteException;

    void setDockedStackDividerTouchRegion(Rect rect) throws RemoteException;

    void setEventDispatching(boolean z) throws RemoteException;

    void setForceShowSystemBars(boolean z) throws RemoteException;

    void setForcedDisplayDensityForUser(int i, int i2, int i3) throws RemoteException;

    void setForcedDisplayScalingMode(int i, int i2) throws RemoteException;

    void setForcedDisplaySize(int i, int i2, int i3) throws RemoteException;

    void setForwardedInsets(int i, Insets insets) throws RemoteException;

    void setInTouchMode(boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void setNavBarVirtualKeyHapticFeedbackEnabled(boolean z) throws RemoteException;

    void setOverscan(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    void setPipVisibility(boolean z) throws RemoteException;

    void setRecentsVisibility(boolean z) throws RemoteException;

    void setRemoveContentMode(int i, int i2) throws RemoteException;

    void setResizeDimLayer(boolean z, int i, float f) throws RemoteException;

    void setRotationLockForBootAnimation(boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void setShelfHeight(boolean z, int i) throws RemoteException;

    void setShouldShowIme(int i, boolean z) throws RemoteException;

    void setShouldShowSystemDecors(int i, boolean z) throws RemoteException;

    void setShouldShowWithInsecureKeyguard(int i, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void setStrictModeVisualIndicatorPreference(String str) throws RemoteException;

    void setSwitchingUser(boolean z) throws RemoteException;

    void setWindowingMode(int i, int i2) throws RemoteException;

    boolean shouldShowIme(int i) throws RemoteException;

    boolean shouldShowSystemDecors(int i) throws RemoteException;

    boolean shouldShowWithInsecureKeyguard(int i) throws RemoteException;

    void showStrictModeViolation(boolean z) throws RemoteException;

    void startFreezingScreen(int i, int i2) throws RemoteException;

    void startFreezingScreenForUserSwitch(int i, int i2, int i3) throws RemoteException;

    boolean startViewServer(int i) throws RemoteException;

    void startWindowTrace() throws RemoteException;

    void statusBarVisibilityChanged(int i, int i2) throws RemoteException;

    void stopFreezingScreen() throws RemoteException;

    boolean stopViewServer() throws RemoteException;

    void stopWindowTrace() throws RemoteException;

    void syncInputTransactions() throws RemoteException;

    void thawDisplayRotation(int i) throws RemoteException;

    @UnsupportedAppUsage
    void thawRotation() throws RemoteException;

    void unregisterDisplayFoldListener(IDisplayFoldListener iDisplayFoldListener) throws RemoteException;

    void unregisterOppoWindowStateObserver(IOppoWindowStateObserver iOppoWindowStateObserver) throws RemoteException;

    void unregisterSystemGestureExclusionListener(ISystemGestureExclusionListener iSystemGestureExclusionListener, int i) throws RemoteException;

    void unregisterWallpaperVisibilityListener(IWallpaperVisibilityListener iWallpaperVisibilityListener, int i) throws RemoteException;

    void updateRotation(boolean z, boolean z2) throws RemoteException;

    int watchRotation(IRotationWatcher iRotationWatcher, int i) throws RemoteException;

    public static class Default implements IWindowManager {
        @Override // android.view.IWindowManager
        public boolean startViewServer(int port) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public boolean stopViewServer() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public boolean isViewServerRunning() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public IWindowSession openSession(IWindowSessionCallback callback) throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowManager
        public void getInitialDisplaySize(int displayId, Point size) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void getBaseDisplaySize(int displayId, Point size) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setForcedDisplaySize(int displayId, int width, int height) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void clearForcedDisplaySize(int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public int getInitialDisplayDensity(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public int getBaseDisplayDensity(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public void setForcedDisplayDensityForUser(int displayId, int density, int userId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void clearForcedDisplayDensityForUser(int displayId, int userId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setForcedDisplayScalingMode(int displayId, int mode) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setOverscan(int displayId, int left, int top, int right, int bottom) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setEventDispatching(boolean enabled) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void addWindowToken(IBinder token, int type, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void removeWindowToken(IBinder token, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback startedCallback, boolean scaleUp, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void overridePendingAppTransitionRemote(RemoteAnimationAdapter remoteAnimationAdapter, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void executeAppTransition() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void endProlongedAnimations() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void startFreezingScreen(int exitAnim, int enterAnim) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void startFreezingScreenForUserSwitch(int exitAnim, int enterAnim, int freezeMillis) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void stopFreezingScreen() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void disableKeyguard(IBinder token, String tag, int userId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void reenableKeyguard(IBinder token, int userId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void exitKeyguardSecurely(IOnKeyguardExitResult callback) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean isKeyguardLocked() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public boolean isKeyguardSecure(int userId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void dismissKeyguard(IKeyguardDismissCallback callback, CharSequence message) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setSwitchingUser(boolean switching) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void closeSystemDialogs(String reason) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public float getAnimationScale(int which) throws RemoteException {
            return 0.0f;
        }

        @Override // android.view.IWindowManager
        public float[] getAnimationScales() throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowManager
        public void setAnimationScale(int which, float scale) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setAnimationScales(float[] scales) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public float getCurrentAnimatorScale() throws RemoteException {
            return 0.0f;
        }

        @Override // android.view.IWindowManager
        public void setInTouchMode(boolean showFocus) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void showStrictModeViolation(boolean on) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setStrictModeVisualIndicatorPreference(String enabled) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void refreshScreenCaptureDisabled(int userId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setRotationLockForBootAnimation(boolean isLock) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public int getDefaultDisplayRotation() throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public int watchRotation(IRotationWatcher watcher, int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public void removeRotationWatcher(IRotationWatcher watcher) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public int getPreferredOptionsPanelGravity(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public void freezeRotation(int rotation) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void thawRotation() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean isRotationFrozen() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void freezeDisplayRotation(int displayId, int rotation) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void thawDisplayRotation(int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean isDisplayRotationFrozen(int displayId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public Bitmap screenshotWallpaper() throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowManager
        public boolean registerWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void unregisterWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void registerSystemGestureExclusionListener(ISystemGestureExclusionListener listener, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void unregisterSystemGestureExclusionListener(ISystemGestureExclusionListener listener, int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean requestAssistScreenshot(IAssistDataReceiver receiver) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void statusBarVisibilityChanged(int displayId, int visibility) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setForceShowSystemBars(boolean show) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setRecentsVisibility(boolean visible) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setPipVisibility(boolean visible) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setShelfHeight(boolean visible, int shelfHeight) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setNavBarVirtualKeyHapticFeedbackEnabled(boolean enabled) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean hasNavigationBar(int displayId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public int getNavBarPosition(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public void lockNow(Bundle options) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean isSafeModeEnabled() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void enableScreenIfNeeded() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean clearWindowContentFrameStats(IBinder token) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public WindowContentFrameStats getWindowContentFrameStats(IBinder token) throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowManager
        public int getDockedStackSide() throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public void setDockedStackDividerTouchRegion(Rect touchableRegion) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void registerDockedStackListener(IDockedStackListener listener) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void registerPinnedStackListener(int displayId, IPinnedStackListener listener) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setResizeDimLayer(boolean visible, int targetWindowingMode, float alpha) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void getStableInsets(int displayId, Rect outInsets) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void setForwardedInsets(int displayId, Insets insets) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void registerShortcutKey(long shortcutCode, IShortcutService keySubscriber) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void createInputConsumer(IBinder token, String name, int displayId, InputChannel inputChannel) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean destroyInputConsumer(String name, int displayId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public Region getCurrentImeTouchRegion() throws RemoteException {
            return null;
        }

        @Override // android.view.IWindowManager
        public void registerDisplayFoldListener(IDisplayFoldListener listener) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void unregisterDisplayFoldListener(IDisplayFoldListener listener) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void startWindowTrace() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void stopWindowTrace() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean isWindowTraceEnabled() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void requestUserActivityNotification() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void dontOverrideDisplayInfo(int displayId) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean isInFreeformMode() throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void getFreeformStackBounds(Rect outBounds) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public int getWindowingMode(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public void setWindowingMode(int displayId, int mode) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public int getRemoveContentMode(int displayId) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public void setRemoveContentMode(int displayId, int mode) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean shouldShowWithInsecureKeyguard(int displayId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void setShouldShowWithInsecureKeyguard(int displayId, boolean shouldShow) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean shouldShowSystemDecors(int displayId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void setShouldShowSystemDecors(int displayId, boolean shouldShow) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean shouldShowIme(int displayId) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void setShouldShowIme(int displayId, boolean shouldShow) throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean injectInputAfterTransactionsApplied(InputEvent ev, int mode) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public void syncInputTransactions() throws RemoteException {
        }

        @Override // android.view.IWindowManager
        public boolean isActivityNeedPalette(String pkg, String activityName) throws RemoteException {
            return false;
        }

        @Override // android.view.IWindowManager
        public int getNavBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public int getStatusBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public int getImeBgColorFromAdaptation(String pkg) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public int getTypedWindowLayer(int type) throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public int getFocusedWindowIgnoreHomeMenuKey() throws RemoteException {
            return 0;
        }

        @Override // android.view.IWindowManager
        public Bitmap captureLayersForKgd() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWindowManager {
        private static final String DESCRIPTOR = "android.view.IWindowManager";
        static final int TRANSACTION_addWindowToken = 16;
        static final int TRANSACTION_captureLayersForKgd = 115;
        static final int TRANSACTION_clearForcedDisplayDensityForUser = 12;
        static final int TRANSACTION_clearForcedDisplaySize = 8;
        static final int TRANSACTION_clearWindowContentFrameStats = 72;
        static final int TRANSACTION_closeSystemDialogs = 33;
        static final int TRANSACTION_createInputConsumer = 83;
        static final int TRANSACTION_destroyInputConsumer = 84;
        static final int TRANSACTION_disableKeyguard = 26;
        static final int TRANSACTION_dismissKeyguard = 31;
        static final int TRANSACTION_dontOverrideDisplayInfo = 92;
        static final int TRANSACTION_enableScreenIfNeeded = 71;
        static final int TRANSACTION_endProlongedAnimations = 22;
        static final int TRANSACTION_executeAppTransition = 21;
        static final int TRANSACTION_exitKeyguardSecurely = 28;
        static final int TRANSACTION_freezeDisplayRotation = 52;
        static final int TRANSACTION_freezeRotation = 49;
        static final int TRANSACTION_getAnimationScale = 34;
        static final int TRANSACTION_getAnimationScales = 35;
        static final int TRANSACTION_getBaseDisplayDensity = 10;
        static final int TRANSACTION_getBaseDisplaySize = 6;
        static final int TRANSACTION_getCurrentAnimatorScale = 38;
        static final int TRANSACTION_getCurrentImeTouchRegion = 85;
        static final int TRANSACTION_getDefaultDisplayRotation = 45;
        static final int TRANSACTION_getDockedStackSide = 74;
        static final int TRANSACTION_getFocusedWindowIgnoreHomeMenuKey = 114;
        static final int TRANSACTION_getFreeformStackBounds = 96;
        static final int TRANSACTION_getImeBgColorFromAdaptation = 112;
        static final int TRANSACTION_getInitialDisplayDensity = 9;
        static final int TRANSACTION_getInitialDisplaySize = 5;
        static final int TRANSACTION_getNavBarColorFromAdaptation = 110;
        static final int TRANSACTION_getNavBarPosition = 68;
        static final int TRANSACTION_getPreferredOptionsPanelGravity = 48;
        static final int TRANSACTION_getRemoveContentMode = 99;
        static final int TRANSACTION_getStableInsets = 80;
        static final int TRANSACTION_getStatusBarColorFromAdaptation = 111;
        static final int TRANSACTION_getTypedWindowLayer = 113;
        static final int TRANSACTION_getWindowContentFrameStats = 73;
        static final int TRANSACTION_getWindowingMode = 97;
        static final int TRANSACTION_hasNavigationBar = 67;
        static final int TRANSACTION_injectInputAfterTransactionsApplied = 107;
        static final int TRANSACTION_isActivityNeedPalette = 109;
        static final int TRANSACTION_isDisplayRotationFrozen = 54;
        static final int TRANSACTION_isInFreeformMode = 95;
        static final int TRANSACTION_isKeyguardLocked = 29;
        static final int TRANSACTION_isKeyguardSecure = 30;
        static final int TRANSACTION_isRotationFrozen = 51;
        static final int TRANSACTION_isSafeModeEnabled = 70;
        static final int TRANSACTION_isViewServerRunning = 3;
        static final int TRANSACTION_isWindowTraceEnabled = 90;
        static final int TRANSACTION_lockNow = 69;
        static final int TRANSACTION_openSession = 4;
        static final int TRANSACTION_overridePendingAppTransitionMultiThumbFuture = 19;
        static final int TRANSACTION_overridePendingAppTransitionRemote = 20;
        static final int TRANSACTION_prepareAppTransition = 18;
        static final int TRANSACTION_reenableKeyguard = 27;
        static final int TRANSACTION_refreshScreenCaptureDisabled = 42;
        static final int TRANSACTION_registerDisplayFoldListener = 86;
        static final int TRANSACTION_registerDockedStackListener = 76;
        static final int TRANSACTION_registerOppoWindowStateObserver = 93;
        static final int TRANSACTION_registerPinnedStackListener = 77;
        static final int TRANSACTION_registerShortcutKey = 82;
        static final int TRANSACTION_registerSystemGestureExclusionListener = 58;
        static final int TRANSACTION_registerWallpaperVisibilityListener = 56;
        static final int TRANSACTION_removeRotationWatcher = 47;
        static final int TRANSACTION_removeWindowToken = 17;
        static final int TRANSACTION_requestAppKeyboardShortcuts = 79;
        static final int TRANSACTION_requestAssistScreenshot = 60;
        static final int TRANSACTION_requestUserActivityNotification = 91;
        static final int TRANSACTION_screenshotWallpaper = 55;
        static final int TRANSACTION_setAnimationScale = 36;
        static final int TRANSACTION_setAnimationScales = 37;
        static final int TRANSACTION_setDockedStackDividerTouchRegion = 75;
        static final int TRANSACTION_setEventDispatching = 15;
        static final int TRANSACTION_setForceShowSystemBars = 62;
        static final int TRANSACTION_setForcedDisplayDensityForUser = 11;
        static final int TRANSACTION_setForcedDisplayScalingMode = 13;
        static final int TRANSACTION_setForcedDisplaySize = 7;
        static final int TRANSACTION_setForwardedInsets = 81;
        static final int TRANSACTION_setInTouchMode = 39;
        static final int TRANSACTION_setNavBarVirtualKeyHapticFeedbackEnabled = 66;
        static final int TRANSACTION_setOverscan = 14;
        static final int TRANSACTION_setPipVisibility = 64;
        static final int TRANSACTION_setRecentsVisibility = 63;
        static final int TRANSACTION_setRemoveContentMode = 100;
        static final int TRANSACTION_setResizeDimLayer = 78;
        static final int TRANSACTION_setRotationLockForBootAnimation = 44;
        static final int TRANSACTION_setShelfHeight = 65;
        static final int TRANSACTION_setShouldShowIme = 106;
        static final int TRANSACTION_setShouldShowSystemDecors = 104;
        static final int TRANSACTION_setShouldShowWithInsecureKeyguard = 102;
        static final int TRANSACTION_setStrictModeVisualIndicatorPreference = 41;
        static final int TRANSACTION_setSwitchingUser = 32;
        static final int TRANSACTION_setWindowingMode = 98;
        static final int TRANSACTION_shouldShowIme = 105;
        static final int TRANSACTION_shouldShowSystemDecors = 103;
        static final int TRANSACTION_shouldShowWithInsecureKeyguard = 101;
        static final int TRANSACTION_showStrictModeViolation = 40;
        static final int TRANSACTION_startFreezingScreen = 23;
        static final int TRANSACTION_startFreezingScreenForUserSwitch = 24;
        static final int TRANSACTION_startViewServer = 1;
        static final int TRANSACTION_startWindowTrace = 88;
        static final int TRANSACTION_statusBarVisibilityChanged = 61;
        static final int TRANSACTION_stopFreezingScreen = 25;
        static final int TRANSACTION_stopViewServer = 2;
        static final int TRANSACTION_stopWindowTrace = 89;
        static final int TRANSACTION_syncInputTransactions = 108;
        static final int TRANSACTION_thawDisplayRotation = 53;
        static final int TRANSACTION_thawRotation = 50;
        static final int TRANSACTION_unregisterDisplayFoldListener = 87;
        static final int TRANSACTION_unregisterOppoWindowStateObserver = 94;
        static final int TRANSACTION_unregisterSystemGestureExclusionListener = 59;
        static final int TRANSACTION_unregisterWallpaperVisibilityListener = 57;
        static final int TRANSACTION_updateRotation = 43;
        static final int TRANSACTION_watchRotation = 46;

        public Stub() {
            attachInterface(this, "android.view.IWindowManager");
        }

        public static IWindowManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.view.IWindowManager");
            if (iin == null || !(iin instanceof IWindowManager)) {
                return new Proxy(obj);
            }
            return (IWindowManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "startViewServer";
                case 2:
                    return "stopViewServer";
                case 3:
                    return "isViewServerRunning";
                case 4:
                    return "openSession";
                case 5:
                    return "getInitialDisplaySize";
                case 6:
                    return "getBaseDisplaySize";
                case 7:
                    return "setForcedDisplaySize";
                case 8:
                    return "clearForcedDisplaySize";
                case 9:
                    return "getInitialDisplayDensity";
                case 10:
                    return "getBaseDisplayDensity";
                case 11:
                    return "setForcedDisplayDensityForUser";
                case 12:
                    return "clearForcedDisplayDensityForUser";
                case 13:
                    return "setForcedDisplayScalingMode";
                case 14:
                    return "setOverscan";
                case 15:
                    return "setEventDispatching";
                case 16:
                    return "addWindowToken";
                case 17:
                    return "removeWindowToken";
                case 18:
                    return "prepareAppTransition";
                case 19:
                    return "overridePendingAppTransitionMultiThumbFuture";
                case 20:
                    return "overridePendingAppTransitionRemote";
                case 21:
                    return "executeAppTransition";
                case 22:
                    return "endProlongedAnimations";
                case 23:
                    return "startFreezingScreen";
                case 24:
                    return "startFreezingScreenForUserSwitch";
                case 25:
                    return "stopFreezingScreen";
                case 26:
                    return "disableKeyguard";
                case 27:
                    return "reenableKeyguard";
                case 28:
                    return "exitKeyguardSecurely";
                case 29:
                    return "isKeyguardLocked";
                case 30:
                    return "isKeyguardSecure";
                case 31:
                    return "dismissKeyguard";
                case 32:
                    return "setSwitchingUser";
                case 33:
                    return "closeSystemDialogs";
                case 34:
                    return "getAnimationScale";
                case 35:
                    return "getAnimationScales";
                case 36:
                    return "setAnimationScale";
                case 37:
                    return "setAnimationScales";
                case 38:
                    return "getCurrentAnimatorScale";
                case 39:
                    return "setInTouchMode";
                case 40:
                    return "showStrictModeViolation";
                case 41:
                    return "setStrictModeVisualIndicatorPreference";
                case 42:
                    return "refreshScreenCaptureDisabled";
                case 43:
                    return "updateRotation";
                case 44:
                    return "setRotationLockForBootAnimation";
                case 45:
                    return "getDefaultDisplayRotation";
                case 46:
                    return "watchRotation";
                case 47:
                    return "removeRotationWatcher";
                case 48:
                    return "getPreferredOptionsPanelGravity";
                case 49:
                    return "freezeRotation";
                case 50:
                    return "thawRotation";
                case 51:
                    return "isRotationFrozen";
                case 52:
                    return "freezeDisplayRotation";
                case 53:
                    return "thawDisplayRotation";
                case 54:
                    return "isDisplayRotationFrozen";
                case 55:
                    return "screenshotWallpaper";
                case 56:
                    return "registerWallpaperVisibilityListener";
                case 57:
                    return "unregisterWallpaperVisibilityListener";
                case 58:
                    return "registerSystemGestureExclusionListener";
                case 59:
                    return "unregisterSystemGestureExclusionListener";
                case 60:
                    return "requestAssistScreenshot";
                case 61:
                    return "statusBarVisibilityChanged";
                case 62:
                    return "setForceShowSystemBars";
                case 63:
                    return "setRecentsVisibility";
                case 64:
                    return "setPipVisibility";
                case 65:
                    return "setShelfHeight";
                case 66:
                    return "setNavBarVirtualKeyHapticFeedbackEnabled";
                case 67:
                    return "hasNavigationBar";
                case 68:
                    return "getNavBarPosition";
                case 69:
                    return "lockNow";
                case 70:
                    return "isSafeModeEnabled";
                case 71:
                    return "enableScreenIfNeeded";
                case 72:
                    return "clearWindowContentFrameStats";
                case 73:
                    return "getWindowContentFrameStats";
                case 74:
                    return "getDockedStackSide";
                case 75:
                    return "setDockedStackDividerTouchRegion";
                case 76:
                    return "registerDockedStackListener";
                case 77:
                    return "registerPinnedStackListener";
                case 78:
                    return "setResizeDimLayer";
                case 79:
                    return "requestAppKeyboardShortcuts";
                case 80:
                    return "getStableInsets";
                case 81:
                    return "setForwardedInsets";
                case 82:
                    return "registerShortcutKey";
                case 83:
                    return "createInputConsumer";
                case 84:
                    return "destroyInputConsumer";
                case 85:
                    return "getCurrentImeTouchRegion";
                case 86:
                    return "registerDisplayFoldListener";
                case 87:
                    return "unregisterDisplayFoldListener";
                case 88:
                    return "startWindowTrace";
                case 89:
                    return "stopWindowTrace";
                case 90:
                    return "isWindowTraceEnabled";
                case 91:
                    return "requestUserActivityNotification";
                case 92:
                    return "dontOverrideDisplayInfo";
                case 93:
                    return "registerOppoWindowStateObserver";
                case 94:
                    return "unregisterOppoWindowStateObserver";
                case 95:
                    return "isInFreeformMode";
                case 96:
                    return "getFreeformStackBounds";
                case 97:
                    return "getWindowingMode";
                case 98:
                    return "setWindowingMode";
                case 99:
                    return "getRemoveContentMode";
                case 100:
                    return "setRemoveContentMode";
                case 101:
                    return "shouldShowWithInsecureKeyguard";
                case 102:
                    return "setShouldShowWithInsecureKeyguard";
                case 103:
                    return "shouldShowSystemDecors";
                case 104:
                    return "setShouldShowSystemDecors";
                case 105:
                    return "shouldShowIme";
                case 106:
                    return "setShouldShowIme";
                case 107:
                    return "injectInputAfterTransactionsApplied";
                case 108:
                    return "syncInputTransactions";
                case 109:
                    return "isActivityNeedPalette";
                case 110:
                    return "getNavBarColorFromAdaptation";
                case 111:
                    return "getStatusBarColorFromAdaptation";
                case 112:
                    return "getImeBgColorFromAdaptation";
                case 113:
                    return "getTypedWindowLayer";
                case 114:
                    return "getFocusedWindowIgnoreHomeMenuKey";
                case 115:
                    return "captureLayersForKgd";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RemoteAnimationAdapter _arg0;
            CharSequence _arg1;
            Bundle _arg02;
            Rect _arg03;
            Insets _arg12;
            InputEvent _arg04;
            if (code != 1598968902) {
                boolean _arg13 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean startViewServer = startViewServer(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(startViewServer ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean stopViewServer = stopViewServer();
                        reply.writeNoException();
                        reply.writeInt(stopViewServer ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isViewServerRunning = isViewServerRunning();
                        reply.writeNoException();
                        reply.writeInt(isViewServerRunning ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface("android.view.IWindowManager");
                        IWindowSession _result = openSession(IWindowSessionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                        return true;
                    case 5:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg05 = data.readInt();
                        Point _arg14 = new Point();
                        getInitialDisplaySize(_arg05, _arg14);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg14.writeToParcel(reply, 1);
                        return true;
                    case 6:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg06 = data.readInt();
                        Point _arg15 = new Point();
                        getBaseDisplaySize(_arg06, _arg15);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg15.writeToParcel(reply, 1);
                        return true;
                    case 7:
                        data.enforceInterface("android.view.IWindowManager");
                        setForcedDisplaySize(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface("android.view.IWindowManager");
                        clearForcedDisplaySize(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result2 = getInitialDisplayDensity(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 10:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result3 = getBaseDisplayDensity(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 11:
                        data.enforceInterface("android.view.IWindowManager");
                        setForcedDisplayDensityForUser(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface("android.view.IWindowManager");
                        clearForcedDisplayDensityForUser(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface("android.view.IWindowManager");
                        setForcedDisplayScalingMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface("android.view.IWindowManager");
                        setOverscan(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setEventDispatching(_arg13);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface("android.view.IWindowManager");
                        addWindowToken(data.readStrongBinder(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface("android.view.IWindowManager");
                        removeWindowToken(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        prepareAppTransition(_arg07, _arg13);
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface("android.view.IWindowManager");
                        IAppTransitionAnimationSpecsFuture _arg08 = IAppTransitionAnimationSpecsFuture.Stub.asInterface(data.readStrongBinder());
                        IRemoteCallback _arg16 = IRemoteCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        overridePendingAppTransitionMultiThumbFuture(_arg08, _arg16, _arg13, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg0 = RemoteAnimationAdapter.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        overridePendingAppTransitionRemote(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface("android.view.IWindowManager");
                        executeAppTransition();
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface("android.view.IWindowManager");
                        endProlongedAnimations();
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface("android.view.IWindowManager");
                        startFreezingScreen(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface("android.view.IWindowManager");
                        startFreezingScreenForUserSwitch(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface("android.view.IWindowManager");
                        stopFreezingScreen();
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface("android.view.IWindowManager");
                        disableKeyguard(data.readStrongBinder(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface("android.view.IWindowManager");
                        reenableKeyguard(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface("android.view.IWindowManager");
                        exitKeyguardSecurely(IOnKeyguardExitResult.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isKeyguardLocked = isKeyguardLocked();
                        reply.writeNoException();
                        reply.writeInt(isKeyguardLocked ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isKeyguardSecure = isKeyguardSecure(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isKeyguardSecure ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface("android.view.IWindowManager");
                        IKeyguardDismissCallback _arg09 = IKeyguardDismissCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        dismissKeyguard(_arg09, _arg1);
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setSwitchingUser(_arg13);
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface("android.view.IWindowManager");
                        closeSystemDialogs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface("android.view.IWindowManager");
                        float _result4 = getAnimationScale(data.readInt());
                        reply.writeNoException();
                        reply.writeFloat(_result4);
                        return true;
                    case 35:
                        data.enforceInterface("android.view.IWindowManager");
                        float[] _result5 = getAnimationScales();
                        reply.writeNoException();
                        reply.writeFloatArray(_result5);
                        return true;
                    case 36:
                        data.enforceInterface("android.view.IWindowManager");
                        setAnimationScale(data.readInt(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface("android.view.IWindowManager");
                        setAnimationScales(data.createFloatArray());
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface("android.view.IWindowManager");
                        float _result6 = getCurrentAnimatorScale();
                        reply.writeNoException();
                        reply.writeFloat(_result6);
                        return true;
                    case 39:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setInTouchMode(_arg13);
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        showStrictModeViolation(_arg13);
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface("android.view.IWindowManager");
                        setStrictModeVisualIndicatorPreference(data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface("android.view.IWindowManager");
                        refreshScreenCaptureDisabled(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean _arg010 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        updateRotation(_arg010, _arg13);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setRotationLockForBootAnimation(_arg13);
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result7 = getDefaultDisplayRotation();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 46:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result8 = watchRotation(IRotationWatcher.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 47:
                        data.enforceInterface("android.view.IWindowManager");
                        removeRotationWatcher(IRotationWatcher.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result9 = getPreferredOptionsPanelGravity(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 49:
                        data.enforceInterface("android.view.IWindowManager");
                        freezeRotation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface("android.view.IWindowManager");
                        thawRotation();
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isRotationFrozen = isRotationFrozen();
                        reply.writeNoException();
                        reply.writeInt(isRotationFrozen ? 1 : 0);
                        return true;
                    case 52:
                        data.enforceInterface("android.view.IWindowManager");
                        freezeDisplayRotation(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface("android.view.IWindowManager");
                        thawDisplayRotation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isDisplayRotationFrozen = isDisplayRotationFrozen(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDisplayRotationFrozen ? 1 : 0);
                        return true;
                    case 55:
                        data.enforceInterface("android.view.IWindowManager");
                        Bitmap _result10 = screenshotWallpaper();
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 56:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean registerWallpaperVisibilityListener = registerWallpaperVisibilityListener(IWallpaperVisibilityListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(registerWallpaperVisibilityListener ? 1 : 0);
                        return true;
                    case 57:
                        data.enforceInterface("android.view.IWindowManager");
                        unregisterWallpaperVisibilityListener(IWallpaperVisibilityListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface("android.view.IWindowManager");
                        registerSystemGestureExclusionListener(ISystemGestureExclusionListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface("android.view.IWindowManager");
                        unregisterSystemGestureExclusionListener(ISystemGestureExclusionListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean requestAssistScreenshot = requestAssistScreenshot(IAssistDataReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(requestAssistScreenshot ? 1 : 0);
                        return true;
                    case 61:
                        data.enforceInterface("android.view.IWindowManager");
                        statusBarVisibilityChanged(data.readInt(), data.readInt());
                        return true;
                    case 62:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setForceShowSystemBars(_arg13);
                        return true;
                    case 63:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setRecentsVisibility(_arg13);
                        return true;
                    case 64:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setPipVisibility(_arg13);
                        return true;
                    case 65:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setShelfHeight(_arg13, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setNavBarVirtualKeyHapticFeedbackEnabled(_arg13);
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean hasNavigationBar = hasNavigationBar(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasNavigationBar ? 1 : 0);
                        return true;
                    case 68:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result11 = getNavBarPosition(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 69:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg02 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        lockNow(_arg02);
                        reply.writeNoException();
                        return true;
                    case 70:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isSafeModeEnabled = isSafeModeEnabled();
                        reply.writeNoException();
                        reply.writeInt(isSafeModeEnabled ? 1 : 0);
                        return true;
                    case 71:
                        data.enforceInterface("android.view.IWindowManager");
                        enableScreenIfNeeded();
                        reply.writeNoException();
                        return true;
                    case 72:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean clearWindowContentFrameStats = clearWindowContentFrameStats(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(clearWindowContentFrameStats ? 1 : 0);
                        return true;
                    case 73:
                        data.enforceInterface("android.view.IWindowManager");
                        WindowContentFrameStats _result12 = getWindowContentFrameStats(data.readStrongBinder());
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 74:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result13 = getDockedStackSide();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 75:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg03 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        setDockedStackDividerTouchRegion(_arg03);
                        reply.writeNoException();
                        return true;
                    case 76:
                        data.enforceInterface("android.view.IWindowManager");
                        registerDockedStackListener(IDockedStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 77:
                        data.enforceInterface("android.view.IWindowManager");
                        registerPinnedStackListener(data.readInt(), IPinnedStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 78:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setResizeDimLayer(_arg13, data.readInt(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface("android.view.IWindowManager");
                        requestAppKeyboardShortcuts(IResultReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 80:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg011 = data.readInt();
                        Rect _arg17 = new Rect();
                        getStableInsets(_arg011, _arg17);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg17.writeToParcel(reply, 1);
                        return true;
                    case 81:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg012 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = Insets.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        setForwardedInsets(_arg012, _arg12);
                        reply.writeNoException();
                        return true;
                    case 82:
                        data.enforceInterface("android.view.IWindowManager");
                        registerShortcutKey(data.readLong(), IShortcutService.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 83:
                        data.enforceInterface("android.view.IWindowManager");
                        IBinder _arg013 = data.readStrongBinder();
                        String _arg18 = data.readString();
                        int _arg2 = data.readInt();
                        InputChannel _arg3 = new InputChannel();
                        createInputConsumer(_arg013, _arg18, _arg2, _arg3);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg3.writeToParcel(reply, 1);
                        return true;
                    case 84:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean destroyInputConsumer = destroyInputConsumer(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(destroyInputConsumer ? 1 : 0);
                        return true;
                    case 85:
                        data.enforceInterface("android.view.IWindowManager");
                        Region _result14 = getCurrentImeTouchRegion();
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 86:
                        data.enforceInterface("android.view.IWindowManager");
                        registerDisplayFoldListener(IDisplayFoldListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 87:
                        data.enforceInterface("android.view.IWindowManager");
                        unregisterDisplayFoldListener(IDisplayFoldListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 88:
                        data.enforceInterface("android.view.IWindowManager");
                        startWindowTrace();
                        reply.writeNoException();
                        return true;
                    case 89:
                        data.enforceInterface("android.view.IWindowManager");
                        stopWindowTrace();
                        reply.writeNoException();
                        return true;
                    case 90:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isWindowTraceEnabled = isWindowTraceEnabled();
                        reply.writeNoException();
                        reply.writeInt(isWindowTraceEnabled ? 1 : 0);
                        return true;
                    case 91:
                        data.enforceInterface("android.view.IWindowManager");
                        requestUserActivityNotification();
                        reply.writeNoException();
                        return true;
                    case 92:
                        data.enforceInterface("android.view.IWindowManager");
                        dontOverrideDisplayInfo(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 93:
                        data.enforceInterface("android.view.IWindowManager");
                        registerOppoWindowStateObserver(IOppoWindowStateObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 94:
                        data.enforceInterface("android.view.IWindowManager");
                        unregisterOppoWindowStateObserver(IOppoWindowStateObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 95:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isInFreeformMode = isInFreeformMode();
                        reply.writeNoException();
                        reply.writeInt(isInFreeformMode ? 1 : 0);
                        return true;
                    case 96:
                        data.enforceInterface("android.view.IWindowManager");
                        Rect _arg014 = new Rect();
                        getFreeformStackBounds(_arg014);
                        reply.writeNoException();
                        reply.writeInt(1);
                        _arg014.writeToParcel(reply, 1);
                        return true;
                    case 97:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result15 = getWindowingMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 98:
                        data.enforceInterface("android.view.IWindowManager");
                        setWindowingMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 99:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result16 = getRemoveContentMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 100:
                        data.enforceInterface("android.view.IWindowManager");
                        setRemoveContentMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 101:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean shouldShowWithInsecureKeyguard = shouldShowWithInsecureKeyguard(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shouldShowWithInsecureKeyguard ? 1 : 0);
                        return true;
                    case 102:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg015 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setShouldShowWithInsecureKeyguard(_arg015, _arg13);
                        reply.writeNoException();
                        return true;
                    case 103:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean shouldShowSystemDecors = shouldShowSystemDecors(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shouldShowSystemDecors ? 1 : 0);
                        return true;
                    case 104:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg016 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setShouldShowSystemDecors(_arg016, _arg13);
                        reply.writeNoException();
                        return true;
                    case 105:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean shouldShowIme = shouldShowIme(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shouldShowIme ? 1 : 0);
                        return true;
                    case 106:
                        data.enforceInterface("android.view.IWindowManager");
                        int _arg017 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setShouldShowIme(_arg017, _arg13);
                        reply.writeNoException();
                        return true;
                    case 107:
                        data.enforceInterface("android.view.IWindowManager");
                        if (data.readInt() != 0) {
                            _arg04 = InputEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean injectInputAfterTransactionsApplied = injectInputAfterTransactionsApplied(_arg04, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(injectInputAfterTransactionsApplied ? 1 : 0);
                        return true;
                    case 108:
                        data.enforceInterface("android.view.IWindowManager");
                        syncInputTransactions();
                        reply.writeNoException();
                        return true;
                    case 109:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isActivityNeedPalette = isActivityNeedPalette(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isActivityNeedPalette ? 1 : 0);
                        return true;
                    case 110:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result17 = getNavBarColorFromAdaptation(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 111:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result18 = getStatusBarColorFromAdaptation(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 112:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result19 = getImeBgColorFromAdaptation(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 113:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result20 = getTypedWindowLayer(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 114:
                        data.enforceInterface("android.view.IWindowManager");
                        int _result21 = getFocusedWindowIgnoreHomeMenuKey();
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 115:
                        data.enforceInterface("android.view.IWindowManager");
                        Bitmap _result22 = captureLayersForKgd();
                        reply.writeNoException();
                        if (_result22 != null) {
                            reply.writeInt(1);
                            _result22.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString("android.view.IWindowManager");
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWindowManager {
            public static IWindowManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "android.view.IWindowManager";
            }

            @Override // android.view.IWindowManager
            public boolean startViewServer(int port) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(port);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startViewServer(port);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean stopViewServer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopViewServer();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isViewServerRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isViewServerRunning();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public IWindowSession openSession(IWindowSessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openSession(callback);
                    }
                    _reply.readException();
                    IWindowSession _result = IWindowSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void getInitialDisplaySize(int displayId, Point size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            size.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getInitialDisplaySize(displayId, size);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void getBaseDisplaySize(int displayId, Point size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            size.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getBaseDisplaySize(displayId, size);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setForcedDisplaySize(int displayId, int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForcedDisplaySize(displayId, width, height);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void clearForcedDisplaySize(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearForcedDisplaySize(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getInitialDisplayDensity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInitialDisplayDensity(displayId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getBaseDisplayDensity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBaseDisplayDensity(displayId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setForcedDisplayDensityForUser(int displayId, int density, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(density);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForcedDisplayDensityForUser(displayId, density, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void clearForcedDisplayDensityForUser(int displayId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearForcedDisplayDensityForUser(displayId, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setForcedDisplayScalingMode(int displayId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForcedDisplayScalingMode(displayId, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setOverscan(int displayId, int left, int top, int right, int bottom) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(left);
                    _data.writeInt(top);
                    _data.writeInt(right);
                    _data.writeInt(bottom);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOverscan(displayId, left, top, right, bottom);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setEventDispatching(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setEventDispatching(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void addWindowToken(IBinder token, int type, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeInt(type);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addWindowToken(token, type, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void removeWindowToken(IBinder token, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeWindowToken(token, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(transit);
                    _data.writeInt(alwaysKeepCurrent ? 1 : 0);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepareAppTransition(transit, alwaysKeepCurrent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback startedCallback, boolean scaleUp, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    IBinder iBinder = null;
                    _data.writeStrongBinder(specsFuture != null ? specsFuture.asBinder() : null);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(scaleUp ? 1 : 0);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().overridePendingAppTransitionMultiThumbFuture(specsFuture, startedCallback, scaleUp, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void overridePendingAppTransitionRemote(RemoteAnimationAdapter remoteAnimationAdapter, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (remoteAnimationAdapter != null) {
                        _data.writeInt(1);
                        remoteAnimationAdapter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().overridePendingAppTransitionRemote(remoteAnimationAdapter, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void executeAppTransition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().executeAppTransition();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void endProlongedAnimations() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().endProlongedAnimations();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void startFreezingScreen(int exitAnim, int enterAnim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(exitAnim);
                    _data.writeInt(enterAnim);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startFreezingScreen(exitAnim, enterAnim);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void startFreezingScreenForUserSwitch(int exitAnim, int enterAnim, int freezeMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(exitAnim);
                    _data.writeInt(enterAnim);
                    _data.writeInt(freezeMillis);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startFreezingScreenForUserSwitch(exitAnim, enterAnim, freezeMillis);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void stopFreezingScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopFreezingScreen();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void disableKeyguard(IBinder token, String tag, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeString(tag);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableKeyguard(token, tag, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void reenableKeyguard(IBinder token, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reenableKeyguard(token, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void exitKeyguardSecurely(IOnKeyguardExitResult callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().exitKeyguardSecurely(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isKeyguardLocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isKeyguardLocked();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isKeyguardSecure(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isKeyguardSecure(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void dismissKeyguard(IKeyguardDismissCallback callback, CharSequence message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dismissKeyguard(callback, message);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setSwitchingUser(boolean switching) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(switching ? 1 : 0);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSwitchingUser(switching);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(reason);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeSystemDialogs(reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public float getAnimationScale(int which) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(which);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAnimationScale(which);
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public float[] getAnimationScales() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAnimationScales();
                    }
                    _reply.readException();
                    float[] _result = _reply.createFloatArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setAnimationScale(int which, float scale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(which);
                    _data.writeFloat(scale);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAnimationScale(which, scale);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setAnimationScales(float[] scales) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeFloatArray(scales);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAnimationScales(scales);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public float getCurrentAnimatorScale() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentAnimatorScale();
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setInTouchMode(boolean showFocus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(showFocus ? 1 : 0);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInTouchMode(showFocus);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void showStrictModeViolation(boolean on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(on ? 1 : 0);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showStrictModeViolation(on);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setStrictModeVisualIndicatorPreference(String enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(enabled);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStrictModeVisualIndicatorPreference(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void refreshScreenCaptureDisabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(userId);
                    if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().refreshScreenCaptureDisabled(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    int i = 1;
                    _data.writeInt(alwaysSendConfiguration ? 1 : 0);
                    if (!forceRelayout) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateRotation(alwaysSendConfiguration, forceRelayout);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setRotationLockForBootAnimation(boolean isLock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(isLock ? 1 : 0);
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRotationLockForBootAnimation(isLock);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getDefaultDisplayRotation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultDisplayRotation();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int watchRotation(IRotationWatcher watcher, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().watchRotation(watcher, displayId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void removeRotationWatcher(IRotationWatcher watcher) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeRotationWatcher(watcher);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getPreferredOptionsPanelGravity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredOptionsPanelGravity(displayId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void freezeRotation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(rotation);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().freezeRotation(rotation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void thawRotation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().thawRotation();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isRotationFrozen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRotationFrozen();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void freezeDisplayRotation(int displayId, int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(rotation);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().freezeDisplayRotation(displayId, rotation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void thawDisplayRotation(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().thawDisplayRotation(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isDisplayRotationFrozen(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDisplayRotationFrozen(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public Bitmap screenshotWallpaper() throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().screenshotWallpaper();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean registerWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerWallpaperVisibilityListener(listener, displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void unregisterWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterWallpaperVisibilityListener(listener, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void registerSystemGestureExclusionListener(ISystemGestureExclusionListener listener, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerSystemGestureExclusionListener(listener, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void unregisterSystemGestureExclusionListener(ISystemGestureExclusionListener listener, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterSystemGestureExclusionListener(listener, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean requestAssistScreenshot(IAssistDataReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestAssistScreenshot(receiver);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void statusBarVisibilityChanged(int displayId, int visibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(visibility);
                    if (this.mRemote.transact(61, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().statusBarVisibilityChanged(displayId, visibility);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setForceShowSystemBars(boolean show) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(show ? 1 : 0);
                    if (this.mRemote.transact(62, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setForceShowSystemBars(show);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setRecentsVisibility(boolean visible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(visible ? 1 : 0);
                    if (this.mRemote.transact(63, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setRecentsVisibility(visible);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setPipVisibility(boolean visible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(visible ? 1 : 0);
                    if (this.mRemote.transact(64, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPipVisibility(visible);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setShelfHeight(boolean visible, int shelfHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(visible ? 1 : 0);
                    _data.writeInt(shelfHeight);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setShelfHeight(visible, shelfHeight);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setNavBarVirtualKeyHapticFeedbackEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNavBarVirtualKeyHapticFeedbackEnabled(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean hasNavigationBar(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(67, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasNavigationBar(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getNavBarPosition(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNavBarPosition(displayId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void lockNow(Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockNow(options);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isSafeModeEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(70, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSafeModeEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void enableScreenIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableScreenIfNeeded();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean clearWindowContentFrameStats(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(72, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearWindowContentFrameStats(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public WindowContentFrameStats getWindowContentFrameStats(IBinder token) throws RemoteException {
                WindowContentFrameStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWindowContentFrameStats(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WindowContentFrameStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getDockedStackSide() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDockedStackSide();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setDockedStackDividerTouchRegion(Rect touchableRegion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (touchableRegion != null) {
                        _data.writeInt(1);
                        touchableRegion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(75, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDockedStackDividerTouchRegion(touchableRegion);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void registerDockedStackListener(IDockedStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(76, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerDockedStackListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void registerPinnedStackListener(int displayId, IPinnedStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerPinnedStackListener(displayId, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setResizeDimLayer(boolean visible, int targetWindowingMode, float alpha) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(visible ? 1 : 0);
                    _data.writeInt(targetWindowingMode);
                    _data.writeFloat(alpha);
                    if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setResizeDimLayer(visible, targetWindowingMode, alpha);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(deviceId);
                    if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestAppKeyboardShortcuts(receiver, deviceId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void getStableInsets(int displayId, Rect outInsets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(80, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            outInsets.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getStableInsets(displayId, outInsets);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setForwardedInsets(int displayId, Insets insets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (insets != null) {
                        _data.writeInt(1);
                        insets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(81, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForwardedInsets(displayId, insets);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void registerShortcutKey(long shortcutCode, IShortcutService keySubscriber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeLong(shortcutCode);
                    _data.writeStrongBinder(keySubscriber != null ? keySubscriber.asBinder() : null);
                    if (this.mRemote.transact(82, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerShortcutKey(shortcutCode, keySubscriber);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void createInputConsumer(IBinder token, String name, int displayId, InputChannel inputChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeString(name);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(83, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            inputChannel.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createInputConsumer(token, name, displayId, inputChannel);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean destroyInputConsumer(String name, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(name);
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().destroyInputConsumer(name, displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public Region getCurrentImeTouchRegion() throws RemoteException {
                Region _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentImeTouchRegion();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Region.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void registerDisplayFoldListener(IDisplayFoldListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerDisplayFoldListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void unregisterDisplayFoldListener(IDisplayFoldListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(87, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterDisplayFoldListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void startWindowTrace() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(88, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWindowTrace();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void stopWindowTrace() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(89, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopWindowTrace();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isWindowTraceEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(90, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWindowTraceEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void requestUserActivityNotification() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(91, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestUserActivityNotification();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void dontOverrideDisplayInfo(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(92, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dontOverrideDisplayInfo(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(93, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerOppoWindowStateObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(94, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterOppoWindowStateObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isInFreeformMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(95, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInFreeformMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void getFreeformStackBounds(Rect outBounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(96, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            outBounds.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getFreeformStackBounds(outBounds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getWindowingMode(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(97, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWindowingMode(displayId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setWindowingMode(int displayId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(98, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWindowingMode(displayId, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getRemoveContentMode(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(99, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemoveContentMode(displayId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setRemoveContentMode(int displayId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(100, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRemoveContentMode(displayId, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean shouldShowWithInsecureKeyguard(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldShowWithInsecureKeyguard(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setShouldShowWithInsecureKeyguard(int displayId, boolean shouldShow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(shouldShow ? 1 : 0);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setShouldShowWithInsecureKeyguard(displayId, shouldShow);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean shouldShowSystemDecors(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldShowSystemDecors(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setShouldShowSystemDecors(int displayId, boolean shouldShow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(shouldShow ? 1 : 0);
                    if (this.mRemote.transact(104, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setShouldShowSystemDecors(displayId, shouldShow);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean shouldShowIme(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(105, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldShowIme(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void setShouldShowIme(int displayId, boolean shouldShow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(shouldShow ? 1 : 0);
                    if (this.mRemote.transact(106, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setShouldShowIme(displayId, shouldShow);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean injectInputAfterTransactionsApplied(InputEvent ev, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    boolean _result = true;
                    if (ev != null) {
                        _data.writeInt(1);
                        ev.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(107, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().injectInputAfterTransactionsApplied(ev, mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public void syncInputTransactions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (this.mRemote.transact(108, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().syncInputTransactions();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public boolean isActivityNeedPalette(String pkg, String activityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(pkg);
                    _data.writeString(activityName);
                    boolean _result = false;
                    if (!this.mRemote.transact(109, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isActivityNeedPalette(pkg, activityName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getNavBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(pkg);
                    _data.writeString(activityName);
                    if (!this.mRemote.transact(110, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNavBarColorFromAdaptation(pkg, activityName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getStatusBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(pkg);
                    _data.writeString(activityName);
                    if (!this.mRemote.transact(111, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStatusBarColorFromAdaptation(pkg, activityName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getImeBgColorFromAdaptation(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(112, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImeBgColorFromAdaptation(pkg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getTypedWindowLayer(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(type);
                    if (!this.mRemote.transact(113, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTypedWindowLayer(type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public int getFocusedWindowIgnoreHomeMenuKey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(114, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFocusedWindowIgnoreHomeMenuKey();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.view.IWindowManager
            public Bitmap captureLayersForKgd() throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!this.mRemote.transact(115, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureLayersForKgd();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWindowManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWindowManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
