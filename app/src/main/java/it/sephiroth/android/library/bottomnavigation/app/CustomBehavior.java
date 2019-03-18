package it.sephiroth.android.library.bottomnavigation.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Created by crugnola on 6/23/16.
 * BottomNavigation
 */

public class CustomBehavior extends CoordinatorLayout.Behavior<Toolbar> {
    private static final String TAG = "CustomBehavior";
    private int toolbarHeight;
    private ScrollHelper helper;
    private boolean scrolling;
    private boolean enabled;

    public CustomBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.toolbarHeight = 0;
        this.helper = new ScrollHelper();
        this.enabled = false;
    }

    @Override
    public boolean onLayoutChild(final CoordinatorLayout parent, final Toolbar child, final int layoutDirection) {
        if (this.toolbarHeight <= 0) {
            this.toolbarHeight = child.getHeight();
            this.helper.setRange(-this.toolbarHeight, 0);
            this.enabled = true;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(
        final CoordinatorLayout coordinatorLayout, final Toolbar child, final View directTargetChild, final View target,
        final int nestedScrollAxes) {
        if (!enabled) {
            return false;
        }
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(
        final CoordinatorLayout coordinatorLayout, final Toolbar child, final View directTargetChild, final View target,
        final int nestedScrollAxes) {
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout coordinatorLayout, final Toolbar child, final View target) {
        if (scrolling) {
            Log.w(TAG, "onStopNestedScroll");
        }
        scrolling = false;
    }

    @Override
    public void onNestedScroll(
        final CoordinatorLayout coordinatorLayout, final Toolbar child, final View target, final int dxConsumed,
        final int dyConsumed, final int dxUnconsumed,
        final int dyUnconsumed) {
        scrolling = true;
    }

    @Override
    public void onNestedPreScroll(
        final CoordinatorLayout coordinatorLayout, final Toolbar child, final View target, final int dx, final int dy,
        final int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    }

    @Override
    public boolean onNestedFling(
        final CoordinatorLayout coordinatorLayout, final Toolbar child, final View target, final float velocityX,
        final float velocityY, final boolean consumed) {
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public WindowInsetsCompat onApplyWindowInsets(
        final CoordinatorLayout coordinatorLayout, final Toolbar child, final WindowInsetsCompat insets) {
        return super.onApplyWindowInsets(coordinatorLayout, child, insets);
    }

    public static class ScrollHelper {
        float max;
        float min;
        float total;
        float current;

        public void setRange(float min, float max) {
            setMin(min);
            setMax(max);
        }

        public void setMin(final float min) {
            this.min = min;
        }

        public void setMax(final float max) {
            this.max = max;
        }

        public boolean inRange() {
            return total <= max && total >= min;
        }

        public void setCurrent(float current) {
            this.current = clamp(current);
        }

        public float clamp(float value) {
            return Math.max(min, Math.min(max, value));
        }

        public void scroll(float dy) {
            total += dy;
            current = clamp(current + dy);
        }

        public float getCurrentScroll() {
            return current;
        }

        public float getTotalScroll() {
            return total;
        }

        public boolean valueInRange(float value) {
            return value > min || value < max;
        }
    }
}
