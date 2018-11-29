package it.sephiroth.android.library.bottomnavigation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Log.INFO
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import it.sephiroth.android.library.bottomnavigation.MiscUtils.log
import it.sephiroth.android.library.bottonnavigation.R

/**
 * Created by crugnola on 4/4/16.
 * MaterialBottomNavigation
 *
 * The MIT License
 */
class FixedLayout(context: Context) : ItemsLayoutContainer(context) {
    private val maxActiveItemWidth: Int
    private val minActiveItemWidth: Int
    private var totalChildrenSize: Int = 0
    private var hasFrame: Boolean = false
    private var selectedIndex: Int = 0
    private var itemFinalWidth: Int = 0
    private var menu: MenuParser.Menu? = null

    init {
        val res = resources
        maxActiveItemWidth = res.getDimensionPixelSize(R.dimen.bbn_fixed_maxActiveItemWidth)
        minActiveItemWidth = res.getDimensionPixelSize(R.dimen.bbn_fixed_minActiveItemWidth)
    }

    override fun removeAll() {
        removeAllViews()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (!hasFrame || childCount == 0) {
            return
        }

        if (totalChildrenSize == 0) {
            totalChildrenSize = itemFinalWidth * (childCount - 1) + itemFinalWidth
        }

        val width = r - l
        var left = (width - totalChildrenSize) / 2

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val params = child.layoutParams
            setChildFrame(child, left, 0, params.width, params.height)
            left += child.width
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        hasFrame = true

        if (null != menu) {
            populateInternal(menu!!)
            menu = null
        }
    }

    private fun setChildFrame(child: View, left: Int, top: Int, width: Int, height: Int) {
        child.layout(left, top, left + width, top + height)
    }

    override fun setSelectedIndex(index: Int, animate: Boolean) {
        MiscUtils.log(Log.INFO, "setSelectedIndex: $index")

        if (selectedIndex == index) {
            return
        }

        val oldSelectedIndex = this.selectedIndex
        this.selectedIndex = index

        if (!hasFrame || childCount == 0) {
            return
        }

        val current = getChildAt(oldSelectedIndex) as BottomNavigationFixedItemView
        val child = getChildAt(index) as BottomNavigationFixedItemView

        current.setExpanded(false, 0, animate)
        child.setExpanded(true, 0, animate)
    }

    override fun setItemEnabled(index: Int, enabled: Boolean) {
        log(INFO, "setItemEnabled(%d, %b)", index, enabled)
        val child = getChildAt(index) as BottomNavigationItemViewAbstract
        child.isEnabled = enabled
        child.postInvalidate()
        requestLayout()
    }

    override fun getSelectedIndex(): Int {
        return selectedIndex
    }

    override fun populate(menu: MenuParser.Menu) {
        log(Log.INFO, "populate: $menu")

        if (hasFrame) {
            populateInternal(menu)
        } else {
            this.menu = menu
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun populateInternal(menu: MenuParser.Menu) {
        log(Log.DEBUG, "populateInternal")

        val parent = parent as BottomNavigation
        val screenWidth = parent.width
        var proposedWidth = Math.min(Math.max(screenWidth / menu.itemsCount, minActiveItemWidth), maxActiveItemWidth)

        if (proposedWidth * menu.itemsCount > screenWidth) {
            proposedWidth = screenWidth / menu.itemsCount
        }

        this.itemFinalWidth = proposedWidth

        for (i in 0 until menu.itemsCount) {
            val item = menu.getItemAt(i)

            val params = LinearLayout.LayoutParams(proposedWidth, height)

            val view = BottomNavigationFixedItemView(parent, i == selectedIndex, menu)
            view.item = item
            view.layoutParams = params
            view.isClickable = true
            view.setTypeface(parent.typeface)
            view.setOnTouchListener { v, event ->
                val action = event.actionMasked
                if (action == MotionEvent.ACTION_DOWN) {
                    itemClickListener?.onItemDown(this@FixedLayout, v, true, event.x, event.y)
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    itemClickListener?.onItemDown(this@FixedLayout, v, false, event.x, event.y)
                }
                false
            }
            view.setOnClickListener { v ->
                itemClickListener?.onItemClick(this@FixedLayout, v, i, true)
            }
            view.setOnLongClickListener {
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                true
            }
            addView(view)
        }
    }
}
