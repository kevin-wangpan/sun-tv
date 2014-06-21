package com.jiaoyang.tv.data;

import java.io.Serializable;
import java.util.Locale;

public class Search implements Serializable {

    private static final long serialVersionUID = 3L;
    public int id;
    public String title;
    public int type;
    public double score;
    public String poster;
    public String directorName;
    public String[] directors;
    public String actorName;
    public String[] actors;
    public String versionInfo;
    public String singleIntro;
    public String bitrate;
    private static final String POSTER_TEMPLATE_URL = "http://images.movie.xunlei.com/gallery%s";

    public String getPosterUrl() {
        return Search.getPosterUrl(poster, type);
    }

    public static String getPosterUrl(String poster, int type) {
        String url = "/" + poster;
        if (url != null) {
            String templateUrl = POSTER_TEMPLATE_URL;
            url = String.format(Locale.US, templateUrl, url);
        }
        return url;
    }
}
