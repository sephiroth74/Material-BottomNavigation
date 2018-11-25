package it.sephiroth.android.library.bottomnavigation

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.SystemClock

/**
 * Created by crugnola on 4/12/16.
 *
 * The MIT License
 */
class BadgeDrawable(color: Int, private val size: Int) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startTimeMillis: Long = 0

    var animating: Boolean = true

    init {
        this.paint.color = color
    }

    override fun draw(canvas: Canvas) {
        if (!animating) {
            paint.alpha = ALPHA_MAX.toInt()
            drawInternal(canvas)
        } else {
            if (startTimeMillis == 0L) {
                startTimeMillis = SystemClock.uptimeMillis()
            }

            val normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION
            if (normalized >= 1f) {
                animating = false
                paint.alpha = ALPHA_MAX.toInt()
                drawInternal(canvas)
            } else {
                val partialAlpha = (ALPHA_MAX * normalized).toInt()
                alpha = partialAlpha
                drawInternal(canvas)
            }
        }
    }

    private fun drawInternal(canvas: Canvas) {
        val bounds = bounds
        val w = bounds.width()
        val h = bounds.height()
        canvas.drawCircle((bounds.centerX() + w / 2).toFloat(), (bounds.centerY() - h / 2).toFloat(), (w / 2).toFloat(), paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getAlpha(): Int {
        return paint.alpha
    }

    override fun isStateful(): Boolean {
        return false
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicHeight(): Int {
        return size
    }

    override fun getIntrinsicWidth(): Int {
        return size
    }

    companion object {
        const val FADE_DURATION = 100f
        const val ALPHA_MAX = 255f
    }
}
