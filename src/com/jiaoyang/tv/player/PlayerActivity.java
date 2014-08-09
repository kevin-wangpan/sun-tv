package com.jiaoyang.tv.player;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaoyang.tv.data.Episode;
import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.video.tv.R;

public class PlayerActivity extends Activity implements OnInfoListener,
                OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener {

    private String playUrl;// = "http://pl.youku.com/playlist/m3u8?ts=1394676342&keyframe=0&vid=XNjU4MTc0Mjky&type=mp4";
    private String title;
    private VideoView mVideoView;
    private ProgressBar loadingProgressBar;
    private TextView downloadRateView, loadRateView;

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
        mVideoView = (VideoView) findViewById(R.id.buffer);
        loadingProgressBar = (ProgressBar) findViewById(R.id.probar);

        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
        PreferenceManager mgr = PreferenceManager.instance(this);
        autoPlayNext = mgr.getAutoPlayNext();
        autoSkip = mgr.getAutoSkip();
        loadPlayUrl();
    }

    private void loadPlayUrl() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    return null;
                }
                title = extras.getString(PlayerAdapter.VIDEO_TITLE_KEY);
                if (TextUtils.isEmpty(title)) {
                    title = "骄阳视频";
                }
                String videoId = extras.getString(PlayerAdapter.VIDEO_ID_KEY);
                String baiduSid = extras.getString(PlayerAdapter.VIDEO_SID_KEY);
                int episodeIndex = extras.getInt(PlayerAdapter.VIDEO_INDEX_KEY);
                if (TextUtils.isEmpty(videoId + baiduSid)) {
                    return null;
                }
                return HttpDataFetcher.getInstance().loadPlayUrl(PlayerActivity.this, videoId, 1, baiduSid, episodeIndex, PlayerAdapter.RST_NORMAL, PlayerAdapter.F_HLS);
            }

            @Override
            protected void onPostExecute(String result) {
                android.util.Log.d("jiaoyang", "播放地址：" + result);
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
        mVideoView.setMediaController(new MediaController(this));
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
        android.util.Log.w("wangpan", "是否跳过片尾：skipAfter=" + skipAfter + ",cur=" + mVideoView.getCurrentPosition() + ",total=" + mVideoView.getDuration());
        if (mVideoView.getCurrentPosition() + skipAfter * 1000 > mVideoView.getDuration()) {
            android.util.Log.e("wangpan", "seekTo片尾");
            mVideoView.stopPlayback();
            playNextIfExist();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            android.util.Log.d("wangpan", "onInfo, BUFFERING_START");
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
            android.util.Log.d("wangpan", "onInfo, BUFFERING_END");
            mVideoView.start();
            loadingProgressBar.setVisibility(View.GONE);
            downloadRateView.setVisibility(View.GONE);
            loadRateView.setVisibility(View.GONE);
            break;
        case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
            android.util.Log.d("wangpan", "onInfo, DOWNLOAD_RATE_CHANGED");
            downloadRateView.setText("" + extra + "kb/s");
            break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        android.util.Log.d("wangpan", "onBufferingUpdate: percent=" + percent + "%");
        loadRateView.setText(percent + "%");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        android.util.Log.e("wangpan", "onCompletion");
        playNextIfExist();
    }

    private void playNextIfExist() {
        if (!autoPlayNext) {
            return;
        }
        loadPlayUrl();
    }
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        android.util.Log.e("wangpan", "onPrepared");
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
}
