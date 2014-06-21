package com.jiaoyang.tv.player.widget;

import android.content.Context;

import com.jiaoyang.base.audio.IAudioManager;
import com.jiaoyang.base.audio.AudioManagerWrapper;
import com.jiaoyang.base.media.IMediaPlayer;

public class AudioManagerFactory {

    public static IAudioManager createAudioManager(IMediaPlayer mediaPlayer, Context context) {
        IAudioManager audioManager = null;

        audioManager = new AudioManagerWrapper(context);

        return audioManager;
    }
}
