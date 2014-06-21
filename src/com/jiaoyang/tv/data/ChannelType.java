package com.jiaoyang.tv.data;

import java.util.HashMap;
import java.util.Map;

import com.jiaoyang.base.data.MovieType;

public class ChannelType {

    /**
     * 电影
     */
    public static final String MOVIE = "movie";

    /**
     * 电视剧
     */
    public static final String TV = "teleplay";

    /**
     * 综艺
     */
    public static final String VARIETY_SHOW = "tv";

    /**
     * 动漫
     */
    public static final String ANIME = "anime";

    /**
     * 付费
     */
    public static final String VIP = "vip";

    /**
     * 视频快报
     */
    public static final String NEWS = "video";

    /**
     * 记录片
     */
    public static final String DOCUMENTARY = "documentary";

    /**
     * 微电影
     */
    public static final String VMOVIE = "vmovie";

    /**
     * 1080p
     */
    public static final String MOVIE_1080 = "1080";
    /**
     * dts
     */
    public static final String DTS = "dts";

    /**
     * 公开课
     */
    public static final String OPEN_COURSES = "lesson";

    /**
     * MTV
     */
    public static final String MTV = "mtv";

    /**
     * 片花
     */
    public static final String VIDEO_CLIPS = "ph";

    /**
     * 娱乐
     */
    public static final String ENTERTAINMENT = "yule";

    /**
     * 灰色内容
     */
    public static final String TELEPLAY = "teleplay";

    /**
     * 未知
     */
    public static final String UNKNOWN = "";
    /**
     * 自定义频道
     */
    public static final String CUSTOM ="add_custom";

    public static String getName(int type) {
        switch (type) {
        case MovieType.MOVIE:
            return MOVIE;
        case MovieType.TV:
            return TV;
        case MovieType.VARIETY_SHOW:
            return VARIETY_SHOW;
        case MovieType.ANIME:
            return ANIME;
        case MovieType.NEWS:
            return NEWS;
        case MovieType.DOCUMENTARY:
            return DOCUMENTARY;
        case MovieType.OPEN_COURSES:
            return OPEN_COURSES;
        case MovieType.VMOVIE:
            return VMOVIE;
        case MovieType.MTV:
            return MTV;
        case MovieType.ENTERTAINMENT:
            return ENTERTAINMENT;
        case MovieType.UNKNOWN:
            return UNKNOWN;
        }
        return UNKNOWN;
    }

    public static String getName(String type) {
        String name = TYPE_NAMES.get(type);

        return name != null ? name : "";
    }
    public static int getMovieType(String type){
        if(null==type||(!MOVIE_TYPES.containsKey(type)))
            return MovieType.UNKNOWN;
        else return MOVIE_TYPES.get(type);
    }

    private static final Map<String, String> TYPE_NAMES = new HashMap<String, String>();
    private static final Map<String, Integer> MOVIE_TYPES = new HashMap<String,Integer>();

    static {
        TYPE_NAMES.put(MOVIE, "电影");
        TYPE_NAMES.put(TV, "电视剧");
        TYPE_NAMES.put(VARIETY_SHOW, "综艺");
        TYPE_NAMES.put(ANIME, "动漫");
        TYPE_NAMES.put(VIP, "首播影院");
        TYPE_NAMES.put(NEWS, "视频快报");
        TYPE_NAMES.put(DOCUMENTARY, "纪录片");
        TYPE_NAMES.put(OPEN_COURSES, "公开课");
        TYPE_NAMES.put(MTV, "MTV");
        TYPE_NAMES.put(VIDEO_CLIPS, "片花");
        TYPE_NAMES.put(ENTERTAINMENT, "娱乐");
        TYPE_NAMES.put(VMOVIE, "微电影");
        TYPE_NAMES.put(MOVIE_1080, "1080P");
        TYPE_NAMES.put(DTS, "DTS");
        
        MOVIE_TYPES.put(MOVIE,MovieType.MOVIE);
        MOVIE_TYPES.put(TV,MovieType.TV);
        MOVIE_TYPES.put(VARIETY_SHOW,MovieType.VARIETY_SHOW);
        MOVIE_TYPES.put(ANIME,MovieType.ANIME);
        MOVIE_TYPES.put(NEWS,MovieType.NEWS);
        MOVIE_TYPES.put(DOCUMENTARY,MovieType.DOCUMENTARY);
        MOVIE_TYPES.put(OPEN_COURSES,MovieType.OPEN_COURSES);
        MOVIE_TYPES.put(VMOVIE,MovieType.VMOVIE);
        MOVIE_TYPES.put(MTV,MovieType.MTV);
        MOVIE_TYPES.put(ENTERTAINMENT,MovieType.ENTERTAINMENT);
        MOVIE_TYPES.put(UNKNOWN,MovieType.UNKNOWN);
    }
    
    public static boolean isShortVideo(String channelType) {
    	return NEWS.equals(channelType) || MTV.equals(channelType);
    }
}
