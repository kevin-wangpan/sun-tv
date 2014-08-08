package com.jiaoyang.tv.data;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.cloudtv.signurl.SignURL;
import com.google.gson.reflect.TypeToken;
import com.jiaoyang.base.app.JiaoyangApplication;
import com.jiaoyang.tv.content.NaviControlFragment;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.tv.util.Util;

public class HttpDataFetcher {

    private static final String KEY = "6uBzlsqFVMozM";
    private static final String HOME_PAGE = "http://ci2.sun-tv.com.cn/topic/movies/%d/xl";// 首页接口
    private static final String USER_ID = "http://ci2.sun-tv.com.cn/uid/get"; // 获取userid
    private static final String EPISODE_PLAY_URL = "http://ci2.sun-tv.com.cn/video/url/%s/%d";

    private static final String APK_UPDATE_URL = "null";


    private static final String START_UP_POSTER_URL = "null";// 启动时的品宣图和背景图

    //从baidutv sdk请求播放地址
    private static final String CONTENT_TEMPLATE = "sid=%s&ep=%d&rst=%s&f=%s";
    private static final String contentId_test = "sid=2_ce4ce_4a0b030d65bc02e&ep=1&rst=900&f=hls";
    private static final String CHANNEL_ID = "st04";
    private static final String BAIDUTV_VIDEO_URL = "http://tv.baidu.com/rest/2.0/video/plink";

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
        return loader.loadObject(addBaseParams(url), Movie.class);
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
    public synchronized String getUserId() {
        if (mUid != null && !TextUtils.isEmpty(mUid.uid)) {
            return mUid.uid;
        }
        return "53e43f6f95001b8e899f066be9f0fe4f69c41a79";
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
            mStartUpPoster = loader.loadObject(START_UP_POSTER_URL, StartUpPoster.class);
        }
        return mStartUpPoster;
    }

    /**
     * @param videoId 视频编号;
     * @param type 类型。1-自适应；2-高清；3-标清；4-超清；5-高清下载；6-标清下载；7-超清下载；8-1080p;"
     * @return
     */
    public String loadPlayUrl(Context context, String videoId, int type, String sid, int episodeIndex, String resolution, String f) {
        if (true) { // 老的接口
            String url = String.format(Locale.US, EPISODE_PLAY_URL, videoId, type);
            URLLoader loader = new URLLoader();
            Episode episode = loader.loadObject(addBaseParams(url), Episode.class);
            if (episode == null) {
                return null;
            } else {
                return episode.url;
            }
        }
        String content = SignURL.signURL(context, String.format(CONTENT_TEMPLATE, sid, episodeIndex, resolution, f), CHANNEL_ID);
        try {
            return sendRequestPost(BAIDUTV_VIDEO_URL, content);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String sendRequestPost(String url, String content) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost hp = new HttpPost(url);
        List params = new ArrayList();
        params.add(new BasicNameValuePair("content", content));
        hp.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

        HttpResponse response = client.execute(hp);
        int code = response.getStatusLine().getStatusCode();
        InputStream inStream = response.getEntity().getContent();// ���ص����

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return new String(data);
    }

    private String addBaseParams(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String curTime = Long.toString(new Date().getTime());
        return url + "?uid=" + getUserId() + "&v=" + Util.getSelfAppVersion(JiaoyangApplication.sInstance)
                +"&os=android&os_v=" + Util.getOSVersion() + "&mac=" + Util.getIMEI(JiaoyangApplication.sInstance)
                +"&sourceid=39&t=" + curTime + "&cs=" + Util.md5(KEY + curTime + getUserId());
    }
}
