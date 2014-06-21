package com.jiaoyang.tv.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jiaoyang.base.misc.JiaoyangConstants;
import com.jiaoyang.tv.data.EpisodeList;
import com.jiaoyang.tv.util.Constants;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.tv.util.Logger;

public class PlayerAdapter {
    private static final Logger LOG = Logger.getLogger(PlayerAdapter.class);


    private static PlayerAdapter sInstance = null;

    synchronized public static PlayerAdapter getInstance() {
        if (sInstance == null) {
            sInstance = new PlayerAdapter();
        }

        return sInstance;
    }

    public void play(final Context context, String poster, String bitrate) {
        Intent intent = new Intent();
        intent.putExtra("poster", poster);
        intent.putExtra("bitrate", bitrate);
        intent.putExtra(JiaoyangConstants.IntentDataKey.PLAY_MODE, JiaoyangConstants.PlayMode.PLAY_MODE_WEB);
        intent.setClass(context, PlayerActivity.class);
        context.startActivity(intent);
    }
    
    public static boolean launchPlayerFromCloud(Context context, Bundle data){
        
        if (context == null || data == null || data.size() <= 0)
            return false;
            
        EpisodeList episodeList = EpisodeAdapterUtil.buildEpisodeListFromCloud(data);
        if (episodeList == null)
            return false;
        
        VideoInfoManager.prepareVideoInfo(episodeList, 0, 0, false, false);
        
        Intent intent = new Intent();
        intent.putExtra(JiaoyangConstants.IntentDataKey.PLAY_MODE, JiaoyangConstants.PlayMode.PLAY_MODE_WEB);
        intent.putExtra(Constants.KEYS.ADD_TO_PLAY_RECORD, true);
        intent.setClass(context, PlayerActivity.class);
        context.startActivity(intent);
        
        return true;
        
    }

    public void startActivity(Context context, Intent intent) {
        configPlayerIntent(context, intent);
        context.startActivity(intent);
    }

    public void configPlayerIntent(Context context, Intent intent) {
        intent.setClass(context, PlayerActivity.class);
    }

    private PlayerAdapter() {
        LOG.debug("construction.");
        LOG.debug("MODEL: {}", android.os.Build.MODEL);
        LOG.debug("PRODUCT: {}", android.os.Build.PRODUCT);
        LOG.debug("MANUFACTURER: {}", android.os.Build.MANUFACTURER);
        LOG.debug("SDK: {}", android.os.Build.VERSION.SDK_INT);
        LOG.debug("RELEASE: {}", android.os.Build.VERSION.RELEASE);
    }
}
