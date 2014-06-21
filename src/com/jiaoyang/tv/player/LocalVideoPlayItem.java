package com.jiaoyang.tv.player;

import java.util.Set;

import android.content.Intent;

import com.jiaoyang.base.misc.JiaoyangConstants;
import com.jiaoyang.base.misc.JiaoyangConstants.PlayMode;

public class LocalVideoPlayItem implements IVideoItem {
    private String mTitle;
    private String mUrl;

    public LocalVideoPlayItem(Intent intent) {
        if (intent != null) {
            mTitle = intent.getStringExtra(JiaoyangConstants.IntentDataKey.TITLE);
            mUrl = intent.getStringExtra(JiaoyangConstants.IntentDataKey.URL);
        }
    }

    @Override
    public int getProfile() {
        return 0;
    }

    @Override
    public void setProfile(int profile) {
    }

    @Override
    public boolean isSupportMultiProfile() {
        return false;
    }

    @Override
    public String getShortIntro() {
        return null;
    }

    @Override
    public String getVideoName() {
        return mTitle;
    }

    @Override
    public String getDisplayTitle() {
        return mTitle;
    }

    @Override
    public boolean isSupportProfile(int profile) {
        return false;
    }

    @Override
    public String getDownloadAbleUrl() {
        return null;
    }

    @Override
    public boolean isSupportDownload() {
        return false;
    }

    @Override
    public String getPlayUrlByProfile(int profile) {
        return null;
    }

    @Override
    public String getDefaultPlayUrl() {
        return mUrl;
    }

    @Override
    public int videoPlayMode() {
        return PlayMode.PLAY_MODE_LOCAL;
    }

    public void moveToPrePart() {
    }

    public void moveToNextPart() {
    }

    public boolean hasPrePart() {
        return false;
    }

    public boolean hasNextPart() {
        return false;
    }

    @Override
    public Set<Integer> getProfiles() {
        return null;
    }

    @Override
    public int getVideoItemIndex() {
        return -1;
    }

}
