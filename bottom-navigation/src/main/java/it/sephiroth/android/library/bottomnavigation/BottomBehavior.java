package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar.SnackbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Interpolator;

import java.util.HashMap;

import it.sephiroth.android.library.bottonnavigation.R;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
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

    final HashMap<View, DependentView> dependentViewHashMap = new HashMap<>();
    FabDependentView fabDependentView;
    SnackBarDependentView snackbarDependentView;
    private OnExpandStatusChangeListener listener;

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

    protected boolean isFloatingActionButton(View dependency) {
        return dependency instanceof FloatingActionButton;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, BottomNavigation child, View dependency) {
        if (!enabled) {
            return false;
        }

        return isFloatingActionButton(dependency) || SnackbarLayout.class.isInstance(dependency);
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
        log(TAG, ERROR, "onDependentViewRemoved(%s)", dependency.getClass().getSimpleName());

        if (isFloatingActionButton(dependency)) {
            fabDependentView = null;
        } else if (SnackbarLayout.class.isInstance(dependency)) {
            snackbarDependentView = null;
            if (null != fabDependentView) {
                fabDependentView.onDependentViewChanged(parent, child);
            }
        }

        final DependentView dependent = dependentViewHashMap.remove(dependency);
        log(TAG, ERROR, "removed: %s", dependent);
        if (null != dependent) {
            dependent.onDestroy();
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigation child, View dependency) {
        boolean isFab = isFloatingActionButton(dependency);
        boolean isSnackBack = SnackbarLayout.class.isInstance(dependency);

        DependentView dependent;

        if (!dependentViewHashMap.containsKey(dependency)) {
            if (!isFab && !isSnackBack) {
                dependent = new GenericDependentView(dependency, height, bottomInset);
            } else if (isFab) {
                dependent = new FabDependentView(dependency, height, bottomInset);
                fabDependentView = (FabDependentView) dependent;
            } else {
                dependent = new SnackBarDependentView((SnackbarLayout) dependency, height, bottomInset);
                snackbarDependentView = (SnackBarDependentView) dependent;
            }
            dependentViewHashMap.put(dependency, dependent);
        } else {
            dependent = dependentViewHashMap.get(dependency);
        }

        if (null != dependent) {
            return dependent.onDependentViewChanged(parent, child);
        }

        return true;
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

        offset += dy;

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
            if (null != fabDependentView) {
                fabDependentView.onDependentViewChanged(coordinatorLayout, child);
            }
        }
    }

    private void ensureOrCancelAnimator(final CoordinatorLayout coordinatorLayout, final BottomNavigation child) {
        if (animator == null) {
            animator = ViewCompat.animate(child);
            animator.setDuration(animationDuration);
            animator.setInterpolator(INTERPOLATOR);
            animator.setUpdateListener(new ViewPropertyAnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final View view) {
                    if (null != fabDependentView) {
                        fabDependentView.onDependentViewChanged(coordinatorLayout, child);
                    }
                    if (null != snackbarDependentView) {
                        snackbarDependentView.onDependentViewChanged(coordinatorLayout, child);
                    }
                }
            });
        } else {
            animator.cancel();
        }
    }

    abstract static class DependentView<V extends View> {
        final V child;
        final MarginLayoutParams layoutParams;
        final int bottomMargin;
        final int height;
        final int bottomInset;

        DependentView(V child, final int height, final int bottomInset) {
            this.child = child;
            this.layoutParams = (MarginLayoutParams) child.getLayoutParams();
            this.bottomMargin = layoutParams.bottomMargin;
            this.height = height;
            this.bottomInset = bottomInset;
        }

        void onDestroy() { }

        abstract boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigation navigation);
    }

    static class GenericDependentView extends DependentView<View> {
        private static final String TAG = BottomBehavior.TAG + "." + GenericDependentView.class.getSimpleName();

        GenericDependentView(final View child, final int height, final int bottomInset) {
            super(child, height, bottomInset);
            log(TAG, INFO, "new GenericDependentView(%s)", child.getClass().getSimpleName());
        }

        @Override
        void onDestroy() { }

        @Override
        boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            log(TAG, VERBOSE, "onDependentViewChanged");
            layoutParams.bottomMargin = bottomMargin + height;
            return true;
        }
    }

    private static class FabDependentView extends DependentView<View> {
        private static final String TAG = BottomBehavior.TAG + "." + FabDependentView.class.getSimpleName();

        FabDependentView(final View child, final int height, final int bottomInset) {
            super(child, height, bottomInset);
            log(TAG, INFO, "new FabDependentView");
        }

        @Override
        boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            final float t = Math.max(0, navigation.getTranslationY() - height);

            if (bottomInset > 0) {
                layoutParams.bottomMargin = (int) (bottomMargin + height - t);
            } else {
                layoutParams.bottomMargin = (int) (bottomMargin + height - navigation.getTranslationY());
            }
            child.requestLayout();
            return true;
        }

        @Override
        void onDestroy() { }
    }

    private static class SnackBarDependentView extends DependentView<SnackbarLayout> {
        private static final String TAG = BottomBehavior.TAG + "." + SnackBarDependentView.class.getSimpleName();
        private int snackbarHeight = -1;

        SnackBarDependentView(final SnackbarLayout child, final int height, final int bottomInset) {
            super(child, height, bottomInset);
        }

        @Override
        boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            log(TAG, VERBOSE, "onDependentViewChanged");

            if (Build.VERSION.SDK_INT < 21) {
                int index1 = parent.indexOfChild(child);
                int index2 = parent.indexOfChild(navigation);
                if (index1 > index2) {
                    log(TAG, WARN, "swapping children");
                    navigation.bringToFront();
                }
            }

            //scrollEnabled = false;
            final boolean expanded = navigation.getTranslationY() == 0;
            if (snackbarHeight == -1) {
                snackbarHeight = child.getHeight();
            }

            log(TAG, VERBOSE, "snack.height:%d, nav.height: %d, snack.y:%g, nav.y:%g, expanded: %b",
                snackbarHeight,
                height,
                child.getTranslationY(),
                navigation.getTranslationY(),
                expanded
            );

            final float maxScroll = Math.max(0, navigation.getTranslationY() - bottomInset);
            layoutParams.bottomMargin = (int) (height - maxScroll);
            child.requestLayout();

            return true;
        }

        @Override
        void onDestroy() {
            log(TAG, INFO, "onDestroy");
            //scrollEnabled = true;
        }
    }

    public interface OnExpandStatusChangeListener {
        void onExpandStatusChanged(boolean expanded, final boolean animate);
    }
}