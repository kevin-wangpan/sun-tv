package com.jiaoyang.tv.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

/**
 * 记录用户的偏好
 * 
 */
public class PreferenceManager {
    private static PreferenceManager sInstance;
    private SharedPreferences mSharedPreferences;
    private static final String PROPORTION_PROFILE = "proportion_profile";
    private static final String DECODER_PROFILE = "decoder_profile";
    private static final String SKIP_PROFILE = "skip_profile";
    private static final String FIRST_LOGIN = "first_login";;
    private static final String PLAY_PROFILE = "play_profile";
    private static final String DOWNLOAD_PROFILE = "download_profile";
    private static final String MOBILE_PLAY = "mobile_play";
    private static final String MOBILE_DOWNLOAD = "mobile_download";

    private PreferenceManager(Context context) {
        mSharedPreferences = context.getSharedPreferences("jiaoyang_tv_preferences", Context.MODE_PRIVATE);
    }

    public static PreferenceManager instance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private void save(String key, int value) {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private void save(String key, boolean value) {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private void save(String key, String value) {
        Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private int retriveIntPreference(String key) {
        int ret = mSharedPreferences.getInt(key, 0);
        return ret;
    }

    /**
     * @return 用户偏好的清晰度
     */

    public void savePlayProfile(int value) {
        save(PLAY_PROFILE, value);
    }

    public void saveSkipProfile(int value) {
        save(SKIP_PROFILE, value);
    }

    public void saveDecoderProfile(int value) {
        save(DECODER_PROFILE, value);
    }

    public void saveProportionProfile(int value) {
        save(PROPORTION_PROFILE, value);
    }

    public void saveDownLoadProfile(int value) {
        save(DOWNLOAD_PROFILE, value);
    }

    /**
     * 清晰度<br/>
     * -1:自动
     * 3:720P<br/>
     * 4:1080P<br/>
     * 5:蓝光<br/>
     * 6:3D
     */
    public int retrivePlayProfilePreference() {
        return retriveIntPreference(PLAY_PROFILE);
    }

    public int retriveDownLoadProfilePreference() {
        return retriveIntPreference(DOWNLOAD_PROFILE);
    }

    public boolean retriveFirstLoginPreference() {
        return mSharedPreferences.getBoolean(FIRST_LOGIN, true);
    }

    public void saveFirstLogin(boolean value) {
        save(FIRST_LOGIN, value);
    }

    public boolean retriveMobilePlayPreference() {
        return mSharedPreferences.getBoolean(MOBILE_PLAY, false);
    }

    public boolean retriveMobileDownloadPreference() {
        return mSharedPreferences.getBoolean(MOBILE_DOWNLOAD, false);
    }

    /**
     * 跳过片头片尾<br/>
     * 0:开(默认)<br/>
     * 1:关
     */
    public int retriveSkipPreference() {
        return mSharedPreferences.getInt(SKIP_PROFILE, 0);
    }

    /**
     * 解码设置<br/>
     * 0:自动(默认)<br/>
     * 1:软解<br/>
     * 2:硬解
     */
    public int retriveDecoderPreference() {
        return mSharedPreferences.getInt(DECODER_PROFILE, 0);
    }

    /**
     * 画面比例<br/>
     * 0:自动
     * 1:原始比例(默认)<br/>
     * 2:自动全屏<br/>
     */
    public int retriveProportionPreference() {
        return mSharedPreferences.getInt(PROPORTION_PROFILE, 0);
    }

    // 忽略某版本更新
    private static final String IGNORED_VERSION_KEY = "ignored_version";
    public void saveIgnoredVersion(String version) {
        save(IGNORED_VERSION_KEY, version);
    }
    public String retriveIgnoredVersion() {
        return mSharedPreferences.getString(IGNORED_VERSION_KEY, "");
    }

    //上次使用的启动图和背景图
    private static final String STARTUP_POSTER_KEY = "startup_poster_key";
    public void saveStartUpPosterUrl(String... urls) {
        Gson gson = new Gson();
        String s = gson.toJson(urls);
        save(STARTUP_POSTER_KEY, s);
    }
    public String[] retriveStartupPosterUrls() {
        Gson gson = new Gson();
        String s =  mSharedPreferences.getString(STARTUP_POSTER_KEY, "");
        return gson.fromJson(s, String[].class);
    }
    public String retriveDefaultBgUrl() {
        String[] urls = retriveStartupPosterUrls();
        if (urls != null && urls.length == 3) {
            return urls[1];
        } else {
            return null;
        }
    }

    private static final String FIRST_TIME_LAUNCH_AFTER_CLEAR_CACHE = "fist_time_launch_clear";
    public boolean getCacheCleared() {
        return mSharedPreferences.getBoolean(FIRST_TIME_LAUNCH_AFTER_CLEAR_CACHE, false);
    }
    public void saveClearCaches(boolean cacheCleared) {
        save(FIRST_TIME_LAUNCH_AFTER_CLEAR_CACHE, cacheCleared);
    }


    //设置
    private static final String AUTO_SKIP_KEY = "auto_skip_key"; //自动跳过片头片尾
    private static final String AUTO_PLAY_NEXT_KEY = "auto_play_next_key"; //自动播放下一集
    public boolean getAutoSkip() {
        return mSharedPreferences.getBoolean(AUTO_SKIP_KEY, false);
    }
    public void saveAutoSkip(boolean autoSkip) {
        save(AUTO_SKIP_KEY, autoSkip);
    }
    public boolean getAutoPlayNext() {
        return mSharedPreferences.getBoolean(AUTO_PLAY_NEXT_KEY, false);
    }
    public void saveAutoPlayNext(boolean autoNext) {
        save(AUTO_PLAY_NEXT_KEY, autoNext);
    }

    private static final String USER_ID_KEY = "user_id_key";
    public String getUserId() {
        return mSharedPreferences.getString(USER_ID_KEY, "53af91c1a188820b7642b5d506cd18f9383b26e2");
    }
    public void saveUserId(String id) {
        save(USER_ID_KEY, id);
    }
}
