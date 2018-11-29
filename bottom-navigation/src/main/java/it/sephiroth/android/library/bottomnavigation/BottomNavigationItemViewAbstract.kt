package it.sephiroth.android.library.bottomnavigation

import android.animation.ArgbEvaluator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import java.lang.ref.SoftReference

/**
 * Created by crugnola on 4/6/16.
 *
 * The MIT License
 */
@Suppress("unused")
internal abstract class BottomNavigationItemViewAbstract(parent: BottomNavigation, expanded: Boolean,
                                                         menu: MenuParser.Menu) : View(parent.context) {
    // this.setBackground(drawable);
    var item: BottomNavigationItem? = null
        set(value) {
            field = value
            value?.let {
                this.id = it.id
                this.isEnabled = it.isEnabled
            }
            invalidateBadge()
        }

    protected val evaluator: ArgbEvaluator = ArgbEvaluator()

    protected val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rippleColor: Int = menu.getRippleColor()

    var isExpanded: Boolean = false
        private set

    protected var textDirty: Boolean = true
    private val provider: BadgeProvider?
    protected var badge: Drawable? = null
    protected var icon: Drawable? = null

    init {
        this.isExpanded = expanded
        this.provider = parent.badgeProvider
    }

    fun invalidateBadge() {
        val d = provider?.getBadge(id)

        if (badge !== d) {
            if (null != badge) {
                badge!!.callback = null
                badge = null
            }
            badge = d

            if (null != badge) {
                badge!!.callback = this
                if (badge is BadgeDrawable && null == parent) {
                    (badge as BadgeDrawable).animating = false
                }
            }

            if (null != parent) {
                invalidate()
            }
        }
    }

    override fun invalidateDrawable(drawable: Drawable) {
        super.invalidateDrawable(drawable)

        if (drawable == badge) {
            invalidate()
        }
    }

    protected abstract fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean)

    fun setExpanded(expanded: Boolean, newSize: Int, animate: Boolean) {
        if (this.isExpanded != expanded) {
            this.isExpanded = expanded
            onStatusChanged(expanded, newSize, animate)
        }
    }

    protected fun drawBadge(canvas: Canvas) {
        if (null != icon) {
            badge?.let {
                val bounds = icon!!.bounds
                it.setBounds(bounds.right - it.intrinsicWidth, bounds.top, bounds.right,
                        bounds.top + it.intrinsicHeight)
                it.draw(canvas)
            }
        }
    }

    fun setTypeface(typeface: SoftReference<Typeface>?) {
        if (null != typeface) {
            val tf = typeface.get()
            if (null != tf) {
                textPaint.typeface = tf
            } else {
                textPaint.typeface = Typeface.DEFAULT
            }

            textDirty = true
            requestLayout()
        }
    }

    companion object {
        const val ALPHA_MAX = 255f
    }
}
