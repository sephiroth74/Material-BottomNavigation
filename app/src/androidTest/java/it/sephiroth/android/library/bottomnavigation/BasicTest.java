package it.sephiroth.android.library.bottomnavigation;

import android.os.SystemClock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import it.sephiroth.android.library.bottomnavigation.app.MainActivity;
import it.sephiroth.android.library.bottomnavigation.app.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by alessandro on 4/9/16 at 7:34 PM.
 * Project: Material-BottomNavigation
 */
@RunWith (AndroidJUnit4.class)
@LargeTest
public class BasicTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void changeSelection() {
        SystemClock.sleep(200);
        onView(withId(R.id.bbn_item2)).perform(click());
        onView(withId(R.id.BottomNavigation)).check(matches(BottomNavigationMatcher.withSelection(1)));
    }

    @Test
    public void checkExpandedStatus() {
        SystemClock.sleep(200);
        onView(withId(android.R.id.content)).perform(ViewActionsCompat.swipeUp(CustomSwipe.MEDIUM));
        SystemClock.sleep(500);
        onView(withId(R.id.BottomNavigation)).check(matches(BottomNavigationMatcher.withExpandStatus(false)));

        onView(withId(android.R.id.content)).perform(ViewActionsCompat.swipeDown(CustomSwipe.MEDIUM));
        SystemClock.sleep(500);
        onView(withId(R.id.BottomNavigation)).check(matches(BottomNavigationMatcher.withExpandStatus(true)));
    }
}
