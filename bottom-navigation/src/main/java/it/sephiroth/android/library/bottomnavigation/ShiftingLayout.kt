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
class ShiftingLayout(context: Context) : ItemsLayoutContainer(context) {
    private val maxActiveItemWidth: Int
    private val minActiveItemWidth: Int
    private val maxInactiveItemWidth: Int
    private val minInactiveItemWidth: Int
    private var totalChildrenSize: Int = 0
    private var minSize: Int = 0
    private var maxSize: Int = 0
    private var selectedIndex: Int = 0
    private var hasFrame: Boolean = false
    private var menu: MenuParser.Menu? = null

    init {
        totalChildrenSize = 0
        maxActiveItemWidth = resources.getDimensionPixelSize(R.dimen.bbn_shifting_maxActiveItemWidth)
        minActiveItemWidth = resources.getDimensionPixelSize(R.dimen.bbn_shifting_minActiveItemWidth)
        maxInactiveItemWidth = resources.getDimensionPixelSize(R.dimen.bbn_shifting_maxInactiveItemWidth)
        minInactiveItemWidth = resources.getDimensionPixelSize(R.dimen.bbn_shifting_minInactiveItemWidth)
    }

    override fun removeAll() {
        removeAllViews()
        totalChildrenSize = 0
        selectedIndex = 0
        menu = null
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (!hasFrame || childCount == 0) {
            return
        }

        if (totalChildrenSize == 0) {
            totalChildrenSize = if (selectedIndex < 0) {
                minSize * childCount
            } else {
                minSize * (childCount - 1) + maxSize
            }
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

    private fun setTotalSize(minSize: Int, maxSize: Int) {
        this.minSize = minSize
        this.maxSize = maxSize
    }

    override fun setSelectedIndex(index: Int, animate: Boolean) {
        Timber.i("setSelectedIndex: $index")

        if (selectedIndex == index) {
            return
        }

        val oldSelectedIndex = this.selectedIndex
        this.selectedIndex = index

        if (!hasFrame || childCount == 0) {
            return
        }

        val current = getChildAt(oldSelectedIndex) as BottomNavigationItemViewAbstract?
        val child = getChildAt(index) as BottomNavigationItemViewAbstract?

        val willAnimate = null != current && null != child

        if (!willAnimate) {
            totalChildrenSize = 0
            requestLayout()
        }

        current?.setExpanded(false, minSize, willAnimate)

        child?.setExpanded(true, maxSize, willAnimate)
    }

    override fun setItemEnabled(index: Int, enabled: Boolean) {
        Timber.i("setItemEnabled($index, $enabled)")
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
        Timber.i("populate: $menu")

        if (hasFrame) {
            populateInternal(menu)
        } else {
            this.menu = menu
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun populateInternal(menu: MenuParser.Menu) {
        Timber.d("populateInternal")

        val parent = parent as BottomNavigation
        val density = resources.displayMetrics.density
        val screenWidth = parent.width

        Timber.v("density: $density")
        Timber.v("screenWidth(dp): ${screenWidth / density}")

        var itemWidthMin: Int
        var itemWidthMax: Int

        val totalWidth = maxInactiveItemWidth * (menu.itemsCount - 1) + maxActiveItemWidth
        Timber.v("totalWidth(dp): ${totalWidth / density}")

        if (totalWidth > screenWidth) {
            var ratio = screenWidth.toFloat() / totalWidth
            ratio = (Math.round(ratio * ROUND_DECIMALS).toDouble() / ROUND_DECIMALS).toFloat() + RATIO_MIN_INCREASE
            Timber.v("ratio: $ratio")

            itemWidthMin = Math.max(maxInactiveItemWidth * ratio, minInactiveItemWidth.toFloat()).toInt()
            itemWidthMax = (maxActiveItemWidth * ratio).toInt()

            if (BottomNavigation.DEBUG) {
                Timber.v("computing sizes...")
                Timber.v("itemWidthMin(dp): ${itemWidthMin / density}")
                Timber.v("itemWidthMax(dp): ${itemWidthMax / density}")
                Timber.v("total items size(dp): ${(itemWidthMin * (menu.itemsCount - 1) + itemWidthMax) / density})")
            }

            if (itemWidthMin * (menu.itemsCount - 1) + itemWidthMax > screenWidth) {
                itemWidthMax = screenWidth - itemWidthMin * (menu.itemsCount - 1) // minActiveItemWidth?
                if (itemWidthMax == itemWidthMin) {
                    itemWidthMin = minInactiveItemWidth
                    itemWidthMax = screenWidth - itemWidthMin * (menu.itemsCount - 1)
                }
            }
        } else {
            itemWidthMax = maxActiveItemWidth
            itemWidthMin = maxInactiveItemWidth
        }

        if (BottomNavigation.DEBUG) {
            Timber.v("active size (dp): ${maxActiveItemWidth / density} , ${minActiveItemWidth / density}")
            Timber.v("inactive size (dp): ${maxInactiveItemWidth / density}, ${minInactiveItemWidth / density}")
            Timber.v("itemWidth(dp): ${itemWidthMin / density}, ${itemWidthMax / density}")
        }

        setTotalSize(itemWidthMin, itemWidthMax)

        for (i in 0 until menu.itemsCount) {
            val item = menu.getItemAt(i)
            Timber.v("item: $item")

            val params = LinearLayout.LayoutParams(itemWidthMin, height)

            if (i == selectedIndex) {
                params.width = itemWidthMax
            }

            val view = BottomNavigationShiftingItemView(parent, i == selectedIndex, menu)
            view.item = item
            view.layoutParams = params
            view.isClickable = true
            view.setTypeface(parent.typeface)

            view.setOnTouchListener { v, event ->
                val action = event.actionMasked
                if (action == MotionEvent.ACTION_DOWN) {
                    itemClickListener?.onItemDown(this@ShiftingLayout, v, true, event.x, event.y)
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    itemClickListener?.onItemDown(this@ShiftingLayout, v, false, event.x, event.y)
                }
                false
            }
            view.setOnClickListener { v ->
                itemClickListener?.onItemClick(this@ShiftingLayout, v, i, true)
            }
            view.setOnLongClickListener {
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                true
            }
            addView(view)
        }
    }

    companion object {
        const val ROUND_DECIMALS = 10.0
        const val RATIO_MIN_INCREASE = 0.05f
    }
}
