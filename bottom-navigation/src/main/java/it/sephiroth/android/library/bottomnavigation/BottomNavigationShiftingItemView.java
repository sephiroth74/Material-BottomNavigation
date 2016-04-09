package it.sephiroth.android.library.bottomnavigation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import it.sephiroth.android.library.bottonnavigation.R;

import static android.util.Log.INFO;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
@SuppressLint ("ViewConstructor")
public class BottomNavigationShiftingItemView extends BottomNavigationItemViewAbstract {
    private static final String TAG = BottomNavigationShiftingItemView.class.getSimpleName();
    public static final float ALPHA_MAX = 255f;
    private final int paddingTop;
    private final int paddingBottomActive;
    private final int iconSize;
    private final int paddingBottomInactive;
    private final int textSize;

    private Drawable icon;
    private int centerY;
    private final float maxAlpha;
    private final float minAlpha;
    private final Interpolator interpolator = new DecelerateInterpolator();
    private float textWidth;
    private long animationDuration;
    private final int colorActive;
    private float textX;
    private int textY;

    public BottomNavigationShiftingItemView(final BottomNavigation parent, boolean expanded, final MenuParser.Menu menu) {
        super(parent, expanded, menu);

        this.paddingTop = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_top);
        this.paddingBottomActive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_active);
        this.paddingBottomInactive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_inactive);
        this.iconSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_icon_size);
        this.textSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_text_size);

        int colorInactive = menu.getColorInactive();
        this.animationDuration = menu.getItemAnimationDuration();
        this.colorActive = menu.getColorActive();
        this.minAlpha = Color.alpha(colorInactive) / ALPHA_MAX;
        this.maxAlpha = Math.max((float) Color.alpha(colorActive) / ALPHA_MAX, minAlpha);

        this.centerY = expanded ? paddingTop : paddingBottomInactive;
        this.textPaint.setHinting(Paint.HINTING_ON);
        this.textPaint.setLinearText(true);
        this.textPaint.setSubpixelText(true);
        this.textPaint.setTextSize(textSize);
        this.textPaint.setColor(colorActive);

        if (!expanded) {
            this.textPaint.setAlpha(0);
        }
    }

    @Override
    protected void onStatusChanged(final boolean expanded, final int size, final boolean animate) {
        log(TAG, INFO, "onStatusChanged(%b, %d)", expanded, size);

        if (!animate) {
            updateLayoutOnAnimation(size, 1, expanded);
            setCenterY(expanded ? paddingTop : paddingBottomInactive);
            return;
        }

        final AnimatorSet set = new AnimatorSet();
        set.setDuration(animationDuration * 2);
        set.setInterpolator(interpolator);
        final ValueAnimator animator1 = ValueAnimator.ofInt(getLayoutParams().width, size);
        final ValueAnimator animator2 = ObjectAnimator.ofInt(this, "centerY", expanded ? paddingBottomInactive : paddingTop,
            expanded ? paddingTop : paddingBottomInactive
        );

        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                int size = (int) animation.getAnimatedValue();
                final float fraction = animation.getAnimatedFraction();

                updateLayoutOnAnimation(size, fraction, expanded);
            }
        });

        set.playTogether(animator1, animator2);
        set.start();
    }

    private void updateLayoutOnAnimation(final int size, final float fraction, final boolean expanded) {
        getLayoutParams().width = size;
        if (expanded) {
            icon.setAlpha((int) ((minAlpha + (fraction * (maxAlpha - minAlpha))) * 255));
            textPaint.setAlpha((int) (((fraction * (maxAlpha))) * 255));
        } else {
            float alpha = 1.0F - fraction;
            icon.setAlpha((int) ((minAlpha + (alpha * (maxAlpha - minAlpha))) * 255));
            textPaint.setAlpha((int) (((alpha * (maxAlpha))) * 255));
        }
    }

    private void measureText() {
        log(TAG, INFO, "measureText");
        this.textWidth = textPaint.measureText(getItem().getTitle());
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (null == this.icon) {
            this.icon = getItem().getIcon(getContext());
            icon.setBounds(0, 0, iconSize, iconSize);
            icon.setColorFilter(colorActive, PorterDuff.Mode.SRC_ATOP);
            icon.setAlpha((int) (isExpanded() ? maxAlpha * 255 : minAlpha * 255));
        }

        if (textDirty) {
            measureText();
            textDirty = false;
        }

        if (changed) {
            int w = right - left;
            int h = bottom - top;
            int centerX = (w - iconSize) / 2;
            this.textY = h - paddingBottomActive;
            this.textX = (w - textWidth) / 2;
            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        icon.draw(canvas);
        canvas.drawText(
            getItem().getTitle(),
            textX,
            textY,
            textPaint
        );
    }

    @SuppressWarnings ("unused")
    @proguard.annotation.Keep
    public int getCenterY() {
        return centerY;
    }

    @SuppressWarnings ("unused")
    @proguard.annotation.Keep
    public void setCenterY(int value) {
        centerY = value;
        requestLayout();
    }

}
