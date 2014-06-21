package com.jiaoyang.tv.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.jiaoyang.tv.util.Util;

public class AsyncLoadApk extends AsyncTask<String, Void, Void> {
    public static final int LOAD_APK_COMPLETED = 2;
    public static final int MSG_START_LOAD_APK = LOAD_APK_COMPLETED + 1;
    public static final int MSG_LOAD_FAILED = MSG_START_LOAD_APK + 1;
    public static final int MSG_LOAD_CANCEL = MSG_LOAD_FAILED + 1;

    private Handler mHandler;
    private State mState;
    private boolean mLoadFailed;

    public AsyncLoadApk(Handler handler) {
        mState = new State();
        mHandler = handler;
        mLoadFailed = false;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params == null) {
            return null;
        }
        String url = null;
        if (params.length > 0) {
            url = params[0];
        }

        InputStream inputStream = null;
        try {
            int len = (int) ApkDownloadHelper.getLength(url);
            if (len != 0 && mHandler != null) {
                Message msg = mHandler.obtainMessage(MSG_START_LOAD_APK);
                msg.arg1 = len;
                mHandler.sendMessage(msg);
            }

            inputStream = ApkDownloadHelper.getStreamFromUrl(url);
            if (inputStream != null) {
                File file = new File(Environment.getExternalStorageDirectory(),
                        Config.UPDATE_SAVENAME);
                if (file.exists()) {
                    file.delete();
                }

                Util.saveFile(inputStream, file, mHandler, mState);
            } else {
                mLoadFailed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLoadFailed = true;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
        }

        if (mLoadFailed) {
            mHandler.sendEmptyMessage(MSG_LOAD_FAILED);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mHandler != null && !mLoadFailed) {
            if (mState.state == State.STOP) {
                mHandler.sendEmptyMessage(MSG_LOAD_CANCEL);// 只把下载窗口关闭，不作其它处理
            } else {
                mHandler.sendEmptyMessage(LOAD_APK_COMPLETED);
            }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        mState.state = State.STOP;
    }

    public void cancelTask() {
        mState.state = State.STOP;
    }
}
