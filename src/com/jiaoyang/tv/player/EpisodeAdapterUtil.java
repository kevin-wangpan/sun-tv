package com.jiaoyang.tv.player;

import android.os.Bundle;
import android.text.TextUtils;

import com.jiaoyang.base.data.MovieType;
import com.jiaoyang.base.misc.JiaoyangConstants;
import com.jiaoyang.tv.data.Episode;
import com.jiaoyang.tv.data.EpisodeList;
import com.jiaoyang.tv.data.FLV;
import com.jiaoyang.tv.data.RecommendResponse;
import com.jiaoyang.tv.data.Episode.Part;
import com.jiaoyang.tv.data.Episode.Part.URL;

public class EpisodeAdapterUtil {


    
    public static EpisodeList buildEpisodeListFromCloud(Bundle data){
        
        EpisodeList episodeList = null;
        String videoDisplayTitle = null;
        String videoGcid = null;
        String videoCid = null;
        String videoUrlQualityLow = null;
        String videoUrlQualitySmooth320P = null;
        String videoUrlQualityNormal480P = null;
        String videoUrlQualityHigh720P = null;
        String videoUrlQualitySuper1080P = null;
        
        
        if(data != null){
            videoDisplayTitle = data.getString(JiaoyangPlayerConstant.VIDEO_DISPLAY_TITLE);
            videoGcid = data.getString(JiaoyangPlayerConstant.VIDEO_GCID);
            videoCid = data.getString(JiaoyangPlayerConstant.VIDEO_CID);
            videoUrlQualityLow = data.getString(JiaoyangPlayerConstant.VIDEO_URL_QUALITY_LOW);
            videoUrlQualitySmooth320P = data.getString(JiaoyangPlayerConstant.VIDEO_URL_QUALITY_SMOOTH_320P);
            videoUrlQualityNormal480P = data.getString(JiaoyangPlayerConstant.VIDEO_URL_QUALITY_NORMAL_480P);
            videoUrlQualityHigh720P = data.getString(JiaoyangPlayerConstant.VIDEO_URL_QUALITY_HIGH_720P);
            videoUrlQualitySuper1080P = data.getString(JiaoyangPlayerConstant.VIDEO_URL_QUALITY_SUPER_1080P);
        }else{
            return null;
        }
        
        if (!TextUtils.isEmpty(videoDisplayTitle)
                && (!TextUtils.isEmpty(videoUrlQualityLow)
                        || !TextUtils.isEmpty(videoUrlQualitySmooth320P)
                        || !TextUtils.isEmpty(videoUrlQualityNormal480P)
                        || !TextUtils.isEmpty(videoUrlQualityHigh720P)
                        || !TextUtils.isEmpty(videoUrlQualitySuper1080P))) {

            episodeList = new EpisodeList(1);
            Episode episode = new Episode(1);
            
            int urlCount = 0;
            if(!TextUtils.isEmpty(videoUrlQualityLow)) urlCount++;
            if(!TextUtils.isEmpty(videoUrlQualitySmooth320P)) urlCount++;
            if(!TextUtils.isEmpty(videoUrlQualityNormal480P)) urlCount++;
            if(!TextUtils.isEmpty(videoUrlQualityHigh720P)) urlCount++;
            if(!TextUtils.isEmpty(videoUrlQualitySuper1080P)) urlCount++;
            Episode.Part part = new Part(urlCount);
            
            int urlIndex = 0;
            if(!TextUtils.isEmpty(videoUrlQualityLow)) {
                URL url = new URL();
                url.profile = JiaoyangConstants.PlayProfile.LOW_PROFILE;
                url.url = videoUrlQualityLow;
                part.addURL(url, urlIndex);
                urlIndex++;
            }
            if(!TextUtils.isEmpty(videoUrlQualitySmooth320P)) {
                URL url = new URL();
                url.profile = JiaoyangConstants.PlayProfile.SMOOTH_PROFILE;
                url.url = videoUrlQualitySmooth320P;
                part.addURL(url, urlIndex);
                urlIndex++;
            }
            if(!TextUtils.isEmpty(videoUrlQualityNormal480P)) {
                URL url = new URL();
                url.profile = JiaoyangConstants.PlayProfile.BASE_PROFILE;
                url.url = videoUrlQualityNormal480P;
                part.addURL(url, urlIndex);
                urlIndex++;
            }
            if(!TextUtils.isEmpty(videoUrlQualityHigh720P)) {
                URL url = new URL();
                url.profile = JiaoyangConstants.PlayProfile.HIGH_PROFILE;
                url.url = videoUrlQualityHigh720P;
                part.addURL(url, urlIndex);
                urlIndex++;
            }
            if(!TextUtils.isEmpty(videoUrlQualitySuper1080P)) {
                URL url = new URL();
                url.profile = JiaoyangConstants.PlayProfile.SUPER_PROFILE;
                url.url = videoUrlQualitySuper1080P;
                part.addURL(url, urlIndex);
                urlIndex++;
            }
            
            episode.addPart(part, 0);
            
            episodeList.gcid = videoGcid;
            episodeList.cid = videoCid;
            episodeList.type = MovieType.UNKNOWN;
            episodeList.title = videoDisplayTitle;
            episodeList.label = "";
            episodeList.productId = -1;
            
            episodeList.displayType2 = 0;
            episodeList.addEpisode(episode, 0);
            
        }
        
        return episodeList;
    }
    
    public static EpisodeList buildEpisodeList(RecommendResponse recommendData, int mMovieType) {
        EpisodeList list = new EpisodeList();
        list.id = recommendData.id;
        list.type = mMovieType;
        list.title = "";// recommendData.title;
        list.label = recommendData.title;
        list.displayType2 = 2;
        list.episodes = new Episode[recommendData.flvs.length];

        for (int i = 0; i < list.episodes.length; i++) {

            Episode episode = new Episode();
            FLV recommendFLV = recommendData.flvs[i];
            episode.index = i;
            episode.label = recommendFLV.episode_title;
            episode.title = recommendFLV.episode_title;
            episode.parts = new Part[1];
            Part part = new Episode.Part(1);
            part.index = 0;
            part.id = java.lang.Integer.valueOf(recommendFLV.movie_id);
            Episode.Part.URL url = new Episode.Part.URL();
            url.profile = recommendFLV.urls[0].display_level;
            url.url = recommendFLV.urls[0].url_play;
            part.addURL(url, 0);

            episode.parts[0] = part;
            list.episodes[i] = episode;

        }
        return list;
    }
}
