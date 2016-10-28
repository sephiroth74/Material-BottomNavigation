package it.sephiroth.android.library.bottomnavigation.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

@TargetApi (Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivity extends BaseActivity implements BottomNavigation.OnMenuItemSelectionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int MENU_TYPE_3_ITEMS = 0;
    public static final int MENU_TYPE_3_ITEMS_NO_BACKGROUND = 1;

    public static final int MENU_TYPE_4_ITEMS = 2;
    public static final int MENU_TYPE_4_ITEMS_NO_BACKGROUND = 3;

    public static final int MENU_TYPE_5_ITEMS = 4;
    public static final int MENU_TYPE_5_ITEMS_NO_BACKGROUND = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BottomNavigation.DEBUG = BuildConfig.DEBUG;

        setContentView(getActivityLayoutResId());
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final int statusbarHeight = getStatusBarHeight();
        final boolean translucentStatus = hasTranslucentStatusBar();

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout01);

        if (translucentStatus) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) coordinatorLayout.getLayoutParams();
            params.topMargin = -statusbarHeight;

            params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = statusbarHeight;
        }

        initializeBottomNavigation(savedInstanceState);
        initializeUI(savedInstanceState);
    }

    protected int getActivityLayoutResId() {return R.layout.activity_main;}

    protected void initializeBottomNavigation(final Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            getBottomNavigation().setDefaultSelectedIndex(0);
            final BadgeProvider provider = getBottomNavigation().getBadgeProvider();
            provider.show(R.id.bbn_item3);
            provider.show(R.id.bbn_item4);
        }
    }

    protected void initializeUI(final Bundle savedInstanceState) {
        final FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        assert floatingActionButton != null;
        if (null != floatingActionButton) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout01);
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction(
                            "Action",
                            null
                        );
                    View snackbarView = snackbar.getView();
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams();
                    params.setAnchorId(R.id.BottomNavigation);
                    params.anchorGravity = Gravity.TOP;
                    params.bottomMargin = getBottomNavigation().getHeight();
                    snackbarView.setLayoutParams(params);
                    snackbar.show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.item1:
                return setMenuType(MENU_TYPE_3_ITEMS);
            case R.id.item2:
                return setMenuType(MENU_TYPE_3_ITEMS_NO_BACKGROUND);
            case R.id.item3:
                return setMenuType(MENU_TYPE_4_ITEMS);
            case R.id.item4:
                return setMenuType(MENU_TYPE_4_ITEMS_NO_BACKGROUND);
            case R.id.item5:
                return setMenuType(MENU_TYPE_5_ITEMS);
            case R.id.item6:
                return setMenuType(MENU_TYPE_5_ITEMS_NO_BACKGROUND);
            case R.id.item7:
                startActivity(new Intent(this, MainActivityTablet.class));
                return true;
            case R.id.item8:
                startActivity(new Intent(this, MainActivityTabletCollapsedToolbar.class));
                return true;
            case R.id.item9:
                startActivity(new Intent(this, MainActivityCustomBehavior.class));
                return true;
            case R.id.item10:
                startActivity(new Intent(this, MainActivityCustomBadge.class));
                return true;
            case R.id.item11:
                startActivity(new Intent(this, MainActivityNoHide.class));
                return true;
            case R.id.item12:
                startActivity(new Intent(this, EnableDisableItemsActivity.class));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public boolean setMenuType(final int type) {
        BottomNavigation navigation = getBottomNavigation();
        if (null == navigation) {
            return false;
        }

        switch (type) {
            case MENU_TYPE_3_ITEMS:
                navigation.inflateMenu(R.menu.bottombar_menu_3items);
                break;

            case MENU_TYPE_3_ITEMS_NO_BACKGROUND:
                navigation.inflateMenu(R.menu.bottombar_menu_3items_no_background);
                break;

            case MENU_TYPE_4_ITEMS:
                navigation.inflateMenu(R.menu.bottombar_menu_4items);
                break;

            case MENU_TYPE_4_ITEMS_NO_BACKGROUND:
                navigation.inflateMenu(R.menu.bottombar_menu_4items_no_background);
                break;

            case MENU_TYPE_5_ITEMS:
                navigation.inflateMenu(R.menu.bottombar_menu_5items);
                break;

            case MENU_TYPE_5_ITEMS_NO_BACKGROUND:
                navigation.inflateMenu(R.menu.bottombar_menu_5items_no_background);
                break;
        }

        return true;
    }

    @Override
    public void onMenuItemSelect(final int itemId, final int position, final boolean fromUser) {
        log(TAG, INFO, "onMenuItemSelect(" + itemId + ", " + position + ", " + fromUser + ")");
        if (fromUser) {
            getBottomNavigation().getBadgeProvider().remove(itemId);
        }
    }

    @Override
    public void onMenuItemReselect(@IdRes final int itemId, final int position, final boolean fromUser) {
        log(TAG, INFO, "onMenuItemReselect(" + itemId + ", " + position + ", " + fromUser + ")");

        if (fromUser) {
            final FragmentManager manager = getSupportFragmentManager();
            MainActivityFragment fragment = (MainActivityFragment) manager.findFragmentById(R.id.fragment);
            fragment.scrollToTop();
        }

    }

    public static class FabBehavior extends FloatingActionButton.Behavior {
        public FabBehavior() {
            super();
        }

        public FabBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onAttachedToLayoutParams(
            @NonNull final CoordinatorLayout.LayoutParams lp) {
            log(TAG, INFO, "onAttachedToLayoutParams: %s", lp);
            super.onAttachedToLayoutParams(lp);
        }

        @Override
        public boolean layoutDependsOn(final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
            if (BottomNavigation.class.isInstance(dependency)) {
                return true;
            } else if (Snackbar.SnackbarLayout.class.isInstance(dependency)) {
                return true;
            }

            return super.layoutDependsOn(parent, child, dependency);
        }

        int originalPosition = 0;

        @Override
        public boolean onDependentViewChanged(
            final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
            log(TAG, INFO, "onDependentViewChanged: " + dependency);

            final List<View> list = parent.getDependencies(child);
            log(TAG, DEBUG, "list size: %d", list.size());
            if (list.size() == 2) {
                log(TAG, VERBOSE, "%s", list.get(0));
                log(TAG, VERBOSE, "%s", list.get(1));
                child.setTranslationY(0);
                return false;
            }

            if (BottomNavigation.class.isInstance(dependency)) {
                BottomNavigation navigation = (BottomNavigation) dependency;
                int bottomMargin = ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
                child.setTranslationY(navigation.getTranslationY() - navigation.getHeight() + bottomMargin);
                //                child.postInvalidate();
                return false;
            }

            return false;
        }

        @Override
        public void onDependentViewRemoved(
            final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
            log(TAG, INFO, "onDependentViewRemoved: %s", dependency);

            super.onDependentViewRemoved(parent, child, dependency);

            final List<View> list = parent.getDependencies(child);
            log(TAG, VERBOSE, "list.size: %d", list.size());

            if (list.size() == 2 && Snackbar.SnackbarLayout.class.isInstance(dependency)) {
                final View other = list.get(0);
                if (BottomNavigation.class.isInstance(other)) {
                    BottomNavigation navigation = (BottomNavigation) other;
                    int bottomMargin = ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
                    child.setTranslationY(navigation.getTranslationY() - navigation.getNavigationHeight() - bottomMargin);
                    //                    child.postInvalidate();
                }
            }
        }
    }
}
