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
    private final int colorActive;
    private final int colorInactive;
    private final int rippleColor;
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
    private float textX;
    private float textY;
    private boolean textDirty;

    public BottomNavigationFixedItemView(final BottomNavigation parent, boolean expanded) {
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

        this.colorActive = parent.fixedItemColorActive;
        this.colorInactive = parent.fixedItemColorInactive;
        this.rippleColor = parent.rippleColor;

        this.expanded = expanded;
        this.centerY = expanded ? paddingTopActive : paddingTopInactive;
        this.canvasTextScale = expanded ? TEXT_SCALE_ACTIVE : 1f;
        this.iconTranslation = expanded ? 0 : (paddingTopInactive - paddingTopActive);

        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setHinting(Paint.HINTING_ON);
        this.textPaint.setLinearText(true);
        this.textPaint.setSubpixelText(true);
        this.textPaint.setTextSize(textSizeInactive);
        this.textPaint.setColor(expanded ? colorActive : colorInactive);
    }

    void setItem(BottomNavigationItem item) {
        final Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.bbn_ripple_selector);
        MiscUtils.setDrawableColor(drawable, rippleColor);

        this.item = item;
        this.setId(item.getId());
        this.setBackground(drawable);
        this.setEnabled(item.isEnabled());

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
        Log.i(TAG, "onLayout(" + changed + ")");

        super.onLayout(changed, left, top, right, bottom);

        if (null == this.icon) {
            this.icon = item.getIcon(getContext());
            this.icon.setColorFilter(expanded ? colorActive : colorInactive, PorterDuff.Mode.SRC_ATOP);
            this.icon.setBounds(0, 0, iconSize, iconSize);
        }

        if (changed) {
            int w = right - left;
            centerX = (w - iconSize) / 2;
            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);

        }

        if (textDirty || changed) {
            measureText();
            textDirty = false;
        }
    }

    private void measureText() {
        Log.i(TAG, "measureText");

        final int width = getWidth();
        final int height = getHeight();

        Log.v(TAG, "width: " + width);
        Log.v(TAG, "height: " + height);

        textWidth = textPaint.measureText(item.getTitle());
        Log.v(TAG, "textWidth: " + textWidth);

        textX = paddingHorizontal + (((width - paddingHorizontal * 2) - textWidth) / 2);
        textY = height - paddingBottom;
        textCenterX = width / 2;
        textCenterY = height - paddingBottom;

        Log.v(TAG, "textX: " + textX);
        Log.v(TAG, "textY: " + textY);
        Log.v(TAG, "textCenterX: " + textCenterX);
        Log.v(TAG, "textCenterY: " + textCenterY);
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
            textX,
            textY,
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
