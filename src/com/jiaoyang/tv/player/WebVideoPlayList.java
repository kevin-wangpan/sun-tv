package com.jiaoyang.tv.player;

import com.jiaoyang.tv.data.Movie;

/**
 * 处理在线视频的播放列表
 * 
 * @author admin
 * 
 */
public class WebVideoPlayList implements IVideoPlayList {
    private int mCurrentIndex;
    private Movie mMovie;
    private WebVideoPlayItem mCurrentVideoPlayItem;

    public WebVideoPlayList(VideoInfoManager videoInfoManager) {
        mCurrentIndex = videoInfoManager.getIndex();
        mMovie = videoInfoManager.getMovie();
        mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex);
    }

    public String getVideoName() {
        return mMovie.title;
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
            mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex);
            mCurrentVideoPlayItem.setProfile(profile);
        }

        return mCurrentVideoPlayItem;
    }

    @Override
    public IVideoItem moveToNextPlayItem() {
        if (hasNextVideo()) {
            final int profile = mCurrentVideoPlayItem.getProfile();
            mCurrentIndex++;
            mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex);
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
        return mCurrentIndex > 0;
    }

    @Override
    public boolean hasNextVideo() {
        return mCurrentIndex < mMovie.videos.length - 1;
    }

    private WebVideoPlayItem getVideoPlayItem(int index) {
        if (mMovie != null) {
            return new WebVideoPlayItem(mMovie, index);
        } else {
            return null;
        }
    }
    
    @Override
    public WebVideoPlayItem moveToPlayItemByIndex(int index) {
        if (index > -1 && index < mMovie.videos.length) {
            final int profile = mCurrentVideoPlayItem.getProfile();
            mCurrentIndex = index;
            mCurrentVideoPlayItem = getVideoPlayItem(mCurrentIndex);
            mCurrentVideoPlayItem.setProfile(profile);
            return mCurrentVideoPlayItem;
        }
        return null;
    }
    
    @Override
    public int getPlayItemSizes() {
        return mMovie.videos.length;
    }

    @Override
    public boolean isTry() {
        return false;
    }

    @Override
    public boolean isAuthorityMovie() {
        return false;
    }
}
