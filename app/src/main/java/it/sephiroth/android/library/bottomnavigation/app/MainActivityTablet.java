package it.sephiroth.android.library.bottomnavigation.app;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottomnavigation.MiscUtils;

import static android.util.Log.INFO;

@TargetApi (Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivityTablet extends BaseActivity implements BottomNavigation.OnMenuItemSelectionListener {
    private static final String TAG = MainActivityTablet.class.getSimpleName();

    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tablet);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        MiscUtils.log(TAG, INFO, "screen density: %g", getResources().getDisplayMetrics().density);

        final int statusbarHeight = getStatusBarHeight();
        final boolean translucentStatus = hasTranslucentStatusBar();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction(
                    "Action",
                    null
                ).show();
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

        if (null != getBottomNavigation()) {
            getBottomNavigation().setDefaultSelectedIndex(0);
        }
    }

    @Override
    public void onMenuItemSelect(final int itemId, final int position) {
        Log.i(TAG, "onMenuItemSelect(" + itemId + ", " + position + ")");
    }

    @Override
    public void onMenuItemReselect(@IdRes final int itemId, final int position) {
        Log.i(TAG, "onMenuItemReselect(" + itemId + ", " + position + ")");

        final FragmentManager manager = getSupportFragmentManager();
        MainActivityFragment fragment = (MainActivityFragment) manager.findFragmentById(R.id.fragment);
        fragment.scrollToTop();

    }
}
