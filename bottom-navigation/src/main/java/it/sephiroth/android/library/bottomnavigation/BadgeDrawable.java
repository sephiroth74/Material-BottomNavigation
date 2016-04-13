package it.sephiroth.android.library.bottomnavigation;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

/**
 * Created by crugnola on 4/12/16.
 */
public class BadgeDrawable extends Drawable {
    private static final String TAG = BadgeDrawable.class.getSimpleName();
    public static final float FADE_DURATION = 100f;
    public static final float ALPHA_MAX = 255f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long startTimeMillis;
    private boolean animating;
    private final int size;

    public BadgeDrawable(final int color, final int size) {
        super();
        this.paint.setColor(color);
        this.size = size;

        this.animating = true;
        this.startTimeMillis = 0;
    }

    public void setIsAnimating(boolean animating) {
        this.animating = animating;
    }

    @Override
    public void draw(final Canvas canvas) {
        if (!animating) {
            paint.setAlpha((int) ALPHA_MAX);
            drawInternal(canvas);
        } else {
            if (startTimeMillis == 0) {
                startTimeMillis = SystemClock.uptimeMillis();
            }

            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animating = false;
                paint.setAlpha((int) ALPHA_MAX);
                drawInternal(canvas);
            } else {
                int partialAlpha = (int) (ALPHA_MAX * normalized);
                setAlpha(partialAlpha);
                drawInternal(canvas);
            }
        }
    }

    private void drawInternal(final Canvas canvas) {
        Rect bounds = getBounds();
        final int w = bounds.width();
        final int h = bounds.height();
        canvas.drawCircle(bounds.centerX() + w / 2, bounds.centerY() - h / 2, w / 2, paint);
    }

    @Override
    public void setAlpha(final int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return paint.getAlpha();
    }

    @Override
    public boolean isStateful() {
        return false;
    }

    @Override
    public void setColorFilter(final ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }
}
