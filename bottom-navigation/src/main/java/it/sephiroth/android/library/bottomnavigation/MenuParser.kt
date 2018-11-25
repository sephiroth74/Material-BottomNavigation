package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Xml
import androidx.core.content.ContextCompat
import it.sephiroth.android.library.bottonnavigation.R
import org.xmlpull.v1.XmlPullParser
import java.util.*

/**
 * Created by alessandro crugnola on 4/3/16 at 7:59 PM.
 * Project: MaterialBottomNavigation
 *
 * The MIT License
 */
class MenuParser {

    private var item: MenuItem? = null
    private var menu: Menu? = null

    @Suppress("unused")
    class Menu(private val context: Context) {

        var items: Array<BottomNavigationItem>? = null
            set(items) {
                field = items
                this.isShifting = null != items && items.size > 3 && !forceFixed
            }
        internal var colorActive: Int = 0
        internal var background: Int = 0
        internal var rippleColor: Int = 0
        internal var colorInactive: Int = 0
        internal var colorDisabled: Int = 0

        var itemAnimationDuration: Int = 0
            internal set

        var isShifting: Boolean = false
            internal set

        var isTablet: Boolean = false
            internal set

        var badgeColor: Int = 0
            internal set

        var forceFixed: Boolean = false
            internal set

        val itemsCount: Int
            get() = if (null != this.items) {
                this.items!!.size
            } else 0

        override fun toString(): String {
            return "Menu{background:$background, colorActive:$colorActive, colorInactive:$colorInactive, colorDisabled: $colorDisabled, shifting:$isShifting, tablet:$isTablet}"
        }

        fun getBackground(): Int {
            return if (0 == background) {
                if (isShifting && !isTablet) {
                    MiscUtils.getColor(context, R.attr.colorPrimary)
                } else {
                    MiscUtils.getColor(context, android.R.attr.windowBackground)
                }
            } else background
        }

        fun getColorActive(): Int {
            if (0 == colorActive) {
                colorActive = if (isShifting && !isTablet) {
                    MiscUtils.getColor(context, android.R.attr.colorForegroundInverse)
                } else {
                    MiscUtils.getColor(context, android.R.attr.colorForeground)
                }
            }
            return colorActive
        }

        fun getColorInactive(): Int {
            if (0 == colorInactive) {
                colorInactive = if (isShifting && !isTablet) {
                    val color = getColorActive()
                    Color.argb(Color.alpha(color) / 2, Color.red(color), Color.green(color), Color.blue(color))
                } else {
                    val color = getColorActive()
                    Color.argb(Color.alpha(color) / 2, Color.red(color), Color.green(color), Color.blue(color))
                }
            }
            return colorInactive
        }

        fun getColorDisabled(): Int {
            if (0 == colorDisabled) {
                val color = getColorInactive()
                colorDisabled = Color.argb(Color.alpha(color) / 2, Color.red(color), Color.green(color), Color.blue(color))
            }
            return colorDisabled
        }

        fun getRippleColor(): Int {
            if (0 == rippleColor) {
                rippleColor = if (isShifting && !isTablet) {
                    ContextCompat.getColor(context, R.color.bbn_shifting_item_ripple_color)
                } else {
                    ContextCompat.getColor(context, R.color.bbn_fixed_item_ripple_color)
                }
            }
            return rippleColor
        }

        fun getItemAt(index: Int): BottomNavigationItem {
            return this.items!![index]
        }

        /**
         * Returns true if the first item of the menu
         * has a color defined
         */
        fun hasChangingColor(): Boolean {
            return this.items!![0].hasColor()
        }

        fun setTabletMode(tablet: Boolean) {
            this.isTablet = tablet
        }
    }

    data class MenuItem(
            var itemId: Int = 0,
            var itemTitle: CharSequence? = null,
            var itemIconResId: Int = 0,
            var isItemEnabled: Boolean = false,
            var itemColor: Int = 0
                       )

    private fun readMenu(context: Context, attrs: AttributeSet) {
        menu = Menu(context)
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenu)

