package com.dante.girls.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.bugtags.library.Bugtags;
import com.dante.girls.BuildConfig;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Custom application for libs init etc.
 */
public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Bugtags.start("1ddf7128d535505cc4adbda213e8c12f", this, Bugtags.BTGInvocationEventNone);
        Realm.init(this);
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
