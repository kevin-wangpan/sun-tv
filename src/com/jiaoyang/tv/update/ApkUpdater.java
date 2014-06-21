package com.jiaoyang.tv.update;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.jiaoyang.tv.data.UpdateInfo;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.tv.widget.JySimpleDialog;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.video.tv.R;

/**
 * 控制apk版本更新
 */
public class ApkUpdater {
    private static final Logger LOG = Logger.getLogger(ApkUpdater.class);

    private Context mContext;
    private boolean mCheckUpdateAutomaticly;
    private Handler mHandler;
    private UpdateInfo mUpdateInfo;
    private ProgressDialog mProgressDialog;
    private AsyncLoadApk mLoadApk;
    private AsyncLoadUpdateInfo mLoadUpdateInfo;
    private int mTotalLen;
    private boolean mForceUpdate;

    private DialogInterface.OnKeyListener mDialogKeyListener = new DialogInterface.OnKeyListener() {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            return keyCode == KeyEvent.KEYCODE_BACK;
        }
    };

    private OnClickListener mDialogClickListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    Toast.makeText(mContext, R.string.no_sdcard_tips, Toast.LENGTH_SHORT).show();
                    return;
                }

                mLoadingDialog = createLoadingDialog(R.string.download);
                mLoadingDialog.show();

                if (mUpdateInfo != null) {
                    String url = mUpdateInfo.latestUrl;
                    if (!(TextUtils.isEmpty(url))) {
                        mLoadApk = new AsyncLoadApk(mHandler);
                        mLoadApk.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
                    }
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if (mLoadApk != null) {
                    mLoadApk.cancelTask();
                    mLoadApk = null;
                }
                if (mForceUpdate) {
                    exit();
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                if (mUpdateInfo != null) {
                    PreferenceManager.instance(mContext).saveIgnoredVersion(mUpdateInfo.latestVersion);
                }
                break;
            default:
                break;
            }
        }
    };

    public static boolean mShowUpdateDialog = false;

    private void disposeUpdate(Object info) {
        if (info != null) {
            mUpdateInfo = (UpdateInfo) info;
            LOG.debug("update value: " + mUpdateInfo.type);

            boolean needTips = false;
            boolean needUpdate = false;
            boolean ignoreVersion = mUpdateInfo != null &&
                    mUpdateInfo.latestVersion.equalsIgnoreCase
                            (PreferenceManager.instance(mContext).retriveIgnoredVersion());
            switch (mUpdateInfo.type) {
            case 0: // 没有更新
                needTips = !mCheckUpdateAutomaticly;
                break;

            case 1: // 有更新，不提示直接下载（还没有实现）
                needTips = false;
                needUpdate = true;
                break;

            case 2: // 有更新，提示更新
                needTips = true;
                needUpdate = true;
                break;

            case 3: // 有更新，提示用户更新，用户不更新则退出应用
                needTips = true;
                needUpdate = true;
                mForceUpdate = true;
                break;

            default:
                break;
            }

            if (needUpdate) {
                if (needTips && !ignoreVersion) {
                    showIsUpdateDialog();
                } else {
                    // TODO 强制后台下载
                }
            } else {
                if (needTips) {
                    showNoNeedUpdateDialog();
                } else {
                    // nothing to do
                    if (mShowLastestTip) {
                        Toast.makeText(mContext, "已是最新版", 1).show();
                    }
                }
            }
        } else {
            if (!mCheckUpdateAutomaticly) {
                Util.showToast(mContext, mContext.getString(R.string.apk_update_failure), Toast.LENGTH_LONG);
            }
        }
    }

    private void hideProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void disposeMessage(Message msg) {
        if (AsyncLoadUpdateInfo.LOAD_UPDATE_INFO_COMPLETED != msg.what) {
            hideProgressDialog();
        }
        if (AsyncLoadUpdateInfo.LOAD_UPDATE_INFO_COMPLETED == msg.what) {
            hideProgressDialog();
            disposeUpdate(msg.obj);
        } else if (Util.MSG_LOADED_PROGRESS == msg.what) {
            LOG.debug("loaded progress:" + msg.arg1);
            int progress = msg.arg1;
            if (progress > mTotalLen) {
                progress = mTotalLen;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mLoadingDialog.setProgressNumberFormat("%1d/%2dKB");
            }
            mLoadingDialog.setProgress(progress);
        } else if (AsyncLoadApk.MSG_START_LOAD_APK == msg.what) {
            LOG.debug("total len:" + msg.arg1 / 1024);
            mTotalLen = msg.arg1 / 1024;
            mLoadingDialog.setMax(mTotalLen);
        } else if (AsyncLoadApk.LOAD_APK_COMPLETED == msg.what) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            installApk(mContext);
        } else if (AsyncLoadApk.MSG_LOAD_FAILED == msg.what) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            Util.showToast(mContext, mContext.getString(R.string.widget_updatefailed), Toast.LENGTH_LONG);
        }
    }

    private static class UpdateHandler extends Handler {
        private WeakReference<ApkUpdater> mUpdate;

        public UpdateHandler(ApkUpdater update) {
            mUpdate = new WeakReference<ApkUpdater>(update);
        }

        public void handleMessage(Message msg) {
            ApkUpdater update = mUpdate.get();
            if (update != null) {
                update.disposeMessage(msg);
            }
        }
    }

    public ApkUpdater(Context context) {
        mContext = context;
        mCheckUpdateAutomaticly = true;
        mHandler = new UpdateHandler(this);
    }

    public void setAutoCheckUpdate(boolean autoUpdate) {
        mCheckUpdateAutomaticly = autoUpdate;
    }

    public boolean isNeedUpdate() {
        boolean isNeedUpdate = false;

        String versionName = Config.getVerName(mContext);
        if (null != versionName) {
            String[] ver = versionName.split("\\.");
            if (ver.length > 0) {
                int value = Integer.valueOf(ver[ver.length - 1]).intValue();
                if (0 == (value % 2)) {
                    isNeedUpdate = true;
                } else {
                    isNeedUpdate = false;
                }
            }
        }

        return isNeedUpdate;
    }

    /**
     * 更新
     * 
     * @param autoCheckUpdate
     *            是否自动检测更新（启动时自动检测） 如果是自动检测更新，那么检测更新的过程中不需要进度对话框； 如果不是自动检查更新，例如用户手动的选择了检查更新，那么需要显示检查更新进度的对话框。
     */
    public void update(boolean autoCheckUpdate) {
        setAutoCheckUpdate(autoCheckUpdate);
        start();
    }

    /**
     * 加载更新信息
     */
    private void start() {
        if (mCheckUpdateAutomaticly == false) {
            mProgressDialog = createProgressDialog(
                    mContext.getString(R.string.progress_tips),
                    mContext.getString(R.string.getting_update_info));
            mProgressDialog.show();
        }
        mLoadUpdateInfo = new AsyncLoadUpdateInfo(mContext, mHandler);
        mLoadUpdateInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void cancel() {
        if (mLoadUpdateInfo != null) {
            mLoadUpdateInfo.cancel(true);
            mLoadUpdateInfo = null;
        }
        if (mLoadApk != null) {
            mLoadApk.cancel(true);
            mLoadApk = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    /**
     * 安装
     */
    private void installApk(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = null;
        try {
            apkFile = new File(Environment.getExternalStorageDirectory(),
                    Config.UPDATE_SAVENAME);
            if (apkFile.exists()) {
                intent.setDataAndType(Uri.fromFile(apkFile),
                        "application/vnd.android.package-archive");
                context.startActivity(intent);
                exit();
            } else {
                Util.showToast(mContext, mContext.getString(R.string.widget_updatefailed), Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示需要更新的对话框
     */
    private void showIsUpdateDialog() {
        if (mUpdateInfo == null) {
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(mContext.getString(R.string.get_new_version) + mUpdateInfo.latestVersion);
        if (mUpdateInfo.changes != null && mUpdateInfo.changes.length > 0) {
            for (int i = 0; i < mUpdateInfo.changes.length; i++) {
                sb.append("\n").append(i + 1).append(".").append(mUpdateInfo.changes[i]);
            }
        }
        mShowUpdateDialog = true;
        JySimpleDialog dialog = new JySimpleDialog(mContext);
        dialog.setTitle(mContext.getString(R.string.software_update))
                .setMessage(sb.toString())
                .setPositiveButton(mContext.getString(R.string.update), mDialogClickListener)
                .setNegativeButton(mForceUpdate ? mContext.getString(R.string.quit) :
                        mContext.getString(R.string.cancel), mDialogClickListener)
                .setOnKeyListener(mDialogKeyListener);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 显示无需要更新的对话框
     */
    private void showNoNeedUpdateDialog() {
        String verName = Config.getVerName(mContext);
        StringBuffer sb = new StringBuffer();
        sb.append("已是最新版.");
        sb.append("\n当前版本：");
        sb.append(verName);
        Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.software_update))
                .setMessage(sb.toString())
                .setPositiveButton(mContext.getString(R.string.ok), null).create();
        dialog.show();
    }

    /**
     * 创建进度框，显示时点击回退键不能取消
     * 
     * @param title
     *            标题
     * @param msg
     *            内容
     * @return 返回进度框
     */
    private ProgressDialog createProgressDialog(String title, String msg) {
        ProgressDialog dialog = null;
        dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setIndeterminate(false);
        dialog.setOnKeyListener(mDialogKeyListener);
        return dialog;
    }

    private JySimpleDialog mLoadingDialog;

    private boolean mShowLastestTip = false;

    private JySimpleDialog createLoadingDialog(int titleId) {
        JySimpleDialog dialog = new JySimpleDialog(mContext);
        dialog.setTitle(mContext.getString(R.string.download));
        dialog.setProgressBar(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(mDialogKeyListener);
        dialog.setNegativeButton(mForceUpdate ? mContext.getString(R.string.quit) :
                mContext.getString(R.string.cancel), mDialogClickListener);
        return dialog;
    }

    private void exit() {
        Activity activity = (Activity) mContext;
        activity.finish();
        activity.getApplication().onTerminate();
    }

    public void showLastTips(boolean showLastestTip) {
        mShowLastestTip = true;
    }
}
