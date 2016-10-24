package it.sephiroth.android.library.bottomnavigation.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;

import it.sephiroth.android.library.bottomnavigation.BottomBehavior;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottomnavigation.MiscUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class EnableDisableActivityFragment extends Fragment implements BottomNavigation.OnMenuChangedListener {
    private static final String TAG = EnableDisableActivityFragment.class.getSimpleName();
    RecyclerView mRecyclerView;
    private SystemBarConfig config;
    private ToolbarScrollHelper scrollHelper;

    public EnableDisableActivityFragment() { }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView01);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final BaseActivity activity = (BaseActivity) getActivity();
        config = activity.getSystemBarTint().getConfig();

        final int navigationHeight;
        final int actionbarHeight;

        if (activity.hasTranslucentNavigation()) {
            navigationHeight = config.getNavigationBarHeight();
        } else {
            navigationHeight = 0;
        }

        if (activity.hasTranslucentStatusBar()) {
            actionbarHeight = config.getActionBarHeight();
        } else {
            actionbarHeight = 0;
        }

        MiscUtils.log(TAG, Log.VERBOSE, "navigationHeight: " + navigationHeight);
        MiscUtils.log(TAG, Log.VERBOSE, "actionbarHeight: " + actionbarHeight);

        final BottomNavigation navigation = activity.getBottomNavigation();
        if (null != navigation) {
            navigation.setOnMenuChangedListener(this);
            navigation.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    navigation.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    final CoordinatorLayout.LayoutParams coordinatorLayoutParams =
                        (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();

                    final CoordinatorLayout.Behavior behavior = coordinatorLayoutParams.getBehavior();
                    final MarginLayoutParams params = (MarginLayoutParams) mRecyclerView.getLayoutParams();

                    MiscUtils.log(TAG, Log.VERBOSE, "behavior: %s", behavior);
                    MiscUtils.log(TAG, Log.VERBOSE, "finalNavigationHeight: " + navigationHeight);
                    MiscUtils.log(TAG, Log.VERBOSE, "bottomNagivation: " + navigation.getNavigationHeight());

                    if (behavior instanceof BottomBehavior) {
                        final boolean scrollable = ((BottomBehavior) behavior).isScrollable();

                        MiscUtils.log(TAG, Log.VERBOSE, "scrollable: " + scrollable);

                        int totalHeight;

                        if (scrollable) {
                            totalHeight = navigationHeight;
                            params.bottomMargin -= navigationHeight;
                        } else {
                            totalHeight = navigation.getNavigationHeight();
                        }

                        MiscUtils.log(TAG, Log.VERBOSE, "totalHeight: " + totalHeight);
                        MiscUtils.log(TAG, Log.VERBOSE, "bottomMargin: " + params.bottomMargin);

                        createAdater(totalHeight, activity.hasAppBarLayout());
                    } else {
                        params.bottomMargin -= navigationHeight;
                        createAdater(navigationHeight, activity.hasAppBarLayout());
                    }
                    mRecyclerView.requestLayout();
                }
            });
        } else {
            final MarginLayoutParams params = (MarginLayoutParams) mRecyclerView.getLayoutParams();
            params.bottomMargin -= navigationHeight;
            createAdater(navigationHeight, activity.hasAppBarLayout());
        }

        if (!activity.hasAppBarLayout()) {
            scrollHelper = new ToolbarScrollHelper(activity, activity.getToolbar());
            scrollHelper.initialize(mRecyclerView);
        }
    }

    public void onMenuItemSelect(final int index, final boolean fromUser) {
        Adapter adapter = (Adapter) mRecyclerView.getAdapter();
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    public void onMenuItemReselect(final int index, final boolean fromUser) {
        Adapter adapter = (Adapter) mRecyclerView.getAdapter();
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    BottomNavigation getBottomNavigation() {
        return ((BaseActivity) getActivity()).getBottomNavigation();
    }

    private void createAdater(int height, final boolean hasAppBarLayout) {
        final BottomNavigation navigation = getBottomNavigation();
        MiscUtils.log(getClass().getSimpleName(), Log.INFO, "createAdapter(" + height + ")");
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new Adapter(getContext(), height, hasAppBarLayout));

        if (null != navigation) {
            refreshAdapter();
        }
    }

    private void refreshAdapter() {
        Adapter adapter = (Adapter) mRecyclerView.getAdapter();
        if (null != adapter) {
            adapter.setData(getBottomNavigation());
        }
    }

    public void scrollToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onMenuChanged(final BottomNavigation parent) {
        refreshAdapter();
    }

    static class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final int marginBottom;
        final CompoundButton switch1;
        final CompoundButton switch2;
        final CompoundButton animate;

        public TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
            marginBottom = ((MarginLayoutParams) itemView.getLayoutParams()).bottomMargin;
            switch1 = (CompoundButton) itemView.findViewById(android.R.id.button1);
            switch2 = (CompoundButton) itemView.findViewById(android.R.id.button2);
            animate = (CompoundButton) itemView.findViewById(android.R.id.button3);
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {
        private final int navigationHeight;
        private final boolean hasAppBarLayout;
        private int count = 0;
        private BottomNavigation navigation;

        public Adapter(final Context context, final int navigationHeight, final boolean hasAppBarLayout) {
            this.navigationHeight = navigationHeight;
            this.hasAppBarLayout = hasAppBarLayout;
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getContext()).inflate(R.layout.enable_disable_card_item, parent, false);
            final TwoLinesViewHolder holder = new TwoLinesViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, final int position) {
            ((MarginLayoutParams) holder.itemView.getLayoutParams()).topMargin = 0;
            if (position == getItemCount() - 1) {
                ((MarginLayoutParams) holder.itemView.getLayoutParams()).bottomMargin = holder.marginBottom + navigationHeight;
            } else if (position == 0 && !hasAppBarLayout) {
                ((MarginLayoutParams) holder.itemView.getLayoutParams()).topMargin = scrollHelper.getToolbarHeight();
            } else {
                ((MarginLayoutParams) holder.itemView.getLayoutParams()).bottomMargin = holder.marginBottom;
            }

            holder.switch1.setOnCheckedChangeListener(null);
            holder.switch2.setOnCheckedChangeListener(null);

            holder.title.setText(navigation.getMenuItemTitle(position) + " (index: " + position + ")");
            holder.switch1.setChecked(navigation.getMenuItemEnabled(position));
            holder.switch2.setChecked(navigation.getSelectedIndex() == position);

            holder.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton compoundButton, final boolean checked) {
                    navigation.setMenuItemEnabled(holder.getAdapterPosition(), checked);
                    notifyDataSetChanged();
                }
            });

            holder.switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton compoundButton, final boolean checked) {
                    if(!checked) {
                        compoundButton.setChecked(true);
                        return;
                    }
                    navigation.setSelectedIndex(holder.getAdapterPosition(), holder.animate.isChecked());
                    notifyDataSetChanged();
                }
            });

        }

        @Override
        public int getItemCount() {
            return count;
        }

        public void setData(final BottomNavigation navigation) {
            this.navigation = navigation;
            this.count = navigation.getMenuItemCount();
            notifyDataSetChanged();
        }
    }

}
