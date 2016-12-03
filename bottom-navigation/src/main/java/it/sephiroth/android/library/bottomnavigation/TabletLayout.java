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
public class TabletLayout extends ViewGroup implements ItemsLayoutContainer {
    private static final String TAG = TabletLayout.class.getSimpleName();
    private final int itemHeight;
    private final int paddingTop;
    private boolean hasFrame;
    private int selectedIndex;
    OnItemClickListener listener;
    private MenuParser.Menu menu;

    public TabletLayout(final Context context) {
        super(context);
        final Resources res = getResources();
        selectedIndex = 0;
        itemHeight = res.getDimensionPixelSize(R.dimen.bbn_tablet_item_height);
        paddingTop = res.getDimensionPixelSize(R.dimen.bbn_tablet_layout_padding_top);
    }

    @Override
    public void removeAll() {
        removeAllViews();
        selectedIndex = 0;
        menu = null;
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        if (!hasFrame || getChildCount() == 0) {
            return;
        }
        int top = paddingTop;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            final LayoutParams params = child.getLayoutParams();
            setChildFrame(child, 0, top, params.width, params.height);
            top += child.getHeight();
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
        MiscUtils.log(TAG, Log.VERBOSE, "setChildFrame: " + left + ", " + top + ", " + width + ", " + height);
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

        final BottomNavigationTabletItemView current = (BottomNavigationTabletItemView) getChildAt(oldSelectedIndex);
        final BottomNavigationTabletItemView child = (BottomNavigationTabletItemView) getChildAt(index);

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

        for (int i = 0; i < menu.getItemsCount(); i++) {
            final BottomNavigationItem item = menu.getItemAt(i);
            MiscUtils.log(TAG, Log.DEBUG, "item: " + item);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), itemHeight);

            BottomNavigationTabletItemView view =
                new BottomNavigationTabletItemView(parent, i == selectedIndex, menu);
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
                            listener.onItemPressed(TabletLayout.this, v, true);
                        }
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        if (null != listener) {
                            listener.onItemPressed(TabletLayout.this, v, false);
                        }
                    }
                    return false;
                }
            });
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (null != listener) {
                        listener.onItemClick(TabletLayout.this, v, finalI, true);
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
