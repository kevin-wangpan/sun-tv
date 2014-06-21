package com.jiaoyang.base.data;

import android.util.SparseArray;

public class MovieType {

    /**
     * 电影
     */
    public static final int MOVIE = 1;

    /**
     * 电视剧
     */
    public static final int TV = 2;

    /**
     * 综艺
     */
    public static final int VARIETY_SHOW = 3;

    /**
     * 动漫
     */
    public static final int ANIME = 4;

    /**
     * 视频快报
     */
    public static final int NEWS = 102;

    /**
     * 记录片
     */
    public static final int DOCUMENTARY = 5;

    /**
     * 公开课
     */
    public static final int OPEN_COURSES = 6;

    /**
     * 微电影
     */
    public static final int VMOVIE = 7;

    /**
     * MTV
     */
    public static final int MTV = 103;

    /**
     * 娱乐
     */
    public static final int ENTERTAINMENT = 101;

    /**
     * 未知
     */
    public static final int UNKNOWN = -1;

    public static boolean isShortVideo(int movieType) {
        return movieType == NEWS || movieType == MTV;
    }

    public static String getName(int type) {
        return TYPE_NAMES.get(type, "");
    }

    private static final SparseArray<String> TYPE_NAMES = new SparseArray<String>();

    static {
        TYPE_NAMES.append(MOVIE, "电影");
        TYPE_NAMES.append(TV, "电视剧");
        TYPE_NAMES.append(VARIETY_SHOW, "综艺");
        TYPE_NAMES.append(ANIME, "动漫");
        TYPE_NAMES.append(NEWS, "视频快报");
        TYPE_NAMES.append(DOCUMENTARY, "纪录片");
        TYPE_NAMES.append(OPEN_COURSES, "公开课");
        TYPE_NAMES.append(MTV, "MTV");
        TYPE_NAMES.append(ENTERTAINMENT, "娱乐");
    }
}
