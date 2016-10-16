package it.sephiroth.android.library.bottomnavigation;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import it.sephiroth.android.library.bottonnavigation.R;

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
@SuppressLint ("ViewConstructor")
public class BottomNavigationTabletItemView extends BottomNavigationItemViewAbstract {
    private static final String TAG = BottomNavigationTabletItemView.class.getSimpleName();
    private final int iconSize;

    private final Interpolator interpolator = new DecelerateInterpolator();
    private long animationDuration;
    private final int colorActive;
    private final int colorInactive;
    private final ArgbEvaluator evaluator;

    public BottomNavigationTabletItemView(final BottomNavigation parent, boolean expanded, final MenuParser.Menu menu) {
        super(parent, expanded, menu);
        this.evaluator = new ArgbEvaluator();
        final Resources res = getResources();
        this.iconSize = res.getDimensionPixelSize(R.dimen.bbn_tablet_item_icon_size);
        this.animationDuration = menu.getItemAnimationDuration();
        this.colorActive = menu.getColorActive();
        this.colorInactive = menu.getColorInactive();
    }

    @Override
    protected void onStatusChanged(final boolean expanded, final int size, final boolean animate) {
        if (!animate) {
            updateLayoutOnAnimation(1, expanded);
            return;
        }

        final ValueAnimator animator = ObjectAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                updateLayoutOnAnimation(animation.getAnimatedFraction(), expanded);
            }
        });
        animator.setDuration(animationDuration);
        animator.setInterpolator(interpolator);
        animator.start();
    }

    private void updateLayoutOnAnimation(final float fraction, final boolean expanded) {
        if (expanded) {
            final int color = (int) evaluator.evaluate(fraction, colorInactive, colorActive);
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            icon.setAlpha(Color.alpha(color));
        } else {
            int color = (int) evaluator.evaluate(fraction, colorActive, colorInactive);
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            icon.setAlpha(Color.alpha(color));
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (null == this.icon) {
            this.icon = getItem().getIcon(getContext()).mutate();
            this.icon.setColorFilter(isExpanded() ? colorActive : colorInactive, PorterDuff.Mode.SRC_ATOP);
            this.icon.setAlpha(Color.alpha(isExpanded() ? colorActive : colorInactive));
            this.icon.setBounds(0, 0, iconSize, iconSize);
        }

        if (changed) {
            final int w = right - left;
            final int h = bottom - top;
            final int centerX = (w - iconSize) / 2;
            final int centerY = (h - iconSize) / 2;
            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        icon.draw(canvas);
        drawBadge(canvas);
    }
}
