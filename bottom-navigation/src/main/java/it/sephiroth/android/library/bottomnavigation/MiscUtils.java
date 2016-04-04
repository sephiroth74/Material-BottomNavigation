package it.sephiroth.android.library.bottomnavigation;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;

import it.sephiroth.android.library.bottonnavigation.R;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

/**
 * Created by alessandro on 4/2/16.
 */
public class MiscUtils {

    private static final String TAG = MiscUtils.class.getSimpleName();

    public static int getDimensionPixelSize(final Context context, final int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    /**
     * Returns if the current theme has the translucent status bar enabled
     *
     * @param activity context
     * @return true if the current theme has the translucent statusbar
     */
    public static boolean hasTranslucentStatusBar(final Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            return
                ((activity.getWindow().getAttributes().flags & FLAG_TRANSLUCENT_STATUS)
                    == FLAG_TRANSLUCENT_STATUS);
        } else {
            return false;
        }
    }

    /**
     * Returns true if the current theme has declared the botton navigation as translucent
     *
     * @param activity context
     * @return true if the activity has the translucent navigation enabled
     */
    public static boolean hasTranslucentNavigation(final Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            return
                ((activity.getWindow().getAttributes().flags & FLAG_TRANSLUCENT_NAVIGATION) == FLAG_TRANSLUCENT_NAVIGATION);
        } else {
            return false;
        }
    }

    /**
     * Returns the current theme defined color
     *
     * @param context
     * @param color
     * @return
     */
    protected static int getColor(Context context, @AttrRes int color) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
        return tv.data;
    }

    protected static void setDrawableColor(@NonNull final Drawable drawable, final int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (RippleDrawable.class.isInstance(drawable)) {
                RippleDrawable rippleDrawable = (RippleDrawable) drawable;
                rippleDrawable.setColor(ColorStateList.valueOf(color));
            }
        } else {
            DrawableCompat.setTint(drawable, color);
        }
    }

    protected static void animate(
        final BottomNavigation navigation, final View v, final View backgroundOverlay, final ColorDrawable backgroundDrawable,
        final int newColor, final long duration) {

        int centerX = (int) (ViewCompat.getX(v) + (v.getWidth() / 2));
        int centerY = navigation.getPaddingTop() + v.getHeight() / 2;

        backgroundOverlay.clearAnimation();

        final Object animator;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Animator currentAnimator = (Animator) backgroundOverlay.getTag(R.id.bbn_backgroundOverlay_animator);
            if (null != currentAnimator) {
                currentAnimator.cancel();
            }

            final float startRadius = 10;
            final float finalRadius = centerX > navigation.getWidth() / 2 ? centerX : navigation.getWidth() - centerX;
            animator =
                ViewAnimationUtils.createCircularReveal(backgroundOverlay, centerX, centerY, startRadius, finalRadius);
            backgroundOverlay.setTag(R.id.bbn_backgroundOverlay_animator, animator);
        } else {
            ViewCompat.setAlpha(backgroundOverlay, 0);
            animator = ViewCompat.animate(backgroundOverlay).alpha(1);
        }

        backgroundOverlay.setBackgroundColor(newColor);
        backgroundOverlay.setVisibility(View.VISIBLE);

        if (ViewPropertyAnimatorCompat.class.isInstance(animator)) {
            ((ViewPropertyAnimatorCompat) animator).setListener(new ViewPropertyAnimatorListener() {
                boolean cancelled;

                @Override
                public void onAnimationStart(final View view) { }

                @Override
                public void onAnimationEnd(final View view) {
                    if (!cancelled) {
                        backgroundDrawable.setColor(newColor);
                        backgroundOverlay.setVisibility(View.INVISIBLE);
                        ViewCompat.setAlpha(backgroundOverlay, 1);
                    }
                }

                @Override
                public void onAnimationCancel(final View view) {
                    cancelled = true;
                }
            })
                .setDuration(duration)
                .start();
        } else {
            Animator animator1 = (Animator) animator;
            animator1.setDuration(duration);
            animator1.setInterpolator(new DecelerateInterpolator());
            animator1.addListener(new Animator.AnimatorListener() {
                boolean cancelled;

                @Override
                public void onAnimationStart(final Animator animation) { }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    if (!cancelled) {
                        backgroundDrawable.setColor(newColor);
                        backgroundOverlay.setVisibility(View.INVISIBLE);
                        ViewCompat.setAlpha(backgroundOverlay, 1);
                    }
                }

                @Override
                public void onAnimationCancel(final Animator animation) {
                    cancelled = true;
                }

                @Override
                public void onAnimationRepeat(final Animator animation) { }
            });

            animator1.start();
        }
    }
}
