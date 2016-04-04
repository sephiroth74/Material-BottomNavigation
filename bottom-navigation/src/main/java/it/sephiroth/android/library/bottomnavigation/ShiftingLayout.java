package it.sephiroth.android.library.bottomnavigation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by crugnola on 4/4/16.
 * MaterialBottomNavigation
 */
public class ShiftingLayout extends ViewGroup {
    private static final String TAG = ShiftingLayout.class.getSimpleName();
    private int totalChildrenSize;
    private boolean hasFrame;
    private int minSize, maxSize;
    private int selectedIndex;
    private final Interpolator interpolator = new DecelerateInterpolator();

    public ShiftingLayout(final Context context) {
        super(context);
        totalChildrenSize = 0;
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        if (!hasFrame || getChildCount() == 0) {
            return;
        }

        if (totalChildrenSize == 0) {
            totalChildrenSize = minSize * (getChildCount() - 1) + maxSize;
        }

        int width = (r - l);
        int left = (width - totalChildrenSize) / 2;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            final LayoutParams params = child.getLayoutParams();
            setChildFrame(child, left, 0, params.width, params.height);
            left += child.getWidth();
        }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        hasFrame = true;
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        // Log.v(TAG, "setChildFrame: " + left + ", " + top + ", " + width + ", " + height);
        child.layout(left, top, left + width, top + height);
    }

    public void setTotalSize(final int minSize, final int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public void setSelectedChild(final int index) {
        Log.i(TAG, "setSelectedChild: " + index);

        if (selectedIndex == index) {
            return;
        }

        int oldSelectedIndex = this.selectedIndex;
        this.selectedIndex = index;

        if (!hasFrame || getChildCount() == 0) {
            return;
        }

        final BottomNavigationShiftingItemView current = (BottomNavigationShiftingItemView) getChildAt(oldSelectedIndex);
        final BottomNavigationShiftingItemView child = (BottomNavigationShiftingItemView) getChildAt(index);

        current.setExpanded(false, minSize);
        child.setExpanded(true, maxSize);
//
//        final ValueAnimator animator1 = ValueAnimator.ofInt(current.getLayoutParams().width, minSize);
//
//        final ValueAnimator animator2 = ValueAnimator.ofInt(child.getLayoutParams().width, maxSize);
//
//        AnimatorSet set = new AnimatorSet();
//        set.setInterpolator(interpolator);
//        set.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
//        set.playTogether(animator1, animator2);
//        set.start();
//
//        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(final ValueAnimator animation) {
//                current.getLayoutParams().width = (int) animation.getAnimatedValue();
//            }
//        });
//
//        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(final ValueAnimator animation) {
//                child.getLayoutParams().width = (int) animation.getAnimatedValue();
//                child.requestLayout();
//            }
//        });
//
//        set.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(final Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(final Animator animation) {
//                current.setExpanded(false);
//                child.setExpanded(true);
//            }
//
//            @Override
//            public void onAnimationCancel(final Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(final Animator animation) {
//
//            }
//        });

    }
}
