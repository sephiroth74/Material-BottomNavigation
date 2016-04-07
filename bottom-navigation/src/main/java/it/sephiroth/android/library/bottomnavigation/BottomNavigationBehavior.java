package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Interpolator;

import java.util.WeakHashMap;

import it.sephiroth.android.library.bottonnavigation.R;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;

import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

/**
 * Created by alessandro on 4/2/16.
 */
@Keep
@KeepClassMembers
public class BottomNavigationBehavior<V extends View> extends VerticalScrollingBehavior<V> {
    private static final String TAG = BottomNavigationBehavior.class.getSimpleName();

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

    private int mSnackbarHeight = -1;

    private final BottomNavigationWithSnackbar mWithSnackBarImpl =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? new LollipopBottomNavWithSnackBarImpl()
            : new PreLollipopBottomNavWithSnackBarImpl();

    private final WeakHashMap<View, Integer> dependencyLayoutMap = new WeakHashMap<>();

    public BottomNavigationBehavior() {
        this(null, null);
    }

    public BottomNavigationBehavior(final Context context, AttributeSet attrs) {
        super(context, attrs);
        log(TAG, INFO, "ctor(attrs:%s)", attrs);

        this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
        this.animationDuration = context.getResources().getInteger(R.integer.bbn_hide_animation_duration);
        this.offset = 0;

    }

    public void setLayoutValues(final int bottomNavHeight, final int bottomInset) {
        log(TAG, INFO, "setLayoutValues(%d, %d)", bottomNavHeight, bottomInset);
        this.height = bottomNavHeight;
        this.bottomInset = bottomInset;
        this.translucentNavigation = bottomInset > 0;
        this.maxOffset = height + (translucentNavigation ? bottomInset : 0);

        log(TAG, VERBOSE, "height: %d, translucent: %b, maxOffset: %d", height, translucentNavigation, maxOffset);

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        //        return !AppBarLayout.class.isInstance(dependency) && !Toolbar.class.isInstance(dependency);
        return false;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, V child, View dependency) {
        //        Log.i(TAG, "onDependentViewRemoved: " + dependency);
        //        updateScrollingForSnackbar(dependency, true);
        //        super.onDependentViewRemoved(parent, child, dependency);
    }

    private void updateScrollingForSnackbar(View dependency, boolean enabled) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            scrollEnabled = enabled;
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        Log.i(TAG, "onDependentViewChanged: " + dependency);

        final MarginLayoutParams layoutParams = (MarginLayoutParams) dependency.getLayoutParams();
        final int originalBottomMargin;

        if (dependencyLayoutMap.containsKey(dependency)) {
            originalBottomMargin = dependencyLayoutMap.get(dependency);
        } else {
            originalBottomMargin = layoutParams.bottomMargin;
            dependencyLayoutMap.put(dependency, originalBottomMargin);
        }

        if (!translucentNavigation) {
            layoutParams.bottomMargin = originalBottomMargin + height;
            return true;
        } else {
            if (Snackbar.SnackbarLayout.class.isInstance(dependency)) {
                Log.d(TAG, "SnackBar");
                //                layoutParams.bottomMargin = originalBottomMargin + height + bottomInset + 300;
                mWithSnackBarImpl.updateSnackbar(parent, child, dependency);
                return true;
            } else if (FloatingActionButton.class.isInstance(dependency)) {
                Log.d(TAG, "FloatingActionButton");
                layoutParams.bottomMargin = originalBottomMargin + height;
                return true;
            }

            return false;
        }
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout coordinatorLayout, final V child, final View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
        offset = 0;
    }

    @Override
    public boolean onStartNestedScroll(
        final CoordinatorLayout coordinatorLayout, final V child, final View directTargetChild, final View target,
        final int nestedScrollAxes) {
        offset = 0;
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onDirectionNestedPreScroll(
        CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed,
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
        CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY,
        @ScrollDirection int scrollDirection) {
        return true;
    }

    @Override
    public void onNestedVerticalOverScroll(
        CoordinatorLayout coordinatorLayout, V child, @ScrollDirection int direction, int currentOverScroll, int totalOverScroll) {
    }

    private void handleDirection(V child, int scrollDirection) {
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

    private void animateOffset(final V child, final int offset) {
        ensureOrCancelAnimator(child);
        animator.translationY(offset).start();
    }

    private void ensureOrCancelAnimator(V child) {
        if (animator == null) {
            animator = ViewCompat.animate(child);
            animator.setDuration(animationDuration);
            animator.setInterpolator(INTERPOLATOR);
        } else {
            animator.cancel();
        }
    }

    private interface BottomNavigationWithSnackbar<V extends View> {
        void updateSnackbar(CoordinatorLayout parent, V child, View dependency);
    }

    private class PreLollipopBottomNavWithSnackBarImpl implements BottomNavigationWithSnackbar {

        @Override
        public void updateSnackbar(CoordinatorLayout parent, View child, View dependency) {
            if (translucentNavigation && dependency instanceof Snackbar.SnackbarLayout) {

            }
        }
    }

    private class LollipopBottomNavWithSnackBarImpl implements BottomNavigationWithSnackbar {
        @Override
        public void updateSnackbar(CoordinatorLayout parent, View child, View dependency) {
            if (translucentNavigation) {
                if (mSnackbarHeight == -1) {
                    mSnackbarHeight = dependency.getPaddingBottom();

                }
                boolean expanded = child.getTranslationY() == 0;

                dependency.setPadding(dependency.getPaddingLeft(),
                    dependency.getPaddingTop(), dependency.getPaddingRight(),
                    mSnackbarHeight + bottomInset + (expanded ? height : 0)
                );
                ((MarginLayoutParams) dependency.getLayoutParams()).bottomMargin -= bottomInset;
            }
        }
    }
}