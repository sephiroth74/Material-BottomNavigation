package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources

/**
 * Created by alessandro on 4/3/16.
 *
 * The MIT License
 */
class BottomNavigationItem(val id: Int, private val iconResource: Int, val title: String) {
    var color: Int = 0
    var isEnabled: Boolean = true

    fun getIcon(context: Context): Drawable? {
        return AppCompatResources.getDrawable(context, this.iconResource)
    }

    fun hasColor(): Boolean {
        return color != 0
    }

    override fun toString(): String {
        return ("BottomNavigationItem{"
                + "id=$id"
                + ", iconResource=${String.format("%x", iconResource)}"
                + ", title='$title'"
                + ", color=${String.format("%x", color)}"
                + ", enabled=$isEnabled"
                + '}'.toString())
    }
}
