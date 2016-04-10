package it.sephiroth.android.library.bottomnavigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.ref.SoftReference;

import it.sephiroth.android.library.bottonnavigation.R;

import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

/**
 * Created by alessandro crugnola on 4/2/16.
 */
public class BottomNavigation extends FrameLayout implements OnItemClickListener {
    private static final String TAG = BottomNavigation.class.getSimpleName();
    public static final boolean DEBUG = false;

    static final int PENDING_ACTION_NONE = 0x0;
    static final int PENDING_ACTION_EXPANDED = 0x1;
    static final int PENDING_ACTION_COLLAPSED = 0x2;
    static final int PENDING_ACTION_ANIMATE_ENABLED = 0x4;

    /**
     * Current pending action (used inside the Behavior instance)
     */
    private int mPendingAction = PENDING_ACTION_NONE;

    /**
     * This is the amount of space we have to cover in case there's a translucent navigation
     * enabled.
     */
    private int bottomInset;

    /**
     * This is the current view height. It does take into account the extra space
     * used in case we have to cover the navigation translucent area, and neither the shadow height.
     */
    private int defaultHeight;

    /**
     * Shadow is created above the widget background. It simulates the
     * elevation.
     */
    private int shadowHeight;

    private SystemBarTintManager systembarTint;

    /**
     * Layout container used to create and manage the UI items.
     * It can be either Fixed or Shifting, based on the widget `mode`
     */
    private ItemsLayoutContainer itemsContainer;

    /**
     * This is where the color animation is happening
     */
    private View backgroundOverlay;

    /**
     * true if translucent navigation is on
     */
    private boolean hasTransucentNavigation;

    /**
     * current menu
     */
    private MenuParser.Menu menu;

    /**
     * Layout mode for the bottomnavigation view.
     * shifting is always true with > 3 items. It's always
     * false with <= 3 items.
     */
    private boolean shifting;

    /**
     * Default selected index.
     * After the items are populated changing this
     * won't have any effect
     */
    private int defaultSelectedIndex = 0;

    /**
     * View visible background color
     */
    private ColorDrawable backgroundDrawable;

    /**
     * Animation duration for the background color change
     */
    private long backgroundColorAnimation;

    /**
     * Optional typeface used for the items' text labels
     */
    SoftReference<Typeface> typeface;

    /**
     * Current Behavior assigned from the CoordinatorLayout
     */
    private Object mBehavior;

    private OnMenuItemSelectionListener listener;

    public BottomNavigation(final Context context) {
        this(context, null);
    }

    public BottomNavigation(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public BottomNavigation(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi (Build.VERSION_CODES.LOLLIPOP)
    public BottomNavigation(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        final Activity activity = (Activity) context;
        systembarTint = new SystemBarTintManager(activity);

        typeface = new SoftReference<>(Typeface.DEFAULT);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigation, defStyleAttr, defStyleRes);
        final int menuResId = array.getResourceId(R.styleable.BottomNavigation_bbn_entries, 0);
        menu = MenuParser.inflateMenu(context, menuResId);
        array.recycle();

        backgroundColorAnimation = getResources().getInteger(R.integer.bbn_background_animation_duration);

        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.bbn_background);
        backgroundDrawable = (ColorDrawable) layerDrawable.findDrawableByLayerId(R.id.bbn_background);

        // replace the background color
        setBackground(layerDrawable);

        defaultHeight = getResources().getDimensionPixelSize(R.dimen.bbn_bottom_navigation_height);
        shadowHeight = getResources().getDimensionPixelOffset(R.dimen.bbn_top_shadow_height);

        // apply the default elevation
        final int elevation = getResources().getDimensionPixelSize(R.dimen.bbn_elevation);
        ViewCompat.setElevation(this, MiscUtils.getDimensionPixelSize(getContext(), elevation));

        // check if the bottom navigation is translucent
        hasTransucentNavigation = MiscUtils.hasTranslucentNavigation(activity);

        if (hasTransucentNavigation
            && systembarTint.getConfig().isNavigationAtBottom()
            && systembarTint.getConfig().hasNavigtionBar()) {
            bottomInset = systembarTint.getConfig().getNavigationBarHeight();
        } else {
            bottomInset = 0;
        }

        log(TAG, VERBOSE, "bottomInset: %d, hasNavigation: %b, navigationAtBottom: %b",
            bottomInset, systembarTint.getConfig().hasNavigtionBar(),
            systembarTint.getConfig().isNavigationAtBottom()
        );

        setPadding(0, shadowHeight, 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        backgroundOverlay = new View(getContext());
        backgroundOverlay.setLayoutParams(params);
        addView(backgroundOverlay);

        setItems(menu);
    }

    int getPendingAction() {
        return mPendingAction;
    }

    void resetPendingAction() {
        mPendingAction = PENDING_ACTION_NONE;
    }

    @SuppressWarnings ("unused")
    public void setSelectedIndex(final int position, final boolean animate) {
        if (null != itemsContainer) {
            setSelectedItemInternal(itemsContainer, ((ViewGroup) itemsContainer).getChildAt(position), position, animate, false);
        }
    }

    @SuppressWarnings ("unused")
    public int getSelectedIndex() {
        if (null != itemsContainer) {
            return itemsContainer.getSelectedIndex();
        }
        return -1;
    }

    @SuppressWarnings ("unused")
    public void setExpanded(boolean expanded, boolean animate) {
        log(TAG, INFO, "setExpanded(%b, %b)", expanded, animate);
        mPendingAction = (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED)
            | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0);
        requestLayout();
    }

