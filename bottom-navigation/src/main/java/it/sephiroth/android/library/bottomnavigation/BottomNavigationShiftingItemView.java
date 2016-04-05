package it.sephiroth.android.library.bottomnavigation;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.lang.ref.SoftReference;

import it.sephiroth.android.library.bottonnavigation.R;

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
public class BottomNavigationShiftingItemView extends View {
    private static final String TAG = BottomNavigationShiftingItemView.class.getSimpleName();
    private final int paddingTop;
    private final int paddingBottomActive;
    private final int textPaddingTop;
    private final int iconSize;
    private final int paddingBottomInactive;
    private final int textSize;
    private boolean expanded;
    private BottomNavigationItem item;
    private Drawable icon;
    private int centerY;
    private final float minAlpha;
    private final Interpolator interpolator = new DecelerateInterpolator();
    private final Paint textPaint;
    private float textWidth;
    private long animationDuration;
    private final boolean invertedTheme;
    private final int colorActive;
    private final int colorInactive;
    private final int rippleColor;
    private final ArgbEvaluator evaluator;
    private boolean textDirty;
    private float textX;
    private int textY;

    public BottomNavigationShiftingItemView(final BottomNavigation parent, boolean expanded, boolean invertedTheme) {
        super(parent.getContext());

        animationDuration = getResources().getInteger(R.integer.bbn_shifting_item_animation_duration);
        paddingTop = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_top);
        paddingBottomActive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_active);
        paddingBottomInactive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_inactive);
        iconSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_icon_size);
        textPaddingTop = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_text_padding_top);
        textSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_text_size);

        this.evaluator = new ArgbEvaluator();

        this.colorActive = parent.backgroundColorPrimary;
        this.colorInactive = parent.inactiveItemInvertedColor;
        this.rippleColor = parent.rippleColor;

        this.invertedTheme = invertedTheme;
        this.minAlpha = getResources().getFraction(R.fraction.bbn_item_shifting_inactive_alpha, 1, 1);
        this.expanded = expanded;
        this.centerY = expanded ? paddingTop : paddingBottomInactive;

        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setHinting(Paint.HINTING_ON);
        this.textPaint.setLinearText(true);
        this.textPaint.setSubpixelText(true);
        this.textPaint.setTextSize(textSize);

        if (invertedTheme) {
            this.textPaint.setColor(expanded ? colorActive : colorInactive);
        }

        this.textPaint.setAlpha(expanded ? 255 : 0);
        this.textDirty = true;
    }

    void setItem(BottomNavigationItem item) {
        final Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.bbn_ripple_selector);
        MiscUtils.setDrawableColor(drawable, rippleColor);

        this.item = item;
        this.setId(item.getId());
        this.setBackground(drawable);
        this.setEnabled(item.isEnabled());
    }

    private void measureText() {
        Log.i(TAG, "measureText");
        this.textWidth = textPaint.measureText(item.getTitle());
        this.textY = getHeight() - paddingBottomActive;
    }

    public BottomNavigationItem getItem() {
        return item;
    }

    public void setExpanded(final boolean expanded, int newSize) {
        this.expanded = expanded;

        final AnimatorSet set = new AnimatorSet();
        set.setDuration(animationDuration);
        set.setInterpolator(interpolator);
        final ValueAnimator animator1 = ValueAnimator.ofInt(getLayoutParams().width, newSize);
        final ValueAnimator animator2 = ObjectAnimator.ofInt(this, "centerY", expanded ? paddingBottomInactive : paddingTop,
            expanded ? paddingTop : paddingBottomInactive
        );

        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                getLayoutParams().width = (int) animation.getAnimatedValue();

                final float fraction = animation.getAnimatedFraction();

                if (expanded) {
                    if (!invertedTheme) {
                        icon.setAlpha((int) ((minAlpha + (fraction * (1.0 - minAlpha))) * 255));
                        textPaint.setAlpha((int) (fraction * 255));
                    } else {
                        icon.setColorFilter(
                            (Integer) evaluator.evaluate(fraction, colorInactive, colorActive), PorterDuff.Mode.SRC_ATOP);
                        textPaint.setColor((Integer) evaluator.evaluate(fraction, 0, colorActive));
                    }
                } else {
                    if (!invertedTheme) {
                        float alpha = 1.0F - fraction;
                        icon.setAlpha((int) ((minAlpha + (alpha * (1.0 - minAlpha))) * 255));
                    } else {
                        icon.setColorFilter(
                            (Integer) evaluator.evaluate(fraction, colorActive, colorInactive), PorterDuff.Mode.SRC_ATOP);
                    }
                    textPaint.setAlpha((int) ((1.0 - fraction) * 255));
                }

                requestLayout();
            }
        });

        set.playTogether(animator1, animator2);
        set.start();
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (null == this.icon) {
            this.icon = item.getIcon(getContext());
            icon.setBounds(0, 0, iconSize, iconSize);
            if (invertedTheme) {
                if (expanded) {
                    icon.setColorFilter(colorActive, PorterDuff.Mode.SRC_ATOP);
                } else {
                    icon.setColorFilter(colorInactive, PorterDuff.Mode.SRC_ATOP);
                }
            } else {
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }

            if (!expanded) {
                if (!invertedTheme) {
                    icon.setAlpha((int) (minAlpha * 255));
                }
            }
        }

        if (changed) {
            int w = right - left;
            int centerX = (w - iconSize) / 2;
            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);
        }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (textDirty) {
            measureText();
            textDirty = false;
        }

        this.textX = (getWidth() - textWidth) / 2;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        icon.draw(canvas);
        canvas.drawText(
            item.getTitle(),
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
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setTypeface(final SoftReference<Typeface> typeface) {
        if (null != typeface) {
            Typeface tf = typeface.get();
            if (null != tf) {
                textPaint.setTypeface(tf);
            } else {
                textPaint.setTypeface(Typeface.DEFAULT);
            }

            textDirty = true;
            requestLayout();
        }
    }
}
