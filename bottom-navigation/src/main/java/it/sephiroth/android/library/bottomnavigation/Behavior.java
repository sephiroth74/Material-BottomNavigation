package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar.SnackbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
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
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

/**
 * Created by alessandro on 4/2/16.
 */
@Keep
@KeepClassMembers
public class Behavior<V extends View> extends VerticalScrollingBehavior<BottomNavigation> {
    private static final String TAG = Behavior.class.getSimpleName();

    private boolean scrollable;
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
     * hide/show is enabled
     */
    private boolean scrollEnabled = true;

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

    //    private final LollipopBottomNavWithSnackBarImpl mWithSnackBarImpl = new LollipopBottomNavWithSnackBarImpl();

    private final HashMap<View, DependentView> dependentViewHashMap = new HashMap<>();
    private SnackBarDependentView snackBarDependentView;
    private FabDependentView fabDependentView;

    public Behavior() {
        this(null, null);
    }

    public Behavior(final Context context, AttributeSet attrs) {
        super(context, attrs);
        log(TAG, INFO, "ctor(attrs:%s)", attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationBehavior);
        this.scrollable = array.getBoolean(R.styleable.BottomNavigationBehavior_bbn_scrollEnabled, true);
        this.animationDuration = array.getInt(
            R.styleable.BottomNavigationBehavior_bbn_animationDuration,
            context.getResources().getInteger(R.integer.bbn_hide_animation_duration)
        );
        this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
        this.offset = 0;
        array.recycle();

        log(TAG, DEBUG, "scrollable: %b, duration: %d, touchSlop: %d", scrollable, animationDuration, scaledTouchSlop);
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
        if (!enabled) {
            return false;
        }

        // log(TAG, INFO, "layoutDependsOn: %s", dependency.getClass().getSimpleName());

        return RecyclerView.class.isInstance(dependency)
            || FloatingActionButton.class.isInstance(dependency)
            || SnackbarLayout.class.isInstance(dependency);

        // return !AppBarLayout.class.isInstance(dependency) && !Toolbar.class.isInstance(dependency);
        // return false;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, BottomNavigation child, View dependency) {
        log(TAG, ERROR, "onDependentViewRemoved(%s)", dependency.getClass().getSimpleName());
        //        Log.i(TAG, "onDependentViewRemoved: " + dependency);
        //        updateScrollingForSnackbar(dependency, true);
        //        super.onDependentViewRemoved(parent, child, dependency);

        if (FloatingActionButton.class.isInstance(dependency)) {
            fabDependentView = null;
        } else if (SnackbarLayout.class.isInstance(dependency)) {
            snackBarDependentView = null;

            if (null != fabDependentView) {
                fabDependentView.onDependentViewChanged(parent, child);
            }
        }

        dependentViewHashMap.remove(dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigation child, View dependency) {
        log(TAG, WARN, "onDependentViewChanged(%s)", dependency.getClass().getSimpleName());

        boolean isFab = FloatingActionButton.class.isInstance(dependency);
        boolean isSnackBack = SnackbarLayout.class.isInstance(dependency);

        DependentView dependent = null;

        if (!dependentViewHashMap.containsKey(dependency)) {
            if (!isFab && !isSnackBack) {
                dependent = new GenericDependentView(dependency);
            } else if (isFab) {
                dependent = new FabDependentView((FloatingActionButton) dependency);
                fabDependentView = (FabDependentView) dependent;
            } else {
                dependent = new SnackBarDependentView((SnackbarLayout) dependency);
                snackBarDependentView = (SnackBarDependentView) dependent;
            }
            dependentViewHashMap.put(dependency, dependent);
        } else {
            dependent = dependentViewHashMap.get(dependency);
        }

        if (null != dependent) {
            return dependent.onDependentViewChanged(parent, child);
        }

        return true;

        //
        //        final MarginLayoutParams layoutParams = (MarginLayoutParams) dependency.getLayoutParams();
        //        final int originalBottomMargin;
        //
        //        if (dependencyLayoutMap.containsKey(dependency)) {
        //            originalBottomMargin = dependencyLayoutMap.get(dependency);
        //        } else {
        //            originalBottomMargin = layoutParams.bottomMargin;
        //            dependencyLayoutMap.put(dependency, originalBottomMargin);
        //        }
        //
        //        log(TAG, VERBOSE, "original bottomMargin: %d", originalBottomMargin);
        //
        //        if (!translucentNavigation) {
        //            //            layoutParams.bottomMargin = originalBottomMargin + height;
        //            log(TAG, VERBOSE, "bottomMargin: %d", layoutParams.bottomMargin);
        //
        //            if (Snackbar.SnackbarLayout.class.isInstance(dependency)) {
        //                mWithSnackBarImpl.updateSnackbar(parent, child, dependency);
        //            }
        //
        //            return true;
        //        } else {
        //            if (Snackbar.SnackbarLayout.class.isInstance(dependency)) {
        //                Log.d(TAG, "SnackBar");
        //                //                layoutParams.bottomMargin = originalBottomMargin + height + bottomInset + 300;
        //                mWithSnackBarImpl.updateSnackbar(parent, child, dependency);
        //                return true;
        //            } else if (FloatingActionButton.class.isInstance(dependency)) {
        //                Log.d(TAG, "FloatingActionButton");
        //                layoutParams.bottomMargin = originalBottomMargin + height;
        //                return true;
        //            }
        //
        //            return false;
        //        }
    }

    private void updateScrollingForSnackbar(View dependency, boolean enabled) {
        if (dependency instanceof SnackbarLayout) {
            scrollEnabled = enabled;
        }
    }

    @Override
    public boolean onStartNestedScroll(
        final CoordinatorLayout coordinatorLayout,
        final BottomNavigation child,
        final View directTargetChild, final View target,
        final int nestedScrollAxes) {

        offset = 0;
        if (!scrollable) {
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
            handleDirection(child, ScrollDirection.SCROLL_DIRECTION_UP);
            offset = 0;
        } else if (offset < -scaledTouchSlop) {
            handleDirection(child, ScrollDirection.SCROLL_DIRECTION_DOWN);
            offset = 0;
        }
    }

    @Override
    protected boolean onNestedDirectionFling(
        CoordinatorLayout coordinatorLayout, BottomNavigation child, View target, float velocityX, float velocityY,
        @ScrollDirection int scrollDirection) {
        return true;
    }

    @Override
    public void onNestedVerticalOverScroll(
        CoordinatorLayout coordinatorLayout, BottomNavigation child, @ScrollDirection int direction, int currentOverScroll,
        int totalOverScroll) {
    }

    private void handleDirection(BottomNavigation child, int scrollDirection) {
        if (!scrollEnabled) {
            return;
        }
        if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_DOWN && hidden) {
            hidden = false;
            animateOffset(child, 0);
        } else if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_UP && !hidden) {
            hidden = true;
            animateOffset(child, maxOffset);
        }
    }

    private void animateOffset(final BottomNavigation child, final int offset) {
        ensureOrCancelAnimator(child);
        animator.translationY(offset).start();
    }

    private void ensureOrCancelAnimator(BottomNavigation child) {
        if (animator == null) {
            animator = ViewCompat.animate(child);
            animator.setDuration(animationDuration);
            animator.setInterpolator(INTERPOLATOR);
        } else {
            animator.cancel();
        }
    }

    private abstract class DependentView<V extends View> {
        final V child;
        final MarginLayoutParams layoutParams;
        final int bottomMargin;

        DependentView(V child) {
            this.child = child;
            this.layoutParams = (MarginLayoutParams) child.getLayoutParams();
            this.bottomMargin = layoutParams.bottomMargin;
        }

        abstract boolean onDependentViewChanged(CoordinatorLayout parent, BottomNavigation navigation);
    }

    private class GenericDependentView extends DependentView<View> {
        final String TAG = GenericDependentView.class.getSimpleName();

        GenericDependentView(final View child) {
            super(child);
            log(TAG, INFO, "new GenericDependentView(%s)", child.getClass().getSimpleName());
        }

        @Override
        boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            log(TAG, VERBOSE, "onDependentViewChanged");
            layoutParams.bottomMargin = bottomMargin + height;
            return true;
        }
    }

    private class FabDependentView extends DependentView<FloatingActionButton> {
        final String TAG = FabDependentView.class.getSimpleName();

        FabDependentView(final FloatingActionButton child) {
            super(child);
            log(TAG, INFO, "new FabDependentView");
        }

        @Override
        boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            log(TAG, VERBOSE, "onDependentViewChanged");
            layoutParams.bottomMargin = bottomMargin + height;
            child.setLayoutParams(layoutParams);
            return true;
        }
    }

    private class SnackBarDependentView extends DependentView<SnackbarLayout> {
        final String TAG = SnackBarDependentView.class.getSimpleName();
        private int snackbarHeight = -1;

        SnackBarDependentView(final SnackbarLayout child) {
            super(child);
            log(TAG, INFO, "new SnackBarDependentView");
        }

        public void updateSnackbar(CoordinatorLayout parent, BottomNavigation child, View dependency) {
            log(TAG, DEBUG, "updateSnackBar");

            boolean expanded = child.getTranslationY() == 0;
            if (snackbarHeight == -1) {
                snackbarHeight = dependency.getPaddingBottom();
            }
            if (translucentNavigation) {

                dependency.setPadding(dependency.getPaddingLeft(),
                    dependency.getPaddingTop(), dependency.getPaddingRight(),
                    snackbarHeight + bottomInset + (expanded ? height : 0)
                );
                ((MarginLayoutParams) dependency.getLayoutParams()).bottomMargin -= bottomInset;
            } else {
                dependency.setPadding(dependency.getPaddingLeft(),
                    dependency.getPaddingTop(), dependency.getPaddingRight(),
                    snackbarHeight + bottomInset + (expanded ? height : 0)
                );
            }
        }

        @Override
        boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
            log(TAG, VERBOSE, "onDependentViewChanged");

            final boolean expanded = navigation.getTranslationY() == 0;
            if (snackbarHeight == -1) {
                snackbarHeight = child.getHeight();
            }

            log(TAG, VERBOSE, "snackbarheight: %d, height: %d, translationY: %g, expanded: %b",
                snackbarHeight,
                height,
                child.getTranslationY(),
                expanded
            );

            //            child.setPadding(
            //                child.getPaddingLeft(),
            //                child.getPaddingTop(),
            //                child.getPaddingRight(),
            //                snackbarHeight + bottomInset + (expanded ? height : 0)
            //            );

            layoutParams.bottomMargin = expanded ? height : 0;
            child.setLayoutParams(layoutParams);

            return true;
        }
    }
}