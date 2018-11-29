package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

/**
 * Created by crugnola on 11/2/16.
 * BottomNavigation
 */
class FloatingActionButtonBehavior(context: Context,
                                   attrs: AttributeSet) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {
    private var navigationBarHeight = 0

    override fun onAttachedToLayoutParams(lp: CoordinatorLayout.LayoutParams) {
        // super.onAttachedToLayoutParams(lp);
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (BottomNavigation::class.java.isInstance(dependency)) {
            return true
        } else if (Snackbar.SnackbarLayout::class.java.isInstance(dependency)) {
            return true
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onDependentViewChanged(
            parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        Timber.v("onDependentViewChanged: $dependency")

        val list = parent.getDependencies(child)
        val bottomMargin = (child.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        var t = 0f
        var t2 = 0f
        var result = false

        for (dep in list) {
            if (dep is Snackbar.SnackbarLayout) {
                t += if(dep.layoutParams is ViewGroup.MarginLayoutParams) {
                    dep.translationY - (dep.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                } else {
                    dep.translationY - dep.height
                }
                result = true
            } else if (dep is BottomNavigation) {
                val navigation = dep
                t2 = navigation.translationY - navigation.height + bottomMargin
                t += t2
                result = true

                if (navigationBarHeight > 0) {
                    if (!navigation.isExpanded) {
                        child.hide()
                    } else {
                        child.show()
                    }
                }
            }
        }

        if (navigationBarHeight > 0 && t2 < 0) {
            t = Math.min(t2, t + navigationBarHeight)
        }

        child.translationY = t
        return result
    }

    override fun onDependentViewRemoved(
            parent: CoordinatorLayout, child: FloatingActionButton, dependency: View) {
        super.onDependentViewRemoved(parent, child, dependency)
        Timber.v("onDependentViewRemoved: $dependency")
    }

    fun setNavigationBarHeight(height: Int) {
        Timber.v("setNavigationBarHeight: $height")
        this.navigationBarHeight = height
    }
}
