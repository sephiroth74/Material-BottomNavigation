package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar.SnackbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Interpolator;

import it.sephiroth.android.library.bottonnavigation.R;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;

import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static android.util.Log.WARN;
import static it.sephiroth.android.library.bottomnavigation.BottomNavigation.PENDING_ACTION_ANIMATE_ENABLED;
import static it.sephiroth.android.library.bottomnavigation.BottomNavigation.PENDING_ACTION_NONE;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

/**
 * Created by alessandro crugnola on 4/2/16.
 * alessandro.crugnola@gmail.com
 */
@Keep
@KeepClassMembers
public class BottomBehavior extends VerticalScrollingBehavior<BottomNavigation> {
    private static final String TAG = BottomBehavior.class.getSimpleName();

    private boolean scrollable;
    private boolean scrollEnabled;
    private boolean enabled;

    /**
     * default hide/show interpolator
     */
    private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();

    /**
     * show/hide animation duration
     */
    private final int animationDuration;

    /**
     * bottom inset when TRANSLUCENT_NAVIGATION is turned on
     */
    private int bottomInset;

    /**
     * bottom navigation real height
     */
    private int height;

    /**
     * maximum scroll offset
     */
    private int maxOffset;

    /**
     * true if the current configuration has the TRANSLUCENT_NAVIGATION turned on
     */
    private boolean translucentNavigation;

    /**
     * Minimum touch distance
     */
    private final int scaledTouchSlop;

    /**
     * hide/show animator
     */
    private ViewPropertyAnimatorCompat animator;

    /**
     * current visibility status
     */
    private boolean hidden = false;

    /**
     * current Y offset
     */
    private int offset;

    private OnExpandStatusChangeListener listener;

    protected SnackBarDependentView snackbarDependentView;

    public BottomBehavior() {
        this(null, null);
    }

    public BottomBehavior(final Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationBehavior);
        this.scrollable = array.getBoolean(R.styleable.BottomNavigationBehavior_bbn_scrollEnabled, true);
        this.scrollEnabled = true;
        this.animationDuration = array.getInt(
            R.styleable.BottomNavigationBehavior_bbn_animationDuration,
            context.getResources().getInteger(R.integer.bbn_hide_animation_duration)
        );
        this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
        this.offset = 0;
        array.recycle();

