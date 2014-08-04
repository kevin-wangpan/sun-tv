package com.jiaoyang.tv.player;

import android.content.Context;
import android.content.Intent;

import com.jiaoyang.base.misc.JiaoyangConstants;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.tv.util.Logger;

public class PlayerAdapter {

    public static final String VIDEO_ID_KEY = "video_id_key";
    public static final String VIDEO_TITLE_KEY = "video_title_key";
    public static final String VIDEO_SID_KEY = "video_sid_key";
    public static final String VIDEO_INDEX_KEY = "video_index_key";

    public final static String RST_NORMAL = "600";
    public final static String RST_HEIGHT = "900";
    public final static String RST_SUPER = "1200";
    
    public final static String F_HLS = "hls";
    public final static String F_MP4 = "mp4";

    private static PlayerAdapter sInstance = null;

    synchronized public static PlayerAdapter getInstance() {
        if (sInstance == null) {
            sInstance = new PlayerAdapter();
        }

        return sInstance;
    }

    public void play(final Context context, Movie movie, int position) {
        Intent intent = new Intent();
        intent.putExtra(VIDEO_ID_KEY, movie.videos[position]);
        intent.putExtra(VIDEO_TITLE_KEY, movie.title);
        intent.putExtra(VIDEO_INDEX_KEY, position);
        intent.putExtra(VIDEO_SID_KEY, movie.baidu_sid);
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
