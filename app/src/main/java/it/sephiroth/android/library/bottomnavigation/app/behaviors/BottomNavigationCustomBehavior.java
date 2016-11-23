package it.sephiroth.android.library.bottomnavigation.app.behaviors;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;

import it.sephiroth.android.library.bottomnavigation.BottomBehavior;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

/**
 * Created by crugnola on 4/13/16.
 */
@SuppressWarnings ("unused")
public class BottomNavigationCustomBehavior extends BottomBehavior {

    @SuppressWarnings ("unused")
    public BottomNavigationCustomBehavior(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
//
//    @Override
//    protected boolean isFloatingActionButton(final View dependency) {
//        return super.isFloatingActionButton(dependency) || dependency instanceof FloatingActionMenu;
//    }
//
//    protected BottomBehavior.FabDependentView createFabDependentView(final View dependency, final int height, final int bottomInset) {
//        return new CustomFab(dependency, height, bottomInset);
//    }
//
//    public class CustomFab extends FabDependentView {
//
//        CustomFab(final View child, final int height, final int bottomInset) {
//            super(child, height, bottomInset);
//        }
//
//        @Override
//        protected boolean onDependentViewChanged(final CoordinatorLayout parent, final BottomNavigation navigation) {
//            final float t = Math.max(0, navigation.getTranslationY() - height);
//
//            final float pos;
//            if (bottomInset > 0) {
//                pos = (bottomMargin + height - t);
//            } else {
//                pos = (bottomMargin + height - navigation.getTranslationY());
//            }
//
//            layoutParams.bottomMargin = (int) pos;
//
//            //child.setTranslationY(originalPosition - pos - offset);
//            // child.postInvalidate();
//            child.requestLayout();
//            return true;
//        }
//
//        @Override
//        protected void onDestroy() {
//            //            layoutParams.bottomMargin = bottomMargin;
//            child.setTranslationY(originalPosition);
//            child.requestLayout();
//            child.post(new Runnable() {
//                @Override
//                public void run() {
//                    layoutParams.bottomMargin = bottomMargin;
//                }
//            });
//        }
//    }
}
