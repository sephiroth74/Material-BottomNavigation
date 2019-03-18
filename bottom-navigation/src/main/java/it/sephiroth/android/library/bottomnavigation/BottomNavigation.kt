/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2016 Alessandro Crugnola
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package it.sephiroth.android.library.bottomnavigation

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import com.readystatesoftware.systembartint.SystemBarTintManager
import it.sephiroth.android.library.bottonnavigation.R
import it.sephiroth.android.library.kotlin_extensions.content.getActivity
import timber.log.Timber
import java.lang.ref.SoftReference
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.math.min

/**
 * Created by alessandro crugnola on 4/2/16.
 * BottomNavigation
 */
@Suppress("SpellCheckingInspection", "unused")
class BottomNavigation : FrameLayout, OnItemClickListener {

    /**
     * Current pending action (used inside the BottomBehavior instance)
     */
    internal var pendingAction = PENDING_ACTION_NONE
        private set

    /**
     * This is the amount of space we have to cover in case there's a translucent navigation
     * enabled.
     */
    private var bottomInset: Int = 0

    /**
     * This is the amount of space we have to cover in case there's a translucent status
     * enabled.
     */
    private var topInset: Int = 0

    /**
     * This is the current view height. It does take into account the extra space
     * used in case we have to cover the navigation translucent area, and neither the shadow height.
     */
    val navigationHeight: Int by lazy { resources.getDimensionPixelSize(R.dimen.bbn_bottom_navigation_height) }

    /**
     * Same as defaultHeight, but for tablet mode.
     */
    private val navigationWidth: Int by lazy { resources.getDimensionPixelSize(R.dimen.bbn_bottom_navigation_width) }

    /**
     * Shadow is created above the widget background. It simulates the
     * elevation.
     */
    private val shadowHeight: Int by lazy { resources.getDimensionPixelOffset(R.dimen.bbn_top_shadow_height) }

    /**
     * Layout container used to create and manage the UI items.
     * It can be either Fixed or Shifting, based on the widget `mode`
     */
    private var layoutManager: LayoutManager? = null

    /**
     * This is where the color animation is happening
     */
    private lateinit var backgroundOverlay: View

    /**
     * View used to show the press ripple overlay.
     */
    private lateinit var rippleOverlay: View

    /**
     * Toggle the ripple background animation on item press
     */
    private var enabledRippleBackground: Boolean = false

    /**
     * current menu
     */
    internal var menu: MenuParser.Menu? = null

    private var pendingMenu: MenuParser.Menu? = null

    /**
     * Default selected index.
     * After the items are populated changing this
     * won't have any effect
     */
    private var defaultSelectedIndex = 0

    /**
     * View visible background color
     */
    private lateinit var backgroundDrawable: ColorDrawable

    /**
     * Animation duration for the background color change
     */
    private val backgroundColorAnimation: Long = resources.getInteger(R.integer.bbn_background_animation_duration).toLong()

    /**
     * Optional typeface used for the items' text labels
     */
    internal lateinit var typeface: SoftReference<Typeface>

    /**
     * Current BottomBehavior assigned from the CoordinatorLayout
     */
    private var mBehavior: CoordinatorLayout.Behavior<*>? = null

    /**
     * Menu selection menuItemSelectionListener
     */
    var menuItemSelectionListener: OnMenuItemSelectionListener? = null

    /**
     * Menu changed
     */
    var menuChangedListener: OnMenuChangedListener? = null

    /**
     * The user defined layout_gravity
     */
    private var gravity: Int = 0

    /**
     * animation duration for the ripple reveal animation on item press
     */
    private var rippleRevealAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

    /**
     * item press reveal animation
     */
    private var circleRevealAnim: Animator? = null

    /**
     * View is attached
     */
    private var attached: Boolean = false

    var badgeProvider: BadgeProvider? = null
        private set

    var selectedIndex: Int = -1
        get() = layoutManager?.getSelectedIndex() ?: -1
        private set

    var isExpanded: Boolean = false
        get() = if (null != mBehavior && mBehavior is BottomBehavior) {
            (mBehavior as BottomBehavior).isExpanded
        } else false
        private set

    /**
     * Returns the current menu items count
     *
     * @return number of items in the current menu
     */
    var menuItemCount: Int = 0
        get() = if (null != menu) {
            menu!!.itemsCount
        } else 0
        private set

