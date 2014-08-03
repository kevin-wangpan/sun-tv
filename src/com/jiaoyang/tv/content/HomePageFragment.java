package com.jiaoyang.tv.content;

import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.jiaoyang.tv.JyBaseFragment;
import com.jiaoyang.video.tv.R;

/**
 * 在首页显示的Fragment，能在方向键的控制下左右切换。
 * 目前主要有四个Fragment：推荐、专题、频道、个人中心
 *
 */
public abstract class HomePageFragment extends JyBaseFragment {

    public static final int FRAGMENT_TYPE_UNKNOWN = -1;
    public static final int FRAGMENT_TYPE_RECOMMENDATION = 0; // 首页推荐
    public static final int FRAGMENT_TYPE_CHANNEL = 1;
    public static final int FRAGMENT_TYPE_TOPIC = 2;
    public static final int FRAGMENT_TYPE_USER = 3; // 个人中心
    public static final int FRAGMENT_TYPE_SETTINGS = 4;

    public static final String KEY_DIRECTION = "key_direction";
    private int mDirection = NaviControlFragment.INVALID_DIRECTION;

    /**
     * 获得该HomePageFragment实例的type
     * 必须是HomePageFragment中定义几种type之一
     * @return
     */
    public abstract int getFragmentType();

    /**
     * 当前Fragment显示时，指定方向下应该获得焦点的View的id
     * @param direction 移动方向，向左(View.FOCUS_LEFT)或者向右(View.FOCUS_RIGHT)
     * @return
     */
    private int getAutoFocusViewId() {
        int id = -1;
        if(mDirection == View.FOCUS_LEFT) {
            id = getLastViewId();
        } else if (mDirection == View.FOCUS_RIGHT) {
            id = getFirstViewId();
        }
        return id;
    }

    /**
     * 该Fragment第一个View的id，当往右切换到一个新的Fragment时，该View将会自动获得Focus
     * @return
     */
    protected abstract int getFirstViewId();

    /**
     * 该Fragment最后一个View的id，当往左切换到一个新的Fragment时，该View将会自动获得Focus
     * @return
     */
    protected abstract int getLastViewId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if (b != null) {
            mDirection = b.getInt(KEY_DIRECTION, NaviControlFragment.INVALID_DIRECTION);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        focusView();
    }

    /**
     * 左右平移时，为第一个(往右平移)或最后一个(往左平移)View请求焦点
     * @return
     */
    protected boolean focusView() {
        if (mDirection == NaviControlFragment.INVALID_DIRECTION) {
            return false;
        }
        View root = getView();
        if (root == null) {
            return false;
        }
        View viewWannaFocus = root.findViewById(getAutoFocusViewId());
        if (viewWannaFocus != null) {
            boolean b = viewWannaFocus.requestFocus();
            if (b) {
                if (mDirection == View.FOCUS_LEFT) {
                    final int paddingForLeftRight = getResources().getDimensionPixelSize(R.dimen.padding_left);
                    try {
                        final HorizontalScrollView hsv = ((HorizontalScrollView) (viewWannaFocus.getParent()
                                .getParent()));
                        hsv.post(new Runnable() {

                            @Override
                            public void run() {
                                hsv.scrollBy(paddingForLeftRight + 50, 0);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mDirection = NaviControlFragment.INVALID_DIRECTION;
            }
            return b;
        } else {
            return false;
        }
    }
    /**
     * 获取首页指定方向的下一个Fragment的type
     * @param curType 当前Fragment的type
     * @param direction 移动方向，向左(View.FOCUS_LEFT)或者向右(View.FOCUS_RIGHT)
     * @return
     */
    public static int getNextType(int curType, int direction) {
        if (!validFragmentType(curType)) {
            return FRAGMENT_TYPE_UNKNOWN;
        }
        if (direction == View.FOCUS_LEFT &&
                curType > FRAGMENT_TYPE_RECOMMENDATION) {
            return curType - 1;
        } else if (direction == View.FOCUS_RIGHT &&
                curType < FRAGMENT_TYPE_SETTINGS) {
            return curType + 1;
        } else {
            return FRAGMENT_TYPE_UNKNOWN;
        }
    }
    private static boolean validFragmentType(int fragmentType) {
        return fragmentType >= 0 && fragmentType < NaviControlFragment.NAVI_CONTROLS_COUNT;
    }

    //SliderBar切换页码时，回调此方法翻页
    protected abstract void flipPager(int page);
}
