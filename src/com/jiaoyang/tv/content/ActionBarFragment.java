package com.jiaoyang.tv.content;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiaoyang.tv.JyBaseFragment;
import com.jiaoyang.tv.widget.JySimpleDialog;
import com.jiaoyang.video.tv.R;

public class ActionBarFragment extends JyBaseFragment {

    TextView mTime;
    ImageView mNetworkStatus;
    Timer timer;
    private static final int UPDATE_INTERVAL = 60 * 1000; // 1m
    private Handler mUIHandler = new Handler();

    private BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connManager = (ConnectivityManager) context.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connManager.getActiveNetworkInfo();
            int level = 5;
            if (info == null || !info.isConnected()) {
                level = 0;
            } else {
                int type = info.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    if (wifiMgr != null) {
                        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                        level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
                    }
                }
            }
            if (mNetworkStatus != null) {
                mNetworkStatus.setImageLevel(level);
            }
        }
    };

    private JySimpleDialog mQuitLoginDialog = null;

    public void dismissShownDialog() {
        if (mQuitLoginDialog != null && mQuitLoginDialog.isShowing()) {
            mQuitLoginDialog.dismiss();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.actionbar_fragment, container, false);
        initViews(root);
        return root;
    }

    private void initViews(View v) {
        mTime = (TextView) v.findViewById(R.id.time);
        mNetworkStatus = (ImageView) v.findViewById(R.id.network_status);

    }


    @Override
    public void onResume() {
        super.onResume();
        startTimer();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mConnectionChangeReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
        getActivity().unregisterReceiver(mConnectionChangeReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startTimer() {
        timer = new Timer("time_updater");
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Message msg = Message.obtain(mUIHandler, new Runnable() {

                    @Override
                    public void run() {
                        if (mTime != null) {
                            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
                            String curTime = df.format(new Date());
                            mTime.setText(curTime);
                        }
                    }
                });
                msg.sendToTarget();
            }
        }, 0, UPDATE_INTERVAL);
    }

    private void stopTimer() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer = null;
    }

}
