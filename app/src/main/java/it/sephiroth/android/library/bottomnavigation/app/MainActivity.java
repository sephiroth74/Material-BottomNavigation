package it.sephiroth.android.library.bottomnavigation.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottomnavigation.FloatingActionButtonBehavior;
import it.sephiroth.android.library.bottomnavigation.MiscUtils;

import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;

@TargetApi (Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivity extends BaseActivity implements BottomNavigation.OnMenuItemSelectionListener {

    static final String TAG = MainActivity.class.getSimpleName();

    public static final int MENU_TYPE_3_ITEMS = 0;
    public static final int MENU_TYPE_3_ITEMS_NO_BACKGROUND = 1;

    public static final int MENU_TYPE_4_ITEMS = 2;
    public static final int MENU_TYPE_4_ITEMS_NO_BACKGROUND = 3;

    public static final int MENU_TYPE_5_ITEMS = 4;
    public static final int MENU_TYPE_5_ITEMS_NO_BACKGROUND = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getActivityLayoutResId());
        final ViewGroup root = findViewById(R.id.CoordinatorLayout01);
        final Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        final int statusbarHeight = getStatusBarHeight();
        final boolean translucentStatus = hasTranslucentStatusBar();
        final boolean translucentNavigation = hasTranslucentNavigation();

        MiscUtils.INSTANCE.log(VERBOSE, "translucentStatus: %b", translucentStatus);

        if (translucentStatus) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
            params.topMargin = -statusbarHeight;

            params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = statusbarHeight;
        }

        if (translucentNavigation) {
            final ViewPager viewPager = getViewPager();
            if (null != viewPager) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
                params.bottomMargin = -getNavigationBarHeight();
            }
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
        final FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        if (null != floatingActionButton) {
            floatingActionButton.setOnClickListener(view -> {
                final View root = findViewById(R.id.CoordinatorLayout01);
                Snackbar snackbar = Snackbar.make(root, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction(
                        "Action",
                        null
                    );
                snackbar.show();
            });

            if (hasTranslucentNavigation()) {
                final ViewGroup.LayoutParams params = floatingActionButton.getLayoutParams();
                if (CoordinatorLayout.LayoutParams.class.isInstance(params)) {
                    CoordinatorLayout.LayoutParams params1 = (CoordinatorLayout.LayoutParams) params;
                    if (FloatingActionButtonBehavior.class.isInstance(params1.getBehavior())) {
                        ((FloatingActionButtonBehavior) params1.getBehavior()).setNavigationBarHeight(getNavigationBarHeight());
                    }
                }
            }
        }

        final ViewPager viewPager = getViewPager();
        if (null != viewPager) {

            getBottomNavigation().setMenuChangedListener(parent -> {

                viewPager.setAdapter(new ViewPagerAdapter(MainActivity.this, parent.getMenuItemCount()));
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(
                        final int position, final float positionOffset, final int positionOffsetPixels) { }

                    @Override
                    public void onPageSelected(final int position) {
                        if (getBottomNavigation().getSelectedIndex() != position) {
                            getBottomNavigation().setSelectedIndex(position, false);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(final int state) { }
                });
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
        int itemId = item.getItemId();
        if (itemId == R.id.item1) {
            return setMenuType(MENU_TYPE_3_ITEMS);
        } else if (itemId == R.id.item2) {
            return setMenuType(MENU_TYPE_3_ITEMS_NO_BACKGROUND);
        } else if (itemId == R.id.item3) {
            return setMenuType(MENU_TYPE_4_ITEMS);
        } else if (itemId == R.id.item4) {
            return setMenuType(MENU_TYPE_4_ITEMS_NO_BACKGROUND);
        } else if (itemId == R.id.item5) {
            return setMenuType(MENU_TYPE_5_ITEMS);
        } else if (itemId == R.id.item6) {
            return setMenuType(MENU_TYPE_5_ITEMS_NO_BACKGROUND);
        } else if (itemId == R.id.item7) {
            startActivity(new Intent(this, MainActivityTablet.class));
            return true;
        } else if (itemId == R.id.item8) {
            startActivity(new Intent(this, MainActivityTabletCollapsedToolbar.class));
            return true;
        } else if (itemId == R.id.item9) {
            startActivity(new Intent(this, MainActivityCustomBehavior.class));
            return true;
        } else if (itemId == R.id.item10) {
            startActivity(new Intent(this, MainActivityCustomBadge.class));
            return true;
        } else if (itemId == R.id.item11) {
            startActivity(new Intent(this, MainActivityNoHide.class));
            return true;
        } else if (itemId == R.id.item12) {
            startActivity(new Intent(this, EnableDisableItemsActivity.class));
            return true;
        } else if (itemId == R.id.item13) {
            startActivity(new Intent(this, MainActivityNoCoordinator.class));
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
        MiscUtils.INSTANCE.log(INFO, "onMenuItemSelect(" + itemId + ", " + position + ", " + fromUser + ")");
        if (fromUser) {
            getBottomNavigation().getBadgeProvider().remove(itemId);
            if (null != getViewPager()) {
//                getViewPager().setCurrentItem(position);
            }
        }
    }

    @Override
    public void onMenuItemReselect(@IdRes final int itemId, final int position, final boolean fromUser) {
        MiscUtils.INSTANCE.log(INFO, "onMenuItemReselect(" + itemId + ", " + position + ", " + fromUser + ")");

        if (fromUser) {
            final FragmentManager manager = getSupportFragmentManager();
            MainActivityFragment fragment = (MainActivityFragment) manager.findFragmentById(R.id.fragment);
            if (null != fragment) {
                fragment.scrollToTop();
            }
        }

    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final int mCount;

        public ViewPagerAdapter(final AppCompatActivity activity, int count) {
            super(activity.getSupportFragmentManager());
            this.mCount = count;
        }

        @Override
        public Fragment getItem(final int position) {
            return new MainActivityFragment();
        }

        @Override
        public int getCount() {
            return mCount;
        }
    }

}
