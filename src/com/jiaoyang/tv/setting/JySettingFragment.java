package com.jiaoyang.tv.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.tv.content.HomePageFragment;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.video.tv.R;

public class JySettingFragment extends HomePageFragment {
    private View mAutoNextContainer, mAutoSkipContainer;
    private boolean mAutoNext, mAutoSkip;
    private ImageView mAutoNextSwitch;
    private ImageView mAutoSkipSwitch;

    private PreferenceManager mPrefs;

    private OnKeyListener mKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            boolean validKeyCode = keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
            if (!validKeyCode
                    || event.getAction() != KeyEvent.ACTION_DOWN
                    || event.getRepeatCount() > 0) {
                return false;
            }
            switch (view.getId()) {
            case R.id.auto_play_next:
                mAutoNext = !mAutoNext;
                mPrefs.saveAutoPlayNext(mAutoNext);
                setSwitcherImage();
                return true;
            case R.id.auto_skip:
                mAutoSkip = !mAutoSkip;
                mPrefs.saveAutoSkip(mAutoSkip);
                setSwitcherImage();
                return true;

            default:
                break;
            }
            return false;
        }
    };

    private OnFocusChangeListener mFocusListener = new OnFocusChangeListener() {
        
        @Override
        public void onFocusChange(View arg0, boolean hasFocus) {
            if (hasFocus) {
                refreshFocusShadow();
            }
        }
    };
    private void refreshFocusShadow() {
        Activity a = getActivity();
        if (a != null && a instanceof MainActivity) {
            ((MainActivity)a).refreshFocusShadow();
        }
    }

    private void setSwitcherImage() {
        mAutoNextSwitch.setImageResource(mAutoNext ? R.drawable.jy_switcher_on : R.drawable.jy_switcher_off);
        mAutoSkipSwitch.setImageResource(mAutoSkip ? R.drawable.jy_switcher_on : R.drawable.jy_switcher_off);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.instance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAutoNext = mPrefs.getAutoPlayNext();
        mAutoSkip = mPrefs.getAutoSkip();
        setSwitcherImage();
    }

    private void fillViews() {
        mAutoNextContainer = getView().findViewById(R.id.auto_play_next);
        mAutoNextContainer.setOnKeyListener(mKeyListener);
        mAutoNextContainer.setOnFocusChangeListener(mFocusListener);
        mAutoSkipContainer = getView().findViewById(R.id.auto_skip);
        mAutoSkipContainer.setOnKeyListener(mKeyListener);
        mAutoSkipContainer.setOnFocusChangeListener(mFocusListener);
        mAutoNextSwitch = ((ImageView) getView().findViewById(
                R.id.auto_play_next_switcher));
        mAutoSkipSwitch = ((ImageView) getView()
                .findViewById(R.id.auto_skip_text_switcher));

        ((MainActivity)getActivity()).getSliderBarFragment().setTotalPages(0);
    }

    @Override
    public int getFragmentType() {
        return 0;
    }

    @Override
    protected int getFirstViewId() {
        return 0;
    }

    @Override
    protected int getLastViewId() {
        return 0;
    }

    @Override
    protected void flipPager(int page) {
    }
}
