package it.sephiroth.android.library.bottomnavigation;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Created by alessandro on 4/9/16 at 7:42 PM.
 * Project: Material-BottomNavigation
 */
public class BottomNavigationMatcher {

    static BoundedMatcher<View, BottomNavigation> withExpandStatus(final boolean expanded) {
        return withExpandStatus(Matchers.is(expanded));
    }

    static BoundedMatcher<View, BottomNavigation> withSelection(final int selection) {
        return withSelection(Matchers.is(selection));
    }

    static BoundedMatcher<View, BottomNavigation> withExpandStatus(final Matcher<Boolean> boolMatcher) {
        return new BoundedMatcher<View, BottomNavigation>(BottomNavigation.class) {
            @Override
            public void describeTo(final Description description) {
                description.appendText("with expand status: ");
                boolMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final BottomNavigation item) {
                return boolMatcher.matches(item.isExpanded());
            }
        };
    }

    static BoundedMatcher<View, BottomNavigation> withSelection(final Matcher<Integer> intMatcher) {
        return new BoundedMatcher<View, BottomNavigation>(BottomNavigation.class) {
            @Override
            public void describeTo(final Description description) {
                description.appendText("with selection: ");
                intMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final BottomNavigation item) {
                final int selection = item.getSelectedIndex();
                return intMatcher.matches(selection);
            }
        };
    }
}
