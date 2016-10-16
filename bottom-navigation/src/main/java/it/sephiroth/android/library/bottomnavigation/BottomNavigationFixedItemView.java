package it.sephiroth.android.library.bottomnavigation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import it.sephiroth.android.library.bottonnavigation.R;
import proguard.annotation.Keep;

import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
@SuppressLint ("ViewConstructor")
public class BottomNavigationFixedItemView extends BottomNavigationItemViewAbstract {
    private static final String TAG = BottomNavigationFixedItemView.class.getSimpleName();
    private final int iconSize;
    private int centerY;
    private final Interpolator interpolator = new DecelerateInterpolator();
    private float textWidth;
    private long animationDuration;
    private final int colorActive;
    private final int colorInactive;

    private final int paddingTopActive;
    private final int paddingTopInactive;
    private final int paddingBottom;
    private final int paddingHorizontal;
    private final int textSizeInactive;

    private static final float TEXT_SCALE_ACTIVE = 1.1666666667f;
    private float canvasTextScale;
    private float iconTranslation;
    private int textCenterX;
    private int textCenterY;
    private int centerX;
    private float textX;
    private float textY;

    public BottomNavigationFixedItemView(final BottomNavigation parent, boolean expanded, final MenuParser.Menu menu) {
        super(parent, expanded, menu);

        final Resources res = getResources();
        this.paddingTopActive = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_active);
        this.paddingTopInactive = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_inactive);
        this.paddingBottom = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_bottom);
        this.paddingHorizontal = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_horizontal);
        this.textSizeInactive = res.getDimensionPixelSize(R.dimen.bbn_fixed_text_size_inactive);
        this.iconSize = res.getDimensionPixelSize(R.dimen.bbn_fixed_item_icon_size);

        this.animationDuration = menu.getItemAnimationDuration();
        this.colorActive = menu.getColorActive();
        this.colorInactive = menu.getColorInactive();
        this.centerY = paddingTopActive;
        this.canvasTextScale = expanded ? TEXT_SCALE_ACTIVE : 1f;
        this.iconTranslation = expanded ? 0 : (paddingTopInactive - paddingTopActive);

        log(TAG, DEBUG, "colors: %x, %x", colorInactive, colorActive);

        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setHinting(Paint.HINTING_ON);
        this.textPaint.setLinearText(true);
        this.textPaint.setSubpixelText(true);
        this.textPaint.setTextSize(textSizeInactive);
        this.textPaint.setColor(expanded ? colorActive : colorInactive);
    }

    @Override
    protected void onStatusChanged(final boolean expanded, final int size, final boolean animate) {

        if (!animate) {
            updateLayoutOnAnimation(1, expanded);
            setIconTranslation(expanded ? 0 : (paddingTopInactive - paddingTopActive));
            return;
        }

        final AnimatorSet set = new AnimatorSet();
        set.setDuration(animationDuration);
        set.setInterpolator(interpolator);

        final ValueAnimator animator1 = ObjectAnimator.ofFloat(this, "textScale", expanded ? TEXT_SCALE_ACTIVE : 1);

        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();
                updateLayoutOnAnimation(fraction, expanded);
            }
        });

        final ValueAnimator animator2 =
            ObjectAnimator.ofFloat(this, "iconTranslation", expanded ? 0 : (paddingTopInactive - paddingTopActive));

        set.playTogether(animator1, animator2);
        set.start();
    }

    private void updateLayoutOnAnimation(final float fraction, final boolean expanded) {
        if (expanded) {
            final int color = (Integer) evaluator.evaluate(fraction, colorInactive, colorActive);
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            textPaint.setColor(color);
            icon.setAlpha(Color.alpha(color));
        } else {
            final int color = (Integer) evaluator.evaluate(fraction, colorActive, colorInactive);
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            textPaint.setColor(color);
            icon.setAlpha(Color.alpha(color));
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        log(TAG, INFO, "onLayout(%b)", changed);

        super.onLayout(changed, left, top, right, bottom);

        if (null == this.icon) {
            this.icon = getItem().getIcon(getContext());
            this.icon.setColorFilter(isExpanded() ? colorActive : colorInactive, PorterDuff.Mode.SRC_ATOP);
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
        log(TAG, INFO, "measureText");

        final int width = getWidth();
        final int height = getHeight();

        textWidth = textPaint.measureText(getItem().getTitle());
        textX = paddingHorizontal + (((width - paddingHorizontal * 2) - textWidth) / 2);
        textY = height - paddingBottom;
        textCenterX = width / 2;
        textCenterY = height - paddingBottom;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(0, iconTranslation);
        icon.draw(canvas);
        drawBadge(canvas);
        canvas.restore();

        canvas.save();
        canvas.scale(canvasTextScale, canvasTextScale, textCenterX, textCenterY);

        canvas.drawText(
            getItem().getTitle(),
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

}
