package com.dante.girl.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.bugtags.library.Bugtags;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.dante.girl.BuildConfig;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

/**
 * Custom application for libs init etc.
 */
public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private RefWatcher refWatcher;

    public static RefWatcher getWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        refWatcher = LeakCanary.install(this);
        Bugtags.start("1ddf7128d535505cc4adbda213e8c12f", this, Bugtags.BTGInvocationEventNone);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        Utils.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
//        Colorful.defaults()
//                .primaryColor(Colorful.ThemeColor.RED)
//                .accentColor(Colorful.ThemeColor.BLUE)
//                .translucent(false)
//                .dark(true);
//        Colorful.init(this);
    }
}
