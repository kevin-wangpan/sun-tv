package com.jiaoyang.tv.player.widget;

import java.util.Formatter;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiaoyang.video.tv.R;

public class VideoGestureSeekWidget extends LinearLayout {
    private static final int SHOW_TIME_OUT = 500;
    private static final int MSG_HIDE = 0;
    private static final int MSG_SHOW = MSG_HIDE + 1;

    private TextView mProgress;
    private Animation fadeOut;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    public VideoGestureSeekWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.gesture_seek_widget, this);
        fadeOut = new AlphaAnimation(1, 0.5f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new FadeoutAnimationListener());
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    @Override
    protected void onFinishInflate() {
        setupViews();
    }

    public boolean isVisiable() {
        return getVisibility() == View.VISIBLE;
    }

    public void onSeek(int pos, int duration) {
        String text = stringForTime(pos) + "/" + stringForTime(duration);
        updateViews(text);
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60);

        mFormatBuilder.setLength(0);
        if (totalSeconds > 0) {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void updateViews(String txtMsg) {
        mProgress.setText(txtMsg);

        mHandler.sendEmptyMessage(MSG_SHOW);
        Message msg = mHandler.obtainMessage(MSG_HIDE);
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendMessageDelayed(msg, SHOW_TIME_OUT);
    }

    private void setupViews() {
        mProgress = (TextView) findViewById(R.id.progress);
    }

    protected void fadeOut() {
        clearAnimation();
        startAnimation(fadeOut);
    }

    protected void fadeIn() {
        setVisibility(View.VISIBLE);
    }

    public class FadeoutAnimationListener implements AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MSG_HIDE:
                fadeOut();
                break;
            case MSG_SHOW:
                fadeIn();
                break;

            default:
                break;
            }
        }
    };
}
