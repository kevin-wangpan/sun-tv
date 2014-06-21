package com.jiaoyang.base.audio;

public interface IAudioManager {

    int getStreamVolume(int streamType);

    int getStreamMaxVolume(int streamType);

    void setStreamVolume(int streamType, int index, int flags);
}
