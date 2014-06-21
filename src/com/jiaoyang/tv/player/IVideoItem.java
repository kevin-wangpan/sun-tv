package com.jiaoyang.tv.player;

import java.util.Set;

public interface IVideoItem {

    /**
     * 返回是哪一种清晰度
     * 
     * @return {@link #SMOOTH_PROFILE} {@link #BASE_PROFILE} {@link #HIGH_PROFILE}
     */
    public abstract int getProfile();

    /**
     * 
     * @param profile
     *            {@link #SMOOTH_PROFILE} {@link #BASE_PROFILE} {@link #HIGH_PROFILE}
     * @throws IllegalArgumentException
     */
    public abstract void setProfile(int profile);
    
    public abstract Set<Integer> getProfiles();
    
    public abstract int getVideoItemIndex();

    /**
     * 是否有多个清晰度的视频
     * 
     * @return
     */
    public abstract boolean isSupportMultiProfile();

    public abstract String getShortIntro();

    public abstract String getVideoName();

    public abstract String getDisplayTitle();

    /**
     * 判断是否支持该清晰度
     * 
     * @param profile
     *            流畅 ({@link #BASE_PROFILE})、标清({@link #HIGH_PROFILE} )、高清({@link #SUPER_PROFILE} )
     * @return
     */
    public abstract boolean isSupportProfile(int profile);

    /**
     * 从低清晰度到高清晰度选择可以提供下载的url
     * 
     * @return
     */
    public abstract String getDownloadAbleUrl();

    public abstract boolean isSupportDownload();

    public abstract String getPlayUrlByProfile(int profile);

    public abstract String getDefaultPlayUrl();

    public abstract int videoPlayMode();

    public abstract void moveToPrePart();

    public abstract void moveToNextPart();

    public abstract boolean hasPrePart();

    public abstract boolean hasNextPart();

}
