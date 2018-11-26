package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by alessandro on 4/2/16.
 */
abstract class VerticalScrollingBehavior<V : View>(context: Context, attrs: AttributeSet) :
        CoordinatorLayout.Behavior<V>(context, attrs) {

    private var mTotalDyUnconsumed = 0
    private var mTotalDy = 0

    /*
       @return Over scroll direction: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE
   */
    @ScrollDirection
    @get:ScrollDirection
    var overScrollDirection = ScrollDirection.SCROLL_NONE
        private set

    /**
     * @return Scroll direction: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE
     */
    @ScrollDirection
    @get:ScrollDirection
    var scrollDirection = ScrollDirection.SCROLL_NONE
        private set

    @Suppress("DEPRECATED_JAVA_ANNOTATION")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(ScrollDirection.SCROLL_DIRECTION_UP, ScrollDirection.SCROLL_DIRECTION_DOWN)
    annotation class ScrollDirection {
        companion object {
            const val SCROLL_DIRECTION_UP = 1
            const val SCROLL_DIRECTION_DOWN = -1
            const val SCROLL_NONE = 0
        }
    }

    /**
     * @param coordinatorLayout
     * @param child
     * @param direction         Direction of the over scroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     * @param currentOverScroll Unconsumed value, negative or positive based on the direction;
     * @param totalOverScroll   Cumulative value for current direction
     */
    abstract fun onNestedVerticalOverScroll(
            coordinatorLayout: CoordinatorLayout, child: V, @ScrollDirection direction: Int, currentOverScroll: Int,
            totalOverScroll: Int)

    /**
     * @param scrollDirection Direction of the over scroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     */
    abstract fun onDirectionNestedPreScroll(
            coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray,
            @ScrollDirection scrollDirection: Int)

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, nestedScrollAxes: Int,
            @ViewCompat.NestedScrollType type: Int): Boolean {
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: V, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
            dyUnconsumed: Int, @ViewCompat.NestedScrollType type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        if (dyUnconsumed > 0 && mTotalDyUnconsumed < 0) {
            mTotalDyUnconsumed = 0
            overScrollDirection = ScrollDirection.SCROLL_DIRECTION_UP
        } else if (dyUnconsumed < 0 && mTotalDyUnconsumed > 0) {
            mTotalDyUnconsumed = 0
            overScrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN
        }
        mTotalDyUnconsumed += dyUnconsumed
        onNestedVerticalOverScroll(coordinatorLayout, child, overScrollDirection, dyConsumed, mTotalDyUnconsumed)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int,
                                   consumed: IntArray, @ViewCompat.NestedScrollType type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        if (dy > 0 && mTotalDy < 0) {
            mTotalDy = 0
            scrollDirection = ScrollDirection.SCROLL_DIRECTION_UP
        } else if (dy < 0 && mTotalDy > 0) {
            mTotalDy = 0
            scrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN
        }
        mTotalDy += dy
        onDirectionNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, scrollDirection)
    }

    override fun onNestedFling(
            coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float,
            consumed: Boolean): Boolean {
        super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
        scrollDirection = if (velocityY > 0) ScrollDirection.SCROLL_DIRECTION_UP else ScrollDirection.SCROLL_DIRECTION_DOWN
        return onNestedDirectionFling(coordinatorLayout, child, target, velocityX, velocityY, scrollDirection)
    }

    protected abstract fun onNestedDirectionFling(
            coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float,
            @ScrollDirection scrollDirection: Int): Boolean

}