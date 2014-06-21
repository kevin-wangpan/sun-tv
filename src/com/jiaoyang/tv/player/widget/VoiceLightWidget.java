package com.jiaoyang.tv.player.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiaoyang.base.audio.IAudioManager;
import com.jiaoyang.tv.util.ScreenUtil;
import com.jiaoyang.video.tv.R;

public class VoiceLightWidget extends LinearLayout {

    private static final int SHOW_TIME_OUT = 500;
    private static final int MSG_HIDE = 0;
    private static final int MSG_SHOW = MSG_HIDE + 1;
    private static final float MAX_BRINTNESS = 100 * 1.0f;// 将亮度的最大值放大100倍

    private ImageView mStatusIcon;
    private TextView mPercentage;
    private Animation fadeOut;
    private IAudioManager mAudioManager;
    private int mScrolledPixPerVoice;// 每变化一个梯度的音量需要滑动的像素值
    private int mCurrentDeltaPix;
    private float mScrolledPixPerBringhtness;//
    private float mCurrentDeltaBrintness;
    private int mMusicMaxVoice;

    public VoiceLightWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.voice_light_widget, this);

        mAudioManager = AudioManagerFactory.createAudioManager(null, context);
        int screenHeight = ScreenUtil.getScreenHeight(context);
        mMusicMaxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mScrolledPixPerVoice = (int) (screenHeight * 0.7) / mMusicMaxVoice;
        mScrolledPixPerBringhtness = screenHeight / MAX_BRINTNESS;

        fadeOut = new AlphaAnimation(1, 0.5f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new FadeoutAnimationListener());
    }

    @Override
    protected void onFinishInflate() {
        setupViews();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    private void fadeOut() {
        clearAnimation();
        startAnimation(fadeOut);
    }

    private void fadeIn() {
        setVisibility(View.VISIBLE);
    }

    private void setupViews() {
        mStatusIcon = (ImageView) findViewById(R.id.status);
        mPercentage = (TextView) findViewById(R.id.percentage);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public void onAudioManagerChange(IAudioManager audioManager) {
        mAudioManager = audioManager;
        mMusicMaxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void onVoiceChange(float delta, int distance) {
        mCurrentDeltaPix = mCurrentDeltaPix + (int) delta;
        mMusicMaxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (Math.abs(mCurrentDeltaPix) >= mScrolledPixPerVoice) {
            int index = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            index = index + (mCurrentDeltaPix / mScrolledPixPerVoice);
            if (index < 0) {
                index = 0;
            } else if (index > mMusicMaxVoice) {
                index = mMusicMaxVoice;
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

            mCurrentDeltaPix = 0;
        }

        performVoiceChange();
    }

    public float getScreenBrightness(Context context) {
        float result = 1;
        int value = 0;
        ContentResolver cr = context.getContentResolver();
        try {
            value = Settings.System.getInt(cr,
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        result = (float) value / 255;
        return result;
    }

    public void onLightChange(float delta, int distance, Window window) {
        WindowManager.LayoutParams params = window.getAttributes();

        mCurrentDeltaBrintness = mCurrentDeltaBrintness + delta;

        if (params.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            params.screenBrightness = getScreenBrightness(getContext());
        }

        if (Math.abs(mCurrentDeltaBrintness) >= mScrolledPixPerBringhtness) {
            float deltaBrightness = mCurrentDeltaBrintness / (mScrolledPixPerBringhtness * MAX_BRINTNESS);
            params.screenBrightness = params.screenBrightness + deltaBrightness;

            if (params.screenBrightness > 1.0f) {
                params.screenBrightness = 1.0f;
            }
            if (params.screenBrightness <= 0.01f) {
                params.screenBrightness = 0.01f;
            }
            window.setAttributes(params);
            mCurrentDeltaBrintness = 0;
        }

        performLightChange(params.screenBrightness);
    }

    private void performLightChange(float brightness) {
        int resId = R.drawable.light_0;

        if (brightness <= 0.01f) {
            resId = R.drawable.light_0;
        } else if (brightness <= 0.25f) {
            resId = R.drawable.light_25;
        } else if (brightness <= 0.5f) {
            resId = R.drawable.light_50;
        } else if (brightness < 1.0f) {
            resId = R.drawable.light_75;
        } else {
            resId = R.drawable.light_100;
        }

        updateViews(resId, (int) (brightness * 100));
    }

    private void performVoiceChange() {
        int voice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        float percent = (float) (voice * mScrolledPixPerVoice + mCurrentDeltaPix) / (mMusicMaxVoice
                * mScrolledPixPerVoice);// 恶心的处理为了增加一些音量进度的步进
        if (percent > 1) {
            percent = 1;
        }
        if (percent < 0) {
            percent = 0;
        }
        int resId = R.drawable.voice_30;
        if (percent == 0.0f) {
            resId = R.drawable.voice_mute;
        } else if (percent <= 0.5f) {
            resId = R.drawable.voice_30;
        } else if (percent < 1.0f) {
            resId = R.drawable.voice_60;
        } else {
            resId = R.drawable.voice_100;
        }

        updateViews(resId, (int) (percent * 100));
    }

    private void updateViews(int resId, int percentage) {
        mStatusIcon.setImageResource(resId);
        mPercentage.setText(percentage + "%");

        mHandler.sendEmptyMessage(MSG_SHOW);
        Message msg = mHandler.obtainMessage(MSG_HIDE);
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendMessageDelayed(msg, SHOW_TIME_OUT);
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
