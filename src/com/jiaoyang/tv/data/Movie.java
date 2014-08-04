package com.jiaoyang.tv.data;


public class Movie extends BaseRspData {

    private static final String DETAIL_URL = "http://ci2.sun-tv.com.cn/movie/get/";


    //在Metro中显示的样式，0表示普通的MetroItem，1表示竖的，2表示横的
    public static final int LAYOUT_TYPE_NORMAL = 0;
    public static final int LAYOUT_TYPE_PORTRAIT = 1;
    public static final int LAYOUT_TYPE_LANDSCAPE = 2;

    public String mid;
    public String title;
    public String imgurl;
    public String vcounts;
    public String jumptype;
    public String language; //语言
    public String area; //地区
    public String time; //年代
    public String points; //看点
    public int layoutType = 1; //在Metro中显示的样式，0表示普通的MetroItem，1表示竖的，2表示横的

    public String[] videos;
    public String intro;
    public String info;
    public String baidu_sid;
    public static String getDetailUrlFromId(String id) {
        return DETAIL_URL + id;
    }

    public String getPosterUrl() {
        return imgurl;
    }

    public String getDetailUrl() {
        return DETAIL_URL + mid;
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
