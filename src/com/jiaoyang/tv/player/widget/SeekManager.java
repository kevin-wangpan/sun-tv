package com.jiaoyang.tv.player.widget;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.jiaoyang.base.widget.MediaController;
import com.jiaoyang.base.widget.MediaController.MediaPlayerControl;
import com.jiaoyang.tv.util.Logger;

public class SeekManager {
    
    private final Logger LOG = Logger.getLogger(SeekManager.class);
    
    private static final int SEEK_INTERVAL_TIME = 200;
    private static final int SEEK_DELTA_TIME = 15000;
    private SEEK_RATE mCurSeekRate = SEEK_RATE.LOW;
    
    private MediaPlayerControl mMediaPlayerController;
    private MediaController mMediaController;
    private ProgressBar mProgressBar;
    private OnSeekListener mOnSeekListener;
    
    private Handler mHandler = new Handler();
    
    private float mCurSeekPercentage = -1f;
    private volatile boolean isFastForwarding = false;
    private volatile boolean isRewinding = false;
    
    public SeekManager(MediaPlayerControl mediaPlayerController, MediaController mediaController) {
        this.mMediaPlayerController = mediaPlayerController;
        this.mMediaController = mediaController;
        this.mProgressBar = mMediaController.getProgressBar();
        mRunnableFastForwardTask = new RunnableFastForwardTask(false);
        mRunnableRewindTask = new RunnableRewindTask(false);
    }
    
    RunnableFastForwardTask mRunnableFastForwardTask = null;
    RunnableRewindTask mRunnableRewindTask = null;
    
    class RunnableRewindTask implements Runnable {

        private int curTime = 0;
        private int durTime = 0;
        
        public RunnableRewindTask(boolean isFastforwarding) {
            
            if(isFastforwarding){
                durTime = mMediaPlayerController.getDuration();
                curTime = (int) (((float) mProgressBar.getProgress() / mProgressBar.getMax()) * durTime);
            }else{
                curTime = mMediaPlayerController.getCurrentPosition();
                durTime = mMediaPlayerController.getDuration();
            }
            
            mCurSeekPercentage = (float) curTime / durTime;
        }
        
        @Override
        public void run() {
            
            if(curTime < 0 && durTime <= 0 && durTime < curTime){
                isRewinding = false;
                curTime = durTime = 0;
                if(mOnSeekListener != null) mOnSeekListener.onRewindError();
                return;
            }
            
            int seekDeltaTime = (int) (mCurSeekRate == null ? SEEK_DELTA_TIME : SEEK_DELTA_TIME * mCurSeekRate.getRateFactor());
            int newTime = curTime - seekDeltaTime;
//            int newTime = curTime - SEEK_DELTA_TIME;
            
            if(newTime <= 0){
                isRewinding = false;
                curTime = durTime = 0;
                if(mOnSeekListener != null) mOnSeekListener.onRewindCompleted();
                return;
            }
            
            curTime = newTime;
            mCurSeekPercentage = (float) curTime / durTime;
            updateSeekbarProgress(mCurSeekPercentage);
//            mMediaPlayerController.seekTo(newTime);
            mHandler.postDelayed(this, SEEK_INTERVAL_TIME);
            
        }
        
    }
    
    class RunnableFastForwardTask implements Runnable {

        private int curTime = 0;
        private int durTime = 0;
        
        public RunnableFastForwardTask(boolean isRewinding) {
            
            if(isRewinding){
                durTime = mMediaPlayerController.getDuration();
                curTime = (int) (((float) mProgressBar.getProgress() / mProgressBar.getMax()) * durTime);
            }else{
                curTime = mMediaPlayerController.getCurrentPosition();
                durTime = mMediaPlayerController.getDuration();
            }
            
            mCurSeekPercentage = (float) curTime / durTime;
        }
        
