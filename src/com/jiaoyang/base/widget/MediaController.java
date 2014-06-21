package com.jiaoyang.base.widget;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jiaoyang.base.media.IMediaPlayer;
import com.jiaoyang.base.media.IMediaPlayer.OnCompletionListener;
import com.jiaoyang.base.media.IMediaPlayer.OnInfoListener;
import com.jiaoyang.base.media.IMediaPlayer.OnPlaybackBufferingUpdateListener;
import com.jiaoyang.base.media.IMediaPlayer.OnPreparedListener;
import com.jiaoyang.base.widget.VideoView.ScreenChangeListener;
import com.jiaoyang.base.widget.VideoView.SurfaceListener;
import com.jiaoyang.video.tv.R;
import com.jiaoyang.tv.util.Logger;

/**
 * A view containing controls for a MediaPlayer. Typically contains the buttons like "Play/Pause", "Rewind",
 * "Fast Forward" and a progress slider. It takes care of synchronizing the controls with the state of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programatically. The MediaController will create a default set of
 * controls and put them in a window floating above your application. Specifically, the controls will float above the
 * view specified with setAnchorView(). The window will disappear if left idle for three seconds and reappear when the
 * user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController is created in an xml layout.
 * 
 * MediaController will hide and show the buttons according to these rules:
 * <ul>
 * <li>The "previous" and "next" buttons are hidden until setPrevNextListeners() has been called
 * <li>The "previous" and "next" buttons are visible but disabled if setPrevNextListeners() was called with null
 * listeners
 * <li>The "rewind" and "fastforward" buttons are shown unless requested otherwise by using the MediaController(Context,
 * boolean) constructor with the boolean set to false
 * </ul>
 */
