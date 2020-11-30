package com.android.internal.widget;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import com.android.internal.R;
import com.android.internal.widget.SwipeDismissLayout;

public class SwipeDismissLayout extends FrameLayout {
    private static final float MAX_DIST_THRESHOLD = 0.33f;
    private static final float MIN_DIST_THRESHOLD = 0.1f;
    private static final String TAG = "SwipeDismissLayout";
    private int mActiveTouchId;
    private boolean mActivityTranslucencyConverted = false;
    private boolean mBlockGesture = false;
    private boolean mDiscardIntercept;
    private final DismissAnimator mDismissAnimator = new DismissAnimator();
    private boolean mDismissable = true;
    private boolean mDismissed;
    private OnDismissedListener mDismissedListener;
    private float mDownX;
    private float mDownY;
    private boolean mIsWindowNativelyTranslucent;
    private float mLastX;
    private int mMinFlingVelocity;
    private OnSwipeProgressChangedListener mProgressListener;
    private IntentFilter mScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    private BroadcastReceiver mScreenOffReceiver;
    private int mSlop;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;

    public interface OnDismissedListener {
        void onDismissed(SwipeDismissLayout swipeDismissLayout);
    }

    public interface OnSwipeProgressChangedListener {
        void onSwipeCancelled(SwipeDismissLayout swipeDismissLayout);

        void onSwipeProgressChanged(SwipeDismissLayout swipeDismissLayout, float f, float f2);
    }