    public boolean isExpanded() {
        if (null != mBehavior && mBehavior instanceof Behavior) {
            return ((Behavior) mBehavior).isExpanded();
        }
        return false;
    }

    public void setOnMenuItemClickListener(final OnMenuItemSelectionListener listener) {
        this.listener = listener;
    }

    public void setMenuItems(@MenuRes final int menuResId) {
        defaultSelectedIndex = 0;
        setItems(MenuParser.inflateMenu(getContext(), menuResId));
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = defaultHeight;

        if (widthMode == MeasureSpec.AT_MOST) {
            throw new IllegalArgumentException("layout_width must be equal to `match_parent`");
        }

        setMeasuredDimension(widthSize, heightSize + bottomInset + shadowHeight);
    }

    public int getNavigationHeight() {
        return defaultHeight;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        log(TAG, INFO, "onSizeChanged(%d, %d)", w, h);
        super.onSizeChanged(w, h, oldw, oldh);
        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
        marginLayoutParams.bottomMargin = -bottomInset;
    }

    @Override
    protected void onAttachedToWindow() {
        log(TAG, INFO, "onAttachedToWindow");
        super.onAttachedToWindow();

        if (null == mBehavior) {
            final ViewGroup.LayoutParams params = getLayoutParams();
            if (CoordinatorLayout.LayoutParams.class.isInstance(params)) {
                mBehavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
                if (Behavior.class.isInstance(mBehavior)) {
                    ((Behavior) mBehavior).setLayoutValues(defaultHeight, bottomInset);
                }
            }
        }
    }

    public void setItems(MenuParser.Menu menu) {
        log(TAG, INFO, "setItems");
        this.menu = menu;

        if (null != menu) {
            if (menu.getItemsCount() < 3 || menu.getItemsCount() > 5) {
                throw new IllegalArgumentException("BottomNavigation expects 3 to 5 items. " + menu.getItemsCount() + " found");
            }

            shifting = menu.getItemsCount() > 3;
            initializeUI(menu);
            initializeContainer(menu.isShifting());
            initializeItems();
        }

        requestLayout();
    }

    private void initializeUI(final MenuParser.Menu menu) {
        log(TAG, INFO, "initializeUI");

        final int color = menu.getBackground();
        log(TAG, VERBOSE, "background: %x", color);
        backgroundDrawable.setColor(color);
    }

    private void initializeContainer(final boolean shifting) {
        if (null != itemsContainer) {
            if ((shifting && !ShiftingLayout.class.isInstance(itemsContainer))
                || (!shifting && !FixedLayout.class.isInstance(itemsContainer))) {
                removeView((View) itemsContainer);
                itemsContainer = null;
            } else {
                itemsContainer.removeAll();
            }
        }

        if (null == itemsContainer) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, defaultHeight);

            if (shifting) {
                itemsContainer = new ShiftingLayout(getContext());
            } else {
                itemsContainer = new FixedLayout(getContext());
            }
            itemsContainer.setLayoutParams(params);
            addView((View) itemsContainer);
        }
    }

    private void initializeItems() {
        log(TAG, INFO, "initializeItems");

        itemsContainer.setSelectedIndex(defaultSelectedIndex, false);
        itemsContainer.populate(menu);
        itemsContainer.setOnItemClickListener(this);

        if (menu.getItemAt(defaultSelectedIndex).hasColor()) {
            backgroundDrawable.setColor(menu.getItemAt(defaultSelectedIndex).getColor());
        }
    }

    @Override
    public void onItemClick(final ItemsLayoutContainer parent, final View view, final int index, boolean animate) {
        log(TAG, INFO, "onItemClick: %d", index);
        setSelectedItemInternal(parent, view, index, animate, true);
    }

    private void setSelectedItemInternal(
        final ItemsLayoutContainer layoutContainer,
        final View view, final int index,
        final boolean animate,
        final boolean fromUser) {

        final BottomNavigationItem item = menu.getItemAt(index);

        if (layoutContainer.getSelectedIndex() != index) {
            layoutContainer.setSelectedIndex(index, animate);

            if (item.hasColor()) {
                if (animate) {
                    MiscUtils.animate(
                        this,
                        view,
                        backgroundOverlay,
                        backgroundDrawable,
                        item.getColor(),
                        backgroundColorAnimation
                    );
                } else {
                    MiscUtils.switchColor(
                        this,
                        view,
                        backgroundOverlay,
                        backgroundDrawable,
                        item.getColor()
                    );
                }
            }

            if (null != listener && fromUser) {
                listener.onMenuItemSelect(item.getId(), index);
            }

        } else {
            if (null != listener && fromUser) {
                listener.onMenuItemReselect(item.getId(), index);
            }
        }

    }

    public void setDefaultTypeface(final Typeface typeface) {
        log(TAG, INFO, "setDefaultTypeface: " + typeface);
        this.typeface = new SoftReference<>(typeface);
    }

    public void setDefaultSelectedIndex(final int defaultSelectedIndex) {
        this.defaultSelectedIndex = defaultSelectedIndex;
    }

    public interface OnMenuItemSelectionListener {
        void onMenuItemSelect(@IdRes final int itemId, final int position);

        void onMenuItemReselect(@IdRes final int itemId, final int position);
    }
}
