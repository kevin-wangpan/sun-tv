package com.jiaoyang.tv.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;

import com.jiaoyang.base.app.JiaoyangApplication;
import com.jiaoyang.base.caching.ImageCache;
import com.jiaoyang.base.misc.JiaoyangConstants;
import com.jiaoyang.tv.update.ModuleManager;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.tv.util.Logger;
import com.kankan.mediaserver.downloadengine.DownloadEngine;

public class JiaoyangTvApplication extends JiaoyangApplication {
    private static final Logger LOG = Logger.getLogger(JiaoyangTvApplication.class);

    private ImageCache mImageCache;

    public static int versionCode;
    public static String versionName;
    public static String sourceDir;

    public static BUILD_VERSION sBuildVersion = BUILD_VERSION.FORMAL;

    private Drawable mBG; //后台设置的背景图

    public enum BUILD_VERSION {
        DEBUG,
        TEST,
        FORMAL
    }

    public ImageCache getImageCache() {
        if (mImageCache == null) {
            mImageCache = ImageCache.getPerfectCache(getApplicationContext(),
                    JiaoyangConstants.Cache.IMAGE_CACHE_NAME, JiaoyangConstants.Cache.IMAGE_CACHE_SIZE_FACTOR);
        }

        return mImageCache;
    }

    @Override
    public void onLowMemory() {
        LOG.warn("The overall memory is low, will clear the cache...");

        if (getImageCache() != null) {
            getImageCache().clearMemeoryCache();
        }

        super.onLowMemory();
    }

    @Override
    protected void onInit() {
        ModuleManager.init(this);
        DownloadEngine.start(this, ModuleManager.getLoadingLibPath(), false);

        try {
            PackageInfo pi = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            if (pi != null) {
                versionCode = pi.versionCode;
                versionName = pi.versionName;
                sourceDir = pi.applicationInfo.sourceDir;
            }
        } catch (NameNotFoundException e) {
            LOG.warn(e);
        }

        sBuildVersion = getBuildVersion(this);
        if (sBuildVersion == BUILD_VERSION.FORMAL) {
            CrashHandler crashHanlder = new CrashHandler(this);
            Thread.setDefaultUncaughtExceptionHandler(crashHanlder);
        }

        if (PreferenceManager.instance(this).getCacheCleared()) {
            sFirstTimeLaunche = true;
            PreferenceManager.instance(this).saveClearCaches(false);
        }
    }

    @Override
    protected void onFini() {
        DownloadEngine.stop();
    }

    private BUILD_VERSION getBuildVersion(Context context) {

        BUILD_VERSION version = BUILD_VERSION.TEST;
        try {
            Signature[] sigs = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            if (sigs.length > 0) {

                int hashCode = sigs[0].hashCode();
                if (hashCode == -1431552580) { // formal version
                    version = BUILD_VERSION.FORMAL;
                } else if (hashCode == -1829478571) { // test version
                    version = BUILD_VERSION.TEST;
                } else if (hashCode == 1472662116) { // debug version
                    version = BUILD_VERSION.DEBUG;
                } else {
                    version = BUILD_VERSION.TEST;
                }
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public Drawable getActivityBG() {
        return mBG;
    }

    public void setActivityBG(Drawable mBG) {
        this.mBG = mBG;
    }
}
