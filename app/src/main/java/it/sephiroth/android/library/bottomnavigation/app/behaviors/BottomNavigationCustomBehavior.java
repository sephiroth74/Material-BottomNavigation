package it.sephiroth.android.library.bottomnavigation.app.behaviors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;

import it.sephiroth.android.library.bottomnavigation.BottomBehavior;

/**
 * Created by crugnola on 4/13/16.
 */
@SuppressWarnings ("unused")
public class BottomNavigationCustomBehavior extends BottomBehavior {

    @SuppressWarnings ("unused")
    public BottomNavigationCustomBehavior(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected boolean isFloatingActionButton(final View dependency) {
        return super.isFloatingActionButton(dependency) || dependency instanceof FloatingActionMenu;
    }
}
