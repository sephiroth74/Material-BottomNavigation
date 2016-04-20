package it.sephiroth.android.library.bottomnavigation;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;

import java.util.HashSet;

import it.sephiroth.android.library.bottonnavigation.R;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;

/**
 * Created by alessandro crugnola on 4/12/16.
 * BadgeProvider
 */
@Keep
@KeepClassMembers
public class BadgeProvider {
    private final BottomNavigation navigation;
    private final HashSet<Integer> map = new HashSet<>();
    private final int badgeSize;

    public BadgeProvider(final BottomNavigation navigation) {
        this.navigation = navigation;
        this.badgeSize = navigation.getContext().getResources().getDimensionPixelSize(R.dimen.bbn_badge_size);
    }

    protected Bundle save() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("map", map);
        return bundle;
    }

    @SuppressWarnings ("unchecked")
    public void restore(final Bundle bundle) {
        HashSet<Integer> set = (HashSet<Integer>) bundle.getSerializable("map");
        if (null != set) {
            map.addAll(set);
        }
    }

    /**
     * Returns if the menu item will require a badge
     *
     * @param itemId the menu item id
     * @return true if the menu item has to draw a badge
     */
    public boolean hasBadge(@IdRes final int itemId) {
        return map.contains(itemId);
    }

    Drawable getBadge(@IdRes final int itemId) {
        if (map.contains(itemId)) {
            return newDrawable(itemId, navigation.menu.getBadgeColor());
        }
        return null;
    }

    @SuppressWarnings ("unused")
    protected Drawable newDrawable(@IdRes final int itemId, final int preferredColor) {
        return new BadgeDrawable(preferredColor, badgeSize);
    }

    /**
     * Request to display a new badge over the passed menu item id
     *
     * @param itemId the menu item id
     */
    public void show(@IdRes final int itemId) {
        map.add(itemId);
        navigation.invalidateBadge(itemId);
    }

    /**
     * Remove the currently displayed badge
     *
     * @param itemId the menu item id
     */
    public void remove(@IdRes final int itemId) {
        if (map.remove(itemId)) {
            navigation.invalidateBadge(itemId);
        }
    }
}
