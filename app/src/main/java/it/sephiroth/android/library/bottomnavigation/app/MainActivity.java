package it.sephiroth.android.library.bottomnavigation.app;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;

import it.sephiroth.android.library.bottomnavigation.Behavior;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

@TargetApi (Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int MENU_TYPE_3_ITEMS = 0;
    public static final int MENU_TYPE_3_ITEMS_NO_BACKGROUND = 1;

    public static final int MENU_TYPE_4_ITEMS = 2;
    public static final int MENU_TYPE_4_ITEMS_NO_BACKGROUND = 3;

    public static final int MENU_TYPE_5_ITEMS = 4;
    public static final int MENU_TYPE_5_ITEMS_NO_BACKGROUND = 5;

    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private boolean mTranslucentStatus;
    private boolean mTranslucentStatusSet;
    private boolean mTranslucentNavigation;
    private boolean mTranslucentNavigationSet;
    private SystemBarTintManager mSystemBarTint;
    private Toolbar mToolbar;
    BottomNavigation mBottomNavigation;
    private FloatingActionButton mFab;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final int statusbarHeight = getStatusBarHeight();
        final boolean translucentStatus = hasTranslucentStatusBar();
        final boolean translucentNavigation = hasTranslucentNavigation();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mBottomNavigation) {

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBottomNavigation.setExpanded(!mBottomNavigation.isExpanded(), false);
                        }
                    }, 200);
                }
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action",null).show();
            }
        });

        mAppBarLayout = (AppBarLayout) findViewById(R.id.AppBarLayout01);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout01);

        if (translucentStatus) {
            Log.d(TAG, "hasTranslucentStatusBar");
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mCoordinatorLayout.getLayoutParams();
            params.topMargin = -statusbarHeight;

            params = (ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams();
            params.topMargin = statusbarHeight;
        }

        if (translucentNavigation) {
            Log.d(TAG, "hasTranslucentNavigation");
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mCoordinatorLayout.getLayoutParams();
            //            params.bottomMargin = -getSystemBarTint().getConfig().getNavigationBarHeight();

            params = (ViewGroup.MarginLayoutParams) mFab.getLayoutParams();
            //            params.bottomMargin += getSystemBarTint().getConfig().getNavigationBarHeight();

        }

        if (null != mBottomNavigation) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
            mBottomNavigation.setDefaultTypeface(typeface);
            mBottomNavigation.setDefaultSelectedIndex(2);
        }

        mCoordinatorLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCoordinatorLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                AppBarLayout.Behavior b =
                    (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
            }
        });
    }

    public Behavior getNavigationBehavior() {
        if (null != mBottomNavigation) {
            final ViewGroup.LayoutParams params = mBottomNavigation.getLayoutParams();
            if (params instanceof CoordinatorLayout.LayoutParams) {
                final CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
                if (behavior instanceof Behavior) {
                    return (Behavior) behavior;
                }
            }
        }
        return null;
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mBottomNavigation = (BottomNavigation) findViewById(R.id.BottomNavigation);
    }

    public SystemBarTintManager getSystemBarTint() {
        if (null == mSystemBarTint) {
            mSystemBarTint = new SystemBarTintManager(this);
        }
        return mSystemBarTint;
    }

    public int getStatusBarHeight() {
        return getSystemBarTint().getConfig().getStatusBarHeight();
    }

    public boolean hasTranslucentStatusBar() {
        if (!mTranslucentStatusSet) {
            if (Build.VERSION.SDK_INT >= 19) {
                mTranslucentStatus =
                    ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                mTranslucentStatus = false;
            }
            mTranslucentStatusSet = true;
        }
        return mTranslucentStatus;
    }

    public boolean hasTranslucentNavigation() {
        if (!mTranslucentNavigationSet) {
            final SystemBarConfig config = getSystemBarTint().getConfig();
            if (Build.VERSION.SDK_INT >= 19) {
                boolean themeConfig =
                    ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                        == WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                mTranslucentNavigation = themeConfig && config.hasNavigtionBar() && config.isNavigationAtBottom()
                    && config.getNavigationBarHeight() > 0;
            }
            mTranslucentNavigationSet = true;
        }
        return mTranslucentNavigation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setMenuType(final int type) {
        if (null == mBottomNavigation) {
            return;
        }

        switch (type) {
            case MENU_TYPE_3_ITEMS:
                mBottomNavigation.setMenuItems(R.menu.bottombar_menu_3items);
                break;

            case MENU_TYPE_3_ITEMS_NO_BACKGROUND:
                mBottomNavigation.setMenuItems(R.menu.bottombar_menu_3items_no_background);
                break;

            case MENU_TYPE_4_ITEMS:
                mBottomNavigation.setMenuItems(R.menu.bottombar_menu_4items);
                break;

            case MENU_TYPE_4_ITEMS_NO_BACKGROUND:
                mBottomNavigation.setMenuItems(R.menu.bottombar_menu_4items_no_background);
                break;

            case MENU_TYPE_5_ITEMS:
                mBottomNavigation.setMenuItems(R.menu.bottombar_menu_5items);
                break;

            case MENU_TYPE_5_ITEMS_NO_BACKGROUND:
                mBottomNavigation.setMenuItems(R.menu.bottombar_menu_5items_no_background);
                break;

        }
    }
}
