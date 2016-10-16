package it.sephiroth.android.library.bottomnavigation;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;

import it.sephiroth.android.library.bottonnavigation.R;

import static android.view.WindowManager.LayoutParams;

/**
 * Created by alessandro on 4/2/16.
 */
public final class MiscUtils {

    private MiscUtils() { }

    public static int getDimensionPixelSize(final Context context, final int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }

    /**
     * Returns if the current theme has the translucent status bar enabled
     *
     * @param activity context
     * @return true if the current theme has the translucent statusbar
     */
    @TargetApi (19)
    public static boolean hasTranslucentStatusBar(@Nullable final Activity activity) {
        if (null == activity) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            return
                ((activity.getWindow().getAttributes().flags & LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    == LayoutParams.FLAG_TRANSLUCENT_STATUS);
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
    @TargetApi (19)
    public static boolean hasTranslucentNavigation(@Nullable final Activity activity) {
        if (null == activity) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            return
                ((activity.getWindow().getAttributes().flags & LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                    == LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
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
        context.getTheme().resolveAttribute(color, tv, true);
        return tv.data;
    }

    @TargetApi (21)
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

    @SuppressLint ("RtlHardcoded")
    static boolean isGravitiyLeft(final int gravity) {
        return gravity == Gravity.LEFT;
    }

    @SuppressLint ("RtlHardcoded")
    static boolean isGravityRight(final int gravity) {
        return gravity == Gravity.RIGHT;
    }

    static boolean isGravityBottom(final int gravity) {
        return gravity == Gravity.BOTTOM;
    }

    protected static void switchColor(
        final BottomNavigation navigation,
        final View v,
        final View backgroundOverlay,
        final ColorDrawable backgroundDrawable,
        final int newColor) {

        backgroundOverlay.clearAnimation();

        if (Build.VERSION.SDK_INT >= 21) {
            Animator currentAnimator = (Animator) backgroundOverlay.getTag(R.id.bbn_backgroundOverlay_animator);
            if (null != currentAnimator) {
                currentAnimator.cancel();
            }
        }

        backgroundDrawable.setColor(newColor);
        backgroundOverlay.setVisibility(View.INVISIBLE);
        ViewCompat.setAlpha(backgroundOverlay, 1);
    }

    protected static void animate(
        final BottomNavigation navigation, final View v, final View backgroundOverlay, final ColorDrawable backgroundDrawable,
        final int newColor, final long duration) {

        int centerX = (int) (ViewCompat.getX(v) + (v.getWidth() / 2));
        int centerY = navigation.getPaddingTop() + v.getHeight() / 2;

        backgroundOverlay.clearAnimation();

        final Object animator;

        if (Build.VERSION.SDK_INT >= 21) {

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

    public static void log(final String tag, final int level, String message, Object... arguments) {
        if (BottomNavigation.DEBUG) {
            Log.println(level, tag, String.format(message, arguments));
        }
    }

    @Nullable
    static Activity getActivity(@Nullable Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

}
