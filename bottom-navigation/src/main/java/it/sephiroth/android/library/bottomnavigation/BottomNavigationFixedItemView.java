package it.sephiroth.android.library.bottomnavigation;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import it.sephiroth.android.library.bottonnavigation.R;
import proguard.annotation.Keep;

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
public class BottomNavigationFixedItemView extends View {
    private static final String TAG = BottomNavigationFixedItemView.class.getSimpleName();
    private final int iconSize;
    private boolean expanded;
    private BottomNavigationItem item;
    private Drawable icon;
    private int centerY;
    private final Interpolator interpolator = new DecelerateInterpolator();
    private final Paint textPaint;
    private float textWidth;
    private long animationDuration;
    private final boolean invertedTheme;
    private final int colorActive;
    private final int colorInactive;
    private final ArgbEvaluator evaluator;

    private final int paddingTopActive;
    private final int paddingTopInactive;
    private final int paddingBottom;
    private final int paddingHorizontal;
    private final int textSizeActive;
    private final int textSizeInactive;

    private static final float TEXT_SCALE_ACTIVE = 1.1666666667f;
    private float canvasTextScale;
    private float iconTranslation;
    private int textCenterX;
    private int textCenterY;
    private int centerX;

    public BottomNavigationFixedItemView(final BottomNavigation parent, boolean expanded, boolean invertedTheme) {
        super(parent.getContext());

        final Resources res = getResources();
        animationDuration = res.getInteger(R.integer.bbn_shifting_item_animation_duration);

        paddingTopActive = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_active);
        paddingTopInactive = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_inactive);
        paddingBottom = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_bottom);
        paddingHorizontal = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_horizontal);
        textSizeActive = res.getDimensionPixelSize(R.dimen.bbn_fixed_text_size_active);
        textSizeInactive = res.getDimensionPixelSize(R.dimen.bbn_fixed_text_size_inactive);
        iconSize = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_icon_size);

        this.evaluator = new ArgbEvaluator();

        this.colorActive = parent.backgroundColorPrimary;
        this.colorInactive = parent.inactiveItemInvertedColor;

        this.invertedTheme = invertedTheme;
        this.expanded = expanded;
        this.centerY = expanded ? paddingTopActive : paddingTopInactive;
        this.canvasTextScale = expanded ? TEXT_SCALE_ACTIVE : 1f;
        this.iconTranslation = expanded ? 0 : (paddingTopInactive - paddingTopActive);

        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setHinting(Paint.HINTING_ON);
        this.textPaint.setLinearText(true);
        this.textPaint.setSubpixelText(true);
        this.textPaint.setTextSize(expanded ? textSizeActive : textSizeInactive);

        if (invertedTheme) {
            this.textPaint.setColor(expanded ? colorActive : colorInactive);
        }
    }

    void setItem(BottomNavigationItem item) {
        final Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.bbn_ripple_selector);
        if (invertedTheme) {
            MiscUtils.setDrawableColor(drawable, colorActive);
        }

        this.item = item;
        this.setId(item.getId());
        this.setBackground(drawable);
        this.setEnabled(item.isEnabled());
        this.textWidth = textPaint.measureText(item.getTitle());
    }

    public BottomNavigationItem getItem() {
        return item;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setExpanded(final boolean expanded) {
        this.expanded = expanded;

        final AnimatorSet set = new AnimatorSet();
        set.setDuration(animationDuration);
        set.setInterpolator(interpolator);

        final ValueAnimator animator1 = ObjectAnimator.ofFloat(this, "textScale", expanded ? TEXT_SCALE_ACTIVE : 1);

        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();

                if (expanded) {
                    icon.setColorFilter(
                        (Integer) evaluator.evaluate(fraction, colorInactive, colorActive), PorterDuff.Mode.SRC_ATOP);
                    textPaint.setColor((Integer) evaluator.evaluate(fraction, colorInactive, colorActive));
                } else {

                    icon.setColorFilter(
                        (Integer) evaluator.evaluate(fraction, colorActive, colorInactive), PorterDuff.Mode.SRC_ATOP);
                    textPaint.setColor((Integer) evaluator.evaluate(fraction, colorActive, colorInactive));
                }
            }
        });

        final ValueAnimator animator2 =
            ObjectAnimator.ofFloat(this, "iconTranslation", expanded ? 0 : (paddingTopInactive - paddingTopActive));

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
        }

        if (changed) {
            int w = right - left;
            centerX = (w - iconSize) / 2;
            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);

            textCenterX = getWidth() / 2;
            textCenterY = getHeight() - paddingBottom;
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(0, iconTranslation);
        icon.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.scale(canvasTextScale, canvasTextScale, textCenterX, textCenterY);

        canvas.drawText(
            item.getTitle(),
            ((getWidth()) - textWidth) / 2,
            getHeight() - paddingBottom,
            textPaint
        );

        canvas.restore();
    }

    @SuppressWarnings ("unused")
    @Keep
    public int getCenterY() {
        return centerY;
    }

    @SuppressWarnings ("unused")
    @Keep
    public void setCenterY(int value) {
        centerY = value;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @SuppressWarnings ("unused")
    @Keep
    public void setTextScale(final float value) {
        canvasTextScale = value;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @SuppressWarnings ("unused")
    @Keep
    public float getTextScale() {
        return canvasTextScale;
    }

    @Keep
    @SuppressWarnings ("unused")
    public void setIconTranslation(final float iconTranslation) {
        this.iconTranslation = iconTranslation;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Keep
    @SuppressWarnings ("unused")
    public float getIconTranslation() {
        return iconTranslation;
    }
}
