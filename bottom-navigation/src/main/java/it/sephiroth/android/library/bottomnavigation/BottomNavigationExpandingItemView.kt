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
internal class BottomNavigationExpandingItemView(parent: BottomNavigation, expanded: Boolean, menu: MenuParser.Menu) :
        BottomNavigationItemViewAbstract(parent, expanded, menu) {

    private val paddingTopItem: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_top)
    private val paddingBottomActive: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_active)
    private val paddingBottomInactive: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_inactive)
    private val iconSize: Int = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_icon_size)
    private val textSize: Int = resources.getDimensionPixelSize(R.dimen.bbn_expanding_text_size)
    private var centerY: Int = 0
    private var centerX: Int = 0
    private val alphaActive: Float
    private val alphaInactive: Float = Color.alpha(this.colorInactive) / BottomNavigationItemViewAbstract.ALPHA_MAX
    private val alphaDisabled: Float = Color.alpha(this.colorDisabled) / BottomNavigationItemViewAbstract.ALPHA_MAX
    private val interpolator = DecelerateInterpolator()
    private var textWidth: Float = 0.toFloat()

    private var textX: Float = 0.toFloat()
    private var textY: Int = 0

    init {
        this.alphaActive = Math.max(Color.alpha(colorActive).toFloat() / BottomNavigationItemViewAbstract.ALPHA_MAX, alphaInactive)
        this.centerY = if (expanded) paddingTopItem else paddingBottomInactive

        this.textPaint.let { paint ->
            paint.hinting = Paint.HINTING_ON
            paint.isLinearText = true
            paint.isSubpixelText = true
            paint.textSize = textSize.toFloat()
            paint.color = colorActive

            if (!expanded) {
                paint.alpha = 0
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        textPaint.alpha =
                ((if (isExpanded) if (enabled) alphaActive else alphaDisabled else 0f) * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()

        if (null != icon) {
            updateLayoutOnAnimation(1f, isExpanded)
        }
        requestLayout()
    }

    override fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean) {
        Timber.i("onStatusChanged($expanded, $size, $animate)")

        if (!animate) {
            updateLayoutOnAnimation(1f, expanded)
            setCenterY(if (expanded) paddingTopItem else paddingBottomInactive)
            return
        }

        val set = AnimatorSet()
        set.duration = animationDuration
        set.interpolator = interpolator
        val animator1 = ValueAnimator.ofInt(layoutParams.width, size)
        val animator2 =
                ObjectAnimator.ofInt(this, "centerY", if (expanded) paddingBottomInactive else paddingTopItem, if (expanded) paddingTopItem else paddingBottomInactive)

        animator1.addUpdateListener { animation ->
            val size = animation.animatedValue as Int
            val fraction = animation.animatedFraction
            updateLayoutOnAnimation(fraction, expanded)
        }

        set.playTogether(animator1, animator2)
        set.start()
    }

    private fun updateLayoutOnAnimation(fraction: Float, expanded: Boolean) {
        val enabled = isEnabled
        val srcColor = if (enabled) if (expanded) colorInactive else colorActive else colorDisabled
        val dstColor = if (enabled) if (expanded) colorActive else colorInactive else colorDisabled
        val srcAlpha = if (enabled) alphaInactive else alphaDisabled
        val dstAlpha = if (enabled) alphaActive else alphaDisabled
        val computedFraction = if (expanded) fraction else (1.0f - fraction)
        val color = evaluator.evaluate(fraction, srcColor, dstColor) as Int

        textPaint.textSize = textSize * computedFraction
        textPaint.alpha = (computedFraction * dstAlpha * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()

        val w = right - left
        val h = bottom - top
        this.textY = h - paddingBottomActive
        this.textX = (w - (textWidth * computedFraction)) / 2

        icon?.let { icon ->
            icon.alpha =
                    ((srcAlpha + computedFraction * (dstAlpha - srcAlpha)) * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
        }
    }

    private fun measureText() {
        this.textWidth = textPaint.measureText(item!!.title)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Timber.i("onLayout($changed)")

        if (null == this.icon) {
            this.icon = item!!.getIcon(context)!!.mutate()
            icon?.let { icon ->
                icon.setBounds(0, 0, iconSize, iconSize)
                icon.setColorFilter(
                        if (isExpanded) if (isEnabled) colorActive else colorDisabled else if (isEnabled) colorInactive else colorDisabled, PorterDuff.Mode.SRC_ATOP)

                icon.alpha = (if (isExpanded)
                    (if (isEnabled) alphaActive else alphaDisabled) * BottomNavigationItemViewAbstract.ALPHA_MAX
                else
                    (if (isEnabled) alphaInactive else alphaDisabled) * BottomNavigationItemViewAbstract.ALPHA_MAX).toInt()
            }
        }

        if (textDirty) {
            measureText()
            textDirty = false
        }

        if (changed) {
            val w = right - left
            val h = bottom - top
            this.centerX = (w - iconSize) / 2
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

    @Suppress("MemberVisibilityCanBePrivate")
    fun setCenterY(value: Int) {
        centerY = value
        requestLayout()
    }
}