    var behavior: CoordinatorLayout.Behavior<*>? = null
        get() {
            if (null == mBehavior) {
                if (layoutParams is CoordinatorLayout.LayoutParams) {
                    return (layoutParams as CoordinatorLayout.LayoutParams).behavior
                }
            }
            return mBehavior
        }
        private set

    private val mLayoutChangedListener = MyLayoutChangedListener()

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        initialize(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context, attrs, defStyleAttr, defStyleRes)
    }

    @Suppress("NAME_SHADOWING")
    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        parcelable?.let { parcelable ->
            val savedState = SavedState(parcelable)

            if (null == menu) {
                savedState.selectedIndex = 0
                savedState.disabledIndices = arrayListOf()
            } else {
                // savedState.selectedIndex = Math.max(0, Math.min(getSelectedIndex(), menu.getItemsCount() - 1));
                savedState.selectedIndex = selectedIndex
                savedState.disabledIndices = arrayListOf()
                val items = menu?.items
                if (items != null) {
                    for (i in items.indices) {
                        if (!menu!!.items!![i].isEnabled) {
                            savedState.disabledIndices.add(i)
                        }
                    }
                }
            }

            if (null != badgeProvider) {
                savedState.badgeBundle = badgeProvider!!.save()
            }

            if (null != pendingMenu) {
                val items = pendingMenu?.items
                if (items != null) {
                    for (i in items.indices) {
                        if (savedState.disabledIndices.contains(i)) {
                            pendingMenu!!.items!![i].isEnabled = false
                        }
                    }
                }
            }
            return savedState
        }
        return AbsSavedState.EMPTY_STATE
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        defaultSelectedIndex = savedState.selectedIndex

        if (null != badgeProvider && null != savedState.badgeBundle) {
            badgeProvider!!.restore(savedState.badgeBundle!!)
        }
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        typeface = SoftReference(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))

        val array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigation, defStyleAttr, defStyleRes)
        val layoutManagerName = array.getString(R.styleable.BottomNavigation_bbn_layoutManager)

        val menuResId = array.getResourceId(R.styleable.BottomNavigation_bbn_entries, 0)
        pendingMenu = MenuParser.inflateMenu(context, menuResId)
        badgeProvider = parseBadgeProvider(this, context, array.getString(R.styleable.BottomNavigation_bbn_badgeProvider))
        array.recycle()

        defaultSelectedIndex = 0

        // check if the bottom navigation is translucent
        if (!isInEditMode) {

            context.getActivity()?.let { activity ->
                val systembarTint = SystemBarTintManager(activity)
                bottomInset = if (MiscUtils.hasTranslucentNavigation(activity)
                                  && systembarTint.config.isNavigationAtBottom
                                  && systembarTint.config.hasNavigtionBar()) {
                    systembarTint.config.navigationBarHeight
                } else {
                    0
                }
                topInset = systembarTint.config.statusBarHeight
            }
        }

        val params = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        backgroundOverlay = View(getContext())
        backgroundOverlay.layoutParams = params
        addView(backgroundOverlay)

        val drawable = ContextCompat.getDrawable(getContext(), R.drawable.bbn_ripple_selector)
        drawable!!.mutate()
        MiscUtils.setDrawableColor(drawable, Color.WHITE)

        rippleOverlay = View(getContext())
        with(rippleOverlay) {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            background = drawable
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
            alpha = 0f
            addView(this)
        }

        layoutManagerName?.let {
            createLayoutManager(context, it, attrs, defStyleAttr, defStyleRes)
        }
    }

    private fun getFullClassName(context: Context, className: String): String {
        if (className[0] == '.') {
            return context.packageName + className
        }
        return if (className.contains(".")) {
            className
        } else BottomNavigation::class.java.getPackage()!!.name + '.'.toString() + className
    }

    private fun createLayoutManager(context: Context,
                                    layoutManagerName: String,
                                    attrs: AttributeSet?,
                                    defStyleAttr: Int,
                                    defStyleRes: Int) {

        var className = layoutManagerName.trim()
        if (!className.isEmpty()) {
            className = getFullClassName(context, className)
            try {
                val classLoader: ClassLoader? = if (isInEditMode) {
                    this.javaClass.classLoader
                } else {
                    context.classLoader
                }
                val layoutManagerClass =
                        classLoader!!.loadClass(className).asSubclass<LayoutManager>(LayoutManager::class.java)
                var constructor: Constructor<out LayoutManager>
                var constructorArgs: Array<Any?> = arrayOf()
                try {
                    constructor = layoutManagerClass.getConstructor(*LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE)
                    constructorArgs = arrayOf(context, attrs, defStyleAttr, defStyleRes)
                } catch (e: NoSuchMethodException) {
                    try {
                        constructor = layoutManagerClass.getConstructor()
                    } catch (e1: NoSuchMethodException) {
                        e1.initCause(e)
                        throw IllegalStateException("${attrs?.positionDescription}: Error creating LayoutManager " + className, e1)
                    }
                }

                constructor.isAccessible = true
                setLayoutManager(constructor.newInstance(*constructorArgs))
            } catch (e: ClassNotFoundException) {
                throw IllegalStateException("${attrs?.positionDescription}: Unable to find LayoutManager " + className, e)
            } catch (e: InvocationTargetException) {
                throw IllegalStateException("${attrs?.positionDescription}: Could not instantiate the LayoutManager: " + className, e)
            } catch (e: InstantiationException) {
                throw IllegalStateException("${attrs?.positionDescription}: Could not instantiate the LayoutManager: " + className, e)
            } catch (e: IllegalAccessException) {
                throw IllegalStateException("${attrs?.positionDescription}: Cannot access non-public constructor " + className, e)
            } catch (e: ClassCastException) {
                throw IllegalStateException("${attrs?.positionDescription}: Class is not a LayoutManager " + className, e)
            }
        }
    }

    private fun clearLayoutManager() {
        layoutManager?.let { layoutManager ->
            layoutManager.removeOnLayoutChangeListener(mLayoutChangedListener)
            layoutManager.removeAll()
            layoutManager.addOnLayoutChangeListener(mLayoutChangedListener)
        }
    }

    private fun setLayoutManager(layout: LayoutManager?) {
        Timber.i("setLayoutManager($layout)")
        Timber.v("navigationWidth = $navigationWidth, navigationHeight = $navigationHeight")

        layoutManager?.let { layoutManager ->
            // remove the layout menuItemSelectionListener
            layoutManager.removeOnLayoutChangeListener(mLayoutChangedListener)
            layoutManager.removeAll()
            removeView(layoutManager)
        }

        layoutManager = null

        val params = LinearLayout.LayoutParams(MATCH_PARENT, navigationHeight)

        layout?.let { layout ->
            layout.id = R.id.bbn_layoutManager
            layout.layoutParams = params
            layout.addOnLayoutChangeListener(mLayoutChangedListener)
            addView(layout)
        }

        layoutManager = layout
    }

    internal fun resetPendingAction() {
        pendingAction = PENDING_ACTION_NONE
    }

    private fun isTablet(gravity: Int): Boolean {
        return MiscUtils.isGravitiyLeft(gravity) || MiscUtils.isGravityRight(gravity)
    }

    fun setSelectedIndex(position: Int, animate: Boolean) {
        layoutManager?.let {
            setSelectedItemInternal(it, (layoutManager as ViewGroup).getChildAt(position), position, animate, false)
        } ?: run {
            defaultSelectedIndex = position
        }
    }

    fun setExpanded(expanded: Boolean, animate: Boolean) {
        pendingAction = (if (expanded) PENDING_ACTION_EXPANDED else PENDING_ACTION_COLLAPSED) or
                if (animate) PENDING_ACTION_ANIMATE_ENABLED else 0
        requestLayout()
    }

    /**
     * Inflate a menu resource into this navigation component
     *
     * @param menuResId the menu resource id
     */
    fun inflateMenu(@MenuRes menuResId: Int) {
        defaultSelectedIndex = 0
        pendingMenu = when {
            isAttachedToWindow -> {
                setItems(MenuParser.inflateMenu(context, menuResId))
                null
            }
            else -> MenuParser.inflateMenu(context, menuResId)
        }
    }

    /**
     * Returns the id of the item at the specified position
     *
     * @param position the position inside the menu
     * @return the item ID
     */
    @IdRes
    fun getMenuItemId(position: Int): Int {
        return if (null != menu) {
            menu!!.getItemAt(position).id
        } else 0
    }

    fun setMenuItemEnabled(index: Int, enabled: Boolean) {
        if (null != menu) {
            menu!!.getItemAt(index).isEnabled = enabled
            if (null != layoutManager) {
                layoutManager!!.setItemEnabled(index, enabled)
            }
        }
    }

    fun getMenuItemEnabled(index: Int): Boolean {
        return if (null != menu) {
            menu!!.getItemAt(index).isEnabled
        } else false
        // menu has not been parsed yet
    }

    fun getMenuItemTitle(index: Int): String? {
        return if (null != menu) {
            menu!!.getItemAt(index).title
        } else null
        // menu has not been parsed yet
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (MiscUtils.isGravityBottom(gravity)) {
            val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

            if (widthMode == View.MeasureSpec.AT_MOST) {
                throw IllegalArgumentException("layout_width must be equal to `match_parent`")
            }
            setMeasuredDimension(widthSize, navigationHeight + bottomInset + shadowHeight)

        } else if (MiscUtils.isGravitiyLeft(gravity) || MiscUtils.isGravityRight(gravity)) {
            val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

            if (heightMode == View.MeasureSpec.AT_MOST) {
                throw IllegalArgumentException("layout_height must be equal to `match_parent`")
            }
            setMeasuredDimension(navigationWidth, heightSize)
        } else {
            throw IllegalArgumentException("invalid layout_gravity. Only one start, end, left, right or bottom is allowed")
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
        marginLayoutParams.bottomMargin = -bottomInset
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            val params = rippleOverlay.layoutParams
            var size = min(width, height)

            if (!isTablet(gravity)) {
                if (bottomInset == 0) {
                    size *= 2
                }
            } else {
                size = (size * 1.5f).toInt()
            }

            params.width = size
            params.height = size
            rippleOverlay.layoutParams = params
        }

    }

    override fun isAttachedToWindow(): Boolean {
        return if (Build.VERSION.SDK_INT >= 19) {
            super.isAttachedToWindow()
        } else attached
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attached = true

        val params = layoutParams
        val layoutParams: CoordinatorLayout.LayoutParams?
        if (CoordinatorLayout.LayoutParams::class.java.isInstance(params)) {
            layoutParams = params as CoordinatorLayout.LayoutParams
            this.gravity = GravityCompat.getAbsoluteGravity(layoutParams.gravity, ViewCompat.getLayoutDirection(this))
        } else {
            layoutParams = null
            // TODO: check the gravity in other viewparent types
            this.gravity = Gravity.BOTTOM
        }

        initializeUI(gravity)

        if (null != pendingMenu) {
            setItems(pendingMenu)
            pendingMenu = null
        }

        if (null == mBehavior) {
            if (null != layoutParams) {
                mBehavior = layoutParams.behavior

                if (isInEditMode) {
                    return
                }

                if (BottomBehavior::class.java.isInstance(mBehavior)) {
                    (mBehavior as BottomBehavior).setLayoutValues(navigationHeight, bottomInset)
                }
            }
        }
    }

    private fun setItems(menu: MenuParser.Menu?) {
        this.menu = menu

        if (null != menu) {
            if (menu.itemsCount < 3 || menu.itemsCount > 5) {
                throw IllegalArgumentException("BottomNavigation expects 3 to 5 items. " + menu.itemsCount + " found")
            }

            enabledRippleBackground = !menu.getItemAt(0).hasColor()

            initializeBackgroundColor(menu)
            clearLayoutManager()
            initializeItems(menu)

            menuChangedListener?.onMenuChanged(this)
        }

        requestLayout()
    }

    private fun initializeUI(gravity: Int) {
        val layerDrawable: LayerDrawable

        val tablet = isTablet(gravity)
        val elevation = resources.getDimensionPixelSize(if (!tablet) R.dimen.bbn_elevation else R.dimen.bbn_elevation_tablet)
        val bgResId = if (!tablet)
            R.drawable.bbn_background
        else
            if (MiscUtils.isGravityRight(gravity))
                R.drawable.bbn_background_tablet_right
            else
                R.drawable.bbn_background_tablet_left
        val paddingBottom = if (!tablet) shadowHeight else 0

        // View elevation
        ViewCompat.setElevation(this, elevation.toFloat())

        // Main background
        layerDrawable = ContextCompat.getDrawable(context, bgResId) as LayerDrawable
        layerDrawable.mutate()
        backgroundDrawable = layerDrawable.findDrawableByLayerId(R.id.bbn_background) as ColorDrawable
        background = layerDrawable

        // Padding bottom
        setPadding(0, paddingBottom, 0, 0)
    }

    private fun initializeBackgroundColor(menu: MenuParser.Menu) {
        backgroundDrawable.color = menu.background
    }

    private fun initializeItems(menu: MenuParser.Menu) {
        Timber.i("initializeItems($menu)")
        layoutManager?.let {
            it.setSelectedIndex(defaultSelectedIndex, false)
            it.populate(menu)
            it.itemClickListener = this
        }

        if (defaultSelectedIndex > -1 && menu.getItemAt(defaultSelectedIndex).hasColor()) {
            backgroundDrawable.color = menu.getItemAt(defaultSelectedIndex).color
        }

        MiscUtils.setDrawableColor(rippleOverlay.background, menu.rippleColor)
    }

    /**
     * Checks if the menu item at the passed index is available and enabled
     */
    private fun isMenuItemEnabled(menu: MenuParser.Menu, index: Int): Boolean {
        return if (menu.itemsCount > index) {
            menu.getItemAt(index).isEnabled
        } else false
    }

    private fun findFirstSelectedIndex(menu: MenuParser.Menu): Int {
        for (i in 0 until menu.itemsCount) {
            if (menu.getItemAt(i).isEnabled) {
                return i
            }
        }
        return -1
    }

    internal inner class MyLayoutChangedListener : View.OnLayoutChangeListener {
        var view: BottomNavigationItemViewAbstract? = null
        private val outRect = Rect()

        override fun onLayoutChange(
                unused: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int) {

            if (null == view) {
                return
            }

            view?.let {
                it.getHitRect(outRect)
                val centerX = rippleOverlay.width / 2
                val centerY = rippleOverlay.height / 2
                rippleOverlay.translationX = (outRect.centerX() - centerX).toFloat()
                rippleOverlay.translationY = (outRect.centerY() - centerY).toFloat()
            }
        }

        fun forceLayout(v: View) {
            view = (v as BottomNavigationItemViewAbstract)
            view?.let { view ->
                onLayoutChange(view, view.left, view.top, view.right, view.bottom, 0, 0, 0, 0)
            }
        }
    }

    override fun onItemDown(parent: LayoutManager, view: View,
                            pressed: Boolean, x: Float, y: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        if (!pressed) {
            if (enabledRippleBackground) {

                circleRevealAnim?.let {
                    if (it.isRunning) {
                        it.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(p0: Animator?) {}
                            override fun onAnimationStart(p0: Animator?) {}
                            override fun onAnimationCancel(p0: Animator?) {}

                            override fun onAnimationEnd(p0: Animator?) {
                                rippleOverlay.animate().alpha(0f).setDuration(rippleRevealAnimationDuration).start()
                            }
                        })
                    } else {
                        rippleOverlay.animate().alpha(0f).setDuration(rippleRevealAnimationDuration).start()
                    }
                }

                rippleOverlay.isPressed = false
            }
            rippleOverlay.isHovered = false
            return
        } else {
            mLayoutChangedListener.forceLayout(view)
            rippleOverlay.isHovered = true

            if (enabledRippleBackground) {
                rippleOverlay.alpha = 1f
                rippleOverlay.isPressed = true

                circleRevealAnim?.removeAllListeners()
                circleRevealAnim?.cancel()
                circleRevealAnim =
                        ViewAnimationUtils.createCircularReveal(rippleOverlay, rippleOverlay.width / 2, rippleOverlay.height / 2, 0f, (rippleOverlay.width / 2).toFloat())
                circleRevealAnim?.duration = rippleRevealAnimationDuration
                circleRevealAnim?.start()


            }
        }
    }

    override fun onItemClick(parent: LayoutManager, view: View, index: Int, animate: Boolean) {
        setSelectedItemInternal(parent, view, index, animate, true)
        mLayoutChangedListener.forceLayout(view)
    }

    private fun setSelectedItemInternal(
            layoutManager: LayoutManager,
            view: View, index: Int,
            animate: Boolean,
            fromUser: Boolean) {

        val item: BottomNavigationItem? = when {
            index > -1 && index < menu!!.itemsCount -> menu!!.getItemAt(index)
            else -> null
        }

        if (layoutManager.getSelectedIndex() != index) {
            layoutManager.setSelectedIndex(index, animate)

            if (null != item && item.hasColor()) {
                if (animate) {
                    MiscUtils.animate(
                            this,
                            view,
                            backgroundOverlay,
                            backgroundDrawable,
                            item.color,
                            backgroundColorAnimation)
                } else {
                    MiscUtils.switchColor(
                            this,
                            view,
                            backgroundOverlay,
                            backgroundDrawable,
                            item.color)
                }
            }

            menuItemSelectionListener?.onMenuItemSelect(item?.id ?: -1, index, fromUser)

        } else {
            menuItemSelectionListener?.onMenuItemReselect(item?.id ?: -1, index, fromUser)
        }
    }

    fun setDefaultTypeface(typeface: Typeface) {
        this.typeface = SoftReference(typeface)
    }

    fun setDefaultSelectedIndex(defaultSelectedIndex: Int) {
        this.defaultSelectedIndex = defaultSelectedIndex
    }

    fun invalidateBadge(itemId: Int) {
        if (null != layoutManager) {
            val viewAbstract = layoutManager!!.findViewById<View>(itemId) as BottomNavigationItemViewAbstract?
            viewAbstract?.invalidateBadge()
        }
    }

    interface OnMenuItemSelectionListener {
        fun onMenuItemSelect(@IdRes itemId: Int, position: Int, fromUser: Boolean)

        fun onMenuItemReselect(@IdRes itemId: Int, position: Int, fromUser: Boolean)
    }

    interface OnMenuChangedListener {
        fun onMenuChanged(parent: BottomNavigation)
    }

    internal class SavedState : View.BaseSavedState {
        var selectedIndex: Int = 0
        var badgeBundle: Bundle? = null
        var disabledIndices = arrayListOf<Int>()

        @SuppressLint("ParcelClassLoader")
        constructor(`in`: Parcel) : super(`in`) {
            selectedIndex = `in`.readInt()
            badgeBundle = `in`.readBundle()
            `in`.readList(disabledIndices, SavedState::class.java.classLoader)
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedIndex)
            out.writeBundle(badgeBundle)
            out.writeList(disabledIndices)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        var DEBUG = false

        internal const val PENDING_ACTION_NONE = 0x0
        internal const val PENDING_ACTION_EXPANDED = 0x1
        internal const val PENDING_ACTION_COLLAPSED = 0x2
        internal const val PENDING_ACTION_ANIMATE_ENABLED = 0x4

        private val WIDGET_PACKAGE_NAME: String?

        init {
            val pkg = BottomNavigation::class.java.getPackage()
            WIDGET_PACKAGE_NAME = pkg?.name
        }

        private val CONSTRUCTOR_PARAMS = arrayOf<Class<*>>(BottomNavigation::class.java)

        private val LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE =
                arrayOf<Class<*>>(Context::class.java, AttributeSet::class.java, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!)

        private val S_CONSTRUCTORS = ThreadLocal<MutableMap<String, Constructor<BadgeProvider>>>()

        @Suppress("UNCHECKED_CAST")
        internal fun parseBadgeProvider(navigation: BottomNavigation, context: Context, name: String?): BadgeProvider {

            if (name.isNullOrEmpty()) {
                return BadgeProvider(navigation)
            }

            val fullName = when {
                name.startsWith(".") -> context.packageName + name
                name.indexOf('.') >= 0 -> name
                else -> // Assume stock behavior in this package (if we have one)
                    if (!TextUtils.isEmpty(WIDGET_PACKAGE_NAME))
                        "$WIDGET_PACKAGE_NAME.$name"
                    else
                        name
            }

            try {
                var constructors: MutableMap<String, Constructor<BadgeProvider>>? = S_CONSTRUCTORS.get()
                if (constructors == null) {
                    constructors = HashMap()
                    S_CONSTRUCTORS.set(constructors)
                }
                var c = constructors[fullName]
                if (c == null) {
                    val clazz = Class.forName(fullName, true, context.classLoader) as Class<BadgeProvider>
                    c = clazz.getConstructor(*CONSTRUCTOR_PARAMS)
                    c!!.isAccessible = true
                    constructors[fullName] = c
                }
                return c.newInstance(navigation)
            } catch (e: Exception) {
                throw RuntimeException("Could not inflate Behavior subclass $fullName", e)
            }

        }
    }
}
