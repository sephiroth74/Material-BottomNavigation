package it.sephiroth.android.library.bottomnavigation;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.sephiroth.android.library.bottomnavigation.app.MainActivity;
import it.sephiroth.android.library.bottomnavigation.app.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

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
        onView(withId(R.id.bbn_item2)).perform(click());
        onView(withId(R.id.BottomNavigation)).check(matches(BottomNavigationMatcher.withSelection(1)));
    }
}