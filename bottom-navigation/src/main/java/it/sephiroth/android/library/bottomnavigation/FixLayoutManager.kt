package it.sephiroth.android.library.bottomnavigation

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.Log.INFO
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import it.sephiroth.android.library.bottomnavigation.MiscUtils.log
import it.sephiroth.android.library.bottonnavigation.R
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * Created by crugnola on 4/4/16.
 * MaterialBottomNavigation
 *
 * The MIT License
 */
@Suppress("unused")
@SuppressLint("ViewConstructor")
class FixLayoutManager(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
        LayoutManager(context, attrs, defStyleAttr, defStyleRes) {
    private val maxItemWidth: Int
    private val minItemWidth: Int
    private var defaultItemWidth: Int

    private var itemsGap: Int = 0
    private var itemWidth: Int = 0

    private var totalChildrenSize: Int = 0
    private var hasFrame: Boolean = false
    private var needLayout: Boolean = false
    private var selectedIndex: Int = 0
    private var menu: MenuParser.Menu? = null


    init {
        val res = resources
        maxItemWidth = res.getDimensionPixelSize(R.dimen.bbn_maxItemWidth)
        minItemWidth = res.getDimensionPixelSize(R.dimen.bbn_minItemWidth)
        defaultItemWidth = res.getDimensionPixelSize(R.dimen.bbn_defaultItemWidth)
    }

    override fun removeAll() {
        removeAllViews()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (!hasFrame || childCount == 0) {
            return
        }

        if (changed || needLayout) {
            val width = r - l
            var childLeft = 0

            if (!distributeEqually && itemWidth > defaultItemWidth) itemWidth = defaultItemWidth

            if (itemWidth * childCount < width) {
                if (distributeEqually) {
                    itemsGap = (width - itemWidth * (childCount)) / (childCount + 1)
                    childLeft = itemsGap
                } else {
                    childLeft = (width - itemWidth * (childCount)) / 2
                    itemsGap = 0
                }
            }

            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val params = child.layoutParams
                setChildFrame(child, childLeft, 0, params.width, params.height)
                childLeft += params.width + itemsGap
            }

            if (needLayout) needLayout = false
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
        val parent = parent as BottomNavigation
        val density = resources.displayMetrics.density
        val screenWidth = parent.width

        Timber.v("density: $density")
        Timber.v("maxItemWidth: $maxItemWidth")
        Timber.v("minItemWidth: $minItemWidth")
        Timber.v("screenWidth: $screenWidth")

        val totalWidth = maxItemWidth * (menu.itemsCount)
        Timber.v("totalWidth: $totalWidth")

        itemWidth = screenWidth / menu.itemsCount
        Timber.v("itemWidth(dp): ${itemWidth / density}")

        itemWidth = min(max(itemWidth, minItemWidth), maxItemWidth)

        Timber.v("itemWidth: $itemWidth")

        if (!distributeEqually && itemWidth > defaultItemWidth) itemWidth = defaultItemWidth

        if (itemWidth * menu.itemsCount < screenWidth) {
            itemsGap = if (distributeEqually) {
                (screenWidth - itemWidth * (menu.itemsCount)) / (menu.itemsCount + 1)
            } else {
                0
            }
        }

        if (BottomNavigation.DEBUG) {
            Timber.v("active size (dp): ${maxItemWidth / density}, ${minItemWidth / density}")
            Timber.v("itemWidth(dp): ${itemWidth / density}")
            Timber.v("itemsGap: $itemsGap")
            Timber.v("itemsCount: ${menu.itemsCount}")
        }

        for (i in 0 until menu.itemsCount) {
            val item = menu.getItemAt(i)
            val params = LinearLayout.LayoutParams(itemWidth, height)
            val view = BottomNavigationFixedItemView(parent, i == selectedIndex, menu)
            view.item = item
            view.layoutParams = params
            view.isClickable = true
            view.setTypeface(parent.typeface)
            view.setOnTouchListener { v, event ->
                val action = event.actionMasked
                if (action == MotionEvent.ACTION_DOWN) {
                    itemClickListener?.onItemDown(this@FixLayoutManager, v, true, event.x, event.y)
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    itemClickListener?.onItemDown(this@FixLayoutManager, v, false, event.x, event.y)
                }
                false
            }
            view.setOnClickListener { v ->
                itemClickListener?.onItemClick(this@FixLayoutManager, v, i, true)
            }
            view.setOnLongClickListener {
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                true
            }
            addView(view)
        }

        needLayout = true
    }
}
