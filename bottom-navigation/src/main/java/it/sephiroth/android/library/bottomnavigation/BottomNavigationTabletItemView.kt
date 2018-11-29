package it.sephiroth.android.library.bottomnavigation

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import it.sephiroth.android.library.bottonnavigation.R

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
@SuppressLint("ViewConstructor")
internal class BottomNavigationTabletItemView(parent: BottomNavigation, expanded: Boolean, menu: MenuParser.Menu) :
        BottomNavigationItemViewAbstract(parent, expanded, menu) {
    private val iconSize: Int

    private val interpolator = DecelerateInterpolator()
    private val animationDuration: Long
    private val colorActive: Int
    private val colorInactive: Int
    private val colorDisabled: Int

    init {
        val res = resources
        this.iconSize = res.getDimensionPixelSize(R.dimen.bbn_tablet_item_icon_size)
        this.animationDuration = menu.itemAnimationDuration.toLong()
        this.colorActive = menu.getColorActive()
        this.colorInactive = menu.getColorInactive()
        this.colorDisabled = menu.getColorDisabled()
    }

    override fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean) {
        if (!animate) {
            updateLayoutOnAnimation(1f, expanded)
            return
        }

        val animator = ObjectAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation -> updateLayoutOnAnimation(animation.animatedFraction, expanded) }
        animator.duration = animationDuration
        animator.interpolator = interpolator
        animator.start()
    }

    private fun updateLayoutOnAnimation(fraction: Float, expanded: Boolean) {
        val enabled = isEnabled
        val dstColor = if (enabled) if (expanded) colorActive else colorInactive else colorDisabled
        val srcColor = if (enabled) if (expanded) colorInactive else colorActive else colorDisabled
        val color = evaluator.evaluate(fraction, srcColor, dstColor) as Int

        icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        icon?.alpha = Color.alpha(color)

        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (null == this.icon) {
            this.icon = item?.getIcon(context)?.mutate()
            this.icon?.setColorFilter(
                    if (isExpanded) if (isEnabled) colorActive else colorDisabled else if (isEnabled) colorInactive else colorDisabled,
                    PorterDuff.Mode.SRC_ATOP
                                     )
            this.icon?.alpha =
                    Color.alpha(if (isExpanded) if (isEnabled) colorActive else colorDisabled else if (isEnabled) colorInactive else colorDisabled)
            this.icon?.setBounds(0, 0, iconSize, iconSize)
        }

        if (changed) {
            val w = right - left
            val h = bottom - top
            val centerX = (w - iconSize) / 2
            val centerY = (h - iconSize) / 2
            icon?.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        icon?.draw(canvas)
        drawBadge(canvas)
    }
}
