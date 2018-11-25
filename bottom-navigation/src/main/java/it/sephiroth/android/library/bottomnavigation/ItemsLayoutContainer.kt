package it.sephiroth.android.library.bottomnavigation

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import proguard.annotation.Keep
import proguard.annotation.KeepClassMembers

/**
 * Created by alessandro on 4/4/16 at 11:13 PM.
 * Project: Material-BottomNavigation
 */
@Keep
@KeepClassMembers
interface ItemsLayoutContainer {

    fun getSelectedIndex(): Int

    fun setSelectedIndex(index: Int, animate: Boolean)

    fun populate(menu: MenuParser.Menu)

    fun setLayoutParams(params: ViewGroup.LayoutParams)

    fun setOnItemClickListener(listener: OnItemClickListener)

    fun <T : View> findViewById(@IdRes id: Int): T

    fun removeAll()

    fun requestLayout()

    fun setItemEnabled(index: Int, enabled: Boolean)
}
