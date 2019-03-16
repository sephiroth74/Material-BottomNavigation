package it.sephiroth.android.library.bottomnavigation

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import it.sephiroth.android.library.bottonnavigation.R
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * Created by crugnola on 4/4/16.
 * MaterialBottomNavigation
 */
@SuppressLint("ViewConstructor")
@Suppress("unused")
class ExpandLayoutManager(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
        LayoutManager(context, attrs, defStyleAttr, defStyleRes) {

    private val maxItemWidth: Int = resources.getDimensionPixelSize(R.dimen.bbn_expanding_maxItemWidth)
    private val minItemWidth: Int = resources.getDimensionPixelSize(R.dimen.bbn_expanding_minItemWidth)
    private val defaultItemWidth: Int = resources.getDimensionPixelSize(R.dimen.bbn_expanding_defaultItemWidth)
    private var itemsGap: Int = 0
    private var itemWidth: Int = 0
    private var selectedIndex: Int = 0
    private var hasFrame: Boolean = false
    private var menu: MenuParser.Menu? = null

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    override fun removeAll() {
        removeAllViews()
        selectedIndex = 0
        menu = null
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
            requestLayout()
        }

        current?.setExpanded(false, itemWidth, willAnimate)
        child?.setExpanded(true, itemWidth, willAnimate)
    }

    override fun setItemEnabled(index: Int, enabled: Boolean) {
        (getChildAt(index) as BottomNavigationItemViewAbstract?)?.let { child ->
            child.isEnabled = enabled
            child.postInvalidate()
            requestLayout()
        }
    }

    override fun getSelectedIndex(): Int = selectedIndex

    override fun populate(menu: MenuParser.Menu) {
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

        var itemWidth = screenWidth / menu.itemsCount
        Timber.v("itemWidth(dp): ${itemWidth / density}")

        itemWidth = min(max(itemWidth, minItemWidth), maxItemWidth)

        Timber.v("itemWidth: $itemWidth")

        var childLeft = 0

        if (!distributeEqually && itemWidth > defaultItemWidth) itemWidth = defaultItemWidth

        if (itemWidth * menu.itemsCount < screenWidth) {
            if (distributeEqually) {
                itemsGap = (screenWidth - itemWidth * (menu.itemsCount)) / (menu.itemsCount + 1)
                childLeft = itemsGap
            } else {
                childLeft = (screenWidth - itemWidth * (menu.itemsCount)) / 2
                itemsGap = 0
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
            val view = BottomNavigationExpandingItemView(parent, i == selectedIndex, menu)
            view.item = item
            view.layoutParams = params
            view.isClickable = true
            view.setTypeface(parent.typeface)

            view.setOnTouchListener { v, event ->
                val action = event.actionMasked
                if (action == MotionEvent.ACTION_DOWN) {
                    itemClickListener?.onItemDown(this@ExpandLayoutManager, v, true, event.x, event.y)
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    itemClickListener?.onItemDown(this@ExpandLayoutManager, v, false, event.x, event.y)
                }
                false
            }
            view.setOnClickListener { v ->
                itemClickListener?.onItemClick(this@ExpandLayoutManager, v, i, true)
            }
            view.setOnLongClickListener {
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                true
            }

            setChildFrame(view, childLeft, 0, params.width, params.height)
            addView(view)

            childLeft += params.width + itemsGap
        }
    }

    companion object {}
}
