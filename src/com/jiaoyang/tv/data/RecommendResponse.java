package com.jiaoyang.tv.data;

public class RecommendResponse {
    public int id;
    public String title;
    public int total_count;
    public int total_actor_count;
    public int total_director_count;
    public RecommendInfo data;

    // 短视频
    public String movie_id;
    public FLV[] flvs;

}
