package com.jiaoyang.tv.player.widget;

import java.util.ArrayList;
import java.util.List;

import com.jiaoyang.base.audio.IAudioManager;
import com.jiaoyang.base.sys.DeviceHelper;
import com.jiaoyang.base.sys.SystemConfigs;
import com.jiaoyang.tv.player.widget.VoiceLightWidget.FadeoutAnimationListener;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.tv.util.ScreenUtil;
import com.jiaoyang.video.tv.R;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class VolumeWidget extends RelativeLayout {
    
    private final Logger LOG = Logger.getLogger(VolumeWidget.class);

    private static final int SHOW_TIME_OUT = 2000;
    private static final int MAX_VOLUME_LEVEL = 10;
    private static final int MSG_HIDE = 0;
    private static final int MSG_SHOW = MSG_HIDE + 1;
    
    private Animation fadeOut;
    private ImageView mVolumeLevelImg;
    private ImageView mVolumeStatusImg;
    private LinearLayout mVolumeLevelLayout;
    private IAudioManager mAudioManager;
    
    private int curLevelVolume = -1;
    private List<View> mListVolumeLevel;
    
    private Context mContext;
    
    public VolumeWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.player_volume_widget, this);
        mAudioManager = AudioManagerFactory.createAudioManager(null, context);
        
        fadeOut = new AlphaAnimation(1, 0.5f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new FadeoutAnimationListener());
        
    }
    
    @Override
    protected void onFinishInflate() {
        initViews();
    }
    
    private void initViews(){
        mVolumeLevelImg = (ImageView) findViewById(R.id.volume_level_img);
        mVolumeStatusImg = (ImageView) findViewById(R.id.volume_status_img);
        mVolumeLevelLayout = (LinearLayout) findViewById(R.id.volume_level_layout);
        
        mListVolumeLevel = new ArrayList<View>();
        
        for(int level = 0 ; level < MAX_VOLUME_LEVEL ; level++){
            
            View volumeLevelView = new View(mContext);
            volumeLevelView.setBackgroundResource(R.drawable.player_volume_level_selector);
            volumeLevelView.setSelected(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            float density = mContext.getResources().getDisplayMetrics().density;
            lp.width = LayoutParams.WRAP_CONTENT;
            lp.height = (int) (density * 10);
            lp.bottomMargin = (int) (density * 3);
            lp.topMargin = (int) (density * 3);
            mVolumeLevelLayout.addView(volumeLevelView, 0 ,lp);
            mListVolumeLevel.add(volumeLevelView);
            
        }
        
    }
    
    public void volumeUp(){
        
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        if(curVolume < 0 || maxVolume < 0 || curVolume > maxVolume)
            return;
        
        if(curLevelVolume < 0) curLevelVolume = (int) (((float) curVolume / maxVolume) * MAX_VOLUME_LEVEL);
        
        curLevelVolume++;
        if(curLevelVolume > MAX_VOLUME_LEVEL) curLevelVolume = MAX_VOLUME_LEVEL;
        
        int newVolume = (int) (((float) curLevelVolume / MAX_VOLUME_LEVEL) * maxVolume);
        
//        LOG.i(Logger.TAG_LUKE, "up maxV,curV,newV,newL = " + maxVolume +"-"+ curVolume +"-"+ newVolume +"-"+ curLevelVolume);
        
        updateViews(newVolume, curLevelVolume);
        
    }
    
    public void volumeDown(){
        
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        if(curVolume < 0 || maxVolume < 0 || curVolume > maxVolume)
            return;
        
        if(curLevelVolume < 0) curLevelVolume = (int) (((float) curVolume / maxVolume) * MAX_VOLUME_LEVEL);
        
        curLevelVolume--;
        if(curLevelVolume < 0) curLevelVolume = 0;
        
        int newVolume = (int) (((float) curLevelVolume / MAX_VOLUME_LEVEL) * maxVolume);
        
//        LOG.i(Logger.TAG_LUKE, "up maxV,curV,newV,newL = " + maxVolume +"-"+ curVolume +"-"+ newVolume +"-"+ curLevelVolume);
        
        updateViews(newVolume, curLevelVolume);
        
    }
    
    // levelVolume : 0 ~ MAX_VOLUME_LEVEL
    private void updateViews(int volume, int levelVolume){
        
//        if(levelVolume < 0) levelVolume = 0;
//        if(levelVolume >= MAX_VOLUME_LEVEL) levelVolume = MAX_VOLUME_LEVEL - 1;
//        mVolumeLevelImg.setImageLevel(levelVolume);
        
        for(int index = 0; index < MAX_VOLUME_LEVEL; index++){
            View volumeLevelView = mListVolumeLevel.get(index);
            int volumeLevel = index + 1;
            
            if(volumeLevel <= levelVolume){
                if(!volumeLevelView.isSelected())
                    volumeLevelView.setSelected(true);
                
            }else{
                if(volumeLevelView.isSelected())
                    volumeLevelView.setSelected(false);
            }
            
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        
        mHandler.sendEmptyMessage(MSG_SHOW);
        Message msg = mHandler.obtainMessage(MSG_HIDE);
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendMessageDelayed(msg, SHOW_TIME_OUT);
        
    }
    
    private void fadeOut(){
        clearAnimation();
        startAnimation(fadeOut);
    }
    
    private void fadeIn(){
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
