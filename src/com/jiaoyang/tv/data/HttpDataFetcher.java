package com.jiaoyang.tv.data;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

public class HttpDataFetcher {

    // 首页接口
    private static final String HOME_PAGE = "http://pad.kankan.com/kktv/index.json";

    private static final String APK_UPDATE_URL = "null";


    private static final String START_UP_POSTER_URL = "null";// 启动时的品宣图和背景图

    private static HttpDataFetcher sInstance = null;

    private HomePage mHomePage;

    synchronized public static HttpDataFetcher getInstance() {
        if (sInstance == null) {
            sInstance = new HttpDataFetcher();
        }

        return sInstance;
    }

    synchronized public HomePage getHomePage() {
        return mHomePage;
    }

    synchronized public void setHomePage(HomePage homePage) {
        mHomePage = homePage;
    }

    public Movie getMovieDetail(int type, int id) {
        URLLoader loader = new URLLoader();
        String url = Movie.getDetailUrlFromId(type, id);
        return loader.loadObject(url, Movie.class);
    }

    public EpisodeList getMovieEpisodes(int type, int id) {
        URLLoader loader = new URLLoader();
        String url = Movie.getEpisodesUrlFromId(type, id);
        return loader.loadObject(url, EpisodeList.class);
    }

    public ApkUpdateInfo getApkUpdateInfo(String version, String osVersion) {
        URLRequest request = new URLRequest(APK_UPDATE_URL, URLRequest.TYPE_EXTRA);
        request.appendQueryParameter("ver", version);
        request.appendQueryParameter("os", osVersion);

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Response<ApkUpdateInfo>>() {
        }.getType();

        return (ApkUpdateInfo) loader.loadObject(request, type);
    }

    public synchronized void loadHomePage() throws InvalidApiVersionException {
        if (mHomePage != null) {
            return;
        }

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Response<HomePage>>() {
        }.getType();

        mHomePage = (HomePage) loader.loadObject(HOME_PAGE, type);

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
