package it.sephiroth.android.library.bottomnavigation

import android.view.View

/**
 * Created by alessandro on 4/4/16 at 11:14 PM.
 * Project: Material-BottomNavigation
 */
interface OnItemClickListener {
    fun onItemClick(parent: ItemsLayoutContainer, view: View, index: Int, animate: Boolean)

    fun onItemDown(parent: ItemsLayoutContainer, view: View,
                   pressed: Boolean, x: Float, y: Float)
}
