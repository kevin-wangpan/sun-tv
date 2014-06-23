package com.jiaoyang.tv.player.widget;

import java.lang.reflect.Method;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaoyang.base.audio.IAudioManager;
import com.jiaoyang.base.media.IMediaPlayer;
import com.jiaoyang.base.media.IMediaPlayer.OnCompletionListener;
import com.jiaoyang.base.media.IMediaPlayer.OnInfoListener;
import com.jiaoyang.base.media.IMediaPlayer.OnPlaybackBufferingUpdateListener;
import com.jiaoyang.base.misc.JiaoyangConstants.PlayMode;
import com.jiaoyang.base.misc.JiaoyangConstants.PlayProfile;
import com.jiaoyang.base.widget.MediaController;
import com.jiaoyang.base.widget.VideoView.SurfaceListener;
import com.jiaoyang.tv.player.IVideoItem;
import com.jiaoyang.tv.player.IVideoPlayList;
import com.jiaoyang.tv.player.PlayerActivity;
import com.jiaoyang.tv.player.WebVideoPlayList;
import com.jiaoyang.tv.player.widget.GestureDetector.SimpleOnGestureListener;
import com.jiaoyang.tv.player.widget.SeekManager.OnSeekListener;
import com.jiaoyang.tv.player.widget.SeekManager.OnSeekRateListener;
import com.jiaoyang.tv.player.widget.SeekManager.SEEK_RATE;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.tv.util.ScreenUtil;
import com.jiaoyang.video.tv.R;
import com.kankan.mediaserver.MediaServerProxy;

