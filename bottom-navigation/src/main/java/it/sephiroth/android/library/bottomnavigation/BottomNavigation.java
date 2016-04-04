package it.sephiroth.android.library.bottomnavigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import it.sephiroth.android.library.bottonnavigation.R;

/**
 * Created by alessandro on 4/2/16.
 */
public class BottomNavigation extends FrameLayout {
    private static final String TAG = BottomNavigation.class.getSimpleName();

    private int bottomInset;
    private int defaultHeight;
    private boolean invertedTheme;

    private int shadowHeight;

    private SystemBarTintManager systembarTint;

    private ViewGroup itemsContainer;
    private View backgroundOverlay;

    /** true if translucent navigation is on */
    private boolean hasTransucentNavigation;
    int backgroundColorPrimary;
    int backgroundColorPrimaryInverted;

    /**
     * inactive item color, when using the invertedTheme
     */
    int inactiveItemInvertedColor;

    private BottomNavigationItem[] entries;

    private boolean shifting;
    private int defaultSelectedIndex = 0;
    private ColorDrawable backgroundDrawable;
    private long backgroundColorAnimation;

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

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigation, defStyleAttr, defStyleRes);
        invertedTheme = array.getBoolean(R.styleable.BottomNavigation_bbn_inverted_theme, false);

        inactiveItemInvertedColor = array.getColor(
            R.styleable.BottomNavigation_bbn_invertedColorInactive,
            ContextCompat.getColor(context, R.color.bbn_item_inverted_color_inactive)
        );

        backgroundColorPrimary = array
            .getColor(R.styleable.BottomNavigation_bbn_backgroundColorPrimary, MiscUtils.getColor(context, R.attr.colorPrimary));
        backgroundColorPrimaryInverted = array.getColor(
            R.styleable.BottomNavigation_bbn_backgroundColorPrimaryInverted,
            ContextCompat.getColor(context, android.R.color.white)
        );

        final int menuResId = array.getResourceId(R.styleable.BottomNavigation_bbn_entries, 0);
        entries = MenuParser.inflateMenu(context, menuResId);
        array.recycle();

        Log.v(TAG, "invertedTheme: " + invertedTheme);
        Log.v(TAG, String.format("backgroundColorPrimary: #%x", backgroundColorPrimary));
        Log.v(TAG, String.format("backgroundColorPrimaryInverted: #%x", backgroundColorPrimaryInverted));

        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.bbn_background);
        backgroundDrawable = (ColorDrawable) layerDrawable.findDrawableByLayerId(R.id.bbn_background);
        backgroundDrawable.setColor(invertedTheme ? backgroundColorPrimaryInverted : backgroundColorPrimary);

        // replace the background color
        setBackground(layerDrawable);

        backgroundColorAnimation = getResources().getInteger(R.integer.bbn_background_animation_duration);
        defaultHeight = getResources().getDimensionPixelSize(R.dimen.bbn_bottom_navigation_height);
        shadowHeight = getResources().getDimensionPixelOffset(R.dimen.bbn_top_shadow_height);

        // apply the default elevation
        final int elevation = getResources().getDimensionPixelSize(R.dimen.bbn_elevation);
        ViewCompat.setElevation(this, MiscUtils.getDimensionPixelSize(getContext(), elevation));

        // check if the botton navigation is translucent
        hasTransucentNavigation = MiscUtils.hasTranslucentNavigation(activity);

        if (hasTransucentNavigation && systembarTint.getConfig().isNavigationAtBottom() && systembarTint.getConfig()
            .hasNavigtionBar()) {
            bottomInset = systembarTint.getConfig().getNavigationBarHeight();
        } else {
            bottomInset = 0;
        }

        Log.v(TAG, "bottomInset: " + bottomInset + ", " + systembarTint.getConfig().hasNavigtionBar() + ", " + systembarTint
            .getConfig().isNavigationAtBottom());
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // Log.i(TAG, "onMeasure");

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
        Log.i(TAG, "onSizeChanged: " + w + "x" + h);
        super.onSizeChanged(w, h, oldw, oldh);

        ((CoordinatorLayout.LayoutParams) getLayoutParams())
            .setBehavior(new BottomNavigationBehavior(getContext(), defaultHeight, bottomInset));

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
        marginLayoutParams.bottomMargin = -bottomInset;

        if (null != entries) {
            initializeContainer();
            initializeItems();
            entries = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        Log.i(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();

        if (null == itemsContainer) {
            setPadding(0, shadowHeight, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, defaultHeight);
            backgroundOverlay = new View(getContext());
            backgroundOverlay.setLayoutParams(params);
            addView(backgroundOverlay);
        }
    }

    private void initializeContainer() {
        if (null == itemsContainer) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, defaultHeight);
            itemsContainer = new ShiftingLayout(getContext());
            itemsContainer.setLayoutParams(params);
            addView(itemsContainer);
        }
    }

    private void initializeItems() {
        Log.i(TAG, "initializeItems: " + entries.length);
        final float density = getResources().getDisplayMetrics().density;
        final int screenWidth = getWidth();

        shifting = entries.length > 3;
        Log.v(TAG, "density: " + density);
        Log.v(TAG, "screenWidth: " + screenWidth);
        Log.v(TAG, "screenWidth(dp): " + (screenWidth / density));

        int maxActiveItemWidth = MiscUtils.getDimensionPixelSize(getContext(), 168);
        int minActiveItemWidth = MiscUtils.getDimensionPixelSize(getContext(), 96);

        int maxInactiveItemWidth = MiscUtils.getDimensionPixelSize(getContext(), 96);
        int minInactiveItemWidth = MiscUtils.getDimensionPixelSize(getContext(), 64);

        int itemWidthMin;
        int itemWidthMax;

        final int totalWidth = maxInactiveItemWidth * (entries.length - 1) + maxActiveItemWidth;
        Log.v(TAG, "totalWidth: " + totalWidth);
        Log.v(TAG, "totalWidth(dp): " + totalWidth / density);

        if (totalWidth > screenWidth) {
            float ratio = (float) screenWidth / totalWidth;
            itemWidthMin = (int) Math.max(maxInactiveItemWidth * ratio, minInactiveItemWidth);
            itemWidthMax = (int) (maxActiveItemWidth * ratio);

            if (itemWidthMin * (entries.length - 1) + itemWidthMax > screenWidth) {
                itemWidthMax = screenWidth - (itemWidthMin * (entries.length - 1)); // minActiveItemWidth?
            }
        } else {
            itemWidthMax = maxActiveItemWidth;
            itemWidthMin = maxInactiveItemWidth;
        }

        Log.v(TAG, "active size: " + maxActiveItemWidth + ", " + minActiveItemWidth);
        Log.v(TAG, "inactive size: " + maxInactiveItemWidth + ", " + minInactiveItemWidth);

        Log.v(TAG, "active size (dp): " + maxActiveItemWidth / density + ", " + minActiveItemWidth / density);
        Log.v(TAG, "inactive size (dp): " + maxInactiveItemWidth / density + ", " + minInactiveItemWidth / density);

        Log.v(TAG, "itemWidth: " + itemWidthMin + ", " + itemWidthMax);
        Log.v(TAG, "itemWidth(dp): " + (itemWidthMin / density) + ", " + (itemWidthMax / density));

        if (shifting) {
            ((ShiftingLayout) itemsContainer).setTotalSize(itemWidthMin, itemWidthMax);
            ((ShiftingLayout) itemsContainer).setSelectedChild(defaultSelectedIndex);
        }

        for (int i = 0; i < entries.length; i++) {
            final BottomNavigationItem item = entries[i];
            Log.d(TAG, "item: " + item);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemWidthMin, defaultHeight);

            if (i == defaultSelectedIndex) {
                params.width = itemWidthMax;
                if (!invertedTheme && item.hasColor()) {
                    backgroundDrawable.setColor(item.getColor());
                }
            }

            BottomNavigationShiftingItemView view =
                new BottomNavigationShiftingItemView(this, i == defaultSelectedIndex, invertedTheme);
            view.setItem(item);
            view.setLayoutParams(params);
            view.setIsShifting(shifting);
            view.setClickable(true);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    int index = itemsContainer.indexOfChild(v);
                    ((ShiftingLayout) itemsContainer).setSelectedChild(index);

                    if (!invertedTheme && item.hasColor()) {
                        MiscUtils.animate(BottomNavigation.this, v,
                            backgroundOverlay,
                            backgroundDrawable,
                            item.getColor(),
                            backgroundColorAnimation
                        );
                    }

                }
            });
            itemsContainer.addView(view);
        }
    }

}
