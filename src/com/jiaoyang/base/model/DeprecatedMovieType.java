package com.jiaoyang.base.model;

import com.jiaoyang.base.misc.JiaoyangConstants.ChannelType;

public class DeprecatedMovieType {

    /**
     * 电影
     */
    public static final int MOVIE = 0;

    /**
     * 电视剧
     */
    public static final int TV = 1;

    /**
     * 综艺
     */
    public static final int VARIETY_SHOW = 2;

    /**
     * 动漫
     */
    public static final int ANIME = 3;

    /**
     * 视频快报
     */
    public static final int NEWS = 4;

    /**
     * 记录片
     */
    public static final int DOCUMENTARY = 5;

    /**
     * 公开课
     */
    public static final int OPEN_COURSES = 6;

    /**
     * MTV
     */
    public static final int MTV = 7;

    /**
     * 片花
     */
    public static final int VIDEO_CLIPS = 8;

    /**
     * 未知
     */
    public static final int UNKNOWN = -1;

    public static boolean isShortVideo(int movieType) {
        return movieType == NEWS || movieType == MTV;
    }

    public static int getMovieTypeFromChannelType(String channelType) {
        int movieType = UNKNOWN;

        if (channelType.equals(ChannelType.KEY_MOVIE)) {
            movieType = MOVIE;
        } else if (channelType.equals(ChannelType.KEY_TV)) {
            movieType = TV;
        } else if (channelType.equals(ChannelType.KEY_ART)) {
            movieType = VARIETY_SHOW;
        } else if (channelType.equals(ChannelType.KEY_ANI)) {
            movieType = ANIME;
        } else if (channelType.equals(ChannelType.KEY_VIDEO)) {
            movieType = NEWS;
        } else if (channelType.equals(ChannelType.KEY_RECORD)) {
            movieType = DOCUMENTARY;
        } else if (channelType.equals(ChannelType.KEY_EDU)) {
            movieType = OPEN_COURSES;
        } else if (channelType.equals(ChannelType.KEY_MTV)) {
            movieType = MTV;
        } else if (channelType.equals(ChannelType.KEY_LISTEN_UP)) {
            movieType = VIDEO_CLIPS;
        }

        return movieType;
    }

    public static String getName(int movieType) {
        String name = null;

        switch (movieType) {
        case MOVIE:
            name = "电影";
            break;

        case TV:
            name = "电视剧";
            break;

        case VARIETY_SHOW:
            name = "综艺";
            break;

        case ANIME:
            name = "动漫";
            break;

        case NEWS:
            name = "视频快报";
            break;

        case DOCUMENTARY:
            name = "纪录片";
            break;

        case OPEN_COURSES:
            name = "公开课";
            break;

        case MTV:
            name = "MTV";
            break;

        case VIDEO_CLIPS:
            name = "精彩片花";
            break;

        default:
            name = "";
            break;
        }

        return name;
    }
}
