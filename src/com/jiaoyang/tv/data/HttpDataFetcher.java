package com.jiaoyang.tv.data;

import java.lang.reflect.Type;
import java.util.Date;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.jiaoyang.base.app.JiaoyangApplication;
import com.jiaoyang.tv.content.NaviControlFragment;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.tv.util.Util;

public class HttpDataFetcher {

    private static final String KEY = "6uBzlsqFVMozM";
    private static final String HOME_PAGE = "http://ci2.sun-tv.com.cn/topic/movies/%d/xl";// 首页接口
    private static final String USER_ID = "http://ci2.sun-tv.com.cn/uid/get";

    private static final String APK_UPDATE_URL = "null";


    private static final String START_UP_POSTER_URL = "null";// 启动时的品宣图和背景图

    private static HttpDataFetcher sInstance = null;

    private HomePageData[] mHomePages;
    private UserId mUid;

    synchronized public static HttpDataFetcher getInstance() {
        if (sInstance == null) {
            sInstance = new HttpDataFetcher();
        }

        return sInstance;
    }

    synchronized public HomePageData getHomePage(int index) {
        if (mHomePages == null) {
            return null;
        }
        return mHomePages[index];
    }

    public Movie getMovieDetail(String id) {
        URLLoader loader = new URLLoader();
        String url = Movie.getDetailUrlFromId(id);
        return loader.loadObject(url, Movie.class);
    }

    public ApkUpdateInfo getApkUpdateInfo(String version, String osVersion) {
        URLRequest request = new URLRequest(APK_UPDATE_URL, URLRequest.TYPE_EXTRA);
        request.appendQueryParameter("ver", version);
        request.appendQueryParameter("os", osVersion);

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<ApkUpdateInfo>() {
        }.getType();

        return (ApkUpdateInfo) loader.loadObject(request, type);
    }

    public synchronized void loadUid() {
        if (mUid != null) {
            return;
        }
        URLLoader loader = new URLLoader();

        mUid = (UserId) loader.loadObject(USER_ID, UserId.class);
        if (mUid == null || !mUid.success || TextUtils.isEmpty(mUid.uid)) {
            mUid = new UserId();
            mUid.success = true;
            mUid.uid = PreferenceManager.instance(JiaoyangApplication.sInstance).getUserId();
        } else {
            PreferenceManager.instance(JiaoyangApplication.sInstance).saveUserId(mUid.uid);
        }
    }
    public synchronized UserId getUserId() {
        return mUid;
    }

    public synchronized void loadHomePage() {
        if (mHomePages != null) {
            return;
        }

        if (mUid == null) {
            loadUid();
        }
        URLLoader loader = new URLLoader();
        mHomePages = new HomePageData[NaviControlFragment.HOME_PAGE_TAB_COUNT];
        for (int i = 1; i <= NaviControlFragment.HOME_PAGE_TAB_COUNT; i++) {
            android.util.Log.e("jiaoyang", "url=" + addBaseParams(String.format(HOME_PAGE, i)));
            mHomePages[i-1] = (HomePageData) loader.loadObject(addBaseParams(String.format(HOME_PAGE, i)), HomePageData.class);
        }

    }

    private StartUpPoster mStartUpPoster;
    public StartUpPoster getStartUpPoster() {
        if (mStartUpPoster == null) {
            URLLoader loader = new URLLoader();
            mStartUpPoster = (StartUpPoster) loader.loadObject(START_UP_POSTER_URL, StartUpPoster.class);
        }
        return mStartUpPoster;
    }

    private String addBaseParams(String url) {
        if (true) {
            return url;
        }
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String curTime = Long.toString(new Date().getTime());
        return url + "?uid=" + mUid.uid + "&v=" + Util.getSelfAppVersion(JiaoyangApplication.sInstance)
                +"&os=android&os_v=" + Util.getOSVersion() + "&mac=" + Util.getIMEI(JiaoyangApplication.sInstance)
                +"&sourceid=36&t=" + curTime + "&cs=" + Util.md5(KEY + curTime + mUid.uid);
    }
}