@SuppressLint("ViewConstructor")
public class AdvancedMediaController extends MediaController implements
        OnClickListener {
    private final Logger LOG = Logger.getLogger(AdvancedMediaController.class);

    private static final double RADIUS_SLOP = Math.PI * 5 / 24;

    private static final int GESTURE_NONE = 0;
    private static final int GESTURE_VOICE = GESTURE_NONE + 1;
    private static final int GESTURE_LIGHT = GESTURE_VOICE + 1;
    private static final int GESTURE_PROGRESS = GESTURE_LIGHT + 1;
    private static final int MIN_MILLOSECONDS_INTERVAL_SEEK = 1000;

    private PreferenceManager mPreferrenceManager;
    private ImageButton mBtnBack;
    private ImageButton mIBtnDownload;
    private ImageButton mIBtnPlayPrevVideo;
    private ImageButton mIBtnPlayNextVideo;
    private ImageButton mIBtnChangePlayQuality;
    private ImageButton mIBtnMuteVideo; // 静音按钮
    private ImageButton mIBtnDLNAVideo;

    private TextView mVideoName;
    private MediaPlayerControl mPlayerController;
    private ViewGroup mControllerTopPannel;
    private ViewGroup mBufferingContainer;
    private TextView mTvBufferingPercentage;
    private ViewGroup mMediaControllerBarPannel;
    private TimePowerWidget mTimePowerWidget;

    private ImageView mMediaControllerPauseImg;
    private ImageView mMediaControllerRewindImg;
    private ImageView mMediaControllerFastforwardImg;
    private TextView mMediaControllerFastforwardRewindTipTv;

    private IVideoPlayList mPlaylist;
    private PlayerActivity mContext;

    private IVideoItem mCurrentVideoPlayItem;
    private Dialog mChangePlayQualityDialog = null;
    private GestureDetector mGestureDetector;
    private boolean mIsPlaying = false;

    private VoiceLightWidget mVoiceLightWidget;
    private VideoGestureSeekWidget mSeekWidget;
    private View mLayer;

    private VolumeWidget mMediaControllerVolumeWidget;

    private SeekManager mSeekManager;

    // 统计
    private static final int BUFFERING_TYPE_FIRST = 0;
    private static final int BUFFERING_TYPE_SEEK = 1;
    private static final int BUFFERING_TYPE_INTERRUPT = 2;

    private long mVideoViewTime;
    private long mFirstBufferingTime;
    private int mInterruptedTimes;

    private int mCurrentGesture = GESTURE_NONE;

    private float mDragPos;
    private float mVideoMillisecondsPerPix;// 没前进一像素划过的毫秒数
    private float mCurrentDeltaScroll;
    private int mScreenWidth;

    private boolean isSystemUIShowing = false;// 虚拟导航栏是否已显示
    private final int HIDE_SYSTEM_UI_AFTER = 800;// 隐藏虚拟导航栏的时长
    private final int HIDE_SYSTEM_UI_AFTER_LONG = 4000;// //隐藏虚拟导航栏的时长,mediacontroller未显示时
    private boolean needChangeSystemUI = false;

    private boolean isNewMediaPlayer;

    /**
     * 更新播放缓冲的进度
     */
    private void updateBufferPercentage(final int percentage) {
        if (percentage < 100) {
            // String percentageStr = getResources().getString(R.string.player_buffering_progress, new int[percentage]);
            mTvBufferingPercentage.setText(percentage + "%");
            if (mBufferingContainer.getVisibility() != View.VISIBLE) {
                if (mMediaControllerPauseImg.getVisibility() != View.VISIBLE) {
                    mBufferingContainer.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (mBufferingContainer.getVisibility() == View.VISIBLE)
                mBufferingContainer.setVisibility(View.GONE);
        }
    }

    public AdvancedMediaController(Activity context) {
        super(context, (AttributeSet) null);
    }

    @SuppressLint("NewApi")
    public AdvancedMediaController(PlayerActivity context,
            IVideoPlayList playlist) {
        super(context, R.layout.extended_media_controller_layout);
        mScreenWidth = ScreenUtil.getScreenWidth(context);
        mContext = context;
        mPlaylist = playlist;
        mCurrentVideoPlayItem = mPlaylist.getCurrentPlayItem();
        mPreferrenceManager = PreferenceManager.instance(mContext);

        mGestureDetector = new GestureDetector(mContext, mGestureListener);

        if (Build.VERSION.SDK_INT >= 14) {
            if (hasVirtualNavigation(mContext)) {// 是否有虚拟导航栏
                needChangeSystemUI = true;
                initSystemUI();
            }
        }
    }

    private void initSystemUI() {
        // 使mediaController宽度不占满全屏
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.width = getUseAbleWidth(mContext);
        setLayoutParams(params);

        setSystemUIChangeListener();
    }

    protected void initControllerView(View root) {
        super.initControllerView(root);

        mBtnBack = (ImageButton) root
                .findViewById(R.id.mediacontroller_btn_goback);
        mBtnBack.setOnClickListener(this);
        mIBtnDownload = (ImageButton) root
                .findViewById(R.id.mediacontroller_ib_download);
        mIBtnDownload.setOnClickListener(this);

        mIBtnPlayPrevVideo = (ImageButton) root
                .findViewById(R.id.mediacontroller_ibtn_prev_video);
        mIBtnPlayPrevVideo.setOnClickListener(this);

        mIBtnPlayNextVideo = (ImageButton) root
                .findViewById(R.id.mediacontroller_ibtn_next_video);
        mIBtnPlayNextVideo.setOnClickListener(this);

        mIBtnChangePlayQuality = (ImageButton) root
                .findViewById(R.id.mediacontroller_ibtn_change_quality);
        mIBtnChangePlayQuality.setOnClickListener(this);
        mVideoName = (TextView) root
                .findViewById(R.id.mediacontroller_tv_title);

        mIBtnMuteVideo = (ImageButton) root
                .findViewById(R.id.mediacontroller_ibtn_mute_video);
        mIBtnMuteVideo.setOnClickListener(this);

        mIBtnDLNAVideo = (ImageButton) root
                .findViewById(R.id.mediacontroller_ibtn_dlna_video);
        mIBtnDLNAVideo.setOnClickListener(this);

        mControllerTopPannel = (ViewGroup) root
                .findViewById(R.id.mediacontroller_rl_top_pannel);
        mControllerTopPannel.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                show();
                return true;
            }
        });
        mBufferingContainer = (ViewGroup) root
                .findViewById(R.id.mediacontroller_fl_playback_buffering);
        mTvBufferingPercentage = (TextView) root.findViewById(R.id.mediacontroller_tv_progress_tips);
        mMediaControllerBarPannel = (ViewGroup) root
                .findViewById(R.id.mediacontroller_rl_control_bar);
        mMediaControllerBarPannel.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                show();
                return true;
            }
        });

        mVoiceLightWidget = (VoiceLightWidget) root
                .findViewById(R.id.voice_controller);
        mSeekWidget = (VideoGestureSeekWidget) root
                .findViewById(R.id.video_seekWidget);
        mLayer = root.findViewById(R.id.layer);

        mTimePowerWidget = (TimePowerWidget) root
                .findViewById(R.id.mediacontroller_battery_time_widget);

        mMediaControllerPauseImg = (ImageView) root.findViewById(R.id.mediacontroller_pause_iv);
        mMediaControllerRewindImg = (ImageView) root.findViewById(R.id.mediacontroller_rewind_iv);
        mMediaControllerFastforwardImg = (ImageView) root.findViewById(R.id.mediacontroller_fastforward_iv);
        mMediaControllerFastforwardRewindTipTv = (TextView) root
                .findViewById(R.id.mediacontroller_fastfordward_rewind_tip_tv);

        mMediaControllerVolumeWidget = (VolumeWidget) root.findViewById(R.id.mediacontroller_volume_widget);

    }

    public void registerReceiver() {
        // mTimePowerWidget.registerBatteryReceiver();
    }

    public void unRegisterReceiver() {
        // mTimePowerWidget.unregisterBatteryReceiver();
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);
        attachBufferingContainerToActivity();
        attachVoiceControllerToActivity();
        attachPlayerStatusComponentToActivity();
    }

    /**
     * 将播放缓冲的进度控件放入Activity的contentview中
     */
    private void attachBufferingContainerToActivity() {
        ViewGroup outFrame = mContext.getContentView();
        ViewGroup parent = (ViewGroup) mBufferingContainer.getParent();
        if (parent != null) {
            parent.removeView(mBufferingContainer);
        }
        if (outFrame != null) {
            outFrame.addView(mBufferingContainer);
        }
    }

    private void attachVoiceControllerToActivity() {
        ViewGroup outFrame = mContext.getContentView();
        ViewGroup parent = (ViewGroup) mLayer.getParent();
        if (parent != null) {
            parent.removeView(mLayer);
        }
        if (outFrame != null) {
            outFrame.addView(mLayer);
        }

        parent = (ViewGroup) mSeekWidget.getParent();
        if (parent != null) {
            parent.removeView(mSeekWidget);
        }
        if (outFrame != null) {
            outFrame.addView(mSeekWidget);
        }
    }

    private void attachPlayerStatusComponentToActivity() {
        attachViewToActivity(mMediaControllerPauseImg);
        attachViewToActivity(mMediaControllerRewindImg);
        attachViewToActivity(mMediaControllerFastforwardImg);
    }

    private void attachViewToActivity(View view) {
        ViewGroup outFrame = mContext.getContentView();
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        if (outFrame != null) {
            outFrame.addView(view);
        }
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        switch (viewId) {
        case R.id.mediacontroller_btn_goback:
            mContext.quit();
            break;

        case R.id.mediacontroller_ibtn_next_video:
            playNextVideo();
            hide();
            break;

        case R.id.mediacontroller_ibtn_prev_video:
            playPrevVideo();
            hide();
            break;

        case R.id.mediacontroller_ibtn_change_quality:
            changeQuality();
            break;
        default:
            break;
        }
    }

    private void changeQuality() {
        createChangePlayQualityDialog();
    }

    protected void initDLNAViews() {
        super.initDLNAViews();
        mBufferingContainer.setVisibility(View.GONE);
        mIBtnDownload.setVisibility(View.GONE);
        mIBtnDLNAVideo.setVisibility(View.GONE);
        mControllerTopPannel.setBackgroundResource(R.drawable.media_player_dlna_control_bg);
        mMediaControllerBarPannel.setBackgroundColor(Color.parseColor("#EAEFF0"));
        mIBtnPlayPrevVideo.setImageResource(R.drawable.player_dlna_last_selector);
        mIBtnPlayNextVideo.setImageResource(R.drawable.player_dlna_next_selector);
        mVideoName.setTextColor(Color.BLACK);
    }

    public void onMediaPlayerChange(IMediaPlayer mediaPlayer) {
        IAudioManager audioManager = AudioManagerFactory.createAudioManager(mediaPlayer, getContext());
        mVoiceLightWidget.onAudioManagerChange(audioManager);
    }

    /**
     * 创建选择清晰度的对话框
     */
    private void createChangePlayQualityDialog() {
    }


    private void restoreState() {
        bufferingNeedSleep = true;
    }

    private void playNextVideo() {
        final int profile = mCurrentVideoPlayItem.getProfile();
        if (mCurrentVideoPlayItem.hasNextPart()) {
            mCurrentVideoPlayItem.moveToNextPart();
            // final String playUrl = mCurrentVideoPlayItem.getPlayUrlByProfile(profile);
            mCurrentVideoPlayItem.setProfile(profile);
            final String playUrl = mCurrentVideoPlayItem.getDefaultPlayUrl();
            playVideo(playUrl);
        } else if (mPlaylist != null && mPlaylist.hasNextVideo()) {
            mCurrentVideoPlayItem = mPlaylist.moveToNextPlayItem();
            // final String url = mCurrentVideoPlayItem.getPlayUrlByProfile(profile);
            mCurrentVideoPlayItem.setProfile(profile);
            final String url = mCurrentVideoPlayItem.getDefaultPlayUrl();
            playVideo(url);
        } else {
            Toast.makeText(getContext(), "没有下一集了", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPrevVideo() {
        final int profile = mCurrentVideoPlayItem.getProfile();
        if (mCurrentVideoPlayItem.hasPrePart()) {
            mCurrentVideoPlayItem.moveToPrePart();
            // final String playUrl = mCurrentVideoPlayItem.getPlayUrlByProfile(profile);
            mCurrentVideoPlayItem.setProfile(profile);
            final String playUrl = mCurrentVideoPlayItem.getDefaultPlayUrl();
            playVideo(playUrl);
        } else if (mPlaylist != null && mPlaylist.hasPrevVideo()) {
            mCurrentVideoPlayItem = mPlaylist.moveToPrevPlayItem();
            // final String url = mCurrentVideoPlayItem.getPlayUrlByProfile(profile);
            mCurrentVideoPlayItem.setProfile(profile);
            final String url = mCurrentVideoPlayItem.getDefaultPlayUrl();
            playVideo(url);
        } else {
            Toast.makeText(getContext(), "没有上一集了", Toast.LENGTH_SHORT).show();
        }
    }

    private void playVideo(String playUrl) {
        mBufferingContainer.setVisibility(View.GONE);// 解决先拖进度后切集，导致的缓冲进度不消失的问题
        if (!TextUtils.isEmpty(playUrl)) {
            onVideoEnd();
            mPlayerController.stop();
            if (playUrl.contains(PlayerActivity.VOD_FILTER)) {
                playUrl = MediaServerProxy.instance().getPlayURI(playUrl).toString();
            }
            hideSeekWidget();
            mContext.setCompletedState(false);
            mPlayerController.setVideoPath(playUrl);
            mPlayerController.start();
            // 解决跳转到新视频时，progressBar进度仍为上个视频最后时间进度，因为progressBar的进度更改只在show下进行
            getProgressBar().setProgress(0);
            restoreState();
            mContext.onStartFirstBuffering();

            onVideoStart();
        }
        else {
            Toast.makeText(getContext(), "抱歉,播放错误", Toast.LENGTH_SHORT).show();
            mContext.quit();
        }

    }

    private void disableUnsupportedButtons() {
        if (mIBtnPlayPrevVideo != null) {
            if (mPlaylist.hasPrevVideo() || mCurrentVideoPlayItem.hasPrePart()) {
                mIBtnPlayPrevVideo.setEnabled(true);
            } else {
                mIBtnPlayPrevVideo.setEnabled(false);
            }
        }

        if (mIBtnPlayNextVideo != null) {
            if (mPlaylist.hasNextVideo() || mCurrentVideoPlayItem.hasNextPart()) {
                mIBtnPlayNextVideo.setEnabled(true);
            } else {
                mIBtnPlayNextVideo.setEnabled(false);
            }
        }
        if (mIBtnDownload != null) {
            final IVideoItem videoPlayItem = mCurrentVideoPlayItem;
            if (videoPlayItem != null) {
                if (videoPlayItem.videoPlayMode() != PlayMode.PLAY_MODE_WEB) {
                    mIBtnDownload.setVisibility(View.GONE);
                }
            } else {
                mIBtnDownload.setVisibility(View.GONE);
            }
        }
    }


    private void showVideoTitle() {
        String title = mCurrentVideoPlayItem.getDisplayTitle();
        if (mPlaylist.isTry()) {
            title += " 试看";
        }
        if (!TextUtils.isEmpty(title)) {
            mVideoName.setText(Html.fromHtml(title));
        }
    }

    private boolean bufferingNeedSleep;// 通过睡觉丢弃掉开始的percent=0,挺恶心的做法
    private OnPlaybackBufferingUpdateListener mBufferingUpdateListener = new OnPlaybackBufferingUpdateListener() {
        @Override
        public void onPlaybackBufferingUpdate(IMediaPlayer mp, int percent) {
            LOG.info("percent={}", percent);
            if (!mContext.isFirstPlayBuffering()) {
                if (bufferingNeedSleep) {// 丢弃掉开始的percent=0;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bufferingNeedSleep = false;
                } else {
                    if (mSeekManager.isFastForwarding() || mSeekManager.isRewinding()) {
                        // mBufferingContainer.setVisibility(View.GONE);
                    } else {
                        updateBufferPercentage(percent);
                    }
                }

            } else {
                if (mOnPlaybackBufferingUpdateListener != null) {
                    mOnPlaybackBufferingUpdateListener
                            .onPlaybackBufferingUpdate(mp, percent);
                }
            }

        }
    };

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);

        mPlayerController = player;
        isNewMediaPlayer = true;
        mPlayerController
                .setOnPlaybackBufferingUpdateListener(mBufferingUpdateListener);
        mPlayerController.setOnCompletionListener(mOnPlayCompleteListener);
        mPlayerController.setSurfaceListener(mSurfaceListener);
        mPlayerController.setOnInfoListener(mOnInfoListener);
        mSeekManager = new SeekManager(mPlayerController, (MediaController) this);
        mSeekManager.setOnSeekListener(mOnSeekListener);
        mSeekManager.setOnSeekRateListener(mOnSeekRateListener);
    }

    private OnCompletionListener mOnPlayCompleteListener = new OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer mediaPlayer) {
            LOG.debug("onCompletion");

            mContext.setCompletedState(true);

            if (mPlaylist.hasNextVideo()) {
                playNextVideo();
            } else {
                mContext.quit();
            }
        }
    };
    private OnPlaybackBufferingUpdateListener mOnPlaybackBufferingUpdateListener;

    public void setOnBufferingListener(
            OnPlaybackBufferingUpdateListener listener) {
        this.mOnPlaybackBufferingUpdateListener = listener;
    }

    @Override
    public void hide() {
        super.hide();
        systemUIHide(0);
    }

    @Override
    public void show() {
        super.show();
        systemUIShow();

        populateView();
        disableUnsupportedButtons();
    }

    @Override
    public void show(int timeout) {
        super.show(timeout);
        systemUIShow();

        populateView();
        disableUnsupportedButtons();
    }

    protected void populateView() {
        showVideoTitle();
        showChangePlayQualityText();
    }

    private void showChangePlayQualityText() {
        switch (mCurrentVideoPlayItem.getProfile()) {
        case PlayProfile.SMOOTH_PROFILE:
            mIBtnChangePlayQuality
                    .setImageResource(R.drawable.media_player_quality_smooth_selector);
            break;

        case PlayProfile.BASE_PROFILE:
            mIBtnChangePlayQuality
                    .setImageResource(R.drawable.media_player_quality_base_selector);
            break;

        case PlayProfile.HIGH_PROFILE:
            mIBtnChangePlayQuality
                    .setImageResource(R.drawable.media_player_quality_high_selector);
            break;

        default:
            if (mCurrentVideoPlayItem.videoPlayMode() == PlayMode.PLAY_MODE_LOCAL) {
                mIBtnChangePlayQuality.setVisibility(View.GONE);
            }
            break;
        }
    }

    private void hideAllViews() {
        AlphaAnimation aa = new AlphaAnimation(1, 0);
        aa.setDuration(200);
        aa.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub
                mControllerTopPannel.setVisibility(View.INVISIBLE);
                mMediaControllerBarPannel.setVisibility(View.INVISIBLE);
            }
        });
        mMediaControllerPauseImg.setVisibility(View.GONE);
        mControllerTopPannel.startAnimation(aa);
        mMediaControllerBarPannel.startAnimation(aa);
        systemUIHide(HIDE_SYSTEM_UI_AFTER);
    }

    private void showAllViews() {
        AlphaAnimation aa = new AlphaAnimation(0, 1);
        aa.setDuration(200);
        aa.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub
                mControllerTopPannel.setVisibility(View.VISIBLE);
                mMediaControllerBarPannel.setVisibility(View.VISIBLE);
            }
        });
        mMediaControllerPauseImg.setVisibility(View.VISIBLE);
        mControllerTopPannel.startAnimation(aa);
        mMediaControllerBarPannel.startAnimation(aa);
        systemUIShow();
    }

    @SuppressWarnings("unused")
    private boolean pannelVisiable() {
        return mControllerTopPannel.getVisibility() == View.VISIBLE
                && mMediaControllerBarPannel.getVisibility() == View.VISIBLE;
    }

    private boolean pannelHide() {
        return mControllerTopPannel.getVisibility() == View.INVISIBLE
                && mMediaControllerBarPannel.getVisibility() == View.INVISIBLE;
    }

    // @Override
    // public boolean onTouchEvent(MotionEvent event) {
    // final int action = event.getAction();
    // if (action == MotionEvent.ACTION_UP
    // || action == MotionEvent.ACTION_CANCEL) {
    // if (mCurrentGesture == GESTURE_PROGRESS) {
    // mPlayerController.seekTo((int) mDragPos);
    // clearDragPos();
    // }
    //
    // if (mCurrentGesture == GESTURE_NONE) {
    // if (!isShowing() || pannelHide()) {
    // showAllViews();
    // show();
    // } else {
    // hideAllViews();
    // }
    // }
    //
    // if (isShowing() && mCurrentGesture != GESTURE_NONE) {
    // fadeOut(1000);
    // }
    // mCurrentGesture = GESTURE_NONE;
    // }
    //
    // if (action == MotionEvent.ACTION_MOVE) {
    // if (isShowing()) {
    // show();
    // }
    // }
    //
    // // mGestureDetector.onTouchEvent(event);
    //
    // return true;
    // }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            judgeStartOrPause();
        }
        return true;
    }

    private void clearDragPos() {
        mDragPos = 0;
    }

    private void onVideoStart() {
        LOG.debug("video start. position={}",
                mPlayerController.getCurrentPosition());

        mVideoViewTime = SystemClock.uptimeMillis();
        mFirstBufferingTime = 0;
        mInterruptedTimes = 0;
    }

    private void onVideoEnd() {
    }

    private final OnInfoListener mOnInfoListener = new OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int msg, int extra) {
            switch (msg) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (extra == BUFFERING_TYPE_INTERRUPT) {
                    mInterruptedTimes++;
                }
                showBufferingWidget();
                break;

            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                hideBufferingWidget();
                break;

            case IMediaPlayer.MEDIA_INFO_VIDEO_PLAYING_START:
                switch (extra) {
                case BUFFERING_TYPE_FIRST:
                    mFirstBufferingTime = SystemClock.uptimeMillis()
                            - mVideoViewTime;
                    break;

                case BUFFERING_TYPE_SEEK:
                    break;

                case BUFFERING_TYPE_INTERRUPT:
                    break;
                }
                break;

            default:
                break;
            }

            return false;
        }
    };

    private final SurfaceListener mSurfaceListener = new SurfaceListener() {

        @Override
        public void onSurfaceCreated() {
            LOG.debug("surface created.");

            onVideoStart();
        }

        @Override
        public void onSurfaceDestroyed() {
            LOG.debug("surface destroyed.");

            mBufferingContainer.setVisibility(View.GONE);

            onVideoEnd();
        }
    };

    private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {

        public boolean onDoubleTap(MotionEvent e) {
            if (mPlayerController.isPlaying()) {
                mPlayerController.pause();
            } else {
                mPlayerController.start();
            }
            return true;
        };

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            if (e1 == null || e2 == null) {
                return false;
            }
            float oldX = e1.getX();
            final double distance = Math.sqrt(Math.pow(distanceX, 2)
                    + Math.pow(distanceY, 2));
            int windowWidth = ScreenUtil.getScreenWidth(mContext);
            final double radius = distanceY / distance;

            if (Math.abs(radius) > RADIUS_SLOP) {
                if (mCurrentGesture != GESTURE_PROGRESS
                        && !mSeekWidget.isVisiable()) {
                    if (oldX > windowWidth / 2) {// TODO右半屏幕处理声音的逻辑
                        mCurrentGesture = GESTURE_VOICE;
                        onVoiceChange(distanceY, distance);
                    } else {// TODO左半屏幕处理亮度的逻辑
                        mCurrentGesture = GESTURE_LIGHT;
                        onLightChange(distanceY, distance);
                    }
                }
            } else {// TODO 处理视频进度
                if (mCurrentGesture != GESTURE_VOICE
                        && mCurrentGesture != GESTURE_LIGHT
                        && !mVoiceLightWidget.isVisible()) {
                    onVideoTouchSeek(distanceX, distance);
                }
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    private void onVoiceChange(float delta, double distance) {
        mSeekWidget.setVisibility(View.GONE);
        mVoiceLightWidget.onVoiceChange(delta, (int) distance);
    }

    private void onLightChange(float delta, double distance) {
        mSeekWidget.setVisibility(View.GONE);
        mVoiceLightWidget.onLightChange(delta, (int) distance,
                mContext.getWindow());
    }

    private void onVideoTouchSeek(float distanceX, double distane) {
        if (isNewMediaPlayer) {
            mVideoMillisecondsPerPix = mPlayerController.getDuration() / (mScreenWidth);
            isNewMediaPlayer = false;
        }

        mVoiceLightWidget.setVisibility(View.GONE);

        if (mDragPos == 0 && mCurrentGesture != GESTURE_PROGRESS) {
            mDragPos = mPlayerController.getCurrentPosition();
        }

        mCurrentGesture = GESTURE_PROGRESS;

        mCurrentDeltaScroll += distanceX;

        if (Math.abs(mCurrentDeltaScroll * mVideoMillisecondsPerPix) >= MIN_MILLOSECONDS_INTERVAL_SEEK) {
            float deltaTime = (mCurrentDeltaScroll * mVideoMillisecondsPerPix);
            mDragPos = mDragPos - deltaTime;
            if (mDragPos > mPlayerController.getDuration()) {
                mDragPos = mPlayerController.getDuration();
            }
            if (mDragPos < 0) {
                mDragPos = 0;
            }
            mCurrentDeltaScroll = 0;
        }

        mSeekWidget.onSeek((int) mDragPos, mPlayerController.getDuration());
    }

    /**
     * 设置系统UI显隐监听
     */
    @SuppressLint("NewApi")
    private void setSystemUIChangeListener() {
        if (Build.VERSION.SDK_INT >= 14) {
            OnSystemUiVisibilityChangeListener systemUIChangeListener = new OnSystemUiVisibilityChangeListener() {

                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (mContext.isFirstPlayBuffering()) {
                        isSystemUIShowing = true;

                        systemUIHide(HIDE_SYSTEM_UI_AFTER_LONG);
                        return;
                    }
                    if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                        if (!isShowing() || mControllerTopPannel.getVisibility() != View.VISIBLE) {
                            showAllViews();
                            show();
                        }
                    }
                }
            };

            setOnSystemUiVisibilityChangeListener(systemUIChangeListener);
            mContext.getVideoView().setOnSystemUiVisibilityChangeListener(systemUIChangeListener);
        }
    }

    /**
     * 隐藏系统导航栏
     */
    @SuppressLint("NewApi")
    private void systemUIHide(long waitTime) {
        if (!needChangeSystemUI) {
            return;
        }

        if (!isSystemUIShowing) {
            return;
        }
        isSystemUIShowing = false;
        postDelayed(new Runnable() {

            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 16) {
                    setSystemUiVisibility(SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                    mContext.getVideoView().setSystemUiVisibility(
                            SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else if (Build.VERSION.SDK_INT >= 14) {
                    // setSystemUiVisibility(SYSTEM_UI_FLAG_LOW_PROFILE);
                    mContext.getVideoView().setSystemUiVisibility(SYSTEM_UI_FLAG_LOW_PROFILE);
                }
            }
        }, waitTime);

    }

    /**
     * 显示系统导航栏
     */
    @SuppressLint("NewApi")
    private void systemUIShow() {
        if (!needChangeSystemUI) {
            return;
        }

        if (isSystemUIShowing) {
            return;
        }
        isSystemUIShowing = true;
        if (Build.VERSION.SDK_INT >= 16) {
            setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else if (Build.VERSION.SDK_INT >= 14) {
            // setSystemUiVisibility(SYSTEM_UI_FLAG_VISIBLE);
            mContext.getVideoView().setSystemUiVisibility(SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    /**
     * 获取屏幕可用宽
     */
    @SuppressLint("NewApi")
    public static int getUseAbleWidth(Activity context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        return width;
    }

    /**
     * 获取硬件实际尺寸
     */
    public static int getDeviceRealWidth(Activity context) {
        int realWidth = 0;
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            realWidth = dm.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realWidth;
    }

    /**
     * 是否设备有虚拟导航栏
     * 
     * @param context
     * @return
     */
    public static boolean hasVirtualNavigation(Activity context) {
        int realWidth = getDeviceRealWidth(context);
        if (realWidth > 0) {
            int useAbleWidth = getUseAbleWidth(context);
            return realWidth > useAbleWidth;
        }
        return true;
    }

    private void start() {
        // 播放
        if (!mPlayerController.isPlaying()) {
            hide();
            mControllerTopPannel.setVisibility(View.GONE);
            mMediaControllerBarPannel.setVisibility(View.GONE);
            mMediaControllerVolumeWidget.setVisibility(View.GONE);
            mMediaControllerPauseImg.setVisibility(View.GONE);
            mMediaControllerRewindImg.setVisibility(View.GONE);
            mMediaControllerFastforwardImg.setVisibility(View.GONE);
            mMediaControllerFastforwardRewindTipTv.setVisibility(View.GONE);
            mPlayerController.start();
        }
    }

    private void pause() {
        // 暂停
        if (mPlayerController.isPlaying()) {
            show(Integer.MAX_VALUE);
            mControllerTopPannel.setVisibility(View.VISIBLE);
            mMediaControllerBarPannel.setVisibility(View.VISIBLE);
            mMediaControllerVolumeWidget.setVisibility(View.GONE);
            mMediaControllerPauseImg.setVisibility(View.VISIBLE);
            mMediaControllerRewindImg.setVisibility(View.GONE);
            mMediaControllerFastforwardImg.setVisibility(View.GONE);
            mMediaControllerFastforwardRewindTipTv.setVisibility(View.GONE);
            if (mBufferingContainer.getVisibility() == View.VISIBLE)
                mBufferingContainer.setVisibility(View.GONE);
            mPlayerController.pause();
        }
    }

    private void judgeStartOrPause() {

        // 快进快退：当前正在快进快退中,则直接开始播放,并取消快进快退
        if (mSeekManager.isFastForwarding() || mSeekManager.isRewinding()) {
            showBufferingWidget();
            mSeekManager.stopRewindOrFastforward(true);
            mPlayerController.start();
            hide();
            mControllerTopPannel.setVisibility(View.GONE);
            mMediaControllerBarPannel.setVisibility(View.GONE);
            mMediaControllerVolumeWidget.setVisibility(View.GONE);
            mMediaControllerPauseImg.setVisibility(View.GONE);
            mMediaControllerRewindImg.setVisibility(View.GONE);
            mMediaControllerFastforwardImg.setVisibility(View.GONE);
            mMediaControllerFastforwardRewindTipTv.setVisibility(View.GONE);
            return;
        }

        // 一般情况：播放与暂停的切换
        // 播放
        if (!mPlayerController.isPlaying()) {
            start();
        }
        // 暂停
        else {
            pause();
        }
    }

    private void volumeUp() {
        if (mMediaControllerVolumeWidget != null) {
            // show();
            // mMediaControllerVolumeWidget.setVisibility(View.VISIBLE);
            // mMediaControllerVolumeWidget.volumeUp();

            if (!isShowing())
                show();
            mMediaControllerVolumeWidget.volumeUp();

        }
    }

    private void volumeDown() {
        if (mMediaControllerVolumeWidget != null) {
            // show();
            // mMediaControllerVolumeWidget.setVisibility(View.VISIBLE);
            // mMediaControllerVolumeWidget.volumeDown();

            if (!isShowing())
                show();
            mMediaControllerVolumeWidget.volumeDown();

        }
    }

    private OnSeekListener mOnSeekListener = new OnSeekListener() {

        @Override
        public void onRewindCompleted() {
            stopSeekingForStartPlay();
            // 自动跳转到preview video,暂不提供
            // if (mPlaylist.hasPrevVideo()) {
            // playPrevVideo();
            // }
        }

        @Override
        public void onFastforwardCompleted() {
            stopSeekingForStartPlay();
            // 自动跳转到next video,暂不提供
            // if (mPlaylist.hasNextVideo()) {
            // playNextVideo();
            // }
        }

        @Override
        public void onFastforwardError() {
            stopSeekingForStartPlay();
        }

        @Override
        public void onRewindError() {
            stopSeekingForStartPlay();
        }
    };

    private OnSeekRateListener mOnSeekRateListener = new OnSeekRateListener() {

        @Override
        public void onSeekRateChanged(SEEK_RATE preSeekRate, SEEK_RATE curSeekRate) {

            if (preSeekRate == null || curSeekRate == null)
                return;

            String tipsDirection = null;
            String tipsSeekRate = null;
            if (mSeekManager.isFastForwarding()) {
                tipsDirection = "快进";
            } else if (mSeekManager.isRewinding()) {
                tipsDirection = "快退";
            }

            if (curSeekRate == SEEK_RATE.LOW
                    || curSeekRate == SEEK_RATE.MEDIUM
                    || curSeekRate == SEEK_RATE.HIGH) {
                tipsSeekRate = " X" + (int) curSeekRate.getRateFactor();
            }

            if (tipsDirection == null || tipsSeekRate == null)
                mMediaControllerFastforwardRewindTipTv.setText("");
            else
                mMediaControllerFastforwardRewindTipTv.setText(tipsDirection + tipsSeekRate);

        }
    };

    private void stopSeekingForStartPlay() {
        mSeekManager.stopRewindOrFastforward(true);
        mPlayerController.start();
        hide();
        mControllerTopPannel.setVisibility(View.GONE);
        mMediaControllerBarPannel.setVisibility(View.GONE);
        mMediaControllerVolumeWidget.setVisibility(View.GONE);
        mMediaControllerPauseImg.setVisibility(View.GONE);
        mMediaControllerRewindImg.setVisibility(View.GONE);
        mMediaControllerFastforwardImg.setVisibility(View.GONE);
        mMediaControllerFastforwardRewindTipTv.setVisibility(View.GONE);
    }

    private void fastForward() {
        if (mSeekManager != null) {

            // 如果正在fastforwarding则尝试加大快进速率,暂不提供
            if (mSeekManager.isFastForwarding())
                mSeekManager.accelerateSeekRate();

            boolean result = mSeekManager.startFastforward();
            if (result) {
                show(Integer.MAX_VALUE);
                mControllerTopPannel.setVisibility(View.VISIBLE);
                mMediaControllerBarPannel.setVisibility(View.VISIBLE);
                mMediaControllerVolumeWidget.setVisibility(View.GONE);
                mMediaControllerPauseImg.setVisibility(View.GONE);
                mMediaControllerRewindImg.setVisibility(View.GONE);
                mMediaControllerFastforwardImg.setVisibility(View.VISIBLE);
                mMediaControllerFastforwardRewindTipTv.setVisibility(View.VISIBLE);
                if (mBufferingContainer.getVisibility() == View.VISIBLE)
                    mBufferingContainer.setVisibility(View.GONE);
            }

        }
    }

    private void rewind() {
        if (mSeekManager != null) {

            // 如果正在rewinding则尝试加大快退速率,暂不提供
            if (mSeekManager.isRewinding())
                mSeekManager.accelerateSeekRate();

            boolean result = mSeekManager.startRewind();
            if (result) {
                show(Integer.MAX_VALUE);
                mControllerTopPannel.setVisibility(View.VISIBLE);
                mMediaControllerBarPannel.setVisibility(View.VISIBLE);
                mMediaControllerVolumeWidget.setVisibility(View.GONE);
                mMediaControllerPauseImg.setVisibility(View.GONE);
                mMediaControllerRewindImg.setVisibility(View.VISIBLE);
                mMediaControllerFastforwardImg.setVisibility(View.GONE);
                mMediaControllerFastforwardRewindTipTv.setVisibility(View.VISIBLE);
                if (mBufferingContainer.getVisibility() == View.VISIBLE)
                    mBufferingContainer.setVisibility(View.GONE);
            }

        }
    }

    public void showVolumeUpOrDownUI(boolean isVolumeUp) {
        mControllerTopPannel.setVisibility(View.GONE);
        mMediaControllerBarPannel.setVisibility(View.GONE);
        if (isVolumeUp) {
            volumeUp();
        } else {
            volumeDown();
        }
    }

    public void showPauseOrStartUI() {
        judgeStartOrPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LOG.i(Logger.TAG_LUKE, "ControllerDispatch action,keycode = " + event.getAction() + "-" + event.getKeyCode());
        boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN ? true : false);
        boolean isActionUp = (event.getAction() == KeyEvent.ACTION_UP ? true : false);

        if (isActionDown) {
            // Center
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                judgeStartOrPause();
                return true;
            }
            // Menu
            else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                // 弹出剧集、清晰度选择框
                if (mPlaylist instanceof WebVideoPlayList)
                    showEpisodeQualitySelectActivity();
                return true;
            }
            // Up
            else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                volumeUp();
                return true;
            }
            // Down
            else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                volumeDown();
                return true;
            }
            // Left
            else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                // mHandler.removeCallbacks(mRunnableStopFastforwardOrRewindTask);
                rewind();
                return true;
            }
            // Right
            else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                // mHandler.removeCallbacks(mRunnableStopFastforwardOrRewindTask);
                fastForward();
                return true;
            }
            // Back
            else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                mContext.quit();
                return true;
            }
            // Volume Up
            else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                volumeUp();
                return true;
            }
            // Volume Down
            else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                volumeDown();
                return true;
            }

        }

        // if(isActionUp){
        // // Left
        // if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
        //
        // // 快进快退：当前正在快进快退中,则直接开始播放,并取消快进快退
        // if(mSeekManager.isFastForwarding() || mSeekManager.isRewinding()){
        // mHandler.postDelayed(mRunnableStopFastforwardOrRewindTask, 400);
        // }
        // return true;
        // }
        // // Right
        // else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
        // // 快进快退：当前正在快进快退中,则直接开始播放,并取消快进快退
        // if(mSeekManager.isFastForwarding() || mSeekManager.isRewinding()){
        // mHandler.postDelayed(mRunnableStopFastforwardOrRewindTask, 400);
        // }
        // return true;
        // }
        // }

        return true;
        // return super.dispatchKeyEvent(event);

    }

    public boolean switchVideo(IVideoItem oldVideoItem, IVideoItem newVideoItem) {

        if (oldVideoItem == null || newVideoItem == null)
            return false;

        int oldVideoItemIndex = oldVideoItem.getVideoItemIndex();
        int newVideoItemIndex = newVideoItem.getVideoItemIndex();

        if (oldVideoItemIndex < 0 || newVideoItemIndex < 0)
            return false;

        // 不同剧集
        if (oldVideoItemIndex != newVideoItemIndex) {

            mPreferrenceManager.savePlayProfile(newVideoItem.getProfile());
            String playUrl = newVideoItem.getPlayUrlByProfile(newVideoItem.getProfile());
            if (TextUtils.isEmpty(playUrl))
                return false;
            mCurrentVideoPlayItem = newVideoItem;
            hide();
            LOG.i(Logger.TAG_LUKE, "不同剧集  playUrl,proile = " + playUrl + " - " + newVideoItem.getProfile());
            playVideo(playUrl);

        }
        // 相同剧集
        else {

            int oldVideoItemProfile = oldVideoItem.getProfile();
            int newVideoItemProfile = newVideoItem.getProfile();

            if (oldVideoItemProfile < PlayProfile.LOW_PROFILE || newVideoItemProfile < PlayProfile.LOW_PROFILE)
                return false;

            // 相同清晰度
            if (oldVideoItemProfile == newVideoItemProfile)
                return false;

            mCurrentVideoPlayItem = newVideoItem;
            hide();
            // 不同清晰度
            mPreferrenceManager.savePlayProfile(newVideoItemProfile);
            mBufferingContainer.setVisibility(View.GONE);
            mPlayerController.pause();
            final int currentPosition = mPlayerController.getCurrentPosition();
            mPlayerController.stop();
            onVideoEnd();
            String playableUrl = newVideoItem.getPlayUrlByProfile(newVideoItemProfile);
            LOG.i(Logger.TAG_LUKE, "不同清晰度  playUrl,proile = " + playableUrl + " - " + newVideoItemProfile);
            if (playableUrl.contains(PlayerActivity.VOD_FILTER))
                playableUrl = MediaServerProxy.instance().getPlayURI(playableUrl).toString();
            hideSeekWidget();
            mContext.setCompletedState(false);
            mPlayerController.setVideoPath(playableUrl);
            mPlayerController.start();
            // 解决跳转到新视频时，progressBar进度仍为上个视频最后时间进度，因为progressBar的进度更改只在show下进行
            // getProgressBar().setProgress(0);
            mPlayerController.seekTo(currentPosition);
            onVideoStart();
            mContext.onChangeVideoQuality();

        }

        return true;

    }

    public void showEpisodeQualitySelectActivity() {

        int totplayItemSizes = mPlaylist.getPlayItemSizes();
        int curPlayItemIndex = mCurrentVideoPlayItem.getVideoItemIndex();
        int curProfile = mCurrentVideoPlayItem.getProfile();
        Set<Integer> totProfileSet = mCurrentVideoPlayItem.getProfiles();
    }

    public boolean dealEpisodeQualitySelectResult(int episodeIndex, int episodeProfile) {

        IVideoItem newVideoPlayItem = mPlaylist.moveToPlayItemByIndex(episodeIndex);
        // 屏蔽超清
        // if(episodeProfile == PlayProfile.SUPER_PROFILE) episodeProfile--;
        newVideoPlayItem.setProfile(episodeProfile);

        int totplayItemSizesNew = mPlaylist.getPlayItemSizes();
        int curPlayItemIndexNew = newVideoPlayItem.getVideoItemIndex();
        Set<Integer> totProfileNew = newVideoPlayItem.getProfiles();
        int curProfileNew = newVideoPlayItem.getProfile();

        LOG.i(Logger.TAG_LUKE, "tI,cI,tP,cP new = " + totplayItemSizesNew + " - " + curPlayItemIndexNew + " - "
                + totProfileNew.toString() + " - " + episodeProfile + "=" + curProfileNew);

        return switchVideo(mCurrentVideoPlayItem, newVideoPlayItem);

    }

    public void onEpisodeQualitySelectActivityShow() {

        if (mSeekManager != null && (mSeekManager.isFastForwarding() || mSeekManager.isRewinding())) {
            mSeekManager.stopRewindOrFastforward(true);
        }

        hide();
        mControllerTopPannel.setVisibility(View.GONE);
        mMediaControllerBarPannel.setVisibility(View.GONE);
        mMediaControllerVolumeWidget.setVisibility(View.GONE);
        mMediaControllerPauseImg.setVisibility(View.GONE);
        mMediaControllerRewindImg.setVisibility(View.GONE);
        mMediaControllerFastforwardImg.setVisibility(View.GONE);
        mMediaControllerFastforwardRewindTipTv.setVisibility(View.GONE);
        if (mBufferingContainer.getVisibility() == View.VISIBLE)
            mBufferingContainer.setVisibility(View.GONE);

    }

    public void onEpisodeQualitySelectActivityHide() {

    }

    // Runnable mRunnableStopFastforwardOrRewindTask = new Runnable() {
    //
    // @Override
    // public void run() {
    // // 快进快退：当前正在快进快退中,则直接开始播放,并取消快进快退
    // if (mSeekManager.isFastForwarding() || mSeekManager.isRewinding()) {
    // hide();
    // mControllerTopPannel.setVisibility(View.GONE);
    // mMediaControllerBarPannel.setVisibility(View.GONE);
    // mMediaControllerVolumeWidget.setVisibility(View.GONE);
    // mMediaControllerPauseImg.setVisibility(View.GONE);
    // mMediaControllerRewindImg.setVisibility(View.GONE);
    // mMediaControllerFastforwardImg.setVisibility(View.GONE);
    // mSeekManager.stopRewindOrFastforward(true);
    // mPlayerController.start();
    // }
    // }
    // };

    private Runnable mRunnableShowBufferingWidget = new Runnable() {

        @Override
        public void run() {
            if (mMediaControllerPauseImg.getVisibility() == View.VISIBLE)
                return;
            if (mBufferingContainer.getVisibility() != View.VISIBLE) {
                mTvBufferingPercentage.setText("");
                mBufferingContainer.setVisibility(View.VISIBLE);
            }
        }
    };

    private static final int MIN_PLAYBACK_BUFFERING_WAIT_TIME = 1000;

    public void showBufferingWidget() {
        mHandler.removeCallbacks(mRunnableShowBufferingWidget);
        mHandler.postDelayed(mRunnableShowBufferingWidget, MIN_PLAYBACK_BUFFERING_WAIT_TIME);
    }

    public void hideBufferingWidget() {
        mHandler.removeCallbacks(mRunnableShowBufferingWidget);
        if (mBufferingContainer.getVisibility() == View.VISIBLE) {
            mBufferingContainer.setVisibility(View.GONE);
        }
    }

    private Handler mHandler = new Handler() {

    };

    private void hideSeekWidget() {
        mMediaControllerPauseImg.setVisibility(View.GONE);
        mMediaControllerRewindImg.setVisibility(View.GONE);
        mMediaControllerFastforwardImg.setVisibility(View.GONE);
        mMediaControllerFastforwardRewindTipTv.setVisibility(View.GONE);
    }

    @Override
    protected void onStopTrackingTouchCallback() {
        super.onStopTrackingTouchCallback();
        mPlayerController.start();
        hide();
        mControllerTopPannel.setVisibility(View.GONE);
        mMediaControllerBarPannel.setVisibility(View.GONE);
        mMediaControllerVolumeWidget.setVisibility(View.GONE);
        mMediaControllerPauseImg.setVisibility(View.GONE);
        mMediaControllerRewindImg.setVisibility(View.GONE);
        mMediaControllerFastforwardImg.setVisibility(View.GONE);
        mMediaControllerFastforwardRewindTipTv.setVisibility(View.GONE);
    }

}
