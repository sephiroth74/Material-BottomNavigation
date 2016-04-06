package it.sephiroth.android.library.bottomnavigation;

import android.graphics.Paint;
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
    private BottomNavigationItem item;
    private final int rippleColor;
    private boolean expanded;
    protected final Paint textPaint;
    protected boolean textDirty;

    public BottomNavigationItemViewAbstract(final BottomNavigation parent, final boolean expanded) {
        super(parent.getContext());
        this.rippleColor = parent.rippleColor;
        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textDirty = true;
        this.expanded = expanded;
    }

    void setItem(BottomNavigationItem item) {
        final Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.bbn_ripple_selector);
        MiscUtils.setDrawableColor(drawable, rippleColor);

        this.item = item;
        this.setId(item.getId());
        this.setBackground(drawable);
        this.setEnabled(item.isEnabled());
    }

    protected abstract void onStatusChanged(final boolean expanded, final int size);

    public final BottomNavigationItem getItem() {
        return item;
    }

    public final boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(final boolean expanded, int newSize) {
        if (this.expanded != expanded) {
            this.expanded = expanded;
            onStatusChanged(expanded, newSize);
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
