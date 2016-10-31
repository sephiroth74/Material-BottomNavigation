package it.sephiroth.android.library.bottomnavigation.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomBehavior;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

import static android.util.Log.INFO;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

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


    public static class FabBehavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {
        public FabBehavior() {
            super();
        }

        public FabBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }


        @Override
        public boolean layoutDependsOn(final CoordinatorLayout parent, final FloatingActionMenu child, final View dependency) {
            if (BottomNavigation.class.isInstance(dependency)) {
                return true;
            } else if (Snackbar.SnackbarLayout.class.isInstance(dependency)) {
                return true;
            }
            return super.layoutDependsOn(parent, child, dependency);
        }

        @Override
        public boolean onDependentViewChanged(
            final CoordinatorLayout parent, final FloatingActionMenu child, final View dependency) {
            log(TAG, INFO, "onDependentViewChanged: " + dependency);

            final List<View> list = parent.getDependencies(child);
            int bottomMargin = ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;

            float t = 0;
            boolean result = false;

            for (View dep : list) {
                if (Snackbar.SnackbarLayout.class.isInstance(dep)) {
                    t += dep.getTranslationY() - dep.getHeight();
                    result = true;
                } else if (BottomNavigation.class.isInstance(dep)) {
                    BottomNavigation navigation = (BottomNavigation) dep;
                    t += navigation.getTranslationY() - navigation.getHeight() + bottomMargin;
                    result = true;
                }
            }

            child.setTranslationY(t);
            return result;
        }

        @Override
        public void onDependentViewRemoved(
            final CoordinatorLayout parent, final FloatingActionMenu child, final View dependency) {
            super.onDependentViewRemoved(parent, child, dependency);
        }
    }
}
