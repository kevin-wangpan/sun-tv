package com.jiaoyang.base.media;

import io.vov.vitamio.MediaPlayer;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.view.SurfaceHolder;

public class JiaoyangMediaPlayerWrapper implements IMediaPlayer {

    private MediaPlayer mMediaPlayer;

    public JiaoyangMediaPlayerWrapper() {
    }

    @Override
    public void init(Context context) {
        mMediaPlayer = new MediaPlayer(context);
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        mMediaPlayer.setDisplay(sh);
    }

    @Override
    public void setAudioStreamType(int streamType) {
//        mMediaPlayer.setAudioStreamType(streamType);
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        mMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException,
            IllegalArgumentException, SecurityException, IllegalStateException {
        mMediaPlayer.setDataSource(context, uri, headers);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        mMediaPlayer.prepare();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        mMediaPlayer.start();
    }

    @Override
    public void pause() throws IllegalStateException {
        mMediaPlayer.pause();
    }

    @Override
    public void stop() throws IllegalStateException {
        mMediaPlayer.stop();
    }

    @Override
    public void reset() {
        mMediaPlayer.reset();
    }

    @Override
    public void release() {
        mMediaPlayer.release();
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        mMediaPlayer.seekTo(msec);
    }

    @Override
    public int getCurrentPosition() {
        return (int) mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return (int) mMediaPlayer.getDuration();
    }

    @Override
    public int getVideoWidth() {
        return mMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mMediaPlayer.getVideoHeight();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void setOnPreparedListener(final OnPreparedListener listener) {
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                listener.onPrepared(JiaoyangMediaPlayerWrapper.this);
            }
        });
    }

    @Override
    public void setOnCompletionListener(final OnCompletionListener listener) {
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                listener.onCompletion(JiaoyangMediaPlayerWrapper.this);
            }
        });
    }

    @Override
    public void setOnBufferingUpdateListener(final OnBufferingUpdateListener listener) {
        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                listener.onBufferingUpdate(JiaoyangMediaPlayerWrapper.this, percent);
            }
        });
    }

    @Override
    public void setOnPlaybackBufferingUpdateListener(final OnPlaybackBufferingUpdateListener listener) {
//        mMediaPlayer.setOnPlaybackBufferingUpdateListener(new MediaPlayer.OnPlaybackBufferingUpdateListener() {
//
//            @Override
//            public void onPlaybackBufferingUpdate(MediaPlayer mp, int percent) {
//                listener.onPlaybackBufferingUpdate(JiaoyangMediaPlayerWrapper.this, percent);
//            }
//        });
    }

    @Override
    public void setOnSeekCompleteListener(final OnSeekCompleteListener listener) {
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(MediaPlayer mp) {
                listener.onSeekComplete(JiaoyangMediaPlayerWrapper.this);
            }
        });
    }

    @Override
    public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener listener) {
        mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                listener.onVideoSizeChanged(JiaoyangMediaPlayerWrapper.this, width, height);
            }
        });
    }

    @Override
    public void setOnErrorListener(final OnErrorListener listener) {
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return listener.onError(JiaoyangMediaPlayerWrapper.this, what, extra);
            }
        });
    }

    @Override
    public void setOnInfoListener(final OnInfoListener listener) {
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return listener.onInfo(JiaoyangMediaPlayerWrapper.this, what, extra);
            }
        });
    }
}
