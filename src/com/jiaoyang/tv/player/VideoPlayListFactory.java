package com.jiaoyang.tv.player;

import android.content.Intent;

import com.jiaoyang.base.misc.JiaoyangConstants;

public class VideoPlayListFactory {

    public static IVideoPlayList createPlayList(Intent intent) {
        IVideoPlayList playlist = null;

        if (intent != null) {
            int playMode =
                    intent.getIntExtra(JiaoyangConstants.IntentDataKey.PLAY_MODE, JiaoyangConstants.PlayMode.PLAY_MODE_IVALID);
            switch (playMode) {
            case JiaoyangConstants.PlayMode.PLAY_MODE_WEB:
                playlist = new WebVideoPlayList(VideoInfoManager.getInstance());
                break;

            case JiaoyangConstants.PlayMode.PLAY_MODE_LOCAL:
                playlist = new LocalVideoPlayList(intent);
                break;

            default:
                break;
            }
        }

        return playlist;
    }
}
