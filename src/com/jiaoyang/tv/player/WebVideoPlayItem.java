package com.jiaoyang.tv.player;

import java.util.List;
import java.util.Set;

import android.text.TextUtils;

import com.jiaoyang.base.misc.JiaoyangConstants.PlayMode;
import com.jiaoyang.base.misc.JiaoyangConstants.PlayProfile;
import com.jiaoyang.tv.data.Episode;
import com.jiaoyang.tv.data.Episode.Part;
import com.jiaoyang.tv.data.Episode.Part.URL;
import com.jiaoyang.tv.data.EpisodeList;

public class WebVideoPlayItem implements IVideoItem {
    private String mVideoName;
    private int mMovieType;
    private int mMovieId;
    private Episode mEpisode;
    private Part mCurrentPart;
    private int mCurrentPartIndex;
    private int mProfile; // 视频当前所采用的那一种清晰度
    private Set<Integer> mProfiles;
    private boolean mDownloadAble;
    private int mProductId;
    private int mDisplayType;
    private int mEpisodesSize;

    public WebVideoPlayItem(EpisodeList episodeList, int index, int partIndex) {
        mMovieId = episodeList.id;
        mMovieType = episodeList.type;
        mDownloadAble = episodeList.isSupportDownload();
        mVideoName = episodeList.title;
        mProductId = episodeList.productId;
        mDisplayType = episodeList.displayType2;
        mEpisodesSize = episodeList.episodes.length;

        mEpisode = episodeList.getEpisodeByIndex(index);
        mCurrentPartIndex = partIndex;
        mCurrentPart = mEpisode.getPartByIndex(mCurrentPartIndex);
        mProfiles = mCurrentPart.getProfiles();
//        mProfile = getProfileLowToHigh();
        mProfile = getProfileHighToLow();
    }

    public int getCurrentPartIndex() {
        return mCurrentPartIndex;
    }

    public void setCurrentPartIndex(int partIndex) {
        mCurrentPartIndex = partIndex;
    }

    public Episode getEpisode() {
        return mEpisode;
    }

    public Part getCurrentPart() {
        return mCurrentPart;
    }

    @Override
    public boolean hasNextPart() {
        return mCurrentPartIndex < mEpisode.parts.length - 1;

    }

    @Override
    public boolean hasPrePart() {
        return mCurrentPartIndex > 0;
    }

    @Override
    public void moveToNextPart() {
        if (hasNextPart()) {
            mCurrentPartIndex++;
            mCurrentPart = mEpisode.getPartByIndex(mCurrentPartIndex);
            mProfiles = mCurrentPart.getProfiles();
        }

    }

    @Override
    public void moveToPrePart() {
        if (hasPrePart()) {
            mCurrentPartIndex--;
            mCurrentPart = mEpisode.getPartByIndex(mCurrentPartIndex);
            mProfiles = mCurrentPart.getProfiles();
        }
    }

    public int getMovieType() {
        return mMovieType;
    }

    public int getMovieId() {
        return mMovieId;
    }

    public int getProductId() {
        return mProductId;
    }

    public int getIndex() {
        return mEpisode.index;
    }

    @Override
    public int getProfile() {
        return mProfile;
    }

