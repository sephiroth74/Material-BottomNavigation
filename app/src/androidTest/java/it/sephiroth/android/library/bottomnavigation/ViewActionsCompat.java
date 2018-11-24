package it.sephiroth.android.library.bottomnavigation;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swiper;
import androidx.test.espresso.action.ViewActions;

/**
 * Created by crugnola on 4/12/16.
 */
public class ViewActionsCompat {
    public static ViewAction swipeUp(final Swiper swipe) {
        return ViewActions.actionWithAssertions(new GeneralSwipeAction(
            swipe,
            GeneralLocation.CENTER,
            GeneralLocation.TOP_CENTER,
            Press.FINGER
        ));
    }

    public static ViewAction swipeDown(final Swiper swipe) {
        return ViewActions.actionWithAssertions(new GeneralSwipeAction(
            swipe,
            GeneralLocation.CENTER,
            GeneralLocation.BOTTOM_CENTER,
            Press.FINGER
        ));
    }
}
