package com.jiaoyang.tv.content;

import com.jiaoyang.base.app.JiaoyangApplication;
import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.video.tv.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * 所有JyBaseActivity的根ViewGroup，实现3个功能：
 * 1. 焦点导航的控制；
 * 2. 绘制Metro Item被选中时的阴影效果
 * 3. 处理UnhandledMove
 *
 */
public class RootRelativeLayout extends RelativeLayout {

    private MainActivity mActivity;
    private View mSystemCandidate;

    private NinePatchDrawable mNPD;
    private Rect mNinePatchPadding = new Rect();

    public static boolean sDrawFocusShadow = true;

    public RootRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RootRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RootRelativeLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        mNPD = (NinePatchDrawable) getResources().getDrawable(R.drawable.focused_bg_blur);
        if(mNPD == null) {
            return;
        }
        mNPD.getPadding(mNinePatchPadding);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        mSystemCandidate = super.focusSearch(focused, direction);
        if (mActivity == null) {
            return mSystemCandidate;
        }
        View lastFocusedNavi = mActivity.getLastFocusedNavi();
        if (direction == View.FOCUS_UP) {
            if (mSystemCandidate != null
                    && focused != null
                    && lastFocusedNavi != null
                    && !systemCandidateInMetroZone(mSystemCandidate, lastFocusedNavi)
                    && focused != lastFocusedNavi) {
                return lastFocusedNavi;
            } else {
                return mSystemCandidate;
            }
        } else if (direction == View.FOCUS_LEFT
                || direction == View.FOCUS_RIGHT) {
            if (focused == null) {
                return mSystemCandidate;
            } else if (mSystemCandidate != null
                    && mSystemCandidate.getParent() == focused.getParent()) {
                return mSystemCandidate;
            } else {
                return null;
            }
        } else {
            return mSystemCandidate;
        }
    }

    /**
     * 系统认为的下一个应该获得焦点的View是否在MetroContainer以内
     */
    private boolean systemCandidateInMetroZone(View systemCandidate, View lastFocusedNavi) {
        Rect rectCandidat = new Rect();
        Rect rectNavi = new Rect();
        systemCandidate.getGlobalVisibleRect(rectCandidat);
        lastFocusedNavi.getGlobalVisibleRect(rectNavi);
        if (rectCandidat.top < rectNavi.bottom) {
            return false;
        } else {
            return true;
        }
    }

    public void setActivity(MainActivity mainActivity) {
        mActivity = mainActivity;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if(mActivity == null) {
            return super.dispatchUnhandledMove(focused, direction);
        }
        boolean consumed = mActivity.onUnhandledMove(focused, direction);
        if (consumed) {
            return true;
        } else {
            return super.dispatchUnhandledMove(focused, direction);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mActivity == null || !sDrawFocusShadow) {
            return;
        }
        ViewGroup tf = mActivity.getContentViewRoot();
        if (tf == null) {
            return;
        }
        View v = tf.getFocusedChild();
        if (v == null || mNPD == null) {
            return;
        }

        while(v != null && !v.isFocused() && v instanceof ViewGroup) {
            v = ((ViewGroup)v).getFocusedChild();
        }
        if (v == null) {
            return;
        }
        Rect rect = new Rect();
        v.getGlobalVisibleRect(rect);

        if (JiaoyangApplication.sFirstTimeLaunche) {
            //TODO：或许有更好的方法解决这个问题
            rect.left = rect.left - 4;
            rect.top = rect.top - 4;
            rect.right = rect.right - 4;
            rect.bottom = rect.bottom - 4;
        }
        mNPD.setBounds(rect.left - mNinePatchPadding.left,
                rect.top - mNinePatchPadding.top,
                rect.right + mNinePatchPadding.right,
                rect.bottom + mNinePatchPadding.bottom);
        mNPD.draw(canvas);
    }

}
