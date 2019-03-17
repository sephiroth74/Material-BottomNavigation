package it.sephiroth.android.library.bottomnavigation

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import it.sephiroth.android.library.bottonnavigation.R
import timber.log.Timber

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
@Suppress("unused")
@SuppressLint("ViewConstructor")
internal class BottomNavigationFixedItemView(parent: BottomNavigation, expanded: Boolean, menu: MenuParser.Menu) :
        BottomNavigationItemViewAbstract(parent, expanded, menu) {
    private val iconSize: Int

    private var centerY: Int = 0
        set(value) {
            field = value
            ViewCompat.postInvalidateOnAnimation(this)
        }

    private val interpolator = DecelerateInterpolator()
    private var textWidth: Float = 0.toFloat()
    private var paddingBottomItem: Int
    private val paddingHorizontal: Int
    private val textSizeInactive: Int
    private var canvasTextScale: Float = 0.toFloat()

    private var iconTranslation: Float = 0.toFloat()
        set(value) {
            Timber.v("iconTranslation = $value")
            field = value
            ViewCompat.postInvalidateOnAnimation(this)
        }

    private var textCenterX: Int = 0
    private var textCenterY: Int = 0
    private var centerX: Int = 0
    private var textX: Float = 0.toFloat()
    private var textY: Float = 0.toFloat()

    var textScale: Float
        get() = canvasTextScale
        set(value) {
            Timber.v("textScale = $value")
            canvasTextScale = value
            ViewCompat.postInvalidateOnAnimation(this)
        }

    init {
        val res = resources
        this.paddingBottomItem = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_bottom)
        this.paddingHorizontal = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_horizontal)
        this.textSizeInactive = res.getDimensionPixelSize(R.dimen.bbn_default_text_size)
        this.iconSize = res.getDimensionPixelSize(R.dimen.bbn_default_item_icon_size)

        this.centerY = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top)
        this.canvasTextScale = 1f
        this.iconTranslation = 0f
        this.textPaint.color = Color.WHITE
        this.textPaint.hinting = Paint.HINTING_ON
        this.textPaint.isLinearText = true
        this.textPaint.isSubpixelText = true
        this.textPaint.textSize = textSizeInactive.toFloat()
        this.textPaint.color = if (expanded) colorActive else colorInactive
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        this.textPaint.color =
                if (isExpanded) if (enabled) colorActive else colorDisabled else if (enabled) colorInactive else colorDisabled

        if (null != icon) {
            updateLayoutOnAnimation(1f, isExpanded)
        }
        requestLayout()
    }

    override fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean) {
        updateLayoutOnAnimation(1f, expanded)
    }

    private fun updateLayoutOnAnimation(fraction: Float, expanded: Boolean) {
        val enabled = isEnabled
        val dstColor = if (enabled) if (expanded) colorActive else colorInactive else colorDisabled
        val srcColor = if (enabled) if (expanded) colorInactive else colorActive else colorDisabled
        val color = evaluator.evaluate(fraction, srcColor, dstColor) as Int

        icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        icon?.alpha = Color.alpha(color)
        textPaint.color = color
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (null == this.icon) {
            this.icon = item!!.getIcon(context)!!.mutate()

            val color =
                    if (isExpanded) if (isEnabled) colorActive else colorDisabled else if (isEnabled) colorInactive else colorDisabled

            this.icon!!.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            this.icon!!.setBounds(0, 0, iconSize, iconSize)
            this.icon!!.alpha = Color.alpha(color)
        }

        if (changed) {
            val w = right - left
            centerX = (w - iconSize) / 2
            icon?.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
        }

        if (textDirty || changed) {
            measureText()
            textDirty = false
        }
    }

    private fun measureText() {
        val width = width
        val height = height

        textWidth = textPaint.measureText(item!!.title)
        textX = paddingHorizontal + (width - paddingHorizontal * 2 - textWidth) / 2
        textY = (height - paddingBottomItem).toFloat()
        textCenterX = width / 2
        textCenterY = height - paddingBottomItem
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.translate(0f, iconTranslation)
        icon?.draw(canvas)
        drawBadge(canvas)
        canvas.restore()

        canvas.save()
        canvas.scale(canvasTextScale, canvasTextScale, textCenterX.toFloat(), textCenterY.toFloat())

        canvas.drawText(
                item!!.title,
                textX,
                textY,
                textPaint)

        canvas.restore()

    }

    companion object {
        private const val TEXT_SCALE_ACTIVE = 1.1666666667f
    }

}
