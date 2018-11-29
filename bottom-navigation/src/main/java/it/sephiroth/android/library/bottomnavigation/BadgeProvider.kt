package it.sephiroth.android.library.bottomnavigation

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.IdRes
import it.sephiroth.android.library.bottonnavigation.R
import java.util.*

/**
 * Created by alessandro crugnola on 4/12/16.
 * BadgeProvider
 *
 * The MIT License
 */

@Suppress("unused")
open class BadgeProvider(private val navigation: BottomNavigation) {
    private val map = HashSet<Int>()
    private val badgeSize: Int = navigation.context.resources.getDimensionPixelSize(R.dimen.bbn_badge_size)

    fun save(): Bundle {
        val bundle = Bundle()
        bundle.putSerializable("map", map)
        return bundle
    }

    @Suppress("UNCHECKED_CAST")
    fun restore(bundle: Bundle) {
        val set = bundle.getSerializable("map")
        if (null != set && set is HashSet<*>) {
            map.addAll(set as HashSet<Int>)
        }
    }

    /**
     * Returns if the menu item will require a badge
     *
     * @param itemId the menu item id
     * @return true if the menu item has to draw a badge
     */
    fun hasBadge(@IdRes itemId: Int): Boolean {
        return map.contains(itemId)
    }

    internal fun getBadge(@IdRes itemId: Int): Drawable? {
        return if (map.contains(itemId)) {
            newDrawable(itemId, navigation.menu!!.badgeColor)
        } else null
    }

    protected open fun newDrawable(@IdRes itemId: Int, preferredColor: Int): Drawable {
        return BadgeDrawable(preferredColor, badgeSize)
    }

    /**
     * Request to display a new badge over the passed menu item id
     *
     * @param itemId the menu item id
     */
    fun show(@IdRes itemId: Int) {
        map.add(itemId)
        navigation.invalidateBadge(itemId)
    }

    /**
     * Remove the currently displayed badge
     *
     * @param itemId the menu item id
     */
    open fun remove(@IdRes itemId: Int) {
        if (map.remove(itemId)) {
            navigation.invalidateBadge(itemId)
        }
    }
}
