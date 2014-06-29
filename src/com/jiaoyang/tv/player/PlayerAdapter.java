package com.jiaoyang.tv.player;

import android.content.Context;
import android.content.Intent;

import com.jiaoyang.base.misc.JiaoyangConstants;
import com.jiaoyang.tv.util.Logger;

public class PlayerAdapter {

    public static final String VIDEO_ID_KEY = "video_id_key";
    public static final String VIDEO_TITLE_KEY = "video_title_key";
    private static PlayerAdapter sInstance = null;

    synchronized public static PlayerAdapter getInstance() {
        if (sInstance == null) {
            sInstance = new PlayerAdapter();
        }

        return sInstance;
    }

    public void play(final Context context, String videoId, String title) {
        Intent intent = new Intent();
        intent.putExtra(VIDEO_ID_KEY, videoId);
        intent.putExtra(VIDEO_TITLE_KEY, title);
        intent.putExtra(JiaoyangConstants.IntentDataKey.PLAY_MODE, JiaoyangConstants.PlayMode.PLAY_MODE_WEB);
        intent.setClass(context, PlayerActivity.class);
        context.startActivity(intent);
    }

    public void startActivity(Context context, Intent intent) {
        configPlayerIntent(context, intent);
        context.startActivity(intent);
    }

    public void configPlayerIntent(Context context, Intent intent) {
        intent.setClass(context, PlayerActivity.class);
    }
}
