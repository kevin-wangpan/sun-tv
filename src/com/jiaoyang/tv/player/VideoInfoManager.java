package com.jiaoyang.tv.player;

import com.jiaoyang.tv.data.EpisodeList;

public class VideoInfoManager {
    private static VideoInfoManager sInstance = null;

    private EpisodeList mEpisodeList;
    private int mIndex;
    private int mPartIndex;
    public boolean isTry;
    public boolean isAuthorityMovie;

    public static void prepareVideoInfo(
            EpisodeList episodeDetailInfo, int index) {
        prepareVideoInfo(episodeDetailInfo, index, 0);
    }

    synchronized public static void prepareVideoInfo(
            EpisodeList episodeDetailInfo, int index,
            int part) {
        if (sInstance == null) {
            sInstance = new VideoInfoManager();
        }

        sInstance.init(episodeDetailInfo, index, part);
    }

    /**
     * 
     * @param episodeDetailInfo
     * @param index
     * @param partIndex
     * @param isTry
     *            是否是试看
     */
    synchronized public static void prepareVideoInfo(
            EpisodeList episodeDetailInfo, int index,
            int partIndex, boolean isTry) {
        if (sInstance == null) {
            sInstance = new VideoInfoManager();
        }
        sInstance.init(episodeDetailInfo, index, partIndex, isTry);
    }

    /**
     * 
     * @param episodeDetailInfo
     * @param index
     * @param partIndex
     * @param isTry
     *            是否是试看
     * @param isAuthorityMovie
     *            是否是鉴权影片（付费影片）
     */
    synchronized public static void prepareVideoInfo(
            EpisodeList episodeDetailInfo, int index,
            int partIndex, boolean isTry, boolean isAuthorityMovie) {
        if (sInstance == null) {
            sInstance = new VideoInfoManager();
        }
        sInstance.init(episodeDetailInfo, index, partIndex, isTry, isAuthorityMovie);
    }

    public static VideoInfoManager getInstance() {
        return sInstance;
    }

    public EpisodeList getEpisodelist() {
        return mEpisodeList;
    }

    public int getIndex() {
        return mIndex;
    }

    public int getPartIndex() {
        return mPartIndex;
    }

    private void init(EpisodeList episodeDetailInfo,
            int index, int partIndex) {
        mEpisodeList = episodeDetailInfo;
        mIndex = index;
        this.mPartIndex = partIndex;
        this.isTry = false;
        this.isAuthorityMovie = false;
    }

    private void init(EpisodeList episodeDetailInfo,
            int index, int partIndex, boolean isTry) {
        init(episodeDetailInfo, index, partIndex);
        this.isTry = isTry;
    }

    private void init(EpisodeList episodeDetailInfo,
            int index, int partIndex, boolean isTry, boolean isAuthorityMovie) {
        init(episodeDetailInfo, index, partIndex);
        this.isAuthorityMovie = isAuthorityMovie;
        this.isTry = isTry;
    }
}
