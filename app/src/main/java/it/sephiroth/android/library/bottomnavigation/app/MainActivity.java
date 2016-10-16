package it.sephiroth.android.library.bottomnavigation.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottomnavigation.MiscUtils;

import static android.util.Log.INFO;

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

        MiscUtils.log(TAG, INFO, "screen density: %g", getResources().getDisplayMetrics().density);

        final int statusbarHeight = getStatusBarHeight();
        final boolean translucentStatus = hasTranslucentStatusBar();

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout01);

        if (translucentStatus) {
            Log.d(TAG, "hasTranslucentStatusBar");
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
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction(
                    "Action",
                    null
                ).show();
            }
        });
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
                navigation.setMenuItems(R.menu.bottombar_menu_3items);
                break;

            case MENU_TYPE_3_ITEMS_NO_BACKGROUND:
                navigation.setMenuItems(R.menu.bottombar_menu_3items_no_background);
                break;

            case MENU_TYPE_4_ITEMS:
                navigation.setMenuItems(R.menu.bottombar_menu_4items);
                break;

            case MENU_TYPE_4_ITEMS_NO_BACKGROUND:
                navigation.setMenuItems(R.menu.bottombar_menu_4items_no_background);
                break;

            case MENU_TYPE_5_ITEMS:
                navigation.setMenuItems(R.menu.bottombar_menu_5items);
                break;

            case MENU_TYPE_5_ITEMS_NO_BACKGROUND:
                navigation.setMenuItems(R.menu.bottombar_menu_5items_no_background);
                break;
        }

        return true;
    }

    @Override
    public void onMenuItemSelect(final int itemId, final int position) {
        Log.i(TAG, "onMenuItemSelect(" + itemId + ", " + position + ")");
        getBottomNavigation().getBadgeProvider().remove(itemId);
    }

    @Override
    public void onMenuItemReselect(@IdRes final int itemId, final int position) {
        Log.i(TAG, "onMenuItemReselect(" + itemId + ", " + position + ")");

        final FragmentManager manager = getSupportFragmentManager();
        MainActivityFragment fragment = (MainActivityFragment) manager.findFragmentById(R.id.fragment);
        fragment.scrollToTop();

    }
}
