package com.jiaoyang.tv.player;

import java.io.File;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaoyang.base.caching.ImageFetcher;
import com.jiaoyang.base.caching.OnLoadImageListener;
import com.jiaoyang.base.media.IMediaPlayer;
import com.jiaoyang.base.media.IMediaPlayer.OnErrorListener;
import com.jiaoyang.base.media.IMediaPlayer.OnPlaybackBufferingUpdateListener;
import com.jiaoyang.base.media.IMediaPlayer.OnPreparedListener;
import com.jiaoyang.base.media.JiaoyangMediaPlayerWrapper;
import com.jiaoyang.base.media.SystemMediaPlayerWrapper;
import com.jiaoyang.base.misc.JiaoyangConstants.IntentDataKey;
import com.jiaoyang.base.misc.JiaoyangConstants.PlayMode;
import com.jiaoyang.base.util.StringEx;
import com.jiaoyang.base.util.WakeLocker;
import com.jiaoyang.base.widget.VideoView;
import com.jiaoyang.tv.app.JiaoyangTvApplication;
import com.jiaoyang.tv.player.widget.AdvancedMediaController;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.tv.util.ScreenUtil;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.video.tv.R;
import com.kankan.mediaserver.MediaServerProxy;

public class PlayerActivity extends Activity {
    private static final Logger LOG = Logger.getLogger(PlayerActivity.class);

    public static final String VOD_FILTER = "pubnet.sandai.net";
    private Timer mTimer;
    private ViewGroup mRootView;
    private LinearLayout mRlFirstBuffering;
    private TextView mBufferingTips;
    private TextView mTvFirstBufferingProgress;
    private VideoView mVideoView;
    private AdvancedMediaController mediaController;
    private ImageView mWaterMark;

    private RelativeLayout mPlayerErrorTipsLayout;
    private LinearLayout mPlayerErrorTipsGobackLayout;
    private Button mPlayerErrorTipsReloadBtn;
    private TextView mPlayerErrorTipsContentTx;
    private IVideoPlayList mVideoPlayList;
    private boolean isFirstBuffering;
    private OnAudioFocusChangeListener mAudioFocusChangeListener;
    private boolean mPlayingBeforePaused;
    private boolean mPlayListUsable;

    private int wiState;
    private AlertDialog mExitAlertDialog;

    private boolean mCompleted = false;

    public ViewGroup getContentView() {
        return mRootView;
    }

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        mVideoPlayList = VideoPlayListFactory.createPlayList(getIntent());
        if (mVideoPlayList != null) {
            mPlayListUsable = true;
            mRootView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.player, null);
            setContentView(mRootView);
            initViews();
            initListeners();
            doPlayVideo();

            mPlayingBeforePaused = false;

            registerReceiver(mHeadsetPlugReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            mediaController.registerReceiver();
        } else {
            mPlayListUsable = false;
            quit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestAudioFocus();
        WakeLocker.acquire(this);
        if (isOnlinePlayback()) {
            registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        if (!isDLNAMediaPlayer()) {
            // 解决home键后切换黑屏的问题，显示首缓冲更加的友好
            if (mVideoView.getCurrentState() == VideoView.STATE_IDLE
                    && mVideoView.getTargetState() == VideoView.STATE_IDLE) {
                onStartFirstBuffering();
            }
            if (mPlayingBeforePaused) {
                // mVideoView.start();
                mPlayingBeforePaused = false;
            }
            mVideoView.start();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mPlayingBeforePaused = (mVideoView.getDuration() == -1) || mVideoView.isPlaying();

        if (!isDLNAMediaPlayer()) {
            mVideoView.pause();
        }

        if (isOnlinePlayback()) {
            unregisterReceiver(mConnectivityReceiver);
        }

        WakeLocker.release();
        abandonAudioFocus();
        stopTimer();
    }

    private void stopTimer() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPlayListUsable) {
            mediaController.unRegisterReceiver();
            unregisterReceiver(mHeadsetPlugReceiver);

            mPlayingBeforePaused = false;
            mVideoView.setMediaplayerPreparedListener(null);
        }

    }

