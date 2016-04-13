package it.sephiroth.android.library.bottomnavigation.app;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;

import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomBehavior;

public class MainActivityCustomBehavior extends MainActivity {

    @Override
    protected int getActivityLayoutResId() {
        return R.layout.activity_main_custom_behavior;
    }

    @Override
    protected void initializeUI(final Bundle savedInstanceState) {

        final FloatingActionMenu floatingActionButton = (FloatingActionMenu) findViewById(R.id.fab);
        assert floatingActionButton != null;
        floatingActionButton.findViewById(R.id.fab_item1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction(
                    "Action",
                    null
                ).show();
            }
        });

        if (null != getBottomNavigation() && null == savedInstanceState) {
            getBottomNavigation().setDefaultSelectedIndex(0);
            ((BottomBehavior) getBottomNavigation().getBehavior()).setOnExpandStatusChangeListener(
                new BottomBehavior.OnExpandStatusChangeListener() {
                    @Override
                    public void onExpandStatusChanged(final boolean expanded, final boolean animate) {
                        if (expanded) {
                            floatingActionButton.showMenu(animate);
                        } else {
                            floatingActionButton.hideMenu(animate);
                        }
                    }
                });

            final BadgeProvider provider = getBottomNavigation().getBadgeProvider();
            provider.show(R.id.bbn_item3);
            provider.show(R.id.bbn_item4);
        }
    }
}
