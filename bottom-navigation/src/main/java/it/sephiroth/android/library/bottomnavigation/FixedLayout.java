package it.sephiroth.android.library.bottomnavigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import it.sephiroth.android.library.bottonnavigation.R;

import static android.util.Log.INFO;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

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
        child.layout(left, top, left + width, top + height);
    }

    @Override
    public void setSelectedIndex(final int index, final boolean animate) {
        MiscUtils.log(TAG, Log.INFO, "setSelectedIndex: " + index);

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

        if (null != current) {
            current.setExpanded(false, 0, animate);
        }
        if (null != child) {
            child.setExpanded(true, 0, animate);
        }
    }

    @Override
    public void setItemEnabled(final int index, final boolean enabled) {
        log(TAG, INFO, "setItemEnabled(%d, %b)", index, enabled);
        final BottomNavigationItemViewAbstract child = (BottomNavigationItemViewAbstract) getChildAt(index);
        if (null != child) {
            child.setEnabled(enabled);
            child.postInvalidate();
            requestLayout();
        }
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void populate(@NonNull final MenuParser.Menu menu) {
        MiscUtils.log(TAG, Log.INFO, "populate: " + menu);

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
        MiscUtils.log(TAG, Log.DEBUG, "populateInternal");

        final BottomNavigation parent = (BottomNavigation) getParent();
        final float density = getResources().getDisplayMetrics().density;
        final int screenWidth = parent.getWidth();

        int proposedWidth = Math.min(Math.max(screenWidth / menu.getItemsCount(), minActiveItemWidth), maxActiveItemWidth);

        if (proposedWidth * menu.getItemsCount() > screenWidth) {
            proposedWidth = screenWidth / menu.getItemsCount();
        }

        this.itemFinalWidth = proposedWidth;

        for (int i = 0; i < menu.getItemsCount(); i++) {
            final BottomNavigationItem item = menu.getItemAt(i);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(proposedWidth, getHeight());

            BottomNavigationFixedItemView view =
                new BottomNavigationFixedItemView(parent, i == selectedIndex, menu);
            view.setItem(item);
            view.setLayoutParams(params);
            view.setClickable(true);
            view.setTypeface(parent.typeface);
            final int finalI = i;
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                @SuppressLint ("ClickableViewAccessibility")
                public boolean onTouch(final View v, final MotionEvent event) {
                    final int action = event.getActionMasked();
                    if (action == MotionEvent.ACTION_DOWN) {
                        if (null != listener) {
                            listener.onItemPressed(FixedLayout.this, v, true);
                        }
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        if (null != listener) {
                            listener.onItemPressed(FixedLayout.this, v, false);
                        }
                    }
                    return false;
                }
            });
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
