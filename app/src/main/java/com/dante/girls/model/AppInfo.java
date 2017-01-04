package com.dante.girls.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yons on 17/1/3.
 */

public class AppInfo {

    /**
     * lastest_version : 1.0
     * lastest_version_code : 2
     * message : 修复一些bug;
     * UI细节调整
     * attach_info :
     * apkName : app-armeabi-v7a-release.apk
     * forceUpdate : true
     * former_version : v1.0
     */

    @SerializedName("lastest_version")
    private String version;
    @SerializedName("lastest_version_code")
    private int versionCode;
    private String message;
    @SerializedName("apk_name")
    private String apkName;


    private boolean forceUpdate;
    @SerializedName("former_version")
    private String formerVersion;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getFormerVersion() {
        return formerVersion;
    }

    public void setFormerVersion(String formerVersion) {
        this.formerVersion = formerVersion;
    }
}