    private String prepareVideoPath(IVideoItem playItem) {
        String filePath = null;

        if (playItem != null) {
            int profile = PreferenceManager.instance(getApplicationContext()).retrivePlayProfilePreference();
            LOG.i(Logger.TAG_LUKE, "PreferenceManager profile = " + profile);
            if (profile > 0) {
                // 屏蔽超清
                // if(profile == PlayProfile.SUPER_PROFILE) profile--;
                playItem.setProfile(profile);
            }
            filePath = playItem.getDefaultPlayUrl();

            if (filePath != null) {
                if (filePath.contains(VOD_FILTER)) {
                    filePath = MediaServerProxy.instance().getPlayURI(filePath).toString();
                } else if (filePath.startsWith("/")) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        if (filePath.toLowerCase(Locale.US).endsWith(".xv")) {
                            filePath = MediaServerProxy.instance().getPlayURI(file).toString();
                        } else {
                            filePath = Uri.encode(filePath, "/");
                        }
                    } else if (filePath.startsWith("/.movies/")) {
                        filePath = MediaServerProxy.instance().getPlayURI(file).toString();
                    } else {
                        filePath = null;
                    }
                }
            }
        }
        LOG.info("videoPath={}", filePath);

        return filePath;
    }

    /**
     * 初始化Views成员变量
     */
    private void initViews() {

        mPlayerErrorTipsLayout = (RelativeLayout) findViewById(R.id.player_error_tips_layout);
        mPlayerErrorTipsGobackLayout = (LinearLayout) findViewById(R.id.player_error_tips_goback_layout);
        mPlayerErrorTipsReloadBtn = (Button) findViewById(R.id.player_error_tips_reload_btn);
        mPlayerErrorTipsContentTx = (TextView) findViewById(R.id.player_error_tips_tv);
        SpannableString errorContent = new SpannableString(mPlayerErrorTipsContentTx.getText());
        errorContent.setSpan(new ForegroundColorSpan(0xFF33b5e5), 12, 16, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mPlayerErrorTipsContentTx.setText(errorContent);

        mBufferingTips = (TextView) findViewById(R.id.player_tips);
        // mTvVideoName = (TextView) findViewById(R.id.player_video_tv_title);
        mTvFirstBufferingProgress = (TextView) findViewById(R.id.player_tv_progress);
        mRlFirstBuffering = (LinearLayout) findViewById(R.id.player_video_rl_first_bufferring);
        loadBufferingBackground(mRlFirstBuffering);
        mVideoView = (VideoView) findViewById(R.id.player_video_view);
        mediaController = new AdvancedMediaController(this, mVideoPlayList);
        mediaController.setOnBufferingListener(mOnPlaybackBufferingUpdateListener);
        mVideoView.setMediaController(mediaController);
        mVideoView.setOnErrorListener(mErrorListener);
        if (Build.VERSION.SDK_INT >= 14) {
            fullVideoView();
        }

        mWaterMark = (ImageView) findViewById(R.id.player_watermark);
    }

    private void loadBufferingBackground(final View bufferingLayout) {
        Drawable d = ((JiaoyangTvApplication) getApplication()).getActivityBG();
        if (d != null) {
            bufferingLayout.setBackgroundDrawable(d);
        } else {
            String url = PreferenceManager.instance(this).retriveDefaultBgUrl();
            if (StringEx.isNullOrEmpty(url)) {
                return;
            }
            ImageFetcher fetcher = ImageFetcher.getInstance(this);
            fetcher.setImageCache(((JiaoyangTvApplication) getApplication()).getImageCache());
            fetcher.setImageLoadListener(new OnLoadImageListener() {

                @Override
                public void onLoadCompleted(Bitmap bmp) {
                    bufferingLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), bmp));
                }
            });
            fetcher.loadImage(url, true);
        }
    }

    /**
     * 返回是否为首缓冲
     * 
     * @return
     */
    public boolean isFirstPlayBuffering() {
        return isFirstBuffering;
    }

    private boolean isDLNAMediaPlayer() {
        boolean isDLNAPlayer = false;

        return isDLNAPlayer;

    }

    public void onStartFirstBuffering() {
        String tips = getString(R.string.player_onloading);
        onBufferingTips(tips);

    }

    private void onBufferingTips(String tips) {
        if (mVideoPlayList != null) {
            final IVideoItem videoPlayItem = mVideoPlayList.getCurrentPlayItem();
            if (videoPlayItem != null) {
                isFirstBuffering = true;

                mRlFirstBuffering.setVisibility(View.VISIBLE);

                // setVideoTitle(videoPlayItem);

                mBufferingTips.setText(tips);

                mWaterMark.setVisibility(View.GONE);
            }
        }
    }

    public void onChangeVideoQuality() {
        String tips = getString(R.string.player_onChange_quality);
        // String tips = getString(R.string.player_onloading);
        onBufferingTips(tips);

    }

    private void initListeners() {
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mPlayerErrorTipsReloadBtn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        mPlayerErrorTipsLayout.setVisibility(View.GONE);
                        doReplayVideo();
                        break;
                    // case KeyEvent.KEYCODE_DPAD_UP:
                    // case KeyEvent.KEYCODE_DPAD_DOWN:
                    // case KeyEvent.KEYCODE_DPAD_LEFT:
                    // case KeyEvent.KEYCODE_DPAD_RIGHT:
                    // mPlayerErrorTipsGobackLayout.requestFocus();
                    // break;
                    default:
                        break;
                    }
                    return true;
                }
                return false;
            }
        });
        // mPlayerErrorTipsGobackLayout.setOnKeyListener(new View.OnKeyListener() {
        //
        // @Override
        // public boolean onKey(View v, int keyCode, KeyEvent event) {
        // if(event.getAction() == KeyEvent.ACTION_DOWN){
        // switch (keyCode) {
        // case KeyEvent.KEYCODE_DPAD_CENTER:
        // finish();
        // break;
        // case KeyEvent.KEYCODE_DPAD_UP:
        // case KeyEvent.KEYCODE_DPAD_DOWN:
        // case KeyEvent.KEYCODE_DPAD_LEFT:
        // case KeyEvent.KEYCODE_DPAD_RIGHT:
        // mPlayerErrorTipsReloadBtn.requestFocus();
        // break;
        // default:
        // break;
        // }
        // return true;
        // }
        // return false;
        // }
        // });
        mPlayerErrorTipsReloadBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
        mPlayerErrorTipsGobackLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
        // mPlayerErrorTipsReloadBtn.requestFocus();

    }


    /**
     * 执行视频播放的操作
     */
    private void doPlayVideo() {
        if (mVideoPlayList != null) {
            IVideoItem playItem = mVideoPlayList.getCurrentPlayItem();
            if (playItem != null) {
                // setVideoTitle(playItem);
                if (mVideoView.getCurrentState() == VideoView.STATE_IDLE) {
                    String filePath = prepareVideoPath(playItem);
                    if (filePath != null) {
                        onStartFirstBuffering();
                        int decoderSetting = PreferenceManager.instance(PlayerActivity.this).retriveDecoderPreference();
                        // 1代表软解
                        if (decoderSetting == 1) {
                            mVideoView.setMediaPlayerImpl(JiaoyangMediaPlayerWrapper.class);
                        }
                        // 2代表硬解
                        else if (decoderSetting == 2) {
                            mVideoView.setMediaPlayerImpl(SystemMediaPlayerWrapper.class);
                        }
                        // 采取默认
                        else {
                            mVideoView.setMediaPlayerImpl(SystemMediaPlayerWrapper.class);
                        }
                        int displayRatio = PreferenceManager.instance(getApplicationContext())
                                .retriveProportionPreference();
                        LOG.i(Logger.TAG_LUKE, "PreferenceManager displayRatio = " + displayRatio);
                        updateDisplayRatio(displayRatio, false);
                        setCompletedState(false);
                        mVideoView.setVideoPath(filePath);
                        mVideoView.start();
                    } else {
                        Util.showToast(getApplicationContext(), "无法播放此视频", Toast.LENGTH_SHORT);
                        quit();
                    }
                }
            }
        }
    }

    private void switchMediaPlayer() {

        LOG.i(Logger.TAG_LUKE, "switchMediaPlayer");

        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
            mVideoView.stop();
        }

        if (mVideoPlayList != null) {
            IVideoItem playItem = mVideoPlayList.getCurrentPlayItem();
            if (playItem != null) {
                // setVideoTitle(playItem);
                if (mVideoView.getCurrentState() == VideoView.STATE_IDLE) {
                    String filePath = prepareVideoPath(playItem);
                    if (filePath != null) {
                        onStartFirstBuffering();
                        // 测试1080P地址
                        // filePath =
                        // "http://127.0.0.1:8080/http://pubnet.sandai.net:8080/6/6beacd65d681356d36f7336d089d1c7d2ae6eb62/403627cfe0d5a525e644f3adc64e1263b90d7d28/2d04688c/200000/0/24ba73/0/0/2d04688c/0/index=0-13856/indexmd5=52db2060d36ef0b675d0343a65a87e7b/b55e15d9a639d04a02d63f1bec05cc14/8b73edae34eb03f4c3e10406a5dac8e4/403627cfe0d5a525e644f3adc64e1263b90d7d28.flv";
                        mVideoView.setMediaPlayerImpl(JiaoyangMediaPlayerWrapper.class);
                        setCompletedState(false);
                        mVideoView.setVideoPath(filePath);
                        mVideoView.start();
                    } else {
                        Util.showToast(getApplicationContext(), "无法播放此视频", Toast.LENGTH_SHORT);
                        quit();
                    }
                }
            }
        }
    }

    private void requestAudioFocus() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

            @Override
            public void onAudioFocusChange(int focusChange) {
                LOG.debug("audio focus changed. focus={}", focusChange);
            }
        };
        // Request audio focus for playback
        int result = am.requestAudioFocus(
                mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            LOG.debug("request audio focus success.");
        } else {
            LOG.warn("request audio focus failed.");
        }
    }

    private void abandonAudioFocus() {
        if (mAudioFocusChangeListener != null) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = am.abandonAudioFocus(mAudioFocusChangeListener);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                LOG.debug("abandon audio focus success.");
            } else {
                LOG.debug("abandon audio focus failed.");
            }
            mAudioFocusChangeListener = null;
        }
    }

    private boolean isOnlinePlayback() {
        return getIntent().getIntExtra(IntentDataKey.PLAY_MODE, PlayMode.PLAY_MODE_IVALID) == PlayMode.PLAY_MODE_WEB;
    }

    private final OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer mp) {
            LOG.debug("onPrepared");

            setCompletedState(false);

            LOG.i(Logger.TAG_LUKE, "onPrepared IMediaPlayer type = " + "System>>" + mVideoView.isSystemMediaPlayer()
                    + "-" + "Custom>>" + mVideoView.isCustomMediaPlayer());

            isFirstBuffering = false;
            mRlFirstBuffering.setVisibility(View.GONE);
            startTimer();
            // 在视频准备就绪时,要根据视频进度初始化Progress进度,否则seek过程中异常
            mediaController.initProgressBar();


        }
    };

    public int mCurrentPos;


    protected void startTimer() {
        if (null == mTimer) {
            mTimer = new Timer();
            mTimer.schedule(new PlayTimerTask(), 0, 1000);
        }
    }

    private class PlayTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mVideoView == null) {
                return;
            }
            try {
                if (mVideoView.isPlaying()) {
                    mCurrentPos = mVideoView.getCurrentPosition() / 1000;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private final OnPlaybackBufferingUpdateListener mOnPlaybackBufferingUpdateListener = new OnPlaybackBufferingUpdateListener() {

        @Override
        public void onPlaybackBufferingUpdate(IMediaPlayer media, int percentage) {

            if (isFirstBuffering) {
                if (percentage < 100) {
                    mTvFirstBufferingProgress.setText(percentage + "%");
                    mRlFirstBuffering.setVisibility(View.VISIBLE);
                    mWaterMark.setVisibility(View.GONE);
                } else {
                    mRlFirstBuffering.setVisibility(View.GONE);
                    isFirstBuffering = false;
                }
            }
        }
    };

    private final BroadcastReceiver mHeadsetPlugReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", -1);

            LOG.debug("headset plug state changed. state={}", state);

            if (state == 0) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();

                    LOG.info("headset unplugged. pause video playback.");
                }
            }
        }
    };

    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wiState = wifi.getWifiState();

            if (wiState == WifiManager.WIFI_STATE_ENABLED ||
                    wiState == WifiManager.WIFI_STATE_ENABLING ||
                    wiState == WifiManager.WIFI_STATE_DISABLING)
                return;
        }
    };

    private final OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {

            // 当前是系统播放器架构
            if (mVideoView.isSystemMediaPlayer()) {
                // 系统播放器不支持，切换至自定义播放器
                switchMediaPlayer();
            }
            // 当前是自定义播放器架构
            else if (mVideoView.isCustomMediaPlayer()) {
                isFirstBuffering = false;
                mRlFirstBuffering.setVisibility(View.GONE);
                // 实现播放错误后,显示error界面
                mediaController.hide();
                mPlayerErrorTipsLayout.setVisibility(View.VISIBLE);
                mPlayerErrorTipsReloadBtn.requestFocus();
            }
            // 未知错误
            else {
                quit();
            }

            return true;
        }
    };

    /**
     * 隐藏虚拟导航栏
     */
    @SuppressLint("NewApi")
    private void fullVideoView() {
        if (!AdvancedMediaController.hasVirtualNavigation(this)) {// 如果有物理键，则无需隐藏系统导航键
            return;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else if (Build.VERSION.SDK_INT >= 14) {
            mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    public VideoView getVideoView() {
        return mVideoView;
    }

    public void quit() {
        if (isDLNAMediaPlayer()) {
            mVideoView.destroy();
        }
        // 解决部分设备退出后,系统底层对Videoview释放过慢导致会黑闪屏一下
        mVideoView.destroy();
        finish();
        overridePendingTransition(0, 0);
    }

    public void createExitDLNAConfirmDialog() {
        if (mExitAlertDialog != null && mExitAlertDialog.isShowing()) {
            return;
        }
        mExitAlertDialog = new AlertDialog.Builder(PlayerActivity.this)
                .setTitle("提示")
                .setMessage("要退出DLNA播放吗?")
                .setNegativeButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                quit();
                            }
                        })
                .setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        })
                .create();
        mExitAlertDialog.setCancelable(false);
        mExitAlertDialog.show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent) 防止mediacontroller点击后退后阻碍回退操作，联想机型会无法回退到上个界面
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LOG.i(Logger.TAG_LUKE, "PlayerDispatch action,keycode = " + event.getAction() + "-" + event.getKeyCode());
        boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN ? true : false);
        // boolean isActionUp = (event.getAction() == KeyEvent.ACTION_UP ? true : false);

        if (isActionDown) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                quit();
                return true;
            }
        }

        if (mPlayerErrorTipsLayout.getVisibility() == View.VISIBLE) {
            mPlayerErrorTipsLayout.dispatchKeyEvent(event);
            return true;
        }

        // 不支持MediaController操作
        if (mVideoView == null) {
            return true;
        } else {
            int state = mVideoView.getCurrentState();
            if (state != VideoView.STATE_PREPARED
                    && state != VideoView.STATE_PLAYING
                    && state != VideoView.STATE_PAUSED) {
                return true;
            }
        }

        // if(isActionDown){
        // // Center
        // if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER){
        // mediaController.showPauseOrStartUI();
        // return true;
        // }
        // // Menu
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
        //
        // return true;
        // }
        // // Up
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP){
        // mediaController.showVolumeUpOrDownUI(true);
        // return true;
        // }
        // // Down
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN){
        // mediaController.showVolumeUpOrDownUI(false);
        // return true;
        // }
        // // Left
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
        // // mediaController.showRewindOrFastforwardUI(false);
        // // return true;
        // }
        // // Right
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
        // // mediaController.showRewindOrFastforwardUI(true);
        // // return true;
        // }
        // // Back
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
        // finish();
        // return true;
        // }
        // // Volume Up
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP){
        // mediaController.showVolumeUpOrDownUI(true);
        // return true;
        // }
        // // Volume Down
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN){
        // mediaController.showVolumeUpOrDownUI(false);
        // return true;
        // }
        // }

        // return true;
        return mediaController.dispatchKeyEvent(event);

    }

    private void doReplayVideo() {

        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
            mVideoView.stop();
        }

        doPlayVideo();

    }

    private static final int DISPLAY_RATIO_AUTO = 0;
    private static final int DISPLAY_RATIO_ORIGIN = 1;
    private static final int DISPLAY_RATIO_FULLSCREEN = 2;
    private static final int DISPLAY_RATIO_4TO3 = 3;
    private static final int DISPLAY_RATIO_16TO9 = 4;

    private void updateDisplayRatio(int displayRatio, boolean needRefresh) {

        switch (displayRatio) {
        case DISPLAY_RATIO_AUTO:
            // 自动
            int exactDisplayWidthAuto = ScreenUtil.getDeviceRealWidth(this);
            int exactDisplayHeightAuto = ScreenUtil.getDeviceRealHeight(this);
            mVideoView.setDisplayRatio(DisplayRatio.DISPLAY_RATIO_AUTO.getmDisplayRatioFlag(), exactDisplayWidthAuto,
                    exactDisplayHeightAuto, needRefresh);
            break;
        case DISPLAY_RATIO_ORIGIN:
            // 原比例
            mVideoView.setDisplayRatio(DisplayRatio.DISPLAY_RATIO_ORIGIN.getmDisplayRatioFlag(), 0, 0, needRefresh);
            break;
        case DISPLAY_RATIO_FULLSCREEN:
            // 全屏
            int exactDisplayWidthFullScreen = ScreenUtil.getDeviceRealWidth(this);
            int exactDisplayHeightFullScreen = ScreenUtil.getDeviceRealHeight(this);
            mVideoView.setDisplayRatio(DisplayRatio.DISPLAY_RATIO_FULLSCREEN.getmDisplayRatioFlag(),
                    exactDisplayWidthFullScreen, exactDisplayHeightFullScreen, needRefresh);
            break;
        case DISPLAY_RATIO_4TO3:
            // 4:3
            mVideoView.setDisplayRatio(DisplayRatio.DISPLAY_RATIO_4TO3.getmDisplayRatioFlag(), 0, 0, needRefresh);
            break;
        case DISPLAY_RATIO_16TO9:
            // 16:9
            mVideoView.setDisplayRatio(DisplayRatio.DISPLAY_RATIO_16TO9.getmDisplayRatioFlag(), 0, 0, needRefresh);
            break;
        default:
            break;
        }

    }

    public void setCompletedState(boolean isCompleted) {
        this.mCompleted = isCompleted;
        // LOG.d(LOG.TAG_LUKE, "setCompletedState = " + isCompleted);
    }

    public boolean isCompletedState() {
        return this.mCompleted;
    }

}
