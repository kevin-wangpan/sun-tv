package com.jiaoyang.tv.data;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

public class HttpDataFetcher {

    // 首页接口
    private static final String HOME_PAGE = "http://ci2.sun-tv.com.cn/topic/movies/1/xl";

    private static final String APK_UPDATE_URL = "null";


    private static final String START_UP_POSTER_URL = "null";// 启动时的品宣图和背景图

    private static HttpDataFetcher sInstance = null;

    private HomePageData mHomePage;

    synchronized public static HttpDataFetcher getInstance() {
        if (sInstance == null) {
            sInstance = new HttpDataFetcher();
        }

        return sInstance;
    }

    synchronized public HomePageData getHomePage() {
        return new HomePageData();
        //return mHomePage;
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

    public synchronized void loadHomePage() {
        if (mHomePage != null) {
            return;
        }

        URLLoader loader = new URLLoader();

        mHomePage = (HomePageData) loader.loadObject(HOME_PAGE, HomePageData.class);

    }

    private StartUpPoster mStartUpPoster;
    public StartUpPoster getStartUpPoster() {
        if (mStartUpPoster == null) {
            URLLoader loader = new URLLoader();
            mStartUpPoster = (StartUpPoster) loader.loadObject(START_UP_POSTER_URL, StartUpPoster.class);
        }
        return mStartUpPoster;
    }
}
