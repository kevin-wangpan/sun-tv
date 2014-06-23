package com.jiaoyang.tv.player;

import java.util.Set;

import com.jiaoyang.base.misc.JiaoyangConstants.PlayMode;
import com.jiaoyang.tv.data.Movie;

public class WebVideoPlayItem implements IVideoItem {
    private Movie mMovie;
    private int mCurIndex;

    public WebVideoPlayItem(Movie movie, int index) {
        mMovie = movie;
        mCurIndex = index;
    }

    @Override
    public boolean hasNextPart() {
        return false;

    }

    @Override
    public boolean hasPrePart() {
        return false;
    }

    @Override
    public void moveToNextPart() {
        if (hasNextPart()) {}

    }

    @Override
    public void moveToPrePart() {
        if (hasPrePart()) {}
    }

    @Override
    public int getProfile() {
        return 0;
    }

    @Override
    public void setProfile(int profile) {}

    /*
     * (non-Javadoc)
     */
    @Override
    public boolean isSupportMultiProfile() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getShortIntro() {
        return mMovie.title;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getVideoName() {
        return mMovie.title;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getDisplayTitle() {
        return mMovie.title;
    }


    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public boolean isSupportProfile(int profile) {
        return false;
    }

    private String getDownloadUrlByProfile(int profile) {
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getDownloadAbleUrl() {
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public boolean isSupportDownload() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getPlayUrlByProfile(int profile) {
        return "http://bcs.duapp.com/video243/e5/b0/600/208003.m3u8";
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getDefaultPlayUrl() {
        String url = getPlayUrlByProfile(0);
        return url;
    }

    @Override
    public int videoPlayMode() {
        return PlayMode.PLAY_MODE_WEB;
    }

    @Override
    public Set<Integer> getProfiles() {
        return null;
    }

    @Override
    public int getVideoItemIndex() {
        return 0;
    }
}
