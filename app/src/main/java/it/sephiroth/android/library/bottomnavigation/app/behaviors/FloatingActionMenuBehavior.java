package it.sephiroth.android.library.bottomnavigation.app.behaviors;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;

public class FloatingActionMenuBehavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {
    private static final int HIDE_MSG = 1;
    private static final int SHOW_MSG = 2;
    private static final String TAG = FloatingActionMenuBehavior.class.getSimpleName();

    private float mFabTranslationY;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case HIDE_MSG:
                    ((FloatingActionMenu) msg.obj).hideMenu(true);
                    break;

                case SHOW_MSG:
                    ((FloatingActionMenu) msg.obj).showMenu(true);
                    break;
            }
        }
    };

    public FloatingActionMenuBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final FloatingActionMenu child, final View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(final CoordinatorLayout parent, final FloatingActionMenu child, final View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child, dependency);
            ViewCompat.setTranslationY(child, mFabTranslationY);
        }
        return false;
    }

    @Override
    public void onDependentViewRemoved(final CoordinatorLayout parent, final FloatingActionMenu child, final View dependency) {
        super.onDependentViewRemoved(parent, child, dependency);
    }

    private void updateFabTranslationForSnackbar(CoordinatorLayout parent, final FloatingActionMenu fab, View snackbar) {
        mFabTranslationY = getFabTranslationYForSnackbar(parent, fab, snackbar);
    }

    private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FloatingActionMenu fab, View snackbar) {
        float minOffset = 0;
        minOffset = Math.min(minOffset, ViewCompat.getTranslationY(snackbar) - snackbar.getHeight());
        return minOffset;
    }
}
