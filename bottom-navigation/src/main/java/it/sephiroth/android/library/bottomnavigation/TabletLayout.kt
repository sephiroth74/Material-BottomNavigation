package it.sephiroth.android.library.bottomnavigation

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import it.sephiroth.android.library.bottonnavigation.R
import timber.log.Timber

/**
 * Created by crugnola on 4/4/16.
 * MaterialBottomNavigation
 */
class TabletLayout(context: Context) : ItemsLayoutContainer(context) {
    private val itemHeight: Int
    private val itemPaddingTop: Int
    private var hasFrame: Boolean = false
    private var selectedIndex: Int = 0
    private var menu: MenuParser.Menu? = null

    init {
        val res = resources
        selectedIndex = 0
        itemHeight = res.getDimensionPixelSize(R.dimen.bbn_tablet_item_height)
        itemPaddingTop = res.getDimensionPixelSize(R.dimen.bbn_tablet_layout_padding_top)
    }

    override fun removeAll() {
        removeAllViews()
        selectedIndex = 0
        menu = null
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (!hasFrame || childCount == 0) {
            return
        }
        var top = itemPaddingTop

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val params = child.layoutParams
            setChildFrame(child, 0, top, params.width, params.height)
            top += child.height
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
        Timber.v("setChildFrame: $left, $top, $width, $height")
        child.layout(left, top, left + width, top + height)
    }

    override fun setSelectedIndex(index: Int, animate: Boolean) {
        Timber.v("setSelectedIndex: $index")

        if (selectedIndex == index) {
            return
        }

        val oldSelectedIndex = this.selectedIndex
        this.selectedIndex = index

        if (!hasFrame || childCount == 0) {
            return
        }

        val current = getChildAt(oldSelectedIndex) as BottomNavigationTabletItemView?
        val child = getChildAt(index) as BottomNavigationTabletItemView?

        current?.setExpanded(false, 0, animate)
        child?.setExpanded(true, 0, animate)
    }

    override fun setItemEnabled(index: Int, enabled: Boolean) {
        Timber.v("setItemEnabled($index, $enabled)")
        val child = getChildAt(index) as BottomNavigationItemViewAbstract?
        child?.let {
            it.isEnabled = enabled
            it.postInvalidate()
            requestLayout()
        }
    }

    override fun getSelectedIndex(): Int {
        return selectedIndex
    }

    override fun populate(menu: MenuParser.Menu) {
        Timber.v("populate: $menu")

        if (hasFrame) {
            populateInternal(menu)
        } else {
            this.menu = menu
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun populateInternal(menu: MenuParser.Menu) {
        Timber.v("populateInternal")

        val parent = parent as BottomNavigation

        for (i in 0 until menu.itemsCount) {
            val item = menu.getItemAt(i)
            Timber.v("item: $item")
            val params = LinearLayout.LayoutParams(width, itemHeight)
            val view = BottomNavigationTabletItemView(parent, i == selectedIndex, menu)
            view.item = item
            view.layoutParams = params
            view.isClickable = true
            view.setTypeface(parent.typeface)
            val finalI = i
            view.setOnTouchListener { v, event ->
                val action = event.actionMasked
                if (action == MotionEvent.ACTION_DOWN) {
                    itemClickListener?.onItemDown(this@TabletLayout, v, true, event.x, event.y)
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    itemClickListener?.onItemDown(this@TabletLayout, v, false, event.x, event.y)
                }
                false
            }
            view.setOnClickListener { v ->
                itemClickListener?.onItemClick(this@TabletLayout, v, finalI, true)
            }
            view.setOnLongClickListener {
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                true
            }
            addView(view)
        }
    }
}
