package com.dante.girls.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.dante.girls.BuildConfig;
import com.dante.girls.R;
import com.dante.girls.base.Constants;
import com.dante.girls.model.AppInfo;
import com.dante.girls.net.API;
import com.dante.girls.net.NetService;
import com.dante.girls.ui.SettingFragment;
import com.dante.girls.utils.SpUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Update app helper.
 */

public class Updater {
    private final Activity context;
    private DownloadHelper helper;
    private Subscription subscription;

    private Updater(Activity context) {
        this.context = context;
    }

    public static Updater getInstance(Activity context) {
        return new Updater(context);

    }

    private void downloadAndInstall(final AppInfo appInfo) {
        subscription = new RxPermissions(context)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean granted) {
                        return granted;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        String url = API.DOWNLOAD_BASE + "/" + appInfo.getVersion() +
                                "/" + appInfo.getApkName();
                        helper = new DownloadHelper(context, url);
                        helper.downWithDownloadManager(getApkName(appInfo.getVersion()), getApkName(appInfo.getFormerVersion()));
                    }
                });
    }

    public void check() {
        NetService.getInstance(API.GITHUB_RAW).getAppApi().getAppInfo()
                .filter(new Func1<AppInfo, Boolean>() {
                    @Override
                    public Boolean call(AppInfo appInfo) {
                        Log.i(TAG, "call: appInfo " + appInfo.toString());
                        return appInfo.getVersionCode() > BuildConfig.VERSION_CODE;//版本有更新
                    }
                })
                .doOnNext(new Action1<AppInfo>() {
                    @Override
                    public void call(AppInfo appInfo) {
                        SpUtil.save(Constants.SHARE_APP, appInfo.getShareApp());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AppInfo>() {
                    @Override
                    public void call(AppInfo appInfo) {
                        showDialog(appInfo);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private void showDialog(final AppInfo appInfo) {
        boolean needUpdate = appInfo.isForceUpdate() || SpUtil.getBoolean(SettingFragment.CHECK_VERSION);
        new AlertDialog.Builder(context).setTitle(R.string.new_version)
                .setCancelable(!needUpdate)//需要更新就不可取消
                .setMessage(String.format(context.getString(R.string.update_message), appInfo.getMessage()))
                .setPositiveButton("Update now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAndInstall(appInfo);
                    }
                }).show();
    }

    private String getApkName(String version) {
        return "Beauty_" + version + ".apk";
    }

    public void release() {
        if (helper != null) {
            helper.release();
        }
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
