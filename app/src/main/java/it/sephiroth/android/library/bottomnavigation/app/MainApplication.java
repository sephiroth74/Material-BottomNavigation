package it.sephiroth.android.library.bottomnavigation.app;

import android.app.Application;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import timber.log.Timber;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            BottomNavigation.Companion.setDEBUG(true);
        }
    }
}
