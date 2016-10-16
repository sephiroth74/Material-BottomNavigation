package it.sephiroth.android.library.bottomnavigation;

import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.lang.ref.SoftReference;

import it.sephiroth.android.library.bottonnavigation.R;

/**
 * Created by crugnola on 4/6/16.
 */
abstract class BottomNavigationItemViewAbstract extends View {
    public static final float ALPHA_MAX = 255f;
    private BottomNavigationItem item;
    private final int rippleColor;
    private boolean expanded;
    protected final Paint textPaint;
    protected boolean textDirty;
    protected final ArgbEvaluator evaluator;
    private final BadgeProvider provider;
    protected Drawable badge;
    protected Drawable icon;

    public BottomNavigationItemViewAbstract(final BottomNavigation parent, final boolean expanded, final MenuParser.Menu menu) {
        super(parent.getContext());
        this.evaluator = new ArgbEvaluator();
        this.rippleColor = menu.getRippleColor();
        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textDirty = true;
        this.expanded = expanded;
        this.provider = parent.getBadgeProvider();
    }

    void setItem(BottomNavigationItem item) {
        final Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.bbn_ripple_selector);
        drawable.mutate();
        MiscUtils.setDrawableColor(drawable, rippleColor);

        this.item = item;
        this.setId(item.getId());
        this.setBackground(drawable);
        this.setEnabled(item.isEnabled());
        invalidateBadge();
    }

    void invalidateBadge() {
        Drawable d = provider.getBadge(getId());

        if (badge != d) {
            if (null != badge) {
                badge.setCallback(null);
                badge = null;
            }
            badge = d;

            if (null != badge) {
                badge.setCallback(this);
                if (badge instanceof BadgeDrawable && null == getParent()) {
                    ((BadgeDrawable) badge).setIsAnimating(false);
                }
            }

            if (null != getParent()) {
                invalidate();
            }
        }
    }

    @Override
    public void invalidateDrawable(final Drawable drawable) {
        super.invalidateDrawable(drawable);

        if (drawable == badge) {
            invalidate();
        }
    }

    protected abstract void onStatusChanged(final boolean expanded, final int size, final boolean animate);

    public final BottomNavigationItem getItem() {
        return item;
    }

    public final boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(final boolean expanded, int newSize, boolean animate) {
        if (this.expanded != expanded) {
            this.expanded = expanded;
            onStatusChanged(expanded, newSize, animate);
        }
    }

    protected final void drawBadge(final Canvas canvas) {
        if (null != badge && null != icon) {
            Rect bounds = icon.getBounds();
            badge.setBounds(bounds.right - badge.getIntrinsicWidth(), bounds.top, bounds.right,
                bounds.top + badge.getIntrinsicHeight()
            );
            badge.draw(canvas);
        }
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
