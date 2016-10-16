package it.sephiroth.android.library.bottomnavigation;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by crugnola on 4/12/16.
 */
public class RecyclerViewActionsCompat {

    public static <VH extends RecyclerView.ViewHolder> ViewAction smoothScrollToPosition(final int position) {
        return new SmoothScrollToPositionViewAction(position);
    }

    private static final class SmoothScrollToPositionViewAction implements ViewAction {
        private final int position;

        private SmoothScrollToPositionViewAction(int position) {
            this.position = position;
        }

        @SuppressWarnings ("unchecked")
        @Override
        public Matcher<View> getConstraints() {
            return allOf(isAssignableFrom(RecyclerView.class), isDisplayed());
        }

        @Override
        public String getDescription() {
            return "scroll RecyclerView to position: " + position;
        }

        @Override
        public void perform(UiController uiController, View view) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.smoothScrollToPosition(position);
        }
    }
}