        menu?.let {
            it.itemAnimationDuration =
                    a.getInt(R.styleable.BottomNavigationMenu_bbn_itemAnimationDuration, context.resources.getInteger(R.integer.bbn_item_animation_duration))
            it.background = a.getColor(R.styleable.BottomNavigationMenu_android_background, 0)
            it.rippleColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_rippleColor, 0)
            it.colorInactive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorInactive, 0)
            it.colorDisabled = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorDisabled, 0)
            it.colorActive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorActive, 0)
            it.badgeColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_badgeColor, Color.RED)
            it.forceFixed = a.getBoolean(R.styleable.BottomNavigationMenu_bbn_alwaysShowLabels, false)
        }
        a.recycle()
    }

    fun pullItem(): MenuItem? {
        val current = item
        item = null
        return current
    }

    fun hasItem(): Boolean {
        return null != item
    }

    fun hasMenu(): Boolean {
        return null != menu
    }

    private fun pullMenu(): Menu? {
        val current = menu
        menu = null
        return current
    }

    /**
     * Called when the parser is pointing to an item tag.
     */
    fun readItem(mContext: Context, attrs: AttributeSet) {
        val a = mContext.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenuItem)
        item = MenuItem()
        item?.let {
            it.itemId = a.getResourceId(R.styleable.BottomNavigationMenuItem_android_id, 0)
            it.itemTitle = a.getText(R.styleable.BottomNavigationMenuItem_android_title)
            it.itemIconResId = a.getResourceId(R.styleable.BottomNavigationMenuItem_android_icon, 0)
            it.isItemEnabled = a.getBoolean(R.styleable.BottomNavigationMenuItem_android_enabled, true)
            it.itemColor = a.getColor(R.styleable.BottomNavigationMenuItem_android_color, 0)
        }
        a.recycle()
    }

    companion object {
        fun inflateMenu(context: Context, menuRes: Int): Menu? {
            val list = ArrayList<BottomNavigationItem>()

            val menuParser = MenuParser()

            try {
                val parser = context.resources.getLayout(menuRes)
                val attrs = Xml.asAttributeSet(parser)

                var tagName: String
                var eventType = parser.eventType
                var lookingForEndOfUnknownTag = false
                var unknownTagName: String? = null

                do {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.name
                        if (tagName == "menu") {
                            menuParser.readMenu(context, attrs)
                            eventType = parser.next()
                            break
                        }
                        throw RuntimeException("Expecting menu, got $tagName")
                    }
                    eventType = parser.next()
                } while (eventType != XmlPullParser.END_DOCUMENT)

                var reachedEndOfMenu = false

                loop@ while (!reachedEndOfMenu) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (lookingForEndOfUnknownTag) {
                                break@loop
                            }
                            tagName = parser.name
                            if (tagName == "item") {
                                menuParser.readItem(context, attrs)
                            } else {
                                lookingForEndOfUnknownTag = true
                                unknownTagName = tagName
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            tagName = parser.name
                            if (lookingForEndOfUnknownTag && tagName == unknownTagName) {
                                lookingForEndOfUnknownTag = false
                                unknownTagName = null
                            } else if (tagName == "item") {
                                if (menuParser.hasItem()) {
                                    val item = menuParser.pullItem()
                                    val tab = BottomNavigationItem(item!!.itemId, item.itemIconResId,
                                            item.itemTitle.toString()
                                                                  )
                                    tab.isEnabled = item.isItemEnabled
                                    tab.color = item.itemColor
                                    list.add(tab)
                                }
                            } else if (tagName == "menu") {
                                reachedEndOfMenu = true
                            }
                        }

                        XmlPullParser.END_DOCUMENT -> throw RuntimeException("Unexpected end of document")

                        else -> {
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: Exception) {
                return null
            }

            if (menuParser.hasMenu()) {
                val menu = menuParser.pullMenu()
                menu!!.items = list.toTypedArray()
                return menu
            }

            return null
        }
    }
}
