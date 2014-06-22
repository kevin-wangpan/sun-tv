package com.jiaoyang.tv.data;

import java.io.Serializable;
import java.util.Locale;

import android.os.Build;

import com.jiaoyang.base.data.MovieType;
import com.jiaoyang.tv.app.JiaoyangTvApplication;

public class Movie implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DETAIL_TEMPLATE_URL = "http://api.pad.kankan.com/api.php?movieid=%d&type=movie&mod=detail&os=kktv&osver=%s&productver=%s";
    private static final String EPISODES_TEMPLATE_URL = "http://api.pad.kankan.com/api.php?movieid=%d&type=movie&mod=subdetail&os=kktv&osver=%s&productver=%s";
    private static final String POSTER_TEMPLATE_URL = "http://images.movie.xunlei.com/gallery%s";

    //在Metro中显示的样式，0表示普通的MetroItem，1表示竖的，2表示横的
    public static final int LAYOUT_TYPE_NORMAL = 0;
    public static final int LAYOUT_TYPE_PORTRAIT = 1;
    public static final int LAYOUT_TYPE_LANDSCAPE = 2;

    public int id;
    public int type;
    public int layoutType; //在Metro中显示的样式，0表示普通的MetroItem，1表示竖的，2表示横的
    public int tv_display = 1; //是否在TV的首页中显示，1表示显示，0表示隐藏
    public String title;
    public double score;
    public String label;
    public String versionInfo;
    public String versioninfo;
    public int episodeCount;
    public int totalEpisodeCount;
    public String[] tags;
    public String directorName;
    public String[] directors;
    public String actorName;
    public String[] actors;
    public String year;
    public String singleIntro;
    public String intro;
    public String area;
    public int displayType2;// 1——第x集； 2——2012-08-09期； 3——片花标题格式（单行文字）
    public boolean downloadable;
    public int productId;
    public double price;
    public int saleTime;
    public int expiresTime;
    public int validTime;
    public String attribute;// 长视频、短视频
    public String poster;
    public String is_vip;
    private String v_poster;

    public String bitrate;

    public static String getDetailUrlFromId(int type, int id) {
        String url = null;

        if (!(MovieType.isShortVideo(type))) {
            String templateUrl = DETAIL_TEMPLATE_URL;

            url = String.format(Locale.US, templateUrl, id, Build.VERSION.RELEASE, JiaoyangTvApplication.versionCode);
        }

        return url;
    }

    public static String getEpisodesUrlFromId(int type, int id) {
        String templateUrl = EPISODES_TEMPLATE_URL;
        return String.format(Locale.US, templateUrl, id, Build.VERSION.RELEASE, JiaoyangTvApplication.versionCode);
    }

    public String getPosterUrl() {
        return Movie.getPosterUrl(poster, type);
    }

    public static String getPosterUrl(String poster, int type) {
        String url = poster;

        if (url != null) {
            if (url.startsWith("http")) {
                return url;
            }
            if (!poster.startsWith("/")) {
                poster = "/" + poster;
            }
            String templateUrl = POSTER_TEMPLATE_URL;
            url = String.format(Locale.US, templateUrl, poster);
        }

        return url;
    }

    public String getDetailUrl() {
        return getDetailUrlFromId(type, id);
    }

    public String getEpisodesUrl() {
        return getEpisodesUrlFromId(type, id);
    }

    public String getTvPosterUrl() {
        return getPosterUrl("/" + v_poster, type);
    }
    

    /**
     * 该视频在首页上占几个格子
     */
    public int getSize() {
        switch (layoutType) {
        case LAYOUT_TYPE_NORMAL:
            return 1;
        case LAYOUT_TYPE_PORTRAIT:
            return 4;
        case LAYOUT_TYPE_LANDSCAPE:
            return 2;
        default:
            return 1;
        }
    }
}
