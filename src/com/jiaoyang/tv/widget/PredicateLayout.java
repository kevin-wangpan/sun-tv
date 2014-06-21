package com.jiaoyang.tv.widget;

import java.util.Hashtable;

import com.jiaoyang.video.tv.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 自定义LinearLayout,向Layout添加的子控件超出屏幕宽度，自动换行
 * 
 * @author liyuejiao
 * 
 */
public class PredicateLayout extends LinearLayout {

    private final int SPACE_VERTICAL = 5;
    private final int SPACE_HORIZONTAL = 8;
    private final int MAX_ROW = 2;
    private int mRow = 1;
    int mLeft, mRight, mTop, mBottom;
    private int mLastBottom = -1;
    Hashtable<View, Position> map = new Hashtable<View, Position>();
    private Context mContext;

    public PredicateLayout(Context context) {
        super(context);
        mContext = context;
    }

    public PredicateLayout(Context context, int horizontalSpacing, int verticalSpacing) {
        super(context);
        mContext = context;
    }

    public PredicateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // mode + size 子View可获得的空间
        int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mCount = getChildCount();
        mLeft = 0;
        mRight = 0;
        mTop = 5;
        mBottom = 0;

        int j = 0;
        for (int i = 0; i < mCount; i++) {
            final View child = getChildAt(i);
            // 设置子View的宽高 AT_MOST = "wrap_content" EXACTLY ="fill_parent" / 50dp
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            // 得到测量后的子View后宽高
            int childw = child.getMeasuredWidth();
            int childh = child.getMeasuredHeight();
            mRight += childw;

            Position position = new Position();
            mLeft = getPosition(i - j, i);
            mRight = mLeft + child.getMeasuredWidth();
            if (mRight >= mWidth) {// 超过layout的width就换行
                j = i;
                // 换行后重新指定第一个子View的初始positon。
                mLeft = getPaddingLeft();
                mRight = mLeft + child.getMeasuredWidth();
                mTop += childh + SPACE_VERTICAL; // top= 上一行高 + 行间距
            }
            mBottom = mTop + child.getMeasuredHeight();
            position.left = mLeft;
            position.top = mTop;
            position.right = mRight;
            position.bottom = mBottom;
            map.put(child, position);
        }
        // 保存ViewGroup自身布局的大小
        setMeasuredDimension(mWidth, mBottom + getPaddingBottom());
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            Position pos = map.get(child);
            if (pos != null) {
                if (mLastBottom == -1) {// 第一行
                    mLastBottom = pos.bottom;
                } else if (mLastBottom != pos.bottom) {// 换行后增加行
                    mRow++;
                }
                if (mRow == MAX_ROW) {
                    child.setNextFocusDownId(R.id.recommend);
                    child.setId(generateId());
                } else {
                    child.setOnKeyListener(new OnKeyListener() {

                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            int action = event.getAction();
                            if (action == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                                switch (keyCode) {
                                case KeyEvent.KEYCODE_DPAD_UP:
                                    return true;
                                default:
                                    break;
                                }
                            }
                            return false;
                        }
                    });
                }
                if (mRow > MAX_ROW)// 控制最大行数
                    return;
                child.layout(pos.left, pos.top, pos.right, pos.bottom);
                mLastBottom = pos.bottom;
            }
        }
    }

    private static final int BASE_ID = 1010;
    private int mIndex = 0;

    private int generateId() {
        return BASE_ID + (mIndex++);
    }

    private class Position {
        int left, top, right, bottom;
    }

    /**
     * 递归： 第一个View: getPaddingLeft() 第二个View: getPosition(0,0) + getChild(0).width + 水平间距 = 第一个子View.left + 第一个子View宽度 +
     * 水平距离 第三个View: getPosition(1,0) + getChild(1).width + 水平间距 = 第二个子View.left + 第二个子View宽度 + 水平距离
     * 
     * @param IndexInRow
     * @param childIndex
     * @return
     */
    public int getPosition(int IndexInRow, int childIndex) {
        if (IndexInRow > 0) {
            return getPosition(IndexInRow - 1, childIndex - 1)
                    + getChildAt(childIndex - 1).getMeasuredWidth() + SPACE_HORIZONTAL;
        }
        return getPaddingLeft();
    }

    public View findNextFocused(int curSelected) {
        if (curSelected < 0) {
            return null;
        }
        View v = null;
        do {
            v = findViewById(BASE_ID + curSelected);
            curSelected -= 1;
        } while (v == null && curSelected >= 0);
        return v;
    }
}
