package it.sephiroth.android.library.bottomnavigation;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import it.sephiroth.android.library.bottonnavigation.R;

/**
 * Created by crugnola on 4/4/16.
 * MaterialBottomNavigation
 */
public class FixedLayout extends ViewGroup implements ItemsLayoutContainer {
    private static final String TAG = FixedLayout.class.getSimpleName();
    private final int maxActiveItemWidth;
    private final int minActiveItemWidth;
    private int totalChildrenSize;
    private boolean hasFrame;
    private int selectedIndex;
    OnItemClickListener listener;
    private int itemFinalWidth;
    private MenuParser.Menu menu;

    public FixedLayout(final Context context) {
        super(context);
        totalChildrenSize = 0;
        selectedIndex = 0;

        final Resources res = getResources();
        maxActiveItemWidth = res.getDimensionPixelSize(R.dimen.bbn_fixed_maxActiveItemWidth);
        minActiveItemWidth = res.getDimensionPixelSize(R.dimen.bbn_fixed_minActiveItemWidth);
    }

    @Override
    public void removeAll() {
        removeAllViews();
        totalChildrenSize = 0;
        itemFinalWidth = 0;
        selectedIndex = 0;
        menu = null;
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        if (!hasFrame || getChildCount() == 0) {
            return;
        }

        if (totalChildrenSize == 0) {
            totalChildrenSize = itemFinalWidth * (getChildCount() - 1) + itemFinalWidth;
        }

        int width = (r - l);
        int left = (width - totalChildrenSize) / 2;

        Log.v(TAG, "width: " + width);
        Log.v(TAG, "left: " + left);

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

        if (null != menu) {
            populateInternal(menu);
            menu = null;
        }
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        Log.v(TAG, "setChildFrame: " + left + ", " + top + ", " + width + ", " + height);
        child.layout(left, top, left + width, top + height);
    }

    @Override
    public void setSelectedIndex(final int index, final boolean animate) {
        Log.i(TAG, "setSelectedIndex: " + index);

        if (selectedIndex == index) {
            return;
        }

        int oldSelectedIndex = this.selectedIndex;
        this.selectedIndex = index;

        if (!hasFrame || getChildCount() == 0) {
            return;
        }

        final BottomNavigationFixedItemView current = (BottomNavigationFixedItemView) getChildAt(oldSelectedIndex);
        final BottomNavigationFixedItemView child = (BottomNavigationFixedItemView) getChildAt(index);

        current.setExpanded(false, 0, animate);
        child.setExpanded(true, 0, animate);
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void populate(@NonNull final MenuParser.Menu menu) {
        Log.i(TAG, "populate: " + menu);

        if (hasFrame) {
            populateInternal(menu);
        } else {
            this.menu = menu;
        }
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void populateInternal(@NonNull final MenuParser.Menu menu) {
        Log.d(TAG, "populateInternal");

        final BottomNavigation parent = (BottomNavigation) getParent();
        final float density = getResources().getDisplayMetrics().density;
        final int screenWidth = parent.getWidth();

        Log.v(TAG, "density: " + density);
        Log.v(TAG, "screenWidth: " + screenWidth);
        Log.v(TAG, "screenWidth(dp): " + (screenWidth / density));

        int proposedWidth = Math.min(Math.max(screenWidth / menu.getItemsCount(), minActiveItemWidth), maxActiveItemWidth);
        Log.v(TAG, "proposedWidth: " + proposedWidth);
        Log.v(TAG, "proposedWidth(dp): " + proposedWidth / density);

        if (proposedWidth * menu.getItemsCount() > screenWidth) {
            proposedWidth = screenWidth / menu.getItemsCount();
        }

        Log.v(TAG, "active size: " + maxActiveItemWidth + ", " + minActiveItemWidth);
        Log.v(TAG, "active size (dp): " + maxActiveItemWidth / density + ", " + minActiveItemWidth / density);

        this.itemFinalWidth = proposedWidth;

        for (int i = 0; i < menu.getItemsCount(); i++) {
            final BottomNavigationItem item = menu.getItemAt(i);
            Log.d(TAG, "item: " + item);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(proposedWidth, getHeight());

            BottomNavigationFixedItemView view =
                new BottomNavigationFixedItemView(parent, i == selectedIndex, menu);
            view.setItem(item);
            view.setLayoutParams(params);
            view.setClickable(true);
            view.setTypeface(parent.typeface);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (null != listener) {
                        listener.onItemClick(FixedLayout.this, v, finalI, true);
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
