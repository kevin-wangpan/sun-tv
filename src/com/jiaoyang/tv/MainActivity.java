package com.jiaoyang.tv;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import cn.com.iresearch.mapptracker.IRMonitor;

import com.jiaoyang.base.sys.DeviceHelper;
import com.jiaoyang.tv.content.ActionBarFragment;
import com.jiaoyang.tv.content.HomePageFragment;
import com.jiaoyang.tv.content.NaviControlFragment;
import com.jiaoyang.tv.content.RootRelativeLayout;
import com.jiaoyang.tv.content.SliderBarFragment;
import com.jiaoyang.tv.util.NetworkHelper;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.video.tv.R;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends JyBaseActivity {

    private Handler mHandler;
    private NaviControlFragment mNaviFragment;
    private SliderBarFragment mSliderBarFragment;
    RootRelativeLayout mRoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        DeviceHelper.loadScreenInfo(this);

        mRoot = getRootView();
        mRoot.setActivity(this);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        mNaviFragment = new NaviControlFragment();
        t.replace(R.id.navi_control, mNaviFragment);
        ActionBarFragment actionbar = new ActionBarFragment();
        t.replace(R.id.action_bar, actionbar);
        mSliderBarFragment = new SliderBarFragment();
        t.replace(R.id.page_number_slider, mSliderBarFragment);
        t.commit();

        if (!NetworkHelper.hasAvailableNetwork(this)) {
            if (Util.isSupportedDevice()) {
                guideUserToLocalVideo();
            } else {
                NetworkHelper.showNotWifiNotic(this);
            }
        }

        IRMonitor.getInstance(this).Init("艾瑞分配的app key", null,true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        QuitDialogManager.getInstance().dismissShownDialog();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count >= 1) {
            super.onBackPressed();
        } else {
            if (mNaviFragment.isHomePageFocused()) {
                showQuitDialog();
            } else {
                mNaviFragment.requestFocus(HomePageFragment.FRAGMENT_TYPE_RECOMMENDATION);
            }
        }
    }

    private void guideUserToLocalVideo() {
    }

    private void showQuitDialog() {
        QuitDialogManager.getInstance().showKkQuitDialog(this, new Runnable() {

            @Override
            public void run() {
                exit();
            }
        }, null);

    }

    private void exit() {
        finish();

        getApplication().onTerminate();
    }

    public View getLastFocusedNavi() {
        return mNaviFragment.getLastFocusedView();
    }

    public HomePageFragment getCurrentFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (f instanceof HomePageFragment) {
            return ((HomePageFragment) f);
        } else {
            return null;
        }
    }
    public int getCurrentFragmentType() {
        HomePageFragment f = getCurrentFragment();
        return f == null ? HomePageFragment.FRAGMENT_TYPE_UNKNOWN : f.getFragmentType();
    }

    @Override
    public void onCreateTvMenu(TvMenu menu) {}

    @Override
    public void onTvMenuItemClicked(int id) {}

    public boolean onUnhandledMove(View focused, int direction) {
        int type = getCurrentFragmentType();
        int nextFragmentType = HomePageFragment.getNextType(type, direction);
        if (nextFragmentType == HomePageFragment.FRAGMENT_TYPE_UNKNOWN) {
            return false;
        } else {
            mNaviFragment.requestFocus(nextFragmentType, true, direction);
            return true;
        }
    }

    /**
     * content_frame的view root
     */
    public ViewGroup getContentViewRoot() {
        View view  = getCurrentFragment().getView();
        if (view instanceof ViewGroup) {
            return (ViewGroup) view;
        } else {
            return null;
        }
    }

    public void refreshFocusShadow() {
        mRoot.invalidate();
    }

    public NaviControlFragment getNaviControlFragment() {
        return mNaviFragment;
    }

    public SliderBarFragment getSliderBarFragment() {
        return mSliderBarFragment;
    }
}
