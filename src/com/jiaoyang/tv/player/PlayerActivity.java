package com.jiaoyang.tv.player;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.MediaController.OnEpisodeSwitchListener;
import io.vov.vitamio.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.video.tv.R;
import com.suntv.tv.coporate.Agent;

public class PlayerActivity extends Activity implements OnInfoListener,
                OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener {

    private String playUrl;// = "http://pl.youku.com/playlist/m3u8?ts=1394676342&keyframe=0&vid=XNjU4MTc0Mjky&type=mp4";
    private String title;
    private int currentPlayedIndex;
    private VideoView mVideoView;
    private MediaController mMediaController;
    private ProgressBar loadingProgressBar;
    private TextView downloadRateView, loadRateView; //下载速度，缓冲百分比

    //每隔固定时间查询下播放状态，可用于播放记录的保存和跳过片尾
    private static final int TIMER_INTERVAL = 1000; //1s
    private Timer timer;
    private boolean autoSkip, autoPlayNext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
        if (PlayerAdapter.sPlayedMovie == null) {
            Toast.makeText(PlayerActivity.this, "加载后台播放数据失败", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.player);
        try {
            currentPlayedIndex = getIntent().getExtras().getInt(PlayerAdapter.VIDEO_INDEX_KEY, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVideoView = (VideoView) findViewById(R.id.buffer);
        loadingProgressBar = (ProgressBar) findViewById(R.id.probar);

        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
        PreferenceManager mgr = PreferenceManager.instance(this);
        autoPlayNext = mgr.getAutoPlayNext();
        autoSkip = mgr.getAutoSkip();
        loadPlayUrl();
    }

    private OnEpisodeSwitchListener episodeSwitchListener = new OnEpisodeSwitchListener() {
        @Override
        public void switchToPre() {
            switchToSpecifiedEpisode(-1);
        }
        @Override
        public void switchToNext() {
            switchToSpecifiedEpisode(1);
        }
    };

    private void switchToSpecifiedEpisode(int offset) {
        if (offset == 0) {
            //不做任何剧集跳转
            return;
        } else if(offset > 0) {
            if (currentPlayedIndex >= PlayerAdapter.sPlayedMovie.videos.length - offset) {
                //没有这么多的剧集，无法跳转到指定剧集
                Toast.makeText(PlayerActivity.this,
                        (offset == 1 ? "当前播放的已经是最后一集啦" : "不存在指定剧集，无法跳转"), Toast.LENGTH_LONG).show();
                return;
            } else {
                currentPlayedIndex += offset;
            }
        } else if (offset < 0) {
            if (currentPlayedIndex + offset < 0) {
                //无法跳转到指定剧集
                Toast.makeText(PlayerActivity.this,
                        (currentPlayedIndex == 0 ? "当前播放的已经是第一集啦" : "不存在指定剧集，无法跳转"), Toast.LENGTH_LONG).show();
                return;
            } else {
                currentPlayedIndex += offset;
            }
        }
        mVideoView.stopPlayback();
        loadingProgressBar.setVisibility(View.VISIBLE);
        downloadRateView.setVisibility(View.VISIBLE);
        downloadRateView.setText("正在自动加载下一集, 请稍等");
        loadPlayUrl();
    }
    private void loadPlayUrl() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                if (PlayerAdapter.sPlayedMovie == null) {
                    return null;
                }
                title = PlayerAdapter.sPlayedMovie.title;
                if (TextUtils.isEmpty(title)) {
                    title = "骄阳视频";
                } else if (PlayerAdapter.sPlayedMovie.videos.length > 1) {
                    title += " - 第" + (currentPlayedIndex + 1) + "集";
                }
                String videoId = PlayerAdapter.sPlayedMovie.videos[currentPlayedIndex];
                String baiduSid = PlayerAdapter.sPlayedMovie.baidu_sid;
                if (TextUtils.isEmpty(videoId + baiduSid)) {
                    return null;
                }
                String url;
                try {
                    url = Agent.getVideoURL(PlayerActivity.this, HttpDataFetcher.getInstance().getUserId(), baiduSid, currentPlayedIndex + 1, PlayerAdapter.RST_SUPER, PlayerAdapter.F_HLS);
                } catch (Exception e) {
                    url = null;
                    e.printStackTrace();
                }
                android.util.Log.e("jiaoyang", "播放地址=" + url);
                return url;//HttpDataFetcher.getInstance().loadPlayUrl(PlayerActivity.this, videoId, 1, baiduSid, currentPlayedIndex, PlayerAdapter.RST_NORMAL, PlayerAdapter.F_HLS);
            }

            @Override
            protected void onPostExecute(String result) {
                android.util.Log.e("jiaoyang", "播放地址：" + result);
                if (result == null || TextUtils.isEmpty(result)) {
                    Toast.makeText(PlayerActivity.this, "加载播放地址失败", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    playUrl = result;
                    beginPlay();
                }
            };
        }.execute();
    }

    private void beginPlay() {
        mVideoView.setVideoChroma(MediaPlayer.VIDEOCHROMA_RGB565);
        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ZOOM, 0f);
        Uri uri = Uri.parse(playUrl);
        mVideoView.setVideoURI(uri);
        mVideoView.setVideoName(title);
        mMediaController = new MediaController(this);
        mMediaController.setOnEpisodeSwitchListener(episodeSwitchListener);
        mVideoView.setMediaController(mMediaController);
        mVideoView.requestFocus();
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        if (autoSkip) {
            beginTimer();
        }
    }
    private void beginTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerTaskImpl();
            }
        }, 0, TIMER_INTERVAL);
    }
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
    private void timerTaskImpl() {
        if (mVideoView == null || !mVideoView.isPlaying()) {
            return;
        }
        int skipAfter = 0;
        try {
            skipAfter = Integer.parseInt(PlayerAdapter.sPlayedMovie.skip_after);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (skipAfter <= 0) {
            return;
        }
        android.util.Log.w("jiaoyang", "是否跳过片尾：skipAfter=" + skipAfter + ",cur=" + mVideoView.getCurrentPosition() + ",total=" + mVideoView.getDuration());
        if (mVideoView.getCurrentPosition() + skipAfter * 1000 > mVideoView.getDuration()) {
            android.util.Log.e("jiaoyang", "seekTo片尾");
            mVideoView.post(new Runnable() {
                @Override
                public void run() {
                    mVideoView.stopPlayback();
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    downloadRateView.setVisibility(View.VISIBLE);
                    downloadRateView.setText("正在自动加载下一集, 请稍等");
                    playNextIfExist();
                }
            });
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            android.util.Log.d("jiaoyang", "onInfo, BUFFERING_START");
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
                loadingProgressBar.setVisibility(View.VISIBLE);
                downloadRateView.setText("");
                loadRateView.setText("");
                downloadRateView.setVisibility(View.VISIBLE);
                loadRateView.setVisibility(View.VISIBLE);

            }
            break;
        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            android.util.Log.d("jiaoyang", "onInfo, BUFFERING_END");
            mVideoView.start();
            loadingProgressBar.setVisibility(View.GONE);
            downloadRateView.setVisibility(View.GONE);
            loadRateView.setVisibility(View.GONE);
            break;
        case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
            android.util.Log.d("jiaoyang", "onInfo, DOWNLOAD_RATE_CHANGED");
            downloadRateView.setText("" + extra + "kb/s");
            break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        android.util.Log.d("jiaoyang", "onBufferingUpdate: percent=" + percent + "%");
        loadRateView.setText(percent + "%");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        android.util.Log.e("jiaoyang", "onCompletion");
        playNextIfExist();
    }

    private void playNextIfExist() {
        if (!autoPlayNext) {
            return;
        }
        if (currentPlayedIndex >= PlayerAdapter.sPlayedMovie.videos.length - 1) {
            Toast.makeText(PlayerActivity.this, "整个剧集已经播放完成, 多谢观看", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentPlayedIndex++;
        loadPlayUrl();
    }
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        android.util.Log.e("jiaoyang", "onPrepared");
        // optional need Vitamio 4.0
        mediaPlayer.setPlaybackSpeed(1.0f);
        skipVideoHead();
    }

    private void skipVideoHead() {
        if (!autoSkip) {
            return;
        }
        int skipBefore = 0;
        try {
            skipBefore = Integer.parseInt(PlayerAdapter.sPlayedMovie.skip_before);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (skipBefore > 0) {
            mVideoView.seekTo(skipBefore * 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.getRepeatCount() == 0) {
                showSwitchEpisodePop();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    PopupWindow mSwitchPop;
    ImageButton mPreEpisode;
    ImageButton mNextEpisode;
    ImageButton mPause;
    private void showSwitchEpisodePop() {
        if (mSwitchPop == null) {
            initSwitchPop();
        }
        mSwitchPop.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
        mPreEpisode.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                mPreEpisode.requestFocus();
            }
        }, 500);
    }
    private void dismissSwitchEpisodePop() {
        if (mSwitchPop != null && mSwitchPop.isShowing()) {
            mSwitchPop.dismiss();
        }
    }
    private void initSwitchPop() {
        if (mSwitchPop != null) {
            return;
        }
        final View view = getLayoutInflater().inflate(R.layout.popup_switch, null);
        mPreEpisode = (ImageButton) view.findViewById(R.id.mediacontroller_play_pre_episode);
        mPreEpisode.setOnClickListener(clickListener);
        mNextEpisode = (ImageButton) view.findViewById(R.id.mediacontroller_play_next_episode);
        mNextEpisode.setOnClickListener(clickListener);
        mPause = (ImageButton) view.findViewById(R.id.mediacontroller_play_pause);
        mSwitchPop = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        mSwitchPop.setTouchable(true);
        mSwitchPop.setOutsideTouchable(true);
        mSwitchPop.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        mSwitchPop.getContentView().setFocusableInTouchMode(true);
        mSwitchPop.getContentView().setFocusable(true);
        mSwitchPop.getContentView().setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mSwitchPop != null && mSwitchPop.isShowing()) {
                        mSwitchPop.dismiss();
                    }
                    return true;
                } else if (event.getRepeatCount() == 0
                        && !mPreEpisode.isFocused()
                        && !mNextEpisode.isFocused()) {
                    mPreEpisode.requestFocus();
                }
                return false;
            }
        });
    }
    private OnClickListener clickListener = new OnClickListener() {
        
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.mediacontroller_play_next_episode:
                Log.e("jiaoyang", "mediacontroller_play_next_episode");
                switchToSpecifiedEpisode(1);
                dismissSwitchEpisodePop();
                break;
            case R.id.mediacontroller_play_pre_episode:
                Log.e("jiaoyang", "mediacontroller_play_pre_episode");
                switchToSpecifiedEpisode(-1);
                dismissSwitchEpisodePop();
                break;

            default:
                break;
            }
        }
    };
}
