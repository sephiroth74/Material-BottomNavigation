package it.sephiroth.android.library.bottomnavigation.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
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

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new Adapter());
    }

    static class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView description;

        public TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
            description = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {

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
