/*
 * Copyright (C) 2006 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.jiaoyang.base.widget;

import java.io.IOException;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.jiaoyang.base.media.IMediaPlayer;
import com.jiaoyang.base.media.IMediaPlayer.OnCompletionListener;
import com.jiaoyang.base.media.IMediaPlayer.OnErrorListener;
import com.jiaoyang.base.media.IMediaPlayer.OnInfoListener;
import com.jiaoyang.base.media.IMediaPlayer.OnPlaybackBufferingUpdateListener;
import com.jiaoyang.base.media.IMediaPlayer.OnPreparedListener;
import com.jiaoyang.base.media.JiaoyangMediaPlayerWrapper;
import com.jiaoyang.base.media.SystemMediaPlayerWrapper;
import com.jiaoyang.base.widget.MediaController.MediaPlayerControl;
import com.jiaoyang.video.tv.R;

/**
 * Displays a video file. The VideoView class can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class VideoView extends SurfaceView implements MediaPlayerControl {
    private String TAG = "VideoView";
    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;
    private int mDuration;

    // all possible internal states
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private IMediaPlayer mMediaPlayer = null;
    private Class<? extends IMediaPlayer> mMediaPlayerImplClass;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private MediaController mMediaController;
    private OnCompletionListener mOnCompletionListener;
    private OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private int mCurrentPlaybackBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private SurfaceListener mSurfaceListener;
    private int mSeekWhenPrepared; // recording the seek position while preparing
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    
    private final boolean mDebugLog = false;

    public int getCurrentState() {
        return mCurrentState;
    }

    public int getTargetState() {
        return mTargetState;
    }

    public VideoView(Context context) {
        super(context);
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 优先选择的播放器实现架构：自定义、系统
        setMediaPlayerImpl(SystemMediaPlayerWrapper.class);
        initVideoView();
    }

    public void setMediaPlayerImpl(Class<? extends IMediaPlayer> implClass) {
        mMediaPlayerImplClass = implClass;
    }

    public Class<? extends IMediaPlayer> getMediaPlayerImpl() {
        return mMediaPlayerImplClass;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i("@@@@", "onMeasure");
//        updateDisplayRatio();
        
        int width = 0;
        int height = 0;
        int ratioVideoWidth = 0;
        int ratioVideoHeight = 0;
        // 计算画面比例后的视频显示大小
        if(mVideoWidth > 0 && mVideoHeight > 0){
            // 自动
            if(mDisplayRatio == DISPLAY_RATIO_AUTO){
                // 视频比例(符合全屏)
                //  16 7   2.28
                //  16 8   2
                //  16 9   1.7778
                //  16 10  1.6
                //  3 2    1.5
                //  4 3    1.333
                float ratio = (float) mVideoWidth / mVideoHeight;
                if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio (原视频比例) video " + ratio);
                if(ratio < 1.6){
                    if(mVideoExactWidth > 0 && mVideoExactHeight > 0){
                        if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 自动(全屏) video " + mVideoWidth + "*" + mVideoHeight);
                        width = mVideoExactWidth;
                        height = mVideoExactHeight;
                        if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 自动(全屏) ratio " + width + "*" + height);
                    }else{
                        if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 自动(全屏error) video " + mVideoWidth + "*" + mVideoHeight);
                        ratioVideoWidth = mVideoWidth;
                        ratioVideoHeight = mVideoHeight;
                        if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 自动(全屏error) ratio " + ratioVideoWidth + "*" + ratioVideoHeight);
                    }
                }
                // 视频比例(符合原比例)
                else{
                    if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 自动(原比例) video " + mVideoWidth + "*" + mVideoHeight);
                    ratioVideoWidth = mVideoWidth;
                    ratioVideoHeight = mVideoHeight;
                    if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 自动(原比例) ratio " + ratioVideoWidth + "*" + ratioVideoHeight);
                }
            }
            // 原比例
            else if(mDisplayRatio == DISPLAY_RATIO_ORIGIN){
                if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 原比例 video " + mVideoWidth + "*" + mVideoHeight);
                ratioVideoWidth = mVideoWidth;
                ratioVideoHeight = mVideoHeight;
                if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 原比例 ratio " + ratioVideoWidth + "*" + ratioVideoHeight);
            }
            // 4:3
            else if(mDisplayRatio == DISPLAY_RATIO_4TO3){
                if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 4:3 video " + mVideoWidth + "*" + mVideoHeight);
                ratioVideoWidth = mVideoWidth;
                ratioVideoHeight = mVideoWidth * 3 / 4;
                if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 4:3 ratio " + ratioVideoWidth + "*" + ratioVideoHeight);
            }
            // 16:9
            else if(mDisplayRatio == DISPLAY_RATIO_16TO9){
                if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 16:9 video " + mVideoWidth + "*" + mVideoHeight);
                ratioVideoWidth = mVideoWidth;
                ratioVideoHeight = mVideoWidth * 9 / 16;
                if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 16:9 ratio " + ratioVideoWidth + "*" + ratioVideoHeight);
            }
            // 全屏
            else if(mDisplayRatio == DISPLAY_RATIO_FULLSCREEN){
                if(mVideoExactWidth > 0 && mVideoExactHeight > 0){
                    if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 全屏 video " + mVideoWidth + "*" + mVideoHeight);
                    width = mVideoExactWidth;
                    height = mVideoExactHeight;
                    if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio 全屏 ratio " + width + "*" + height);
                }
            }
        }
        
        if(width == 0 || height == 0){
            width = getDefaultSize(ratioVideoWidth, widthMeasureSpec);
            height = getDefaultSize(ratioVideoHeight, heightMeasureSpec);
            if (ratioVideoWidth > 0 && ratioVideoHeight > 0) {
                if (ratioVideoWidth * height > width * ratioVideoHeight) {
                    height = width * ratioVideoHeight / ratioVideoWidth;
                } else if (ratioVideoWidth * height < width * ratioVideoHeight) {
                    width = height * ratioVideoWidth / ratioVideoHeight;
                } else {
                }
            }
        }
        
        if(mDebugLog) Log.i("luke", "VideoView updateDisplayRatio width*height= " + width + "*" + height);
        setMeasuredDimension(width, height);
        
//        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
//        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
//        if (mVideoWidth > 0 && mVideoHeight > 0) {
//            if (mVideoWidth * height > width * mVideoHeight) {
//                // Log.i("@@@", "image too tall, correcting");
//                height = width * mVideoHeight / mVideoWidth;
//            } else if (mVideoWidth * height < width * mVideoHeight) {
//                // Log.i("@@@", "image too wide, correcting");
//                width = height * mVideoWidth / mVideoHeight;
//            } else {
//                // Log.i("@@@", "aspect ratio is correct: " +
//                // width+"/"+height+"="+
//                // mVideoWidth+"/"+mVideoHeight);
//            }
//        }
//        Log.i("VideoView", "onMeasure VideoW,VideoH,MeasW,MeasH" +mVideoWidth+"-"+mVideoHeight +">>"+width+"-"+height);
        // Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
        // 全屏
//        if(mDisplayRatio == 3){
//            if(mVideoExactWidth > 0 && mVideoExactHeight > 0){
//                width = mVideoExactWidth;
//                height = mVideoExactHeight;
//            }
//        }
//        Log.i("luke", "VideoView onMeasure w*h = " + width + "*" + height);
//        setMeasuredDimension(width, height);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            /*
             * Parent says we can be as big as we want. Just don't be larger than max size imposed on ourselves.
             */
            result = desiredSize;
            break;

        case MeasureSpec.AT_MOST:
            /*
             * Parent says we can be as big as we want, up to specSize. Don't be larger than specSize, and don't be
             * larger than the max size imposed on ourselves.
             */
            result = Math.min(desiredSize, specSize);
            break;

        case MeasureSpec.EXACTLY:
            // No choice. Do what we are told.
            result = specSize;
            break;
        }
        return result;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);

