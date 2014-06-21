package com.jiaoyang.tv.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FLV implements Serializable {
    public String episode_title;
    public String movie_id;
    public int movie_type;
    public String playtimes;
    public int episode_index;
    public FLVPlayUrl[] urls;
    public int chaptertype;
}
