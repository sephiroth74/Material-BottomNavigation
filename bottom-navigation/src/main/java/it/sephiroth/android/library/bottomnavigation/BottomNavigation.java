package it.sephiroth.android.library.bottomnavigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
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
     * Default background color in the 'shifting' mode
     */
    int shiftingBackgroundColorPrimary;

    /**
     * Default background color in the 'fixed' mode
     */
    int fixedBackgroundColorPrimary;

    private BottomNavigationItem[] tmpEntries;
    private BottomNavigationItem[] items;

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
     * Icon/Text color for the inactive items
     * in the fixed style
     */
    int fixedItemColorInactive;

    /**
     * Icon/text color for the active items
     * in the fixed style
     */
    int fixedItemColorActive;

    /**
     * Icon/Text color for the active items
     * in the shifting mode
     */
    int shiftingItemColorActive;

    /**
     * Icon alpha for the inactive items
     * in the shifting mode
     */
    float shiftingItemAlphaInactive;

    /**
     * Item background selector
     */
    int rippleColor;

    /**
     * Optional typeface used for the items' text labels
     */
    SoftReference<Typeface> typeface;

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

        fixedItemColorInactive = array.getColor(
            R.styleable.BottomNavigation_bbn_fixedItemColorInactive,
            ContextCompat.getColor(context, R.color.bbn_item_fixed_color_inactive)
        );

        fixedItemColorActive = array.getColor(
            R.styleable.BottomNavigation_bbn_fixedItemColorActive,
            MiscUtils.getColor(context, android.R.attr.colorForeground)
        );

        shiftingItemColorActive = array.getColor(
            R.styleable.BottomNavigation_bbn_shiftingItemColorActive,
            MiscUtils.getColor(context, android.R.attr.colorForegroundInverse)
        );

        shiftingItemAlphaInactive = array.getFloat(
            R.styleable.BottomNavigation_bbn_shiftingItemInactiveAlpha,
            getResources().getFraction(R.fraction.bbn_item_shifting_inactive_alpha, 1, 1)
        );

        shiftingBackgroundColorPrimary = array
            .getColor(R.styleable.BottomNavigation_bbn_shifting_backgroundColor, MiscUtils.getColor(context, R.attr.colorPrimary));

        fixedBackgroundColorPrimary = array.getColor(
            R.styleable.BottomNavigation_bbn_fixed_backgroundColor,
            MiscUtils.getColor(getContext(), android.R.attr.windowBackground)
        );

        rippleColor = array.getColor(R.styleable.BottomNavigation_bbn_rippleColor, shiftingBackgroundColorPrimary);

        final int menuResId = array.getResourceId(R.styleable.BottomNavigation_bbn_entries, 0);
        BottomNavigationItem[] entries = MenuParser.inflateMenu(context, menuResId);
        array.recycle();

        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.bbn_background);
        backgroundDrawable = (ColorDrawable) layerDrawable.findDrawableByLayerId(R.id.bbn_background);

        // replace the background color
        setBackground(layerDrawable);

        backgroundColorAnimation = getResources().getInteger(R.integer.bbn_background_animation_duration);
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

        setItems(entries);
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

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        log(TAG, INFO, "onSizeChanged(%d, %d)", w, h);
        super.onSizeChanged(w, h, oldw, oldh);

        ((CoordinatorLayout.LayoutParams) getLayoutParams())
            .setBehavior(new BottomNavigationBehavior(getContext(), defaultHeight, bottomInset));

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
        marginLayoutParams.bottomMargin = -bottomInset;

        if (null != tmpEntries) {
            shifting = tmpEntries.length > 3;
            initializeUI();
            initializeContainer();
            initializeItems();
            tmpEntries = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        log(TAG, INFO, "onAttachedToWindow");
        super.onAttachedToWindow();

        if (null == itemsContainer) {
            setPadding(0, shadowHeight, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            backgroundOverlay = new View(getContext());
            backgroundOverlay.setLayoutParams(params);
            addView(backgroundOverlay);
        }
    }

    public void setItems(BottomNavigationItem[] entries) {
        log(TAG, INFO, "setItems");

        tmpEntries = entries;
        items = entries;
        requestLayout();
    }

    private void initializeUI() {
        log(TAG, INFO, "initializeUI");

        log(TAG, VERBOSE, "shiftingBackgroundColorPrimary: %x", shiftingBackgroundColorPrimary);
        log(TAG, VERBOSE, "fixedBackgroundColorPrimary: %x", fixedBackgroundColorPrimary);
        backgroundDrawable.setColor(shifting ? shiftingBackgroundColorPrimary : fixedBackgroundColorPrimary);
    }

    private void initializeContainer() {
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
        log(TAG, INFO, "initializeItems: %d", tmpEntries.length);

        itemsContainer.setSelectedIndex(defaultSelectedIndex);
        itemsContainer.populate(tmpEntries);
        itemsContainer.setOnItemClickListener(this);

        if (items[defaultSelectedIndex].hasColor()) {
            backgroundDrawable.setColor(items[defaultSelectedIndex].getColor());
        }
    }

    @Override
    public void onItemClick(final ItemsLayoutContainer parent, final View view, final int index) {
        log(TAG, INFO, "onItemClick: %d", index);
        parent.setSelectedIndex(index);

        final BottomNavigationItem item = items[index];

        if (item.hasColor()) {
            MiscUtils.animate(
                this,
                view,
                backgroundOverlay,
                backgroundDrawable,
                item.getColor(),
                backgroundColorAnimation
            );
        }
    }

    public void setDefaultTypeface(final Typeface typeface) {
        log(TAG, INFO, "setDefaultTypeface: " + typeface);
        this.typeface = new SoftReference<>(typeface);
    }

    public void setDefaultSelectedIndex(final int defaultSelectedIndex) {
        this.defaultSelectedIndex = defaultSelectedIndex;
    }
}
