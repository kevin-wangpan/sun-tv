package com.jiaoyang.tv.update;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.jiaoyang.tv.data.DataProxy;
import com.jiaoyang.tv.data.UpdateInfo;
import com.jiaoyang.tv.util.Logger;

/**
 * 加载更新信息
 * 
 * @author admin
 * 
 */
public class AsyncLoadUpdateInfo extends AsyncTask<Void, Void, UpdateInfo> {
    public static final Logger LOG = Logger.getLogger(AsyncLoadUpdateInfo.class);

    public static final int LOAD_UPDATE_INFO_COMPLETED = 1;

    private Context mContext;
    private Handler mHandler;

    public AsyncLoadUpdateInfo(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    @Override
    protected UpdateInfo doInBackground(Void... params) {
        String version = Config.getVerName(mContext);
        String osVersion = android.os.Build.VERSION.RELEASE;
        return DataProxy.getInstance().getUpdateInfo(version, osVersion);
    }

    @Override
    protected void onPostExecute(UpdateInfo result) {
        if (!isCancelled()) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(LOAD_UPDATE_INFO_COMPLETED, result);
                mHandler.sendMessage(msg);
            }
        }
    }
}
