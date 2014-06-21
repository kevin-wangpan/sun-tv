package com.jiaoyang.tv.setting;
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.util.AttributeSet;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//
//import com.jiaoyang.video.tv.R;
//
//public class SettingItemView extends RelativeLayout implements OnClickListener {
//    private Context mContext;
//
//    public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        init(context, attrs);
//    }
//
//    public SettingItemView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init(context, attrs);
//    }
//
//    public SettingItemView(Context context) {
//        super(context);
//        init(context, null);
//    }
//
//    private void init(Context context, AttributeSet attrs) {
//        mContext = context;
//        inflate(context, R.layout.setting_item, this);
//    }
//
//    // 设置Button内容
//    public void setText(String content) {
//        mBtnContent.setText(content);
//    }
//
//    // 设置Button可选项
//    public void setOptions(int[] options) {
//        mOptions = options;
//    }
//
//    private OnKeyListener mKeyListener = new OnKeyListener() {
//
//        @Override
//        public boolean onKey(View v, int keyCode, KeyEvent event) {
//            if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
//                    changeSettingModeLeft();
//                    return true;
//                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//                    changeSettingModeRight();
//                    return true;
//                }
//            }
//            return false;
//        }
//
//    };
//
//    private void changeSettingModeRight() {
//        if (++mCurrentMode >= mOptions.length) {
//            mCurrentMode -= mOptions.length;
//        }
//        mBtnContent.setText(mOptions[mCurrentMode]);
//        savePreference();
//    }
//
//    protected void changeSettingModeLeft() {
//        if (mCurrentMode-- <= 0) {
//            mCurrentMode += mOptions.length;
//        }
//        mBtnContent.setText(mOptions[mCurrentMode]);
//        savePreference();
//    }
//
//    private void savePreference() {
//        mPrefs = PreferenceManager.instance(mContext);
//        switch (mRefer) {
//        case REFER_PROFILE:
//            savePlayProfile(mCurrentMode);
//            break;
//        case REFER_SKIP:
//            mPrefs.saveSkipProfile(mCurrentMode);
//            break;
//        case REFER_PROPORTION:
//            mPrefs.saveProportionProfile(mCurrentMode);
//            break;
//        case REFER_DECODER:
//            mPrefs.saveDecoderProfile(mCurrentMode);
//            break;
//        default:
//            break;
//        }
//    }
//
//    private void savePlayProfile(int playMode) {
//        switch (playMode) {
//        case 0:// auto
//            mPrefs.savePlayProfile(-1);
//            break;
//        case 1:// 1080p
//            mPrefs.savePlayProfile(4);
//            break;
//        case 2:// 720p
//            mPrefs.savePlayProfile(3);
//            break;
//        case 3:// Blue
//            mPrefs.savePlayProfile(5);
//            break;
//        case 4:// 3D
//            mPrefs.savePlayProfile(6);
//            break;
//        default:
//            break;
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//        case R.id.arrow_left:
//            changeSettingModeLeft();
//            break;
//        case R.id.arrow_right:
//            changeSettingModeRight();
//            break;
//        default:
//            break;
//        }
//    }
//
//}
