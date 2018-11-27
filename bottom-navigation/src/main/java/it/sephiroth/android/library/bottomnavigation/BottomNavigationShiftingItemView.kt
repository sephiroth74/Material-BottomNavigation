package it.sephiroth.android.library.bottomnavigation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.animation.DecelerateInterpolator
import it.sephiroth.android.library.bottonnavigation.R
import timber.log.Timber

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
@Suppress("unused")
@SuppressLint("ViewConstructor")
internal class BottomNavigationShiftingItemView(parent: BottomNavigation, expanded: Boolean, menu: MenuParser.Menu) :
        BottomNavigationItemViewAbstract(parent, expanded, menu) {

    private val paddingTopItem: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_top)
    private val paddingBottomActive: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_active)
    private val paddingBottomInactive: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_inactive)
    private val iconSize: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_icon_size)
    private val textSize: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_text_size)
    private val animationDuration: Long = menu.itemAnimationDuration.toLong()

    private var centerY: Int = 0
    private val alphaActive: Float
    private val alphaInactive: Float
    private val alphaDisabled: Float
    private val interpolator = DecelerateInterpolator()
    private var textWidth: Float = 0.toFloat()
    private val colorActive: Int = menu.getColorActive()
    private val colorInactive: Int = menu.getColorInactive()
    private val colorDisabled: Int = menu.getColorDisabled()
    private var textX: Float = 0.toFloat()
    private var textY: Int = 0

    init {

        this.alphaInactive = Color.alpha(this.colorInactive) / BottomNavigationItemViewAbstract.ALPHA_MAX
        this.alphaDisabled = Color.alpha(this.colorDisabled) / BottomNavigationItemViewAbstract.ALPHA_MAX
        this.alphaActive = Math.max(Color.alpha(colorActive).toFloat() / BottomNavigationItemViewAbstract.ALPHA_MAX, alphaInactive)

        this.centerY = if (expanded) paddingTopItem else paddingBottomInactive
        this.textPaint.hinting = Paint.HINTING_ON
        this.textPaint.isLinearText = true
        this.textPaint.isSubpixelText = true
        this.textPaint.textSize = textSize.toFloat()
        this.textPaint.color = colorActive

        if (!expanded) {
            this.textPaint.alpha = 0
        }

        if (BottomNavigation.DEBUG) {
            Timber.v("colors: %x, %x, %x", colorDisabled, colorInactive, colorActive)
            Timber.v("alphas: %g, %g, %g", alphaDisabled, alphaInactive, alphaActive)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        textPaint.alpha =
                ((if (isExpanded) if (enabled) alphaActive else alphaDisabled else 0f) * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()

        if (null != icon) {
            updateLayoutOnAnimation(layoutParams.width, 1f, isExpanded)
        }

        requestLayout()
    }

    override fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean) {
        if (!animate) {
            updateLayoutOnAnimation(size, 1f, expanded)
            setCenterY(if (expanded) paddingTopItem else paddingBottomInactive)
            return
        }

        val set = AnimatorSet()
        set.duration = animationDuration * 2
        set.interpolator = interpolator
        val animator1 = ValueAnimator.ofInt(layoutParams.width, size)
        val animator2 = ObjectAnimator.ofInt(this, "centerY", if (expanded) paddingBottomInactive else paddingTopItem,
                if (expanded) paddingTopItem else paddingBottomInactive
                                            )

        animator1.addUpdateListener { animation ->
            val size = animation.animatedValue as Int
            val fraction = animation.animatedFraction
            updateLayoutOnAnimation(size, fraction, expanded)
        }

        set.playTogether(animator1, animator2)
        set.start()
    }

    private fun updateLayoutOnAnimation(size: Int, fraction: Float, expanded: Boolean) {
        layoutParams.width = size
        val color: Int
        val enabled = isEnabled

        val srcColor = if (enabled) if (expanded) colorInactive else colorActive else colorDisabled
        val dstColor = if (enabled) if (expanded) colorActive else colorInactive else colorDisabled
        val srcAlpha = if (enabled) alphaInactive else alphaDisabled
        val dstAlpha = if (enabled) alphaActive else alphaDisabled
        if (expanded) {
            color = evaluator.evaluate(fraction, srcColor, dstColor) as Int
            icon!!.alpha = ((srcAlpha + fraction * (dstAlpha - srcAlpha)) * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()
            textPaint.alpha = (fraction * dstAlpha * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()
        } else {
            color = evaluator.evaluate(fraction, srcColor, dstColor) as Int
            val alpha = 1.0f - fraction
            icon!!.alpha = ((srcAlpha + alpha * (dstAlpha - srcAlpha)) * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()
            textPaint.alpha = (alpha * dstAlpha * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()
        }

        icon!!.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    private fun measureText() {
        this.textWidth = textPaint.measureText(item!!.title)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (null == this.icon) {
            this.icon = item!!.getIcon(context)!!.mutate()
            icon?.setBounds(0, 0, iconSize, iconSize)
            icon?.setColorFilter(
                    if (isExpanded) if (isEnabled) colorActive else colorDisabled else if (isEnabled) colorInactive else colorDisabled, PorterDuff.Mode.SRC_ATOP)

            icon?.alpha = (if (isExpanded)
                (if (isEnabled) alphaActive else alphaDisabled) * BottomNavigationItemViewAbstract.ALPHA_MAX
            else
                (if (isEnabled) alphaInactive else alphaDisabled) * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()
        }

        if (textDirty) {
            measureText()
            textDirty = false
        }

        if (changed) {
            val w = right - left
            val h = bottom - top
            val centerX = (w - iconSize) / 2
            this.textY = h - paddingBottomActive
            this.textX = (w - textWidth) / 2
            icon?.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        icon?.draw(canvas)
        canvas.drawText(
                item!!.title,
                textX,
                textY.toFloat(),
                textPaint)
        drawBadge(canvas)
    }

    fun getCenterY(): Int {
        return centerY
    }

    fun setCenterY(value: Int) {
        centerY = value
        requestLayout()
    }
}
