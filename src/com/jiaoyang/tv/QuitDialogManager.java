package com.jiaoyang.tv;

import android.app.Activity;
import android.content.res.Resources;

import com.jiaoyang.tv.widget.JySimpleDialog;
import com.jiaoyang.video.tv.R;

public class QuitDialogManager {

    private static QuitDialogManager mQuitManager;
    private JySimpleDialog mQuiteDialog = null;

    public static QuitDialogManager getInstance() {
        if (mQuitManager == null) {
            mQuitManager = new QuitDialogManager();
        }
        return mQuitManager;
    }

    public void dismissShownDialog() {
        if (mQuiteDialog != null && mQuiteDialog.isShowing()) {
            mQuiteDialog.dismiss();
        }
    }
    
    public void showKkQuitDialog(final Activity context, final Runnable positiveCallback, 
            final Runnable negativeCallback) {
        Resources resources = context.getResources();
        mQuiteDialog = new JySimpleDialog(context, R.style.KkSimpleDialog);
        mQuiteDialog.setTitle(resources.getString(R.string.quit_info))
            .setPositiveBtnString(resources.getString(R.string.ok))
            .setNegativeBtnString(resources.getString(R.string.cancel))
            .setPositiveCallback(positiveCallback)
            .setNegativeCallback(negativeCallback)
            .show();
    }
    
}
