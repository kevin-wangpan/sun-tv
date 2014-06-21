package com.jiaoyang.tv.player.widget;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.video.tv.R;

public class TimePowerWidget extends RelativeLayout {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TimePowerWidget.class.getSimpleName());

    private static final int TIME_UPDATE_INTERVAL = 60 * 1000;
    private Context mContext;
    private TextView mTime;
    private ImageView mPowerBody;
    private ImageView mChargeCover;
    private ImageView mChargeThunder;

    private int mHeight;
    private BatteryBroadcastReceiver mBatteryChangeReceiver = new BatteryBroadcastReceiver();
    private Handler mTimerUpdateHandler = new Handler();
    private Runnable mTimerUpdateRunnable = new Runnable() {

        @Override
        public void run() {
            mTime.setText(getCurTime());
            mTimerUpdateHandler.postDelayed(this, TIME_UPDATE_INTERVAL);
        }
    };

    public TimePowerWidget(Context context) {
        super(context);
    }

    public TimePowerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.time_power_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        fillView();
    }

    private void fillView() {
        mTime = (TextView) findViewById(R.id.mediacontroller_tv_time);
        mPowerBody = (ImageView) findViewById(R.id.mediacontroller_iv_battery_charge);
        mPowerBody.measure(0, 0);
        mHeight = mPowerBody.getMeasuredHeight();
        mChargeCover = (ImageView) findViewById(R.id.mediacontroller_iv_battery_charging);
        mChargeThunder = (ImageView) findViewById(R.id.mediacontroller_charging_thunder);
    }

    private void startTimer() {
        mTimerUpdateHandler.post(mTimerUpdateRunnable);
    }

    private void stopTimer() {
        mTimerUpdateHandler.removeCallbacks(mTimerUpdateRunnable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        startTimer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        stopTimer();
    }

    public void registerBatteryReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(mBatteryChangeReceiver, intentFilter);
    }

    public void unregisterBatteryReceiver() {
        mContext.unregisterReceiver(mBatteryChangeReceiver);
    }

    private String getCurTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        String hourString = Integer.toString(hour);
        String minString = Integer.toString(min);
        if (hour < 10) {
            hourString = "0" + hourString;
        }
        if (min < 10) {
            minString = "0" + minString;
        }
        return hourString + ":" + minString;
    }

    public class BatteryBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                final int level = intent.getIntExtra("level", 0);
                final int scale = intent.getIntExtra("scale", 100);
                final int status = intent.getIntExtra("status", -1);

                switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    mChargeThunder.setVisibility(View.VISIBLE);
                    mChargeThunder.setImageResource(R.drawable.mediacontroller_chargin_thunder);

                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    mChargeThunder.setVisibility(View.GONE);

                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    mChargeThunder.setVisibility(View.GONE);

                    break;
                default:
                    break;
                }

                int curPower = level * 100 / scale;
                if (curPower > 90) {
                    mPowerBody.setBackgroundResource(R.drawable.meida_player_battery_shape_green);
                    mChargeCover.setVisibility(View.GONE);
                } else {
                    mChargeCover.setVisibility(View.VISIBLE);
                    if (curPower < 15) {
                        curPower = 15;
                    }
                    final int deltaHeight = mHeight * curPower / 100;
                    android.view.ViewGroup.LayoutParams linearParams = mChargeCover.getLayoutParams();
                    linearParams.height = deltaHeight;
                    mChargeCover.setLayoutParams(linearParams);
                }
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                mChargeThunder.setVisibility(View.GONE);
            }
        }
    }
}
