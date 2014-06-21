package com.jiaoyang.tv.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.tv.content.HomePageFragment;
import com.jiaoyang.tv.util.PreferenceManager;
import com.jiaoyang.video.tv.R;

public class JySettingFragment extends HomePageFragment {
    private Switch mAutoPlayNext;
    private Switch mAutoSkip;

    private PreferenceManager mPrefs;

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
        boolean autoPlayNext = mPrefs.getAutoPlayNext();
        mAutoPlayNext.setText(autoPlayNext ? "开" : "关");
        boolean autoSkip = mPrefs.getAutoSkip();
        mAutoSkip.setText(autoSkip ? "开" : "关");
    }

    private void fillViews() {
        mAutoPlayNext = ((Switch) getView().findViewById(
                R.id.auto_play_next_switcher));
        mAutoSkip = ((Switch) getView()
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