//        getHolder().setFormat(PixelFormat.RGBA_8888);
        // 取消设置SurfaceHolder类型以修正在2.3下无画面输出的问题
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    @Override
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * @hide
     */
    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        getContext().sendBroadcast(i);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try {
            mMediaPlayer = mMediaPlayerImplClass.newInstance();
            mMediaPlayer.init(getContext());
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnPlaybackBufferingUpdateListener(mPlaybackBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mCurrentPlaybackBufferPercentage = 0;
            if(mDebugLog) Log.i("luke", "VideoView mediaplayer setDataSource = " + mUri);
            mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
            mMediaController.onMediaPlayerChange(mMediaPlayer);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
//                    Log.i("VideoView", "onVideoSizeChanged VideoW,VideoH" + mVideoWidth+"-"+mVideoHeight);
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                // Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
//                Log.i("VideoView", "onPrepared1 VideoW,VideoH,SurW,SurH" + mVideoWidth+"-"+mVideoHeight+">>"+ mSurfaceWidth+"-"+mSurfaceHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
//                Log.i("VideoView", "onPrepared2 VideoW,VideoH,SurW,SurH" + mVideoWidth+"-"+mVideoHeight+">>"+ mSurfaceWidth+"-"+mSurfaceHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mMediaController != null) {
                            // mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null) {
                            // Show the media controls when we're paused into a video and make 'em stick.
                            mMediaController.show(0);
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
            
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    /*
                     * Otherwise, pop up an error dialog so the user knows that something bad has happened. Only try and
                     * pop up the dialog if we're attached to a window. When we're going away and no longer have a
                     * window, don't bother showing the user an error.
                     */
                    if (getWindowToken() != null) {
                        int messageId;

                        if (framework_err == IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                            messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                        } else {
                            messageId = R.string.VideoView_error_text_unknown;
                        }

                        new AlertDialog.Builder(getContext())
                                .setMessage(messageId)
                                .setPositiveButton(R.string.VideoView_error_button,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                /*
                                                 * If we get here, there is no onError listener, so at least inform them
                                                 * that the video is over.
                                                 */
                                                if (mOnCompletionListener != null) {
                                                    mOnCompletionListener.onCompletion(mMediaPlayer);
                                                }
                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }
                    return true;
                }
            };
            
    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                    if(mOnInfoListener != null)
                        mOnInfoListener.onInfo(mp, what, extra);
                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    private IMediaPlayer.OnPlaybackBufferingUpdateListener mPlaybackBufferingUpdateListener =
            new IMediaPlayer.OnPlaybackBufferingUpdateListener() {
                public void onPlaybackBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentPlaybackBufferPercentage = percent;
                    if (mOnBufferingUdateListener != null) {
                        mOnBufferingUdateListener.onPlaybackBufferingUpdate(mp, percent);
                    }
                }
            };
    // =================
    // 通过回调的方式返回播放缓冲的值，也可以去论询getPlaybackBufferPercentage接口
    // =================
    private OnPlaybackBufferingUpdateListener mOnBufferingUdateListener;

    @Override
    public void setOnPlaybackBufferingUpdateListener(OnPlaybackBufferingUpdateListener listener) {
        this.mOnBufferingUdateListener = listener;
    }

    /**
     * Register a callback to be invoked when the media file is loaded and ready to go.
     * 
     * @param l
     *            The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been reached during playback.
     * 
     * @param l
     *            The callback that will be run
     */
    @Override
    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or setup. If no listener is specified, or
     * if the listener returned false, VideoView will inform the user of any errors.
     * 
     * @param l
     *            The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event occurs during playback or setup.
     * 
     * @param l
     *            The callback that will be run
     */
    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
