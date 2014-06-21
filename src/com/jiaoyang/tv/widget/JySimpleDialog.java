package com.jiaoyang.tv.widget;

import java.text.NumberFormat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jiaoyang.tv.util.Res;
import com.jiaoyang.video.tv.R;

/**
 * 简单dialog， 支持一个标题和一个message。
 * 假如对应内容没有设置，则对应控件不显示；
 * 最多支持两个按钮，一个确定按钮和一个取消按钮；
 * 按钮的显示与否，根据对应的按钮文字是否为空来判断
 */
public class JySimpleDialog extends Dialog implements View.OnClickListener {
    private String mTitle;
    private String mMessage;

    private String mPositiveBtnString;
    private String mNegativeBtnString;

    private Runnable mPositiveCallback;
    private Runnable mNegativeCallback;

    private DialogInterface.OnClickListener mPositiveListener;
    private DialogInterface.OnClickListener mNegativeListener;
    // ProgressBar
    private String mProgressNumberFormat;
    private boolean mIsProgressBar = false;
    private TextView mProgressNumber;
    private TextView mProgressPercent;
    private ProgressBar mProgressBar;
    private Handler mViewUpdateHandler;
    private NumberFormat mProgressPercentFormat;
    
    private int mTitleGravity = Gravity.CENTER_HORIZONTAL;//标题gravity，默认横向居中
    private int mMsgGravity = Gravity.LEFT;//内容默认居左显示

    /**
     * 
     * 设置positive按钮文字，若不设置，或者设置的字符串为空，则该按钮将不会显示
     * 
     * @Title: setPositiveBtnString
     * @param positiveString
     * @return
     * @return KkSimpleDialog
     * @date 2013-12-19 下午1:16:40
     */
    public JySimpleDialog setPositiveBtnString(String positiveString) {
        mPositiveBtnString = positiveString;
        return this;
    }

    /**
     * 
     * 设置negative按钮文字，若不设置，或者设置的字符串为空，则该按钮将不会显示
     * 
     * @Title: setNegativeBtnString
     * @param negativeString
     * @return
     * @return KkSimpleDialog
     * @date 2013-12-19 下午1:17:38
     */
    public JySimpleDialog setNegativeBtnString(String negativeString) {
        mNegativeBtnString = negativeString;
        return this;
    }

    /**
     * 
     * 设置标题，若不设置，则对应标题控件不显示
     * 
     * @Title: setTitle
     * @param title
     * @return
     * @return KkSimpleDialog
     * @date 2013-12-19 下午1:15:41
     */
    public JySimpleDialog setTitle(String title) {
        mTitle = title;
        return this;
    }
    
    /**
     * 
     * 设置title gravity，值用Gravity里面的静态变量设置
     * @Title: setTitleGravity
     * @param gravity
     * @return
     * @return KkSimpleDialog
     * @date 2013-12-31 上午11:27:19
     */
    public JySimpleDialog setTitleGravity(int gravity) {
        mTitleGravity = gravity;
        return this;
    }

    /**
     * 
     * 设置message，若不设置，则对应message控件不显示
     * 
     * @Title: setMessage
     * @param message
     * @return
     * @return KkSimpleDialog
     * @date 2013-12-19 下午1:16:17
     */
    public JySimpleDialog setMessage(String message) {
        mMessage = message;
        return this;
    }
    
    /**
     * 
     * 设置dialog内容Gravity，值用Gravity静态变量设置
     * @Title: setMsgGravity
     * @param gravity
     * @return
     * @return KkSimpleDialog
     * @date 2013-12-31 下午3:49:15
     */
    public JySimpleDialog setMsgGravity(int gravity) {
        mMsgGravity = gravity;
        return this;
    }

    /**
     * 设置是否有ProgressBar，不设置，不显示Progressbar控件 此处写方法描述
     * 
     * @Title: setProgressBar
     * @param isProgressBar
     * @return
     * @return KkSimpleDialog
     * @date 2013-12-24 上午11:17:12
     * @author lyj
     */
    public JySimpleDialog setProgressBar(boolean isProgressBar) {
        mIsProgressBar = isProgressBar;
        return this;
    }