        log(TAG, DEBUG, "scrollable: %b, duration: %d, touchSlop: %d", scrollable, animationDuration, scaledTouchSlop);
    }

    public void setOnExpandStatusChangeListener(final OnExpandStatusChangeListener listener) {
        this.listener = listener;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(final boolean scrollable) {
        this.scrollable = scrollable;
    }

    public boolean isExpanded() {
        return !hidden;
    }

    public void setLayoutValues(final int bottomNavHeight, final int bottomInset) {
        log(TAG, INFO, "setLayoutValues(%d, %d)", bottomNavHeight, bottomInset);
        this.height = bottomNavHeight;
        this.bottomInset = bottomInset;
        this.translucentNavigation = bottomInset > 0;
        this.maxOffset = height + (translucentNavigation ? bottomInset : 0);
        this.enabled = true;
        log(
            TAG, DEBUG, "height: %d, translucent: %b, maxOffset: %d, bottomInset: %d", height, translucentNavigation, maxOffset,
            bottomInset
        );
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, BottomNavigation child, View dependency) {
        log(TAG, INFO, "layoutDependsOn: %s", dependency);

        if (!enabled) {
            return false;
        }
        return isSnackbar(dependency);
    }

    private boolean isSnackbar(@NonNull final View view) {
        return SnackbarLayout.class.isInstance(view);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, BottomNavigation abl, int layoutDirection) {
        boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

        final int pendingAction = abl.getPendingAction();
        if (pendingAction != PENDING_ACTION_NONE) {
            final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
            if ((pendingAction & BottomNavigation.PENDING_ACTION_COLLAPSED) != 0) {
                setExpanded(parent, abl, false, animate);
            } else {
                if ((pendingAction & BottomNavigation.PENDING_ACTION_EXPANDED) != 0) {
                    setExpanded(parent, abl, true, animate);
                }
            }
            // Finally reset the pending state
            abl.resetPendingAction();
        }

        return handled;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, BottomNavigation child, View dependency) {
        if (isSnackbar(dependency)) {
            if (null != snackbarDependentView) {
                snackbarDependentView.onDestroy();
            }
            snackbarDependentView = null;
        }
    }

    @Override
    public boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation child, View dependency) {
        if (isSnackbar(dependency)) {
            if (null == snackbarDependentView) {
                snackbarDependentView = new SnackBarDependentView((SnackbarLayout) dependency, height, bottomInset);
            }
            return snackbarDependentView.onDependentViewChanged(parent, child);
        }
        return super.onDependentViewChanged(parent, child, dependency);
    }

    @Override
    public boolean onStartNestedScroll(
        final CoordinatorLayout coordinatorLayout,
        final BottomNavigation child,
        final View directTargetChild, final View target,
        final int nestedScrollAxes) {

        offset = 0;
        if (!scrollable || !scrollEnabled) {
            return false;
        }

        if ((nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0) {
            log(
                TAG, INFO, "isScrollContainer: %b, canScrollUp: %b, canScrollDown: %b", target.isScrollContainer(),
                target.canScrollVertically(-1), target.canScrollVertically(1)
            );
            if (target.isScrollContainer() && (!target.canScrollVertically(-1) && !target.canScrollVertically(1))) {
                return false;
            }
        }

        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout coordinatorLayout, final BottomNavigation child, final View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
        offset = 0;
    }

    @Override
    public void onDirectionNestedPreScroll(
        CoordinatorLayout coordinatorLayout,
        BottomNavigation child,
        View target, int dx, int dy, int[] consumed,
        @ScrollDirection int scrollDirection) {

        // stop nested scroll if target is not scrollable
        // FIXME: not yet verified
        if (target.isScrollContainer() && !target.canScrollVertically(scrollDirection)) {
            log(TAG, WARN, "stopNestedScroll");
            ViewCompat.stopNestedScroll(target);
        }

        offset += dy;

        if (BottomNavigation.DEBUG) {
            log(
                TAG, INFO, "onDirectionNestedPreScroll(%d, %s, %b)", scrollDirection, target,
                target.canScrollVertically(scrollDirection)
            );
        }

        if (offset > scaledTouchSlop) {
            handleDirection(coordinatorLayout, child, ScrollDirection.SCROLL_DIRECTION_UP);
            offset = 0;
        } else if (offset < -scaledTouchSlop) {
            handleDirection(coordinatorLayout, child, ScrollDirection.SCROLL_DIRECTION_DOWN);
            offset = 0;
        }
    }

    @Override
    protected boolean onNestedDirectionFling(
        CoordinatorLayout coordinatorLayout, BottomNavigation child, View target, float velocityX, float velocityY,
        @ScrollDirection int scrollDirection) {
        log(TAG, INFO, "onNestedDirectionFling(%g, %d)", velocityY, scrollDirection);

        if (Math.abs(velocityY) > 1000) {
            handleDirection(coordinatorLayout, child, scrollDirection);
        }

        return true;
    }

    @Override
    public void onNestedScroll(
        final CoordinatorLayout coordinatorLayout, final BottomNavigation child, final View target, final int dxConsumed,
        final int dyConsumed, final int dxUnconsumed,
        final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(
        final CoordinatorLayout coordinatorLayout, final BottomNavigation child, final View target, final int dx, final int dy,
        final int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    }

    @Override
    public void onNestedVerticalOverScroll(
        CoordinatorLayout coordinatorLayout, BottomNavigation child, @ScrollDirection int direction, int currentOverScroll,
        int totalOverScroll) {
    }

    private void handleDirection(final CoordinatorLayout coordinatorLayout, BottomNavigation child, int scrollDirection) {
        if (!enabled || !scrollable || !scrollEnabled) {
            return;
        }
        if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_DOWN && hidden) {
            setExpanded(coordinatorLayout, child, true, true);
        } else if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_UP && !hidden) {
            setExpanded(coordinatorLayout, child, false, true);
        }
    }

    protected void setExpanded(
        final CoordinatorLayout coordinatorLayout, final BottomNavigation child, boolean expanded, boolean animate) {
        log(TAG, INFO, "setExpanded(%b)", expanded);
        animateOffset(coordinatorLayout, child, expanded ? 0 : maxOffset, animate);
        if (null != listener) {
            listener.onExpandStatusChanged(expanded, animate);
        }
    }

    private void animateOffset(
        final CoordinatorLayout coordinatorLayout,
        final BottomNavigation child,
        final int offset,
        final boolean animate) {

        log(TAG, INFO, "animateOffset(%d)", offset);
        hidden = offset != 0;
        ensureOrCancelAnimator(coordinatorLayout, child);

        if (animate) {
            animator.translationY(offset).start();
        } else {
            child.setTranslationY(offset);
        }
    }

    private void ensureOrCancelAnimator(final CoordinatorLayout coordinatorLayout, final BottomNavigation child) {
        if (animator == null) {
            animator = ViewCompat.animate(child);
            animator.setDuration(animationDuration);
            animator.setInterpolator(INTERPOLATOR);
        } else {
            animator.cancel();
        }
    }

    public abstract static class DependentView<V extends View> {
        protected final V child;
        protected final MarginLayoutParams layoutParams;
        protected final int bottomMargin;
        protected int height;
        protected final int bottomInset;
        protected final float originalPosition;

        DependentView(V child, final int height, final int bottomInset) {
            this.child = child;
            this.originalPosition = child.getTranslationY();
            this.layoutParams = (MarginLayoutParams) child.getLayoutParams();
            this.bottomMargin = layoutParams.bottomMargin;
            this.height = height;
            this.bottomInset = bottomInset;
        }

        protected void onDestroy() {
            layoutParams.bottomMargin = bottomMargin;
            child.setTranslationY(originalPosition);
            child.requestLayout();
        }

        abstract boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigation navigation);
    }

    public static class GenericDependentView extends DependentView<View> {
        private static final String TAG = BottomBehavior.TAG + "." + GenericDependentView.class.getSimpleName();

        GenericDependentView(final View child, final int height, final int bottomInset) {
            super(child, height, bottomInset);
        }

        @Override
        protected boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            return true;
        }
    }

    private static class SnackBarDependentView extends DependentView<SnackbarLayout> {
        private static final String TAG = BottomBehavior.TAG + "." + SnackBarDependentView.class.getSimpleName();
        private int snackbarHeight = -1;

        SnackBarDependentView(final SnackbarLayout child, final int height, final int bottomInset) {
            super(child, height, bottomInset);
        }

        @Override
        boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            log(TAG, INFO, "onDependentViewChanged");

            if (Build.VERSION.SDK_INT < 21) {
                int index1 = parent.indexOfChild(child);
                int index2 = parent.indexOfChild(navigation);
                if (index1 > index2) {
                    log(TAG, WARN, "swapping children");
                    navigation.bringToFront();
                }
            }

            if (snackbarHeight == -1) {
                snackbarHeight = child.getHeight();
            }

            final float maxScroll = Math.max(0, navigation.getTranslationY() - bottomInset);
            final int newBottomMargin = (int) (height - maxScroll);

            if (layoutParams.bottomMargin != newBottomMargin) {
                layoutParams.bottomMargin = newBottomMargin;
                child.requestLayout();
                return true;
            }
            return false;
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            //scrollEnabled = true;
        }
    }

    public interface OnExpandStatusChangeListener {
        void onExpandStatusChanged(boolean expanded, final boolean animate);
    }
}