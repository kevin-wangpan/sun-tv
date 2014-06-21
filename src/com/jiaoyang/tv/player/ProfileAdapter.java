package com.jiaoyang.tv.player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;

import com.jiaoyang.base.misc.JiaoyangConstants.PlayProfile;
import com.jiaoyang.tv.data.Episode.Part.URL;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.video.tv.R;

public class ProfileAdapter {
    private static final Logger LOG = Logger.getLogger(ProfileAdapter.class);

    private final Context mContext;
    private List<URL> mEpisodePartUrls = null;
    String[] mProfiles;

    public String[] getProfileIntro() {
        return mProfiles;
    }

    public int getPlayedProfileIndex(IVideoItem videoPlayItem) {
        int ret = 0;
        for (int i = 0; i < mEpisodePartUrls.size(); i++) {
            if (videoPlayItem.getProfile() == mEpisodePartUrls.get(i).profile) {
                return i;
            }
        }
        return ret;
    }

    public int getDownLoadProfileIndex(int profile) {
        int ret = 0;
        for (int i = 0; i < mEpisodePartUrls.size(); i++) {
            if (profile == mEpisodePartUrls.get(i).profile) {
                return i;
            }
        }
        return ret;
    }

    public boolean isExistProfile(int profile) {
        for (int i = 0; i < mEpisodePartUrls.size(); i++) {
            if (profile == mEpisodePartUrls.get(i).profile) {
                return true;
            }
        }
        return false;
    }

    public ProfileAdapter(Context context, List<URL> epUrls) {
        this.mContext = context;
        mEpisodePartUrls = epUrls;

        if (mEpisodePartUrls.size() > 1) {
            Collections.sort(mEpisodePartUrls, new Comparator<URL>() {

                @Override
                public int compare(URL lhs, URL rhs) {
                    if (lhs.profile == rhs.profile) {
                        return 0;
                    } else if (lhs.profile < rhs.profile) {
                        return -1;
                    } else {
                        return 1;
                    }
                }

            });
        }
        initProfileIntroArray();
    }

    private void initProfileIntroArray() {
        LOG.info("mEpisodePartUrls.size()={}", mEpisodePartUrls.size());

        mProfiles = new String[mEpisodePartUrls.size()];
        URL epUrl = null;
        String text = null;
        for (int i = 0; i < mEpisodePartUrls.size(); i++) {
            epUrl = mEpisodePartUrls.get(i);
//            if (epUrl != null && epUrl.profile <= PlayProfile.HIGH_PROFILE
              if (epUrl != null && epUrl.profile <= PlayProfile.SUPER_PROFILE
                    && epUrl.profile >= PlayProfile.SMOOTH_PROFILE) {
                final int profile = epUrl.profile;
                switch (profile) {
                case PlayProfile.SMOOTH_PROFILE:
                    text = mContext.getResources().getString(
                            R.string.profile_smooth);
                    break;

                case PlayProfile.BASE_PROFILE:
                    text = mContext.getResources().getString(
                            R.string.profile_base);
                    break;

                case PlayProfile.HIGH_PROFILE:
                    text = mContext.getResources().getString(
                            R.string.profile_high);
                    break;
                case PlayProfile.SUPER_PROFILE:
                    text = mContext.getResources().getString(
                            R.string.profile_super);
                    break;

                default:
                    break;
                }
                mProfiles[i] = text;
            }
        }
    }

    public URL getItem(int position) {
        return mEpisodePartUrls == null ? null : mEpisodePartUrls
                .get(position);
    }

    public URL getHighestProfileItem() {
        return mEpisodePartUrls == null ? null : mEpisodePartUrls.get(mEpisodePartUrls.size() > 1 ? mEpisodePartUrls
                .size() - 1 : 0);
    }

}
