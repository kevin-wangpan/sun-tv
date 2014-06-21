package com.jiaoyang.tv.player;

import java.util.List;

import com.jiaoyang.tv.data.EpisodeList;

/**
 * 处理在线视频的播放列表
 * 
 * @author admin
 * 
 */
public class WebVideoPlayList implements IVideoPlayList {
    private int mCurrentIndex;
    private EpisodeList mEpisodeList;
    private WebVideoPlayItem mCurrentVideoPlayItem;
    private boolean isTry;
    private boolean isAuthorityMovie;

    public WebVideoPlayList(VideoInfoManager videoInfoManager) {
        mCurrentIndex = videoInfoManager.getIndex();
        mEpisodeList = videoInfoManager.getEpisodelist();
        isTry = videoInfoManager.isTry;
        isAuthorityMovie = videoInfoManager.isAuthorityMovie;
        mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex, videoInfoManager.getPartIndex());
    }

    public String getVideoName() {
        return mEpisodeList != null ? mEpisodeList.title : "";
    }

    @Override
    public WebVideoPlayItem getCurrentPlayItem() {
        return mCurrentVideoPlayItem;
    }

    @Override
    public IVideoItem moveToPrevPlayItem() {
        if (hasPrevVideo()) {
            final int profile = mCurrentVideoPlayItem.getProfile();
            mCurrentIndex--;
            mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex, 0);
            mCurrentVideoPlayItem.setProfile(profile);
        }

        return mCurrentVideoPlayItem;
    }

    @Override
    public IVideoItem moveToNextPlayItem() {
        if (hasNextVideo()) {
            final int profile = mCurrentVideoPlayItem.getProfile();
            mCurrentIndex++;
            mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex, 0);
            mCurrentVideoPlayItem.setProfile(profile);
        }

        return mCurrentVideoPlayItem;
    }

    @Override
    public int getCurrentPlayIndex() {
        return mCurrentIndex;
    }

    @Override
    public boolean hasPrevVideo() {
        return mEpisodeList != null && mCurrentIndex > 0;
    }

    @Override
    public boolean hasNextVideo() {
        return mEpisodeList != null && (mCurrentIndex + 1) < mEpisodeList.episodes.length;
    }

    private WebVideoPlayItem getVideoPlayItem(int index, int partIndex) {
        if (index > -1 && index < mEpisodeList.episodes.length) {
            return new WebVideoPlayItem(mEpisodeList, index, partIndex);
        } else {
            return null;
        }
    }
    
    @Override
    public WebVideoPlayItem moveToPlayItemByIndex(int index) {
        if (index > -1 && index < mEpisodeList.episodes.length) {
            final int profile = mCurrentVideoPlayItem.getProfile();
            mCurrentIndex = index;
            mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex, 0);
            mCurrentVideoPlayItem.setProfile(profile);
            return mCurrentVideoPlayItem;
        }
        return null;
    }
    
    @Override
    public int getPlayItemSizes() {
        return mEpisodeList != null ? mEpisodeList.episodes.length : 0;
    }

    @Override
    public boolean isTry() {
        return isTry;
    }

    @Override
    public boolean isAuthorityMovie() {
        return isAuthorityMovie;
    }
    
    public EpisodeList getEpisodeList(){
        return mEpisodeList;
    }
    
}
