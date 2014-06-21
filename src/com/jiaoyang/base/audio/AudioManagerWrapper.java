package com.jiaoyang.base.audio;

import android.content.Context;
import android.media.AudioManager;

public class AudioManagerWrapper implements IAudioManager {
    private AudioManager mAudioManager;

    @Override
    public int getStreamVolume(int streamType) {
        return mAudioManager.getStreamVolume(streamType);
    }

    @Override
    public int getStreamMaxVolume(int streamType) {
        return mAudioManager.getStreamMaxVolume(streamType);
    }

    @Override
    public void setStreamVolume(int streamType, int index, int flags) {
        mAudioManager.setStreamVolume(streamType, index, flags);
    }

    public AudioManagerWrapper(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

}