    @Override
    public void setProfile(int profile) {
        if (profile != mProfile) {
            mProfile = -1;

            if (isSupportProfile(profile)) {
                mProfile = profile;
            } else {
                for (int i = profile - 1; i >= PlayProfile.SMOOTH_PROFILE; i--) {
                    if (isSupportProfile(i)) {
                        mProfile = i;
                        break;
                    }
                }
                if (mProfile == -1) {
//                    for (int i = profile + 1; i <= PlayProfile.HIGH_PROFILE; i++) {
                        for (int i = profile + 1; i <= PlayProfile.SUPER_PROFILE; i++) {
                        if (isSupportProfile(i)) {
                            mProfile = i;
                            break;
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public boolean isSupportMultiProfile() {
        return mProfiles.size() > 1 ? true : false;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getShortIntro() {
        String shortIntro = null;
        shortIntro = mEpisode.title;
        return shortIntro;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getVideoName() {
        return mVideoName;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getDisplayTitle() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(getVideoName())) {
            sb.append(getVideoName());
        }

        if (!TextUtils.isEmpty(getShortIntro())) {
            sb.append(" ").append(getShortIntro());
        }

        if (!TextUtils.isEmpty(getPartTitle())) {
            sb.append(" ").append(getPartTitle());
        }

        return sb.toString();
    }

    private String getPartTitle() {
        String partTitle = "";
        final int partSize = mEpisode.parts.length;
        final int currentPartIndex = mCurrentPart.index;
        if (partSize == 1) {

        } else if (partSize == 2) {
            if (currentPartIndex == 0) {
                partTitle = "上";
            } else {
                partTitle = "下";
            }
        } else if (partSize == 3) {
            if (currentPartIndex == 0) {
                partTitle = "上";
            } else if (currentPartIndex == 1) {
                partTitle = "中";
            } else {
                partTitle = "下";
            }
        } else {
            partTitle = String.valueOf(currentPartIndex + 1);
        }
        return partTitle;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public boolean isSupportProfile(int profile) {
        return mProfiles.contains(profile);
    }

    private String getDownloadUrlByProfile(int profile) {
        URL url = mCurrentPart.getURLByProfile(profile);
        return url == null ? null : url.url;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getDownloadAbleUrl() {
        String url = null;
        int profile = mProfile;
        url = getDownloadUrlByProfile(profile);
        if (url == null) {
            url = getDefaultPlayUrl();
        }
        return url;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public boolean isSupportDownload() {
        return mDownloadAble;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getPlayUrlByProfile(int profile) {
        URL url = mCurrentPart.getURLByProfile(profile);
        return url == null ? null : url.url;
    }

    /*
     * (non-Javadoc)
     * 
     */
    @Override
    public String getDefaultPlayUrl() {
        String url = getPlayUrlByProfile(mProfile);
        return url;
    }

    /**
     * 清晰度从低到高
     * 
     * @return
     */
    private int getProfileLowToHigh() {
        int profile = 0;
        if (mProfiles.contains(PlayProfile.SMOOTH_PROFILE)) {
            profile = PlayProfile.SMOOTH_PROFILE;
        } else if (mProfiles.contains(PlayProfile.BASE_PROFILE)) {
            profile = PlayProfile.BASE_PROFILE;
        } else if (mProfiles.contains(PlayProfile.HIGH_PROFILE)) {
            profile = PlayProfile.HIGH_PROFILE;
        } else if (mProfiles.contains(PlayProfile.SUPER_PROFILE)) {
            profile = PlayProfile.SUPER_PROFILE;
        }
        return profile;
    }
    
    private int getProfileHighToLow() {
        int profile = PlayProfile.SMOOTH_PROFILE;
        if (mProfiles.contains(PlayProfile.SUPER_PROFILE)) {
            profile = PlayProfile.SUPER_PROFILE;
        }
        else if (mProfiles.contains(PlayProfile.HIGH_PROFILE)) {
            profile = PlayProfile.HIGH_PROFILE;
        }
        else if (mProfiles.contains(PlayProfile.BASE_PROFILE)) {
            profile = PlayProfile.BASE_PROFILE;
        }
        else if (mProfiles.contains(PlayProfile.SMOOTH_PROFILE)) {
            profile = PlayProfile.SMOOTH_PROFILE;
        }
        return profile;
    }

    @Override
    public int videoPlayMode() {
        return PlayMode.PLAY_MODE_WEB;
    }

    public List<URL> getPartUrl() {
        return mCurrentPart.getURLS();
    }

    @Override
    public Set<Integer> getProfiles() {
        return mProfiles;
    }

    @Override
    public int getVideoItemIndex() {
        return getIndex();
    }
}