    public JySimpleDialog setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
        return this;
    }

    public JySimpleDialog setProgress(int value) {
        mProgressBar.setProgress(value);
        if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
            mViewUpdateHandler.sendEmptyMessage(0);
        }
        return this;
    }

    public void setMax(int max) {
        if (mProgressBar != null) {
            mProgressBar.setMax(max);
        }
    }

    private void initFormats() {
        mProgressNumberFormat = "%1d/%2d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
        mProgressPercent.setText("0%");
        mProgressNumber.setText("0/" + mProgressBar.getMax());
    }

    public JySimpleDialog setPositiveCallback(final Runnable positiveCallback) {
        mPositiveCallback = positiveCallback;
        return this;
    }

    public JySimpleDialog setNegativeCallback(final Runnable negativeCallback) {
        mNegativeCallback = negativeCallback;
        return this;
    }

    public JySimpleDialog setPositiveButton(String text, DialogInterface.OnClickListener listener) {
        mPositiveBtnString = text;
        mPositiveListener = listener;
        return this;
    }

    public JySimpleDialog setNegativeButton(String text, DialogInterface.OnClickListener listener) {
        mNegativeBtnString = text;
        mNegativeListener = listener;
        return this;
    }
    
    public JySimpleDialog(Context context) {
        this(context, R.style.KkSimpleDialog);
    }

    public JySimpleDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = getLayoutInflater().inflate(R.layout.dialog_simple_kk, null);
        initViews(rootView);

        setContentView(rootView);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setWindowStyle();
    }

    private void setWindowStyle() {
        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.alpha = 0.75f;
        getWindow().getWindowManager().updateViewLayout(view, lp);
    }

    private void initViews(View rootView) {
        TextView titleTextView = Res.t(rootView, R.id.dialog_tv_title);
        View msgWrapperView = rootView.findViewById(R.id.dialog_msgwrapper);
        Button positiveButton = Res.b(rootView, R.id.dialog_btn_positive);
        Button negativeButton = Res.b(rootView, R.id.dialog_btn_negative);
        mProgressBar = Res.p(rootView, R.id.dialog_progressBar);
        mProgressNumber = Res.t(rootView, R.id.dialog_progress_number);
        mProgressPercent = Res.t(rootView, R.id.dialog_progress_percent);

        if (TextUtils.isEmpty(mTitle)) {
            titleTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setGravity(mTitleGravity);
            titleTextView.setText(mTitle);
        }

        if (TextUtils.isEmpty(mMessage)) {
            msgWrapperView.setVisibility(View.GONE);
        } else {
            msgWrapperView.setVisibility(View.VISIBLE);
            TextView titleMsgTextView = Res.t(rootView, R.id.dialog_tv_message);
            titleMsgTextView.setGravity(mMsgGravity);
            titleMsgTextView.setText(mMessage);
        }

        if (!TextUtils.isEmpty(mPositiveBtnString)) {
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.requestFocus();
            positiveButton.setText(mPositiveBtnString);
            positiveButton.setOnClickListener(this);
        } else {
            positiveButton.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mNegativeBtnString)) {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(mNegativeBtnString);
            negativeButton.setOnClickListener(this);
        } else {
            negativeButton.setVisibility(View.GONE);
        }

        if (mIsProgressBar) {
            initFormats();
        } else {
            mProgressBar.setVisibility(View.GONE);
            mProgressNumber.setVisibility(View.GONE);
            mProgressPercent.setVisibility(View.GONE);
        }

        mViewUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                /* Update the number and percent */
                int progress = mProgressBar.getProgress();
                int max = mProgressBar.getMax();
                if (mProgressNumberFormat != null) {
                    String format = mProgressNumberFormat;
                    mProgressNumber.setText(String.format(format, progress, max));
                } else {
                    mProgressNumber.setText("0/" + max);
                }
                if (mProgressPercentFormat != null) {
                    double percent = (double) progress / (double) max;
                    SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                    tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mProgressPercent.setText(tmp);
                } else {
                    mProgressPercent.setText("0%");
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.dialog_btn_positive:
            this.dismiss();
            if (mPositiveCallback != null)
                mPositiveCallback.run();
            if (mPositiveListener != null)
                mPositiveListener.onClick(this, DialogInterface.BUTTON_POSITIVE);
            break;
        case R.id.dialog_btn_negative:
            this.dismiss();
            if (mNegativeCallback != null)
                mNegativeCallback.run();
            if (mNegativeListener != null)
                mNegativeListener.onClick(this, DialogInterface.BUTTON_NEGATIVE);
            break;
        default:
            break;
        }
    }
}
