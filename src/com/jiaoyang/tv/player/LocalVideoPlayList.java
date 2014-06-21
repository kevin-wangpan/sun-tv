package com.jiaoyang.tv.player;

import android.content.Intent;

/**
 * 下载的本地视频播放列表
 * 
 * @author admin
 */
public class LocalVideoPlayList implements IVideoPlayList {
    private LocalVideoPlayItem mLocalVideoPlayItem;

    public LocalVideoPlayList(Intent intent) {
        mLocalVideoPlayItem = new LocalVideoPlayItem(intent);
    }

    @Override
    public LocalVideoPlayItem getCurrentPlayItem() {
        return mLocalVideoPlayItem;
    }

    @Override
    public IVideoItem moveToPrevPlayItem() {
        return null;
    }

    @Override
    public IVideoItem moveToNextPlayItem() {
        return null;
    }

    @Override
    public int getCurrentPlayIndex() {
        return 0;
    }

    @Override
    public boolean hasPrevVideo() {
        return false;
    }

    @Override
    public boolean hasNextVideo() {
        return false;
    }

    @Override
    public boolean isTry() {
        return false;
    }

    @Override
    public boolean isAuthorityMovie() {
        return false;
    }

    @Override
    public IVideoItem moveToPlayItemByIndex(int index) {
        return mLocalVideoPlayItem;
    }

    @Override
    public int getPlayItemSizes() {
        return 1;
    }
}
