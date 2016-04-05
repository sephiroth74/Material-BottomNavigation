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
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.lang.ref.SoftReference;

import it.sephiroth.android.library.bottonnavigation.R;

import static android.util.Log.INFO;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

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
    private final float maxAlpha;
    private final float minAlpha;
    private final Interpolator interpolator = new DecelerateInterpolator();
    private final Paint textPaint;
    private float textWidth;
    private long animationDuration;
    private final int colorActive;
    private final int rippleColor;
    private final ArgbEvaluator evaluator;
    private boolean textDirty;
    private float textX;
    private int textY;

    public BottomNavigationShiftingItemView(final BottomNavigation parent, boolean expanded) {
        super(parent.getContext());

        animationDuration = getResources().getInteger(R.integer.bbn_shifting_item_animation_duration);
        paddingTop = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_top);
        paddingBottomActive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_active);
        paddingBottomInactive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_inactive);
        iconSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_icon_size);
        textPaddingTop = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_text_padding_top);
        textSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_text_size);

        this.evaluator = new ArgbEvaluator();

        this.colorActive = parent.shiftingItemColorActive;
        this.rippleColor = parent.rippleColor;

        log(TAG, Log.INFO, "colorActive: %x", colorActive);

        this.maxAlpha = (float) Color.alpha(colorActive) / 255f;
        this.minAlpha = parent.shiftingItemAlphaInactive;

        log(TAG, Log.VERBOSE, "maxAlpha: %g", this.maxAlpha);
        log(TAG, Log.VERBOSE, "minAlpha: %g", this.minAlpha);

        this.expanded = expanded;
        this.centerY = expanded ? paddingTop : paddingBottomInactive;

        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setHinting(Paint.HINTING_ON);
        this.textPaint.setLinearText(true);
        this.textPaint.setSubpixelText(true);
        this.textPaint.setTextSize(textSize);
        this.textPaint.setColor(colorActive);

        if (!expanded) {
            this.textPaint.setAlpha(0);
        }

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
        log(TAG, INFO, "measureText");
        this.textWidth = textPaint.measureText(item.getTitle());
    }

    public BottomNavigationItem getItem() {
        return item;
    }

    public void setExpanded(final boolean expanded, int newSize) {
        log(TAG, INFO, "setExpanded(%b, %d)", expanded, newSize);
        this.expanded = expanded;

        final AnimatorSet set = new AnimatorSet();
        set.setDuration(animationDuration * 2);
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
                    icon.setAlpha((int) ((minAlpha + (fraction * (maxAlpha - minAlpha))) * 255));
                    textPaint.setAlpha((int) (((fraction * (maxAlpha))) * 255));
                } else {
                    float alpha = 1.0F - fraction;
                    icon.setAlpha((int) ((minAlpha + (alpha * (maxAlpha - minAlpha))) * 255));
                    textPaint.setAlpha((int) (((alpha * (maxAlpha))) * 255));
                }
            }
        });

        set.playTogether(animator1, animator2);
        set.start();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (null == this.icon) {
            this.icon = item.getIcon(getContext());
            icon.setBounds(0, 0, iconSize, iconSize);
            icon.setColorFilter(colorActive, PorterDuff.Mode.SRC_ATOP);
            icon.setAlpha((int) (expanded ? maxAlpha * 255 : minAlpha * 255));
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
        requestLayout();
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
