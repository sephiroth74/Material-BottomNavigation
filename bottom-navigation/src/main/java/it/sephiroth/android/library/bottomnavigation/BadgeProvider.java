package it.sephiroth.android.library.bottomnavigation;

import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;

import java.util.HashSet;

import it.sephiroth.android.library.bottonnavigation.R;

/**
 * Created by alessandro crugnola on 4/12/16.
 */
public class BadgeProvider {
    private final BottomNavigation navigation;
    private final HashSet<Integer> map = new HashSet<>();
    private final int badgeSize;

    public BadgeProvider(final BottomNavigation navigation) {
        this.navigation = navigation;
        this.badgeSize = navigation.getContext().getResources().getDimensionPixelSize(R.dimen.bbn_badge_size);
    }

    public boolean hasBadge(@IdRes final int itemId) {
        return map.contains(itemId);
    }

    Drawable getBadge(@IdRes final int itemId) {
        if (map.contains(itemId)) {
            return new BadgeDrawable(navigation.menu.getBadgeColor(), badgeSize);
        }
        return null;
    }

    public void show(@IdRes final int itemId) {
        map.add(itemId);
        navigation.invalidateBadge(itemId);
    }

    public void remove(@IdRes final int itemId) {
        if (map.remove(itemId)) {
            navigation.invalidateBadge(itemId);
        }
    }
}