public class MediaController extends FrameLayout {
    @SuppressWarnings("unused")
    private Logger LOG = Logger.getLogger(getClass());
    private MediaPlayerControl mPlayController;
    private Context mContext;
    private View mAnchor;
    private View mRoot;
    private WindowManager mWindowManager;
    private Window mWindow;
    private View mDecor;
    private WindowManager.LayoutParams mDecorLayoutParams;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing;
    private boolean mDragging;
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mUseFastForward;
    private boolean mFromXml;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private ImageButton mPauseButton;
    private ImageButton mFfwdButton;
    private ImageButton mRewButton;
    private int mLayoutRes = R.layout.media_controller_layout;

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
    }

    public MediaController(Context context, int layoutRes) {
        this(context, true);
        mLayoutRes = layoutRes;
        LayoutInflater inflate = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(mLayoutRes, null);
        initControllerView(mRoot);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public MediaController(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;
        initFloatingWindowLayout();
        initFloatingWindow();
    }

    public MediaController(Context context) {
        this(context, true);
    }

    private Window invokeMakeNewWindow() {
        Window window = null;
        final String className = "com.android.internal.policy.PolicyManager";
        final String methodName = "makeNewWindow";
        Method method = null;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
            method = clazz.getDeclaredMethod(methodName, Context.class);
            window = (Window) method.invoke(null, mContext);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return window;
    }

    private void initFloatingWindow() {
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        // 通过反射调用PolicyManager.makeNewWindow(context)
        mWindow = invokeMakeNewWindow();
        mWindow.setWindowManager(mWindowManager, null, null);
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        mDecor = mWindow.getDecorView();
        mDecor.setOnTouchListener(mTouchListener);
        mWindow.setContentView(this);
        mWindow.setBackgroundDrawableResource(android.R.color.transparent);

        // While the media controller is up, the volume control keys should
        // affect the media stream type
        mWindow.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        requestFocus();
    }

    // Allocate and initialize the static parts of mDecorLayoutParams. Must
    // also call updateFloatingWindowLayout() to fill in the dynamic parts
    // (y and width) before mDecorLayoutParams can be used.
    private void initFloatingWindowLayout() {
        mDecorLayoutParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.gravity = Gravity.TOP;
        p.height = LayoutParams.MATCH_PARENT;
        p.x = 0;
        p.format = PixelFormat.TRANSLUCENT;
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
        p.token = null;
        p.windowAnimations = 0;// android.R.style.Animation_Dialog;
    }

    // Update the dynamic parts of mDecorLayoutParams
    // Must be called with mAnchor != NULL.
    private void updateFloatingWindowLayout() {
        int[] anchorPos = new int[2];
        mAnchor.getLocationOnScreen(anchorPos);

        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.width = mAnchor.getWidth();
        p.y = anchorPos[1] + mAnchor.getHeight() - p.height;
        if (mShowing) {
            mWindowManager.updateViewLayout(mDecor, mDecorLayoutParams);
        }
    }

    // 系统的处理，为了兼容低版本所以在ontouch中自己处理
    // This is called whenever mAnchor's layout bound changes
    // private OnLayoutChangeListener mLayoutChangeListener =
    // new OnLayoutChangeListener() {
    // public void onLayoutChange(View v, int left, int top, int right,
    // int bottom, int oldLeft, int oldTop, int oldRight,
    // int oldBottom) {
    // updateFloatingWindowLayout();
    // if (mShowing) {
    // mWindowManager.updateViewLayout(mDecor, mDecorLayoutParams);
    // }
    // }
    // };

    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                }
            }
            return false;
        }
    };

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayController = player;
        updatePausePlay();
    }

    /**
     * Set the view that acts as the anchor for the control view. This can for example be a VideoView, or your
     * Activity's main view.
     * 
     * @param view
     *            The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {
        // 系统的处理，待需求稳定之后修改删除
        // if (mAnchor != null) {
        // mAnchor.removeOnLayoutChangeListener(mLayoutChangeListener);
        // }
        mAnchor = view;
        // if (mAnchor != null) {
        // mAnchor.addOnLayoutChangeListener(mLayoutChangeListener);
        // }
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        removeAllViews();
        // 由于需要在播放页做电量的广播接收，需要将控件的初始化提前，所有在构造方法中去调用makeControllerView();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback. Derived classes can override this to create their
     * own.
     * 
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        // LayoutInflater inflate = (LayoutInflater) mContext
        // .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // mRoot = inflate.inflate(mLayoutRes, null);
        //
        // initControllerView(mRoot);
        //
        // return mRoot;
        return mRoot;
    }

    @SuppressLint("WrongViewCast")
    protected void initControllerView(View root) {
        mPauseButton = (ImageButton) root
                .findViewById(R.id.mediacontroller_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFfwdButton = (ImageButton) root
                .findViewById(R.id.mediacontroller_ibtn_fwd_video);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE
                        : View.GONE);
            }
        }

        mRewButton = (ImageButton) root.findViewById(R.id.mediacontroller_ibtn_rew_video);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE
                        : View.GONE);
            }
        }

        // 改变系统的切集按钮隐藏行为
        // if (mNextButton != null && !mFromXml && !mListenersSet) {
        // mNextButton.setVisibility(View.GONE);
        // }
        // mPrevButton = (ImageButton) root.findViewById(R.id.mediacontroller_ibtn_prev_video);
        // if (mPrevButton != null && !mFromXml && !mListenersSet) {
        // mPrevButton.setVisibility(View.GONE);
        // }

        mProgress = (ProgressBar) root.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }
//        mProgress.setEnabled(false);

        mEndTime = (TextView) root
                .findViewById(R.id.mediacontroller_time_total);
        mCurrentTime = (TextView) root
                .findViewById(R.id.mediacontroller_time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * Show the controller on screen. It will go away automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked. This requires the control interface to be
     * a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayController.canPause()) {
                mPauseButton.setEnabled(false);
            }
            // 系统的处理待需求确定之后再修改删除
            if (mRewButton != null && !mPlayController.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayController.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't
            // disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away automatically after 'timeout' milliseconds of inactivity.
     * 
     * @param timeout
     *            The timeout in milliseconds. Use 0 to show the controller until hide() is called.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        // TODO Auto-generated method stub
        super.onLayout(changed, left, top, right, bottom);
        if (changed)
            updateFloatingWindowLayout();
    }

    protected void onPreShow() {

    }

    public void show(int timeout) {
        onPreShow();

        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            mWindowManager.addView(mDecor, mDecorLayoutParams);
            mShowing = true;
        }
        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true. This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        fadeOut(timeout);
    }

    protected void fadeOut(int timeout) {
        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    protected void fadeOut() {
        fadeOut(sDefaultTimeout);
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null)
            return;

        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                mWindowManager.removeView(mDecor);
            } catch (IllegalArgumentException ex) {
                // Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }

    public void onMediaPlayerChange(IMediaPlayer mediaPlayer) {
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
            case FADE_OUT:
                hide();
                break;
            case SHOW_PROGRESS:
                
                // 如果是正在快进快退则不执行定时更新播放时间操作，交予快进快退逻辑处理
                if(isFastforwardOrRewinding){
                    return;
                }
                
                pos = setProgress();
//                if (!mDragging && mShowing && mPlayController.isPlaying()) {
//                    msg = obtainMessage(SHOW_PROGRESS);
//                    sendMessageDelayed(msg, 1000 - (pos % 1000));
//                }
                if (!mDragging && mShowing) {
                    msg = obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg, 1000 - (pos % 1000));
                }
                break;
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayController == null || mDragging) {
            return 0;
        }
        int position = mPlayController.getCurrentPosition();
        int duration = mPlayController.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayController.getBufferPercentage();
//            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));
        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);

        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;

        // 播放和暂停按键
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;

            // 播放和暂停按键
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayController.isPlaying()) {
                mPlayController.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;

            // 播放和暂停按键
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayController.isPlaying()) {
                mPlayController.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;

            // 音量加-按键
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            if (keyCode == KeyEvent.KEYCODE_BACK && isBackKeyEventControllerHandled(event)) {
                if (uniqueDown) {
                    hide();
                }
                return true;
            } else {
                return false;
            }
        }
        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    protected boolean isBackKeyEventControllerHandled(KeyEvent event) {
        return true;
    }

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if (mPlayController.isPlaying()) {
            if (isDLNADeviceSelected()) {
                mPauseButton.setImageResource(R.drawable.ic_media_dlna_pause);
            } else {
                mPauseButton.setImageResource(R.drawable.ic_media_pause);
            }
        } else {
            if (isDLNADeviceSelected()) {
                mPauseButton.setImageResource(R.drawable.ic_media_dlna_play);
            } else {
                mPauseButton.setImageResource(R.drawable.ic_media_play);
            }
        }
    }

    protected void initDLNAViews() {
        mCurrentTime.setTextColor(Color.parseColor("#9FABB3"));
        mEndTime.setTextColor(Color.parseColor("#9FABB3"));
        if (mProgress instanceof SeekBar) {
            ((SeekBar) mProgress)
                    .setProgressDrawable(getResources().getDrawable(R.drawable.player_dlna_seek_bar_style));
            ((SeekBar) mProgress).setThumb(getResources().getDrawable(R.drawable.player_dlna_seek_bar_selector));
        }
    }

    protected boolean isDLNADeviceSelected() {
        return false;
    }

    private void doPauseResume() {
        if (mPlayController.isPlaying()) {
            mPlayController.pause();
        } else {
            mPlayController.start();
        }
        updatePausePlay();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by
    // onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the
    // dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch
    // notifications,
    // we will simply apply the updated position without suspending regular
    // updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress,
                boolean fromuser) {
            
            // 如果是由快进快退导致的进度改变，则需要更新
            if(!isFastforwardOrRewinding){
                if (!fromuser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }
            }
            
            if(mPlayController != null){
                long duration = mPlayController.getDuration();
                long newposition = (duration * progress) / 1000L;
                // 在用户拖动进度条的过程中不进行seek操作，将seek操作放到停止拖动进度条的时候
                // mPlayer.seekTo((int) newposition);
                if (mCurrentTime != null)
                    mCurrentTime.setText(stringForTime((int) newposition));
            }

        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;

            final long position = bar.getProgress();
            final long duration = mPlayController.getDuration();
            // 用长整型来存储数据，避免在乘法运算的时候溢出
            final long newPosition = (duration * position) / 1000L;
            mPlayController.seekTo((int) newPosition);

            setProgress();
            updatePausePlay();
//            show(sDefaultTimeout);
            onStopTrackingTouchCallback();

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private View.OnClickListener mRewListener = new View.OnClickListener() {
        public void onClick(View v) {
            int pos = mPlayController.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayController.seekTo(pos);
            setProgress();
            show(sDefaultTimeout);
        }
    };

    protected void doFfw(int state) {
        long pos = mPlayController.getCurrentPosition();
        if (state == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
            pos += 15000;
        } else {
            pos -= 5000;
        }

        mPlayController.seekTo((int) pos);
        setProgress();
        show(sDefaultTimeout);
    }

    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View v) {
            int pos = mPlayController.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayController.seekTo(pos);
            setProgress();
            show(sDefaultTimeout);
        }
    };

    public interface MediaPlayerControl extends android.widget.MediaController.MediaPlayerControl {
        void setVideoScalingMode(ScreenChangeListener listener);

        void setVideoPath(String path);

        void stop();

        void setOnCompletionListener(OnCompletionListener l);

        void setSurfaceListener(SurfaceListener listener);

        void setOnPlaybackBufferingUpdateListener(OnPlaybackBufferingUpdateListener listener);

        void setMediaplayerPreparedListener(OnPreparedListener listener);

        void setOnInfoListener(OnInfoListener listener);

        @Deprecated
        int getPlaybackBufferPercentage();
    }
    
    private volatile boolean isFastforwardOrRewinding = false;
    public void setFastforwardOrRewinding(boolean b){
        isFastforwardOrRewinding = b;
    }
    
    public ProgressBar getProgressBar(){
        return mProgress;
    }
    
    public void initProgressBar(){
        
        if(mPlayController == null || mProgress == null)
            return;
        
        int position = mPlayController.getCurrentPosition();
        int duration = mPlayController.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                int progress = (int) (((float)position / duration) * mProgress.getMax());
                mProgress.setProgress(progress);
            }
        }
    }
    
    protected void onStopTrackingTouchCallback() {
    }
    
}
