package it.sephiroth.android.library.bottomnavigation

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager.LayoutParams
import android.view.animation.DecelerateInterpolator
import androidx.annotation.AttrRes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.ViewPropertyAnimatorListener
import it.sephiroth.android.library.bottonnavigation.R
import timber.log.Timber

/**
 * Created by alessandro on 4/2/16.
 */
@Suppress("unused", "UNUSED_PARAMETER")
object MiscUtils {

    fun getDimensionPixelSize(context: Context, dp: Int): Int {
        return (context.resources.displayMetrics.density * dp).toInt()
    }

    /**
     * Returns if the current theme has the translucent status bar enabled
     *
     * @param activity context
     * @return true if the current theme has the translucent statusbar
     */
    @TargetApi(19)
    fun hasTranslucentStatusBar(activity: Activity?): Boolean {
        if (null == activity) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= 19) {
            activity.window.attributes.flags and LayoutParams.FLAG_TRANSLUCENT_STATUS == LayoutParams.FLAG_TRANSLUCENT_STATUS
        } else {
            false
        }
    }

    /**
     * Returns true if the current theme has declared the botton navigation as translucent
     *
     * @param activity context
     * @return true if the activity has the translucent navigation enabled
     */
    @TargetApi(19)
    fun hasTranslucentNavigation(activity: Activity?): Boolean {
        if (null == activity) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= 19) {
            activity.window.attributes.flags and LayoutParams.FLAG_TRANSLUCENT_NAVIGATION == LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        } else {
            false
        }
    }

    /**
     * Returns the current theme defined color
     *
     * @param context
     * @param color
     * @return
     */
    fun getColor(context: Context, @AttrRes color: Int): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(color, tv, true)
        return tv.data
    }

    @TargetApi(21)
    fun setDrawableColor(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= 21 && drawable is RippleDrawable) {
            if (RippleDrawable::class.java.isInstance(drawable)) {
                drawable.setColor(ColorStateList.valueOf(color))
            }
        } else {
            DrawableCompat.setTint(drawable, color)
        }
    }

    @SuppressLint("RtlHardcoded")
    fun isGravitiyLeft(gravity: Int): Boolean {
        return gravity == Gravity.LEFT
    }

    @SuppressLint("RtlHardcoded")
    fun isGravityRight(gravity: Int): Boolean {
        return gravity == Gravity.RIGHT
    }

    fun isGravityBottom(gravity: Int): Boolean {
        return gravity == Gravity.BOTTOM
    }

    fun switchColor(
            navigation: BottomNavigation,
            v: View,
            backgroundOverlay: View,
            backgroundDrawable: ColorDrawable,
            newColor: Int) {

        backgroundOverlay.clearAnimation()

        if (Build.VERSION.SDK_INT >= 21) {
            val currentAnimator = backgroundOverlay.getTag(R.id.bbn_backgroundOverlay_animator) as Animator?
            currentAnimator?.cancel()
        }

        backgroundDrawable.color = newColor
        backgroundOverlay.visibility = View.INVISIBLE
        @Suppress("DEPRECATION")
        ViewCompat.setAlpha(backgroundOverlay, 1f)
    }

    fun animate(
            navigation: BottomNavigation, v: View, backgroundOverlay: View, backgroundDrawable: ColorDrawable,
            newColor: Int, duration: Long) {

        val centerX = (v.x + v.width / 2).toInt()
        val centerY = navigation.paddingTop + v.height / 2

        backgroundOverlay.clearAnimation()

        val animator: Any

        if (Build.VERSION.SDK_INT >= 21) {

            val currentAnimator = backgroundOverlay.getTag(R.id.bbn_backgroundOverlay_animator) as Animator?
            currentAnimator?.cancel()

            val startRadius = 10f
            val finalRadius = (if (centerX > navigation.width / 2) centerX else navigation.width - centerX).toFloat()
            animator = ViewAnimationUtils.createCircularReveal(backgroundOverlay, centerX, centerY, startRadius, finalRadius)
            backgroundOverlay.setTag(R.id.bbn_backgroundOverlay_animator, animator)
        } else {
            @Suppress("DEPRECATION")
            ViewCompat.setAlpha(backgroundOverlay, 0f)
            animator = ViewCompat.animate(backgroundOverlay).alpha(1f)
        }

        backgroundOverlay.setBackgroundColor(newColor)
        backgroundOverlay.visibility = View.VISIBLE

        if (ViewPropertyAnimatorCompat::class.java.isInstance(animator)) {
            (animator as ViewPropertyAnimatorCompat).setListener(object : ViewPropertyAnimatorListener {
                var cancelled: Boolean = false

                override fun onAnimationStart(view: View) {}

                override fun onAnimationEnd(view: View) {
                    if (!cancelled) {
                        backgroundDrawable.color = newColor
                        backgroundOverlay.visibility = View.INVISIBLE
                        @Suppress("DEPRECATION")
                        ViewCompat.setAlpha(backgroundOverlay, 1f)
                    }
                }

                override fun onAnimationCancel(view: View) {
                    cancelled = true
                }
            })
                .setDuration(duration)
                .start()
        } else {
            val animator1 = animator as Animator
            animator1.duration = duration
            animator1.interpolator = DecelerateInterpolator()
            animator1.addListener(object : Animator.AnimatorListener {
                var cancelled: Boolean = false

                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    if (!cancelled) {
                        backgroundDrawable.color = newColor
                        backgroundOverlay.visibility = View.INVISIBLE
                        @Suppress("DEPRECATION")
                        ViewCompat.setAlpha(backgroundOverlay, 1f)
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })

            animator1.start()
        }
    }

    fun log(level: Int, message: String, vararg arguments: Any) {
        if (BottomNavigation.DEBUG) {
            Timber.log(level, message, arguments)
        }
    }

    fun getActivity(context: Context?): Activity? {
        return when (context) {
            null -> null
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

}
