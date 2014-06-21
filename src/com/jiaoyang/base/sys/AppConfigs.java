package com.jiaoyang.base.sys;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.jiaoyang.tv.util.Logger;

/**
 * 应用程序常用到的“常量”， 各个子项目必须自行设置
 * 
 * @author admin
 * 
 */
public class AppConfigs {
    private static final Logger LOG = Logger.getLogger(AppConfigs.class);

    /**
     * 应用程序的版本号，从AndroidManifest.xml中读取
     */
    public static String appVersion;

    /**
     * 应用程序的版本代码，从AndroidManifest.xml中读取
     */
    public static int appVersionCode;

    public static SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    public static void initAppConfigs(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = pi.versionName;
            appVersionCode = pi.versionCode;
        } catch (NameNotFoundException e) {
            LOG.warn("init app configs failed. err={}", e.getMessage());
        }
    }
}
