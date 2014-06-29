package com.jiaoyang.tv.player;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.video.tv.R;

public class PlayerActivity extends Activity implements OnInfoListener, OnBufferingUpdateListener {

    private String playUrl = "http://pl.youku.com/playlist/m3u8?ts=1394676342&keyframe=0&vid=XNjU4MTc0Mjky&type=mp4";
    private String title;
    private VideoView mVideoView;
    private ProgressBar loadingProgressBar;
    private TextView downloadRateView, loadRateView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.player);
        mVideoView = (VideoView) findViewById(R.id.buffer);
        loadingProgressBar = (ProgressBar) findViewById(R.id.probar);

        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
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
                if (TextUtils.isEmpty(videoId)) {
                    return null;
                }
                return HttpDataFetcher.getInstance().loadPlayUrl(videoId, 1);
            }

            @Override
            protected void onPostExecute(String result) {
                if (TextUtils.isEmpty(result)) {
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
        Uri uri = Uri.parse(playUrl);
        mVideoView.setVideoURI(uri);
        mVideoView.setVideoName(title);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // optional need Vitamio 4.0
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
    }
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
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
            mVideoView.start();
            loadingProgressBar.setVisibility(View.GONE);
            downloadRateView.setVisibility(View.GONE);
            loadRateView.setVisibility(View.GONE);
            break;
        case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
            downloadRateView.setText("" + extra + "kb/s");
            break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }

}