        @Override
        public void run() {
            
            if(curTime < 0 && durTime <= 0 && durTime < curTime){
                isFastForwarding = false;
                curTime = durTime = 0;
                if(mOnSeekListener != null) mOnSeekListener.onFastforwardError();
                return;
            }
            
            int seekDeltaTime = (int) (mCurSeekRate == null ? SEEK_DELTA_TIME : SEEK_DELTA_TIME * mCurSeekRate.getRateFactor());
            int newTime = curTime + seekDeltaTime;
//            int newTime = curTime + SEEK_DELTA_TIME;
            
            if(newTime >= durTime){
                isFastForwarding = false;
                curTime = durTime = 0;
                if(mOnSeekListener != null) mOnSeekListener.onFastforwardCompleted();
                return;
            }
            
            curTime = newTime;
            LOG.i(Logger.TAG_LUKE, "seek curTime = " + curTime);
            mCurSeekPercentage = (float) curTime / durTime;
            updateSeekbarProgress(mCurSeekPercentage);
//            mMediaPlayerController.seekTo(newTime);
            mHandler.postDelayed(this, SEEK_INTERVAL_TIME);
            
        }
        
    }
    
    public boolean startFastforward(){
        
        boolean isRewinding = isRewinding();
        
        if(isFastForwarding()) return false;
        
        if(!canFastforward(isRewinding)) return false;
        
        stopRewindOrFastforward(false);
        
        LOG.i(Logger.TAG_LUKE, "startFastforward");
        
        mMediaController.setFastforwardOrRewinding(true);
        isFastForwarding = true;
        changeSeekRate(SEEK_RATE.LOW);
//        mMediaPlayerController.pause();
        mRunnableFastForwardTask = new RunnableFastForwardTask(isRewinding);
        mHandler.post(mRunnableFastForwardTask);
        
        return true;
        
    }
    
    public boolean startRewind(){
        
        boolean isFastforwarding = isFastForwarding();
        
        if(isRewinding()) return false;
        
        if(!canRewind(isFastforwarding)) return false;
        
        stopRewindOrFastforward(false);
        
        LOG.i(Logger.TAG_LUKE, "startRewind");
        
        mMediaController.setFastforwardOrRewinding(true);
        isRewinding = true;
        changeSeekRate(SEEK_RATE.LOW);
//        mMediaPlayerController.pause();
        mRunnableRewindTask = new RunnableRewindTask(isFastforwarding);
        mHandler.post(mRunnableRewindTask);
        
        return true;
        
    }
    
    public void stopRewindOrFastforward(boolean needSeek){
        
        LOG.i(Logger.TAG_LUKE, "stopRewindOrFastforward");
        
        if(needSeek){
            if(mMediaPlayerController != null && mCurSeekPercentage >= 0 && mCurSeekPercentage <= 1){
                int progress = (int) (mCurSeekPercentage * mMediaPlayerController.getDuration());
                mMediaPlayerController.seekTo(progress);
            }
        }
        
        mCurSeekPercentage = -1f;
        isFastForwarding = false;
        isRewinding = false;
        changeSeekRate(SEEK_RATE.LOW);
        mHandler.removeCallbacks(mRunnableFastForwardTask);
        mHandler.removeCallbacks(mRunnableRewindTask);
        mMediaController.setFastforwardOrRewinding(false);
        
    }
    
    public boolean canFastforward(boolean isRewinding){
        
        int curTime = 0;
        int durTime = 0;
        
        if(isRewinding){
            durTime = mMediaPlayerController.getDuration();
            curTime = (int) (((float) mProgressBar.getProgress() / mProgressBar.getMax()) * durTime);
        }else{
            curTime = mMediaPlayerController.getCurrentPosition();
            durTime = mMediaPlayerController.getDuration();
        }
        
        if(curTime < 0 && durTime <= 0 && durTime < curTime)
            return false;
        
        int newTime = curTime + SEEK_DELTA_TIME;
        
        if(newTime >= durTime)
            return false;
        
        return true;
        
    }
    
