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
import android.widget.ImageView;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;
import com.squareup.picasso.Picasso;

import it.sephiroth.android.library.bottomnavigation.BottomBehavior;
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

        BaseActivity activity = (BaseActivity) getActivity();
        final SystemBarConfig config = activity.getSystemBarTint().getConfig();

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

        Log.d(TAG, "navigationHeight: " + navigationHeight);
        Log.d(TAG, "actionbarHeight: " + actionbarHeight);

        final BottomNavigation navigation = activity.getBottomNavigation();
        if (null != navigation) {
            navigation.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    navigation.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    final CoordinatorLayout.LayoutParams coordinatorLayoutParams =
                        (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();

                    final CoordinatorLayout.Behavior behavior = coordinatorLayoutParams.getBehavior();
                    final MarginLayoutParams params = (MarginLayoutParams) mRecyclerView.getLayoutParams();

                    if (behavior instanceof BottomBehavior) {
                        final boolean scrollable = ((BottomBehavior) behavior).isScrollable();

                        Log.d(TAG, "scrollable: " + scrollable);
                        Log.d(TAG, "bottomNagivation: " + navigation.getNavigationHeight());
                        Log.d(TAG, "finalNavigationHeight: " + navigationHeight);

                        int totalHeight;

                        if (scrollable) {
                            totalHeight = navigationHeight;
                            params.bottomMargin -= navigationHeight;
                        } else {
                            totalHeight = navigation.getNavigationHeight();
                        }

                        Log.d(TAG, "totalHeight: " + totalHeight);
                        Log.d(TAG, "bottomMargin: " + params.bottomMargin);

                        createAdater(totalHeight);
                    } else {
                        params.bottomMargin -= navigationHeight;
                        createAdater(navigationHeight);
                    }
                    mRecyclerView.requestLayout();
                }
            });
        } else {
            final MarginLayoutParams params = (MarginLayoutParams) mRecyclerView.getLayoutParams();
            params.bottomMargin -= navigationHeight;
            createAdater(navigationHeight);
        }
    }

    private void createAdater(int height) {
        Log.i(getClass().getSimpleName(), "createAdapter(" + height + ")");
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new Adapter(getContext(), height, createData()));
    }

    public void scrollToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    static class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView description;
        final ImageView imageView;
        final int marginBottom;

        public TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
            description = (TextView) itemView.findViewById(android.R.id.text1);
            imageView = (ImageView) itemView.findViewById(android.R.id.icon);
            marginBottom = ((MarginLayoutParams) itemView.getLayoutParams()).bottomMargin;
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {
        private final Picasso picasso;
        private final int navigationHeight;
        private final Book[] data;

        public Adapter(final Context context, final int navigationHeight, final Book[] data) {
            this.navigationHeight = navigationHeight;
            this.data = data;
            this.picasso = Picasso.with(context);
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getContext()).inflate(R.layout.simple_card_item, parent, false);
            final TwoLinesViewHolder holder = new TwoLinesViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, final int position) {
            if (position == getItemCount() - 1) {
                ((MarginLayoutParams) holder.itemView.getLayoutParams()).bottomMargin = holder.marginBottom + navigationHeight;
            } else {
                ((MarginLayoutParams) holder.itemView.getLayoutParams()).bottomMargin = holder.marginBottom;
            }

            final Book item = data[position];
            holder.title.setText(item.title);
            holder.description.setText("By " + item.author);
            holder.imageView.setImageBitmap(null);

            picasso.cancelRequest(holder.imageView);

            picasso
                .load(item.imageUrl)
                .noPlaceholder()
                .resizeDimen(R.dimen.simple_card_image_width, R.dimen.simple_card_image_height)
                .centerCrop()
                .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }

    private Book[] createData() {
        return new Book[]{
            new Book("The Flight", "Scott Masterson", "http://i.imgur.com/dyyP2iO.jpg"),
            new Book("Room of Plates", "Ali Conners", "http://i.imgur.com/da6QIlR.jpg"),
            new Book("The Sleek Boot", "Sandra Adams", "http://i.imgur.com/YHoOJh4.jpg"),
            new Book("Night Hunting", "Janet Perkins", "http://i.imgur.com/3jxqrKP.jpg"),
            new Book("Rain and Coffee", "Peter Carlsson", "http://i.imgur.com/AZRynvM.jpg"),
            new Book("Ocean View", "Trevor Hansen", "http://i.imgur.com/IvhOJcw.jpg"),
            new Book("Lovers Of The Roof", "Britta Holt", "http://i.imgur.com/pxgI1b4.png"),
            new Book("Lessons from Delhi", "Mary Johnson", "http://i.imgur.com/oT1WYX9.jpg"),
            new Book("Mountaineers", "Abbey Christensen", "http://i.imgur.com/CLLDz.jpg"),
            new Book("Plains In The Night", "David Park", "http://i.imgur.com/7MrSvXE.jpg?1"),
            new Book("Dear Olivia", "Sylvia Sorensen", "http://i.imgur.com/3mkUuux.jpg"),
            new Book("Driving Lessons", "Halime Carver", "http://i.imgur.com/LzYAfFL.jpg"),
        };
    }

    static class Book {
        final String title;
        final String author;
        final String imageUrl;

        Book(final String title, final String author, final String imageUrl) {
            this.title = title;
            this.author = author;
            this.imageUrl = imageUrl;
        }
    }
}
