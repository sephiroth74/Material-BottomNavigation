package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import it.sephiroth.android.library.bottonnavigation.R;
import proguard.annotation.Keep;

/**
 * Created by crugnola on 4/4/16.
 * MaterialBottomNavigation
 */
public class ShiftingLayout extends ViewGroup implements ItemsLayoutContainer {
    private static final String TAG = ShiftingLayout.class.getSimpleName();
    private final int maxActiveItemWidth;
    private final int minActiveItemWidth;
    private final int maxInactiveItemWidth;
    private final int minInactiveItemWidth;
    private int totalChildrenSize;
    private int minSize, maxSize;
    private int selectedIndex;
    private boolean hasFrame;
    BottomNavigationItem[] entries;
    OnItemClickListener listener;

    public ShiftingLayout(final Context context) {
        super(context);
        totalChildrenSize = 0;
        maxActiveItemWidth = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_maxActiveItemWidth);
        minActiveItemWidth = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_minActiveItemWidth);
        maxInactiveItemWidth = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_maxInactiveItemWidth);
        minInactiveItemWidth = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_minInactiveItemWidth);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        Log.i(TAG, "onLayout(" + changed + ")");

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
        Log.i(TAG, "onSizeChanged(" + w + ", " + h + ")");
        super.onSizeChanged(w, h, oldw, oldh);
        hasFrame = true;

        if (null != entries) {
            populateInternal(entries);
            entries = null;
        }
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        // Log.v(TAG, "setChildFrame: " + left + ", " + top + ", " + width + ", " + height);
        child.layout(left, top, left + width, top + height);
    }

    public void setTotalSize(final int minSize, final int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    @Override
    public void setSelectedIndex(final int index) {
        Log.i(TAG, "setSelectedIndex: " + index);

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
    }

    @Override
    @Keep
    @SuppressWarnings ("unused")
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void populate(@NonNull final BottomNavigationItem[] entries) {
        Log.i(TAG, "populate: " + entries);

        if (hasFrame) {
            populateInternal(entries);
        } else {
            this.entries = entries;
        }
    }

    private void populateInternal(@NonNull final BottomNavigationItem[] entries) {
        Log.d(TAG, "populateInternal");

        final BottomNavigation parent = (BottomNavigation) getParent();
        final float density = getResources().getDisplayMetrics().density;
        final int screenWidth = parent.getWidth();

        Log.v(TAG, "density: " + density);
        Log.v(TAG, "screenWidth: " + screenWidth);
        Log.v(TAG, "screenWidth(dp): " + (screenWidth / density));

        int itemWidthMin;
        int itemWidthMax;

        final int totalWidth = maxInactiveItemWidth * (entries.length - 1) + maxActiveItemWidth;
        Log.v(TAG, "totalWidth: " + totalWidth);
        Log.v(TAG, "totalWidth(dp): " + totalWidth / density);

        if (totalWidth > screenWidth) {
            float ratio = (float) screenWidth / totalWidth;
            ratio = (float) ((double) Math.round(ratio * 10d) / 10d) + 0.05f;
            Log.v(TAG, "ratio: " + ratio);

            itemWidthMin = (int) Math.max(maxInactiveItemWidth * ratio, minInactiveItemWidth);
            itemWidthMax = (int) (maxActiveItemWidth * ratio);

            Log.d(TAG, "computing sizes...");
            Log.v(TAG, "itemWidthMin(dp): " + itemWidthMin / density);
            Log.v(TAG, "itemWidthMax(dp): " + itemWidthMax / density);
            Log.v(TAG, "total items size(dp): " + (itemWidthMin * (entries.length - 1) + itemWidthMax) / density);

            if (itemWidthMin * (entries.length - 1) + itemWidthMax > screenWidth) {
                itemWidthMax = screenWidth - (itemWidthMin * (entries.length - 1)); // minActiveItemWidth?
            }
        } else {
            itemWidthMax = maxActiveItemWidth;
            itemWidthMin = maxInactiveItemWidth;
        }

        Log.v(TAG, "active size: " + maxActiveItemWidth + ", " + minActiveItemWidth);
        Log.v(TAG, "inactive size: " + maxInactiveItemWidth + ", " + minInactiveItemWidth);

        Log.v(TAG, "active size (dp): " + maxActiveItemWidth / density + ", " + minActiveItemWidth / density);
        Log.v(TAG, "inactive size (dp): " + maxInactiveItemWidth / density + ", " + minInactiveItemWidth / density);

        Log.v(TAG, "itemWidth: " + itemWidthMin + ", " + itemWidthMax);
        Log.v(TAG, "itemWidth(dp): " + (itemWidthMin / density) + ", " + (itemWidthMax / density));

        setTotalSize(itemWidthMin, itemWidthMax);

        for (int i = 0; i < entries.length; i++) {
            final BottomNavigationItem item = entries[i];
            Log.d(TAG, "item: " + item);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemWidthMin, getHeight());

            if (i == selectedIndex) {
                params.width = itemWidthMax;
            }

            BottomNavigationShiftingItemView view =
                new BottomNavigationShiftingItemView(parent, i == selectedIndex, parent.isInvertedTheme());
            view.setItem(item);
            view.setLayoutParams(params);
            view.setClickable(true);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (null != listener) {
                        listener.onItemClick(ShiftingLayout.this, v, finalI);
                    }
                }
            });
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            addView(view);
        }
    }
}
