package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup.MarginLayoutParams
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import it.sephiroth.android.library.bottomnavigation.BottomNavigation.Companion.PENDING_ACTION_ANIMATE_ENABLED
import it.sephiroth.android.library.bottomnavigation.BottomNavigation.Companion.PENDING_ACTION_NONE
import it.sephiroth.android.library.bottonnavigation.R
import it.sephiroth.android.library.kotlin_extensions.os.isAtLeastAPI
import timber.log.Timber

/**
 * Created by alessandro crugnola on 4/2/16.
 * alessandro.crugnola@gmail.com
 */
open class BottomBehavior constructor(context: Context, attrs: AttributeSet) :
        VerticalScrollingBehavior<BottomNavigation>(context, attrs) {

    var isScrollable: Boolean = false
    private val scrollEnabled: Boolean
    private var enabled: Boolean = false

    /**
     * show/hide animation duration
     */
    private val animationDuration: Int

    /**
     * bottom inset when TRANSLUCENT_NAVIGATION is turned on
     */
    private var bottomInset: Int = 0

    /**
     * bottom navigation real height
     */
    private var height: Int = 0

    /**
     * maximum scroll offset
     */
    private var maxOffset: Int = 0

    /**
     * true if the current configuration has the TRANSLUCENT_NAVIGATION turned on
     */
    private var translucentNavigation: Boolean = false

    /**
     * Minimum touch distance
     */
    private val scaledTouchSlop: Int

    /**
     * hide/show animator
     */
    private var animator: ViewPropertyAnimatorCompat? = null

    /**
     * current visibility status
     */
    private var hidden = false

    /**
     * current Y offset
     */
    private var offset: Int = 0

    private var listener: OnExpandStatusChangeListener? = null

    protected var snackbarDependentView: SnackBarDependentView? = null

    val isExpanded: Boolean
        get() = !hidden

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationBehavior)
        this.isScrollable = array.getBoolean(R.styleable.BottomNavigationBehavior_bbn_scrollEnabled, true)
        this.scrollEnabled = true
        this.animationDuration =
                array.getInt(R.styleable.BottomNavigationBehavior_bbn_animationDuration, context.resources.getInteger(R.integer.bbn_hide_animation_duration))
        this.scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop * 2
        this.offset = 0
        array.recycle()
    }

    fun setOnExpandStatusChangeListener(listener: OnExpandStatusChangeListener) {
        this.listener = listener
    }

    fun setLayoutValues(bottomNavHeight: Int, bottomInset: Int) {
        Timber.i("setLayoutValues($bottomNavHeight, $bottomInset)")
        this.height = bottomNavHeight
        this.bottomInset = bottomInset
        this.translucentNavigation = bottomInset > 0
        this.maxOffset = height + if (translucentNavigation) bottomInset else 0
        this.enabled = true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigation, dependency: View): Boolean {
        return if (!enabled) {
            false
        } else isSnackbar(dependency)
    }

    private fun isSnackbar(view: View): Boolean {
        return view is SnackbarLayout
    }

    override fun onLayoutChild(parent: CoordinatorLayout, abl: BottomNavigation, layoutDirection: Int): Boolean {
        val handled = super.onLayoutChild(parent, abl, layoutDirection)

        val pendingAction = abl.pendingAction
        if (pendingAction != PENDING_ACTION_NONE) {
            val animate = pendingAction and PENDING_ACTION_ANIMATE_ENABLED != 0
            if (pendingAction and BottomNavigation.PENDING_ACTION_COLLAPSED != 0) {
                setExpanded(parent, abl, false, animate)
            } else {
                if (pendingAction and BottomNavigation.PENDING_ACTION_EXPANDED != 0) {
                    setExpanded(parent, abl, true, animate)
                }
            }
            // Finally reset the pending state
            abl.resetPendingAction()
        }

        return handled
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: BottomNavigation, dependency: View) {
        if (isSnackbar(dependency)) {
            snackbarDependentView?.onDestroy()
            snackbarDependentView = null
        }
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: BottomNavigation, dependency: View): Boolean {
        if (isSnackbar(dependency)) {
            if (null == snackbarDependentView) {
                snackbarDependentView = SnackBarDependentView(dependency as SnackbarLayout, height, bottomInset)
            }
            return snackbarDependentView?.onDependentViewChanged(parent, child) ?: false
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: BottomNavigation,
            directTargetChild: View, target: View,
            nestedScrollAxes: Int, @ViewCompat.NestedScrollType type: Int): Boolean {

        offset = 0
        if (!isScrollable || !scrollEnabled) {
            return false
        }

        if (nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0) {
            if (target.isScrollContainer && !target.canScrollVertically(-1) && !target.canScrollVertically(1)) {
                return false
            }
        }

        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes, type)
    }

    override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: BottomNavigation, target: View,
            @ViewCompat.NestedScrollType type: Int) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        offset = 0
    }

    override fun onDirectionNestedPreScroll(
            coordinatorLayout: CoordinatorLayout,
            child: BottomNavigation,
            target: View, dx: Int, dy: Int, consumed: IntArray,
            @ScrollDirection scrollDirection: Int) {

        // stop nested scroll if target is not scrollable
        // FIXME: not yet verified
        if (target.isScrollContainer && !target.canScrollVertically(scrollDirection)) {
            ViewCompat.stopNestedScroll(target)
        }

        offset += dy

        if (offset > scaledTouchSlop) {
            handleDirection(coordinatorLayout, child, VerticalScrollingBehavior.ScrollDirection.SCROLL_DIRECTION_UP)
            offset = 0
        } else if (offset < -scaledTouchSlop) {
            handleDirection(coordinatorLayout, child, VerticalScrollingBehavior.ScrollDirection.SCROLL_DIRECTION_DOWN)
            offset = 0
        }
    }

    override fun onNestedDirectionFling(
            coordinatorLayout: CoordinatorLayout, child: BottomNavigation, target: View, velocityX: Float, velocityY: Float,
            @ScrollDirection scrollDirection: Int): Boolean {

        if (Math.abs(velocityY) > 1000) {
            handleDirection(coordinatorLayout, child, scrollDirection)
        }

        return true
    }

    override fun onNestedVerticalOverScroll(
            coordinatorLayout: CoordinatorLayout, child: BottomNavigation, @ScrollDirection direction: Int, currentOverScroll: Int,
            totalOverScroll: Int) {
    }

    private fun handleDirection(coordinatorLayout: CoordinatorLayout, child: BottomNavigation, scrollDirection: Int) {
        if (!enabled || !isScrollable || !scrollEnabled) {
            return
        }
        if (scrollDirection == VerticalScrollingBehavior.ScrollDirection.SCROLL_DIRECTION_DOWN && hidden) {
            setExpanded(coordinatorLayout, child, true, true)
        } else if (scrollDirection == VerticalScrollingBehavior.ScrollDirection.SCROLL_DIRECTION_UP && !hidden) {
            setExpanded(coordinatorLayout, child, false, true)
        }
    }

    protected fun setExpanded(
            coordinatorLayout: CoordinatorLayout, child: BottomNavigation, expanded: Boolean, animate: Boolean) {
        animateOffset(coordinatorLayout, child, if (expanded) 0 else maxOffset, animate)
        listener?.onExpandStatusChanged(expanded, animate)
    }

    private fun animateOffset(
            coordinatorLayout: CoordinatorLayout,
            child: BottomNavigation,
            offset: Int,
            animate: Boolean) {

        hidden = offset != 0
        ensureOrCancelAnimator(coordinatorLayout, child)

        if (animate) {
            animator?.translationY(offset.toFloat())?.start()
        } else {
            child.translationY = offset.toFloat()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun ensureOrCancelAnimator(coordinatorLayout: CoordinatorLayout, child: BottomNavigation) {
        if (animator == null) {
            animator = ViewCompat.animate(child)
            animator!!.duration = animationDuration.toLong()
            animator!!.interpolator = INTERPOLATOR
        } else {
            animator!!.cancel()
        }
    }

    abstract class DependentView<V : View> internal constructor(protected val child: V, protected var height: Int,
                                                                protected val bottomInset: Int) {
        protected val layoutParams: MarginLayoutParams = child.layoutParams as MarginLayoutParams
        protected val bottomMargin: Int
        protected val originalPosition: Float = child.translationY

        init {
            this.bottomMargin = layoutParams.bottomMargin
        }

        protected open fun onDestroy() {
            layoutParams.bottomMargin = bottomMargin
            child.translationY = originalPosition
            child.requestLayout()
        }

        internal abstract fun onDependentViewChanged(parent: CoordinatorLayout, navigation: BottomNavigation): Boolean
    }

    class SnackBarDependentView internal constructor(child: SnackbarLayout, height: Int,
                                                     bottomInset: Int) : DependentView<SnackbarLayout>(child, height, bottomInset) {
        @Suppress("SpellCheckingInspection")
        private var snackbarHeight = -1

        override fun onDependentViewChanged(parent: CoordinatorLayout, navigation: BottomNavigation): Boolean {

            if (!isAtLeastAPI(21)) {
                val index1 = parent.indexOfChild(child)
                val index2 = parent.indexOfChild(navigation)
                if (index1 > index2) {
                    navigation.bringToFront()
                }
            }

            if (snackbarHeight == -1) {
                snackbarHeight = child.height
            }

            val maxScroll = Math.max(0f, navigation.translationY - bottomInset)
            val newBottomMargin = (height + bottomInset - maxScroll).toInt()

//            Timber.v("snackbarHeight = $snackbarHeight, maxScroll = $maxScroll")
//            Timber.v("bottomMargin = $bottomMargin, newBottomMargin = $newBottomMargin")
//            Timber.v("translationY = ${child.translationY}")

            if (child.translationY > newBottomMargin) child.translationY = newBottomMargin.toFloat()


            if (layoutParams.bottomMargin != newBottomMargin) {
                layoutParams.bottomMargin = newBottomMargin
                child.requestLayout()
                return true
            }
            return false
        }

        public override fun onDestroy() {
            super.onDestroy()
        }
    }

    interface OnExpandStatusChangeListener {
        fun onExpandStatusChanged(expanded: Boolean, animate: Boolean)
    }

    companion object {
        /**
         * default hide/show interpolator
         */
        private val INTERPOLATOR = LinearOutSlowInInterpolator()
    }
}