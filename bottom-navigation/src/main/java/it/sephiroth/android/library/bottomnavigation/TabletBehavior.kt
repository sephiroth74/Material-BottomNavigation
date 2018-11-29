package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber

/**
 * Created by alessandro on 4/10/16 at 2:12 PM.
 * Project: Material-BottomNavigation
 */
class TabletBehavior(context: Context, attrs: AttributeSet) : VerticalScrollingBehavior<BottomNavigation>(context, attrs) {
    private var topInset: Int = 0
    private var enabled: Boolean = false
    private var width: Int = 0
    private var translucentStatus: Boolean = false

    fun setLayoutValues(bottomNavWidth: Int, topInset: Int, translucentStatus: Boolean) {
        this.translucentStatus = translucentStatus
        Timber.v("setLayoutValues(bottomNavWidth: $bottomNavWidth, topInset: $topInset)")
        Timber.v("translucentStatus: $translucentStatus")
        this.width = bottomNavWidth
        this.topInset = topInset
        this.enabled = true

    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigation, dependency: View): Boolean {
        return dependency is AppBarLayout || dependency is Toolbar
    }

    override fun onDependentViewChanged(
            parent: CoordinatorLayout, child: BottomNavigation, dependency: View): Boolean {
        val params = child.layoutParams as ViewGroup.MarginLayoutParams
        val top = if (Build.VERSION.SDK_INT > 19) topInset else if (translucentStatus) topInset else 0
        params.topMargin = Math.max(dependency.top + dependency.height - top, if (translucentStatus) 0 else -top)

        if (translucentStatus) {
            if (params.topMargin < top) {
                child.setPadding(0, top - params.topMargin, 0, 0)
            } else {
                child.setPadding(0, 0, 0, 0)
            }
        }

        child.requestLayout()
        return true
    }

    override fun onNestedVerticalOverScroll(
            coordinatorLayout: CoordinatorLayout, child: BottomNavigation, @ScrollDirection direction: Int,
            currentOverScroll: Int, totalOverScroll: Int) {
    }

    override fun onDirectionNestedPreScroll(
            coordinatorLayout: CoordinatorLayout, child: BottomNavigation, target: View, dx: Int, dy: Int,
            consumed: IntArray,
            @ScrollDirection scrollDirection: Int) {
    }

    override fun onNestedDirectionFling(
            coordinatorLayout: CoordinatorLayout, child: BottomNavigation, target: View, velocityX: Float,
            velocityY: Float,
            @ScrollDirection scrollDirection: Int): Boolean {
        return false
    }
}
