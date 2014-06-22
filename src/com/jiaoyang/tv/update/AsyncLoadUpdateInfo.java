package com.jiaoyang.tv.update;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.tv.data.ApkUpdateInfo;
import com.jiaoyang.tv.util.Logger;

/**
 * 加载更新信息
 * 
 * @author admin
 * 
 */
public class AsyncLoadUpdateInfo extends AsyncTask<Void, Void, ApkUpdateInfo> {
    public static final Logger LOG = Logger.getLogger(AsyncLoadUpdateInfo.class);

    public static final int LOAD_UPDATE_INFO_COMPLETED = 1;

    private Context mContext;
    private Handler mHandler;

    public AsyncLoadUpdateInfo(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    @Override
    protected ApkUpdateInfo doInBackground(Void... params) {
        String version = Config.getVerName(mContext);
        String osVersion = android.os.Build.VERSION.RELEASE;
        return HttpDataFetcher.getInstance().getApkUpdateInfo(version, osVersion);
    }

    @Override
    protected void onPostExecute(ApkUpdateInfo result) {
        if (!isCancelled()) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(LOAD_UPDATE_INFO_COMPLETED, result);
                mHandler.sendMessage(msg);
            }
        }
    }
}