//        if (mMediaPlayer != null) {
//            mMediaPlayer.setOnInfoListener(mOnInfoListener);
//        }
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder holder, int format,
                int w, int h)
        {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
//                Log.i("VideoView", "surfaceChanged success");
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder)
        {
            mSurfaceHolder = holder;
            if (mMediaPlayer == null) {
                openVideo();
            }
            
            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceCreated();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            
            if (mMediaController != null)
                mMediaController.hide();
            
            release(true);

            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceDestroyed();
            }
        }
    };

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            // toggleMediaControlsVisiblity();
            mMediaController.onTouchEvent(ev);
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        boolean dispatchByProxy = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;

        if (isPreparedTAllowKeyAction() && dispatchByProxy && mMediaController != null) {
            return mMediaController.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void destroy() {
        release(true);
    }

    public void resume() {
        openVideo();
    }

    @Override
    // cache duration as mDuration for faster access
    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public int getPlaybackBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentPlaybackBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    private boolean isPreparedTAllowKeyAction() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    public interface ScreenChangeListener {
        void onScreenChangeComplete(int resId);
    }

    @Override
    public void setVideoScalingMode(ScreenChangeListener listener) {
    }

    @Override
    public void stop() {
        stopPlayback();
    }

    @Override
    public void setSurfaceListener(SurfaceListener listener) {
        mSurfaceListener = listener;
    }

    public interface SurfaceListener {
        public void onSurfaceDestroyed();

        public void onSurfaceCreated();
    }

    @Override
    public void setMediaplayerPreparedListener(OnPreparedListener listener) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnPreparedListener(listener);
        }
    }

    @Override
    public int getAudioSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public boolean isSystemMediaPlayer() {
        if(mMediaPlayer != null && (mMediaPlayer instanceof SystemMediaPlayerWrapper)){
            return true;
        }
        return false;
    }
    
    public boolean isCustomMediaPlayer() {
        if(mMediaPlayer != null && (mMediaPlayer instanceof JiaoyangMediaPlayerWrapper)){
            return true;
        }
        return false;
    }
    
    /**
     * 
     * 设置视频播放画面比列模式
     * @Title: setDisplayRatio
     * @param displayRatio
     * @param exactDisplayWidth
     * @param exactDisplayHeight
     * @return void
     * @date 2014-1-12 下午2:58:40
     */
    public void setDisplayRatio(int displayRatio, int exactDisplayWidth, int exactDisplayHeight){
        setDisplayRatio(displayRatio, exactDisplayWidth, exactDisplayHeight, false);
    }
    
    public void setDisplayRatio(int displayRatio, int exactDisplayWidth, int exactDisplayHeight, boolean needRefresh){
        this.mDisplayRatio = displayRatio;
        this.mVideoExactWidth = exactDisplayWidth;
        this.mVideoExactHeight = exactDisplayHeight;
        if(needRefresh){
            requestLayout();
        }
    }
    
    // 画面显示比例
    // -1>自动
    // 0>原比例
    // 1>4:3
    // 2>16:9
    // 3>全屏
    private static final int DISPLAY_RATIO_AUTO = 0;
    private static final int DISPLAY_RATIO_ORIGIN = 1;
    private static final int DISPLAY_RATIO_4TO3 = 2;
    private static final int DISPLAY_RATIO_16TO9 = 3;
    private static final int DISPLAY_RATIO_FULLSCREEN = 4;
    
    private int mDisplayRatio = DISPLAY_RATIO_ORIGIN;
    private int mVideoExactWidth = 0;
    private int mVideoExactHeight = 0;

}