package com.jiaoyang.tv.content;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jiaoyang.tv.JyBaseFragment;
import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.tv.setting.JySettingFragment;
import com.jiaoyang.video.tv.R;

public class NavigationalControlFragment extends JyBaseFragment implements OnFocusChangeListener, OnClickListener {

    public static final int NAVI_CONTROLS_COUNT = 5;

    private TextView[] mNaviControls = new TextView[NAVI_CONTROLS_COUNT];

    private TextView mLastFocusedView;

    //实现左右在各个频道之间滚动
    public static final int INVALID_DIRECTION = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigational_control, container, false);
        initViews(root);
        return root;
    }

    private void initViews(View v) {
        mNaviControls[HomePageFragment.FRAGMENT_TYPE_RECOMMENDATION]
                = (TextView) v.findViewById(R.id.home_page);
        mNaviControls[HomePageFragment.FRAGMENT_TYPE_RECOMMENDATION].setTag(JyPagedFragment.class);

        mNaviControls[HomePageFragment.FRAGMENT_TYPE_CHANNEL]
                = (TextView) v.findViewById(R.id.video_channel);
        mNaviControls[HomePageFragment.FRAGMENT_TYPE_CHANNEL].setTag(JyPagedFragment.class);

        mNaviControls[HomePageFragment.FRAGMENT_TYPE_TOPIC]
                = (TextView) v.findViewById(R.id.video_topic);
        mNaviControls[HomePageFragment.FRAGMENT_TYPE_TOPIC].setTag(JyPagedFragment.class);

        mNaviControls[HomePageFragment.FRAGMENT_TYPE_USER]
                = (TextView) v.findViewById(R.id.user_center);
        mNaviControls[HomePageFragment.FRAGMENT_TYPE_USER].setTag(JyPagedFragment.class);

        mNaviControls[HomePageFragment.FRAGMENT_TYPE_SETTINGS]
                = (TextView) v.findViewById(R.id.navi_settings);
        mNaviControls[HomePageFragment.FRAGMENT_TYPE_SETTINGS].setTag(JySettingFragment.class);

                setFocusListener();
        setClickListener();
        mNaviControls[HomePageFragment.FRAGMENT_TYPE_RECOMMENDATION].requestFocus();
    }

    private void setFocusListener() {
        for (TextView iv : mNaviControls) {
            iv.setOnFocusChangeListener(this);
        }
    }

    private void setClickListener() {
        for (TextView iv : mNaviControls) {
            iv.setOnClickListener(this);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == null || !(view instanceof TextView)) {
            return;
        }
        float scaleFactor = hasFocus ? 1.2f : 1.0f;
        TextView involvedView = (TextView) view;
        if (hasFocus) {
            if (mLastFocusedView != null) {
                mLastFocusedView.setSelected(false);
            }
            //切换fragment
            if (involvedView.getTag() != null) {
                replaceFragment((Class<?>) involvedView.getTag(), null, INVALID_DIRECTION);
            }
            mLastFocusedView = involvedView;
        } else {
            involvedView.setSelected(true);
        }
        involvedView.animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(200).start();
    }

    public View getLastFocusedView() {
        return mLastFocusedView;
    }

    public void requestFocus(int fragmentType) {
        requestFocus(fragmentType, false, INVALID_DIRECTION);
    }

    public void requestFocus(int fragmentType, boolean focusLastFocused, int direction) {
        if (focusLastFocused &&
                (direction != View.FOCUS_LEFT || direction != View.FOCUS_RIGHT)) {
            //
        }
        mNaviControls[fragmentType].requestFocus();
    }

    public boolean isHomePageFocused() {
        return mNaviControls[HomePageFragment.FRAGMENT_TYPE_RECOMMENDATION] != null
                && mNaviControls[HomePageFragment.FRAGMENT_TYPE_RECOMMENDATION].isFocused();
    }

    @Override
    public void onClick(View view) {
        view.requestFocus();
    }

    private int getCurrentFragmentType() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof MainActivity) {
            return ((MainActivity)activity).getCurrentFragmentType();
        }
        return HomePageFragment.FRAGMENT_TYPE_UNKNOWN;
    }

    public View getCurrentNaviControl() {
        return mNaviControls[getCurrentFragmentType()];
    }
}
