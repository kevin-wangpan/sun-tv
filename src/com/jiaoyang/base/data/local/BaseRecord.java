package com.jiaoyang.base.data.local;

import com.jiaoyang.base.data.local.DbField.DataType;

public class BaseRecord extends BaseInfo {

    public static final String COLUMN_MOVIE_ID = "movieid";
    public static final String COLUMN_IS_ONLINE = "isonline";

    public static final int INVALID_PART_INDEX = -1;
    @DbField(name="index", type = DataType.INTEGER, isNull = false)
    public int index; //第几集
    public int episodeid; //json中的第几集，对综艺，显示的是日期，anyway，废物！！！

    @DbField(name="partindex", type = DataType.INTEGER, isNull = false)
    public int partindex = INVALID_PART_INDEX; //第几个part

    @DbField(name="verbigposter",type = DataType.TEXT, isNull = false)
    public String verbigposter;

    @DbField(name="resolution",type = DataType.TEXT, isNull = false)
    public String resolution; //保持是视频的最高清晰度，而不是播放时使用的清晰度

    @DbField(name = COLUMN_IS_ONLINE,type=DataType.INTEGER,isNull = false)
    public int isOnline=0; //是否是在线播放记录，默认是本地记录

    @DbField(name="subid",type=DataType.INTEGER,isNull = false)
    public int subid; //子集id

    @DbField(name="chaptername",type = DataType.TEXT, isNull = false)
    public String chaptername; //子集名称

    @DbField(name="devicename",type = DataType.TEXT, isNull = false)
    public String deviceName="手机"; //观看的设备名称

    @DbField(name="movietitle",type = DataType.TEXT, isNull = false)
    public String movietitle;// 视频标题

    @DbField(name=COLUMN_MOVIE_ID, type = DataType.INTEGER, isNull = false)
    public int movieid;// 视频id

    @DbField(name="type",type = DataType.INTEGER, isNull = false)
    public int type;// 频道
    public String movietype; //用于解析播放记录和收藏记录同步的json字段

    @DbField(name="vodaddress",type = DataType.TEXT, isNull = false)
    public String vodaddress;// 播放地址

    @DbField(name="movietiming",type = DataType.INTEGER, isNull = false)
    public int movietiming;// 视频播放时长，单位秒

    @DbField(name="movielength",type = DataType.INTEGER, isNull = false)
    public int movielength; //视频的长度

    @DbField(name = "productid", type = DataType.INTEGER, isNull = false)
    public int productid;// productid

    public int charge; // 是否是付费 0:免费，1付费
}
