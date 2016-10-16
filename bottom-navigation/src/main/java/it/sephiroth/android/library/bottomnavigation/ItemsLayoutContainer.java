package it.sephiroth.android.library.bottomnavigation;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;

/**
 * Created by alessandro on 4/4/16 at 11:13 PM.
 * Project: Material-BottomNavigation
 */
@Keep
@KeepClassMembers
public interface ItemsLayoutContainer {
    void setSelectedIndex(int index, final boolean animate);

    int getSelectedIndex();

    void populate(@NonNull MenuParser.Menu menu);

    void setLayoutParams(ViewGroup.LayoutParams params);

    void setOnItemClickListener(OnItemClickListener listener);

    View findViewById(@IdRes final int id);

    void removeAll();
}