    public boolean canRewind(boolean isFastfarwarding){
        
        int curTime = 0;
        int durTime = 0;
        
        if(isFastfarwarding){
            durTime = mMediaPlayerController.getDuration();
            curTime = (int) (((float) mProgressBar.getProgress() / mProgressBar.getMax()) * durTime);
        }else{
            curTime = mMediaPlayerController.getCurrentPosition();
            durTime = mMediaPlayerController.getDuration();
        }
        
        if(curTime < 0 && durTime <= 0 && durTime < curTime)
            return false;
        
        int newTime = curTime - SEEK_DELTA_TIME;
        
        if(newTime <= 0)
            return false;
        
        return true;
        
    }
    
    private void updateSeekbarProgress(float percentage){
        if(this.mProgressBar != null && percentage >= 0 && percentage <= 1){
            int progress = (int) (percentage * mProgressBar.getMax());
            LOG.i(Logger.TAG_LUKE, "seek progress = " + progress);
            mProgressBar.setProgress(progress);
        }
    }
    
    public boolean isFastForwarding(){
        return isFastForwarding;
    }
    
    public boolean isRewinding(){
        return isRewinding;
    }
    
    public void setOnSeekListener(OnSeekListener onSeekListener){
        this.mOnSeekListener = onSeekListener;
    }
    
    public interface OnSeekListener {
        void onFastforwardError();
        void onFastforwardCompleted();
        void onRewindError();
        void onRewindCompleted();
    }
    
    private OnSeekRateListener mOnSeekRateListener;
    
    public void setOnSeekRateListener(OnSeekRateListener listener){
        this.mOnSeekRateListener = listener;
    }
    
    public interface OnSeekRateListener {
        void onSeekRateChanged(SEEK_RATE preSeekRate, SEEK_RATE curSeekRate);
    }
    
    public void accelerateSeekRate(){
        
        SEEK_RATE newSeekRate = this.mCurSeekRate.accelerate();
        LOG.i(Logger.TAG_LUKE, "accelerateSeekRate = " + (newSeekRate == null ? "isTop":newSeekRate.getRateFactor()));
        if(newSeekRate != null)
            changeSeekRate(newSeekRate);
        
    }
    
    @Deprecated
    public void decelerateSeekRate(){
        
        SEEK_RATE newSeekRate = this.mCurSeekRate.accelerate();
        if(newSeekRate != null)
            changeSeekRate(newSeekRate);
        
    }
    
    private void changeSeekRate(SEEK_RATE seekRate){
        if(mOnSeekRateListener != null){
            mOnSeekRateListener.onSeekRateChanged(mCurSeekRate, seekRate);
        }
        mCurSeekRate = seekRate;
    }
    
    public enum SEEK_RATE {
        
        LOW(1.0f),MEDIUM(2.0f),HIGH(4.0f);
        
        private SEEK_RATE(float mRateFactor) {
            this.mRateFactor = mRateFactor;
        }
        
        private float mRateFactor = 1.0f;

        public float getRateFactor() {
            return mRateFactor;
        }
        
        public List<SEEK_RATE> getSupportedSeekRates(){
            List<SEEK_RATE> list = new ArrayList<SeekManager.SEEK_RATE>();
            list.add(LOW);
            list.add(MEDIUM);
            list.add(HIGH);
            return list;
        }
        
        public SEEK_RATE accelerate(){
            List<SEEK_RATE> listAll = getSupportedSeekRates();
            int listIndex = listAll.indexOf(this);
            int listSize = listAll.size();
            if((listIndex + 1) < listSize){
                return listAll.get(listIndex + 1);
            }
            return null;
        }
        
        public SEEK_RATE decelerate(){
            List<SEEK_RATE> listAll = getSupportedSeekRates();
            int listIndex = listAll.indexOf(this);
            int listSize = listAll.size();
            if((listIndex - 1) >= 0){
                return listAll.get(listIndex - 1);
            }
            return null;
        }
        
    }
    

}
