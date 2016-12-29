package com.dante.girls.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.bugtags.library.Bugtags;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.realm.Realm;

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

//        Colorful.defaults()
//                .primaryColor(Colorful.ThemeColor.RED)
//                .accentColor(Colorful.ThemeColor.BLUE)
//                .translucent(false)
//                .dark(true);
//        Colorful.init(this);
    }
}
