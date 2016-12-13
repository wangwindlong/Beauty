package com.dante.girls.base;

import android.app.Application;
import android.content.Context;

import com.bugtags.library.Bugtags;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.realm.Realm;

/**
 * Created by yons on 16/12/8.
 */
public class App extends Application {
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
        Bugtags.start("1ddf7128d535505cc4adbda213e8c12f", this, Bugtags.BTGInvocationEventShake);
        Realm.init(this);
    }
}
