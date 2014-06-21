package com.jiaoyang.tv.update;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.jiaoyang.tv.util.Logger;

public class Config {
    private static final Logger LOG = Logger.getLogger(Config.class);

    public static final String UPDATE_SAVENAME = "jiaoyang.apk";

    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            LOG.warn(e);
        }
        return verName;
    }

}
