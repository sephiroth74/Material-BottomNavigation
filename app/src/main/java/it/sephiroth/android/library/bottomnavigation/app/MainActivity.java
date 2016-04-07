package it.sephiroth.android.library.bottomnavigation.app;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

@TargetApi (Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivity extends AppCompatActivity implements View.OnLayoutChangeListener {

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
    private SystemBarTintManager mSystemBarTint;
    private Toolbar mToolbar;
    private BottomNavigation mBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        mAppBarLayout = (AppBarLayout) findViewById(R.id.AppBarLayout01);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout01);
        mCoordinatorLayout.addOnLayoutChangeListener(this);

        if (hasTranslucentStatusBar()) {
            int statusbarHeight = getStatusBarHeight();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mCoordinatorLayout.getLayoutParams();
            params.topMargin = -statusbarHeight;
            params = (ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams();
            params.topMargin = statusbarHeight;
        }

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        mBottomNavigation.setDefaultTypeface(typeface);
        mBottomNavigation.setDefaultSelectedIndex(2);
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

    @Override
    public void onLayoutChange(
        final View v, final int left, final int top, final int right, final int bottom, final int oldLeft, final int oldTop,
        final int oldRight, final int oldBottom) {

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mAppBarLayout.getLayoutParams();

        final int topInset = 63;

        //        params.height = 224 + topInset;
        //        params.topMargin = topInset;
        //        mAppBarLayout.setLayoutParams(params);
        //        mAppBarLayout.setPadding(0, topInset, 0, 0);

        mCoordinatorLayout.removeOnLayoutChangeListener(this);
    }

    public void setMenuType(final int type) {
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
