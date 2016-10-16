package it.sephiroth.android.library.bottomnavigation.app.providers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;

import java.util.HashMap;

import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class CustomBadgeProvider extends BadgeProvider {
    private final HashMap<Integer, Integer> countMap = new HashMap<>();

    public CustomBadgeProvider(final BottomNavigation navigation) {
        super(navigation);
    }

    public int getBadgeTextCount(@IdRes final int itemId) {
        if (countMap.containsKey(itemId)) {
            return countMap.get(itemId);
        }
        return 0;
    }

    public void show(@IdRes final int itemId, int count) {
        countMap.put(itemId, count);
        super.show(itemId);
    }

    @Override
    public void remove(@IdRes final int itemId) {
        countMap.remove(itemId);
        super.remove(itemId);
    }

    @Override
    protected Drawable newDrawable(@IdRes final int itemId, final int preferredColor) {
        int count = 1;
        if (countMap.containsKey(itemId)) {
            count = countMap.get(itemId);
        }
        return new Badge(preferredColor, count);
    }

    public static final class Badge extends Drawable {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        private String text;
        private float top;
        private float left;

        public Badge(final int color, final int count) {
            super();
            this.text = String.valueOf(count);

            paint.setColor(color);

            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(24);
        }

        @Override
        public void draw(final Canvas canvas) {
            final Rect rect = getBounds();
            canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 2, paint);
            canvas.drawText(text, 0, text.length(), left, top, textPaint);
        }

        @Override
        protected void onBoundsChange(final Rect bounds) {
            super.onBoundsChange(bounds);
            bounds.offset(bounds.width() / 2, -bounds.height() / 2);
            Paint.FontMetrics metrics = textPaint.getFontMetrics();
            float size = textPaint.measureText(text, 0, text.length());
            left = (bounds.left + (bounds.width() - size) / 2);
            top = bounds.centerY() - (metrics.ascent / 2) - metrics.descent / 2;
        }

        @Override
        public void setAlpha(final int alpha) {
            paint.setAlpha(alpha);
            textPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(final ColorFilter colorFilter) { }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return 50;
        }

        @Override
        public int getIntrinsicHeight() {
            return 50;
        }
    }
}
