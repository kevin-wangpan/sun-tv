package com.jiaoyang.tv;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.jiaoyang.base.caching.ImageCache;
import com.jiaoyang.base.caching.ImageFetcher;
import com.jiaoyang.base.sys.DeviceHelper;
import com.jiaoyang.tv.app.JiaoyangTvApplication;
import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.tv.data.StartUpPoster;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.video.tv.R;

public class LaunchingActivity extends Activity {
    private static final int MIN_SHOW_TIME = 4000;
    private static final int MAX_SHOW_TIME = 9000;

    private ImageView mPoster;
    private TransitionDrawable mTd;

    private static final int DISPLAY_DEFAULT_BG_LATER = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DISPLAY_DEFAULT_BG_LATER:
//                NinePatchDrawable npd = (NinePatchDrawable) getResources().getDrawable(R.drawable.main_start);
//                mTd = new TransitionDrawable(new Drawable[] { npd, npd });
//                mTd.setId(0, 0);
//                mTd.setId(1, 1);
//                mPoster.setImageDrawable(mTd);
                break;
            }
        }
    };

    private static final Logger LOG = Logger.getLogger(LaunchingActivity.class);

    @Override
    public void onBackPressed() {
        showQuitDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isNormalStartup()) {
            LOG.debug("onCreate.");

            super.setContentView(R.layout.startup);

            mPoster = (ImageView)(LaunchingActivity.this.findViewById(R.id.start_up_bg));
            mHandler.sendEmptyMessageDelayed(DISPLAY_DEFAULT_BG_LATER, 0);
            mStartupTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mTimeoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            LOG.info("application exit because of special startup.");

            exit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        QuitDialogManager.getInstance().dismissShownDialog();
        mStartupTask.cancel(true);
        mTimeoutTask.cancel(true);
    }

    private void showQuitDialog() {
        QuitDialogManager.getInstance().showKkQuitDialog(this, new Runnable() {

            @Override
            public void run() {
                exit();
            }
        }, null);
    }

    private void exit() {
        finish();

        getApplication().onTerminate();
    }

    private boolean isNormalStartup() {
        return true;
    }

    private final AsyncTask<Void, StartUpPoster, Void> mStartupTask = new AsyncTask<Void, StartUpPoster, Void>() {

        @Override
        protected Void doInBackground(Void... params) {
            // 提前获取相关参数，以方便后续使用
            DeviceHelper.loadScreenInfo(LaunchingActivity.this);
            //StartUpPoster startUpPoster = DataProxy.getInstance().getStartUpPoster();
            //publishProgress(startUpPoster);
            Util.getPeerid(LaunchingActivity.this);
            Util.getSelfAppVersion(LaunchingActivity.this);
            Util.getIMEI(LaunchingActivity.this);
            HttpDataFetcher.getInstance().loadUid();
            HttpDataFetcher.getInstance().loadHomePage();
            return null;
        }

        @Override
        protected void onProgressUpdate(final StartUpPoster... values) {
            if (values == null || values[0] == null) {
                return;
            }
            ImageFetcher fetcher = ImageFetcher.getInstance(LaunchingActivity.this);
            ImageCache cache = ((JiaoyangTvApplication)getApplication()).getImageCache();
            fetcher.setImageCache(cache);
            Bitmap poster = cache.getBitmapFromDiskCache(values[0].getStartup(), true);
            if (poster == null) {
                //更换新的启动了图了，去下载
                fetcher.loadImage(values[0].getStartup(), true);
                //尝试加载原来的启动图
                PreferenceManager pm = PreferenceManager.instance(getApplication());
                String[] oldPosters = pm.retriveStartupPosterUrls();
                if (oldPosters != null) {
                    poster = cache.getBitmapFromDiskCache(oldPosters[0], true);
                }
            }
            if (poster != null){
                if (mTd != null) {
                    mTd.setDrawableByLayerId(1, new BitmapDrawable(LaunchingActivity.this.getResources(), poster));
                    mTd.startTransition(100);
                } else if (mPoster != null) {
                    mHandler.removeMessages(DISPLAY_DEFAULT_BG_LATER);
                    mPoster.setImageBitmap(poster);
                }
            }
        }

        @Override
        protected void onPostExecute(Void result) {
        };
    };

    private final AsyncTask<Void, Void, Void> mTimeoutTask = new AsyncTask<Void, Void, Void>() {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(MIN_SHOW_TIME);

                mStartupTask.get(MAX_SHOW_TIME - MIN_SHOW_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOG.warn("interrupted.");
            } catch (ExecutionException e) {
                LOG.warn(e);
            } catch (TimeoutException e) {
                LOG.warn("timeout.");
            } catch (CancellationException e) {
                LOG.warn("AsyncTask has cancled ");
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            finish();

            startActivity(new Intent(LaunchingActivity.this, MainActivity.class));
        };
    };
}
