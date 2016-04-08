package it.sephiroth.android.library.bottomnavigation.app;

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
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;

import it.sephiroth.android.library.bottomnavigation.Behavior;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG = MainActivityFragment.class.getSimpleName();
    RecyclerView mRecyclerView;

    public MainActivityFragment() { }

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

        MainActivity activity = (MainActivity) getActivity();
        final SystemBarConfig config = activity.getSystemBarTint().getConfig();

        int navigationHeight = 0;
        int actionbarHeight = 0;

        if (activity.hasTranslucentNavigation()) {
            navigationHeight = config.getNavigationBarHeight();
        }

        if (activity.hasTranslucentStatusBar()) {
            actionbarHeight = config.getActionBarHeight();
        }

        final BottomNavigation navigation = activity.mBottomNavigation;
        if (null != navigation) {
            final int finalNavigationHeight = navigationHeight;
            final int finalActionbarHeight = actionbarHeight;
            navigation.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.i(TAG, "onGlobalLayout");
                    navigation.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    final CoordinatorLayout.LayoutParams coordinatorLayoutParams =
                        (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
                    final Behavior behavior = (Behavior) coordinatorLayoutParams.getBehavior();
                    final boolean scrollable = behavior.isScrollable();
                    int totalHeight = finalNavigationHeight + navigation.getNavigationHeight() - finalActionbarHeight;
                    final MarginLayoutParams params = (MarginLayoutParams) mRecyclerView.getLayoutParams();

                    Log.v(TAG, "scrollable: " + scrollable);
                    Log.v(TAG, "navigationHeight: " + finalNavigationHeight);

                    if (scrollable) {
                        totalHeight = finalNavigationHeight;
                        params.bottomMargin -= finalNavigationHeight;
                    }

                    mRecyclerView.requestLayout();
                    createAdater(totalHeight);
                }
            });
        } else {
            createAdater(navigationHeight - actionbarHeight);
        }
    }

    private void createAdater(int height) {
        Log.i(getClass().getSimpleName(), "createAdapter(" + height + ")");
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new Adapter(height));
    }

    static class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView description;
        final int marginBottom;

        public TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
            description = (TextView) itemView.findViewById(android.R.id.text1);
            marginBottom = ((MarginLayoutParams) itemView.getLayoutParams()).bottomMargin;
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {

        private final int navigationHeight;

        public Adapter(final int navigationHeight) {
            this.navigationHeight = navigationHeight;
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getContext()).inflate(R.layout.simple_card_item, parent, false);
            final TwoLinesViewHolder holder = new TwoLinesViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onItemClick(holder.getAdapterPosition());
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, final int position) {
            if (position == getItemCount() - 1) {
                ((MarginLayoutParams) holder.itemView.getLayoutParams()).bottomMargin = holder.marginBottom + navigationHeight;
            } else {
                ((MarginLayoutParams) holder.itemView.getLayoutParams()).bottomMargin = holder.marginBottom;
            }

            switch (position) {
                case 0:
                    holder.title.setText("3 items");
                    holder.description.setText("Switch to BottomNavigation with 3 fixed items");
                    break;

                case 1:
                    holder.title.setText("3 items no background");
                    holder.description.setText("Switch to BottomNavigation with 3 fixed items without changing background");
                    break;

                case 2:
                    holder.title.setText("4 items");
                    holder.description.setText("Switch to BottomNavigation with 4 shifting items");
                    break;

                case 3:
                    holder.title.setText("4 items no background");
                    holder.description.setText("Switch to BottomNavigation with 4 shifting items without changing background");
                    break;

                case 4:
                    holder.title.setText("5 items");
                    holder.description.setText("Switch to BottomNavigation with 5 shifting items");
                    break;

                case 5:
                    holder.title.setText("5 items no background");
                    holder.description.setText("Switch to BottomNavigation with 5 shfting items without changing background");
                    break;

                default:
                    holder.title.setText("Item " + position);
                    holder.description.setText("Description\nDescription line 2");
                    break;

            }
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        private void onItemClick(final int position) {
            MainActivity activity = ((MainActivity) getActivity());
            switch (position) {
                case 0:
                    activity.setMenuType(MainActivity.MENU_TYPE_3_ITEMS);
                    break;

                case 1:
                    activity.setMenuType(MainActivity.MENU_TYPE_3_ITEMS_NO_BACKGROUND);
                    break;

                case 2:
                    activity.setMenuType(MainActivity.MENU_TYPE_4_ITEMS);
                    break;

                case 3:
                    activity.setMenuType(MainActivity.MENU_TYPE_4_ITEMS_NO_BACKGROUND);
                    break;

                case 4:
                    activity.setMenuType(MainActivity.MENU_TYPE_5_ITEMS);
                    break;

                case 5:
                    activity.setMenuType(MainActivity.MENU_TYPE_5_ITEMS_NO_BACKGROUND);
                    break;
            }
        }
    }
}