    public SwipeDismissLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        this.mSlop = vc.getScaledTouchSlop();
        this.mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        TypedArray a = context.getTheme().obtainStyledAttributes(R.styleable.Theme);
        this.mIsWindowNativelyTranslucent = a.getBoolean(5, false);
        a.recycle();
    }

    public void setOnDismissedListener(OnDismissedListener listener) {
        this.mDismissedListener = listener;
    }

    public void setOnSwipeProgressChangedListener(OnSwipeProgressChangedListener listener) {
        this.mProgressListener = listener;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            this.mScreenOffReceiver = new BroadcastReceiver() {
                /* class com.android.internal.widget.SwipeDismissLayout.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    SwipeDismissLayout.this.post(new Runnable() {
                        /* class com.android.internal.widget.$$Lambda$SwipeDismissLayout$1$NDXsqpv65OVP2OutTHthDxJywg */

                        public final void run() {
                            SwipeDismissLayout.AnonymousClass1.this.lambda$onReceive$0$SwipeDismissLayout$1();
                        }
                    });
                }

                public /* synthetic */ void lambda$onReceive$0$SwipeDismissLayout$1() {
                    if (SwipeDismissLayout.this.mDismissed) {
                        SwipeDismissLayout.this.dismiss();
                    } else {
                        SwipeDismissLayout.this.cancel();
                    }
                    SwipeDismissLayout.this.resetMembers();
                }
            };
            getContext().registerReceiver(this.mScreenOffReceiver, this.mScreenOffFilter);
        } catch (ReceiverCallNotAllowedException e) {
            this.mScreenOffReceiver = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        if (this.mScreenOffReceiver != null) {
            getContext().unregisterReceiver(this.mScreenOffReceiver);
            this.mScreenOffReceiver = null;
        }
        super.onDetachedFromWindow();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        checkGesture(ev);
        if (this.mBlockGesture) {
            return true;
        }
        if (!this.mDismissable) {
            return super.onInterceptTouchEvent(ev);
        }
        ev.offsetLocation(ev.getRawX() - ev.getX(), 0.0f);
        int actionMasked = ev.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked != 2) {
                    if (actionMasked != 3) {
                        if (actionMasked == 5) {
                            this.mActiveTouchId = ev.getPointerId(ev.getActionIndex());
                        } else if (actionMasked == 6) {
                            int actionIndex = ev.getActionIndex();
                            if (ev.getPointerId(actionIndex) == this.mActiveTouchId) {
                                this.mActiveTouchId = ev.getPointerId(actionIndex == 0 ? 1 : 0);
                            }
                        }
                    }
                } else if (this.mVelocityTracker != null && !this.mDiscardIntercept) {
                    int pointerIndex = ev.findPointerIndex(this.mActiveTouchId);
                    if (pointerIndex == -1) {
                        Log.e(TAG, "Invalid pointer index: ignoring.");
                        this.mDiscardIntercept = true;
                    } else {
                        float dx = ev.getRawX() - this.mDownX;
                        float x = ev.getX(pointerIndex);
                        float y = ev.getY(pointerIndex);
                        if (dx == 0.0f || !canScroll(this, false, dx, x, y)) {
                            updateSwiping(ev);
                        } else {
                            this.mDiscardIntercept = true;
                        }
                    }
                }
            }
            resetMembers();
        } else {
            resetMembers();
            this.mDownX = ev.getRawX();
            this.mDownY = ev.getRawY();
            this.mActiveTouchId = ev.getPointerId(0);
            this.mVelocityTracker = VelocityTracker.obtain("int1");
            this.mVelocityTracker.addMovement(ev);
        }
        if (this.mDiscardIntercept || !this.mSwiping) {
            return false;
        }
        return true;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        checkGesture(ev);
        if (this.mBlockGesture) {
            return true;
        }
        if (this.mVelocityTracker == null || !this.mDismissable) {
            return super.onTouchEvent(ev);
        }
        ev.offsetLocation(ev.getRawX() - ev.getX(), 0.0f);
        int actionMasked = ev.getActionMasked();
        if (actionMasked == 1) {
            updateDismiss(ev);
            if (this.mDismissed) {
                this.mDismissAnimator.animateDismissal(ev.getRawX() - this.mDownX);
            } else if (this.mSwiping && this.mLastX != -2.14748365E9f) {
                this.mDismissAnimator.animateRecovery(ev.getRawX() - this.mDownX);
            }
            resetMembers();
        } else if (actionMasked == 2) {
            this.mVelocityTracker.addMovement(ev);
            this.mLastX = ev.getRawX();
            updateSwiping(ev);
            if (this.mSwiping) {
                setProgress(ev.getRawX() - this.mDownX);
            }
        } else if (actionMasked == 3) {
            cancel();
            resetMembers();
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setProgress(float deltaX) {
        OnSwipeProgressChangedListener onSwipeProgressChangedListener = this.mProgressListener;
        if (onSwipeProgressChangedListener != null && deltaX >= 0.0f) {
            onSwipeProgressChangedListener.onSwipeProgressChanged(this, progressToAlpha(deltaX / ((float) getWidth())), deltaX);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismiss() {
        OnDismissedListener onDismissedListener = this.mDismissedListener;
        if (onDismissedListener != null) {
            onDismissedListener.onDismissed(this);
        }
    }

    /* access modifiers changed from: protected */
    public void cancel() {
        Activity activity;
        if (!this.mIsWindowNativelyTranslucent && (activity = findActivity()) != null && this.mActivityTranslucencyConverted) {
            activity.convertFromTranslucent();
            this.mActivityTranslucencyConverted = false;
        }
        OnSwipeProgressChangedListener onSwipeProgressChangedListener = this.mProgressListener;
        if (onSwipeProgressChangedListener != null) {
            onSwipeProgressChangedListener.onSwipeCancelled(this);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetMembers() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mVelocityTracker = null;
        this.mDownX = 0.0f;
        this.mLastX = -2.14748365E9f;
        this.mDownY = 0.0f;
        this.mSwiping = false;
        this.mDismissed = false;
        this.mDiscardIntercept = false;
    }

    private void updateSwiping(MotionEvent ev) {
        Activity activity;
        boolean oldSwiping = this.mSwiping;
        if (!this.mSwiping) {
            float deltaX = ev.getRawX() - this.mDownX;
            float deltaY = ev.getRawY() - this.mDownY;
            float f = (deltaX * deltaX) + (deltaY * deltaY);
            int i = this.mSlop;
            boolean z = false;
            if (f > ((float) (i * i))) {
                if (deltaX > ((float) (i * 2)) && Math.abs(deltaY) < Math.abs(deltaX)) {
                    z = true;
                }
                this.mSwiping = z;
            } else {
                this.mSwiping = false;
            }
        }
        if (this.mSwiping && !oldSwiping && !this.mIsWindowNativelyTranslucent && (activity = findActivity()) != null) {
            this.mActivityTranslucencyConverted = activity.convertToTranslucent(null, null);
        }
    }

    private void updateDismiss(MotionEvent ev) {
        float deltaX = ev.getRawX() - this.mDownX;
        this.mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = this.mVelocityTracker.getXVelocity();
        if (this.mLastX == -2.14748365E9f) {
            xVelocity = deltaX / ((float) ((ev.getEventTime() - ev.getDownTime()) / 1000));
        }
        if (!this.mDismissed && ((deltaX > ((float) getWidth()) * Math.max(Math.min(((-0.23000002f * xVelocity) / ((float) this.mMinFlingVelocity)) + MAX_DIST_THRESHOLD, (float) MAX_DIST_THRESHOLD), 0.1f) && ev.getRawX() >= this.mLastX) || xVelocity >= ((float) this.mMinFlingVelocity))) {
            this.mDismissed = true;
        }
        if (this.mDismissed && this.mSwiping && xVelocity < ((float) (-this.mMinFlingVelocity))) {
            this.mDismissed = false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean canScroll(View v, boolean checkV, float dx, float x, float y) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (x + ((float) scrollX) >= ((float) child.getLeft()) && x + ((float) scrollX) < ((float) child.getRight()) && y + ((float) scrollY) >= ((float) child.getTop()) && y + ((float) scrollY) < ((float) child.getBottom()) && canScroll(child, true, dx, (x + ((float) scrollX)) - ((float) child.getLeft()), (y + ((float) scrollY)) - ((float) child.getTop()))) {
                    return true;
                }
            }
        }
        if (checkV) {
            if (v.canScrollHorizontally((int) (-dx))) {
                return true;
            }
        }
        return false;
    }

    public void setDismissable(boolean dismissable) {
        if (!dismissable && this.mDismissable) {
            cancel();
            resetMembers();
        }
        this.mDismissable = dismissable;
    }

    private void checkGesture(MotionEvent ev) {
        if (ev.getActionMasked() == 0) {
            this.mBlockGesture = this.mDismissAnimator.isAnimating();
        }
    }

    private float progressToAlpha(float progress) {
        return 1.0f - ((progress * progress) * progress);
    }

    private Activity findActivity() {
        for (Context context = getContext(); context instanceof ContextWrapper; context = ((ContextWrapper) context).getBaseContext()) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class DismissAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private final long DISMISS_DURATION = 250;
        private final TimeInterpolator DISMISS_INTERPOLATOR = new DecelerateInterpolator(1.5f);
        private final ValueAnimator mDismissAnimator = new ValueAnimator();
        private boolean mDismissOnComplete = false;
        private boolean mWasCanceled = false;

        DismissAnimator() {
            this.mDismissAnimator.addUpdateListener(this);
            this.mDismissAnimator.addListener(this);
        }

        /* access modifiers changed from: package-private */
        public void animateDismissal(float currentTranslation) {
            animate(currentTranslation / ((float) SwipeDismissLayout.this.getWidth()), 1.0f, 250, this.DISMISS_INTERPOLATOR, true);
        }

        /* access modifiers changed from: package-private */
        public void animateRecovery(float currentTranslation) {
            animate(currentTranslation / ((float) SwipeDismissLayout.this.getWidth()), 0.0f, 250, this.DISMISS_INTERPOLATOR, false);
        }

        /* access modifiers changed from: package-private */
        public boolean isAnimating() {
            return this.mDismissAnimator.isStarted();
        }

        private void animate(float from, float to, long duration, TimeInterpolator interpolator, boolean dismissOnComplete) {
            this.mDismissAnimator.cancel();
            this.mDismissOnComplete = dismissOnComplete;
            this.mDismissAnimator.setFloatValues(from, to);
            this.mDismissAnimator.setDuration(duration);
            this.mDismissAnimator.setInterpolator(interpolator);
            this.mDismissAnimator.start();
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            SwipeDismissLayout swipeDismissLayout = SwipeDismissLayout.this;
            swipeDismissLayout.setProgress(((float) swipeDismissLayout.getWidth()) * value);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
            this.mWasCanceled = false;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            this.mWasCanceled = true;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            if (this.mWasCanceled) {
                return;
            }
            if (this.mDismissOnComplete) {
                SwipeDismissLayout.this.dismiss();
            } else {
                SwipeDismissLayout.this.cancel();
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
