package com.dante.girls.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.dante.girls.BuildConfig;
import com.dante.girls.base.App;
import com.dante.girls.base.Constants;

import java.io.File;
import java.util.List;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

/**
 * Created by Dante on 2016/2/19.
 */
public class AppUtil {

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static void openAppInfo(Context context) {
        //redirect user to app Settings
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
        context.startActivity(i);
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static boolean isIntentSafe(Intent intent) {
        PackageManager packageManager = App.context.getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }


    public static void donate(Activity activity) {
        if (AlipayZeroSdk.hasInstalledAlipayClient(activity.getApplicationContext())) {
            AlipayZeroSdk.startAlipayClient(activity, Constants.ALI_PAY);
        }
    }
}
