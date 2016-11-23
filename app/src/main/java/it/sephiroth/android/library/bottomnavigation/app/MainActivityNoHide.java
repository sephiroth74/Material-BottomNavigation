package it.sephiroth.android.library.bottomnavigation.app;

import android.os.Bundle;

public class MainActivityNoHide extends MainActivity {

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.activity_main_fixed_scroll;
    }

    @Override
    protected void initializeUI(final Bundle savedInstanceState) {
        super.initializeUI(savedInstanceState);
    }

    @Override
    protected void initializeBottomNavigation(final Bundle savedInstanceState) { }

}
