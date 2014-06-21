package com.jiaoyang.tv.content;



import com.jiaoyang.tv.util.ScreenUtil;
import com.jiaoyang.video.tv.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class MetroContainer extends FrameLayout {

    private final int rowSpace;
    private final int columnSpace;

    private final int rowCount; //一共有几行，如果是竖着排的话就是一共有几列
    private final int rowHeight; //一行的高度

    //整个Metro外围的Margin，用来预留metro item放大后占用的空间
    private int surroundingMarginLeft;
    private int surroundingMarginRight;
    private final int surroundingMarginTop;
    private final int surroundingMarginBottom;
    private final int surroundingMargin;

    private int[] leftMargin;
    private int nextRow;

    private final int orientation; //方向
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    //控制焦点滚动时，预滚动出来的空间
    private final int extraScrollSpace;

    private MetroContainerAdapter mAdapter;

    public MetroContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MetroContainer,
                0, 0);
        try {
            rowCount = a.getInt(R.styleable.MetroContainer_rowCount, 2);
            leftMargin = new int[rowCount];
            orientation = a.getInt(R.styleable.MetroContainer_layoutOrientation, HORIZONTAL);
            rowSpace = (int) a.getDimension(R.styleable.MetroContainer_rowSpace, 10f);
            columnSpace = (int) a.getDimension(R.styleable.MetroContainer_columnSpace, 10f);
            rowHeight = (int) a.getDimension(R.styleable.MetroContainer_rowHeight, 50f);
            extraScrollSpace = (int) a.getDimension(R.styleable.MetroContainer_extraScrollSpace, 166f);
            surroundingMargin=(int) a.getDimension(
                    R.styleable.MetroContainer_surroundingMargin, Integer.MIN_VALUE);
            if (surroundingMargin != Integer.MIN_VALUE) {
                surroundingMarginLeft = surroundingMarginRight =
                        surroundingMarginTop = surroundingMarginBottom = surroundingMargin;
            } else {
                surroundingMarginLeft = (int) a.getDimension(
                        R.styleable.MetroContainer_surroundingMarginLeft, 15);
                surroundingMarginRight = (int) a.getDimension(
                        R.styleable.MetroContainer_surroundingMarginRight, 15);
                surroundingMarginTop = (int) a.getDimension(
                        R.styleable.MetroContainer_surroundingMarginTop, 15);
                surroundingMarginBottom = (int) a.getDimension(
                        R.styleable.MetroContainer_surroundingMarginBottom, 15);
            }
            //左右各多留一些padding，放置在TV上贴边
            int paddingForLeftRight = context.getResources().getDimensionPixelSize(R.dimen.padding_left);
            surroundingMarginLeft += paddingForLeftRight;
            surroundingMarginRight += paddingForLeftRight;
        } finally {
            a.recycle();
        }
    }

    /**
     * 当MetroContainer中的某个view获得focus时，如果被focus的view靠近屏幕的边缘了，则提前做个scroll
     * @param focused
     */
    public void setFocusedView(View focused) {
        if (focused == null) {
            return;
        }
        bringChildToFront(focused);
        final int temp = extraScrollSpace / 2;
        if (this.getParent() instanceof HorizontalScrollView) {
            HorizontalScrollView hsv = (HorizontalScrollView) this.getParent();
            int [] location = new int[2];
            focused.getLocationOnScreen(location);
            if(location[0] < focused.getWidth() + this.getX()) {
                hsv.smoothScrollBy(-temp, 0);
                hsv.smoothScrollBy(-temp, 0);
            } else if (location[0] + focused.getWidth() * 2 > ScreenUtil.getScreenWidth(getContext())) {
                hsv.smoothScrollBy(temp, 0);
                hsv.smoothScrollBy(temp, 0);
            }
        } else if (this.getParent() instanceof ScrollView) {
            ScrollView sv = (ScrollView) this.getParent();
            int [] location = new int[2];
            focused.getLocationOnScreen(location);
            if(location[1] < focused.getHeight()) {
                sv.smoothScrollBy(-temp, 0);
                sv.smoothScrollBy(-temp, 0);
            } else if (location[1] + focused.getHeight() * 2 > ScreenUtil.getScreenHeight(getContext())) {
                sv.smoothScrollBy(temp, 0);
                sv.smoothScrollBy(temp, 0);
            }
        }
    }

    public void setAdapter(MetroContainerAdapter adapter) {
        initParams();
        mAdapter = adapter;
        FrameLayout.LayoutParams params =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.rightMargin = surroundingMarginRight;
        params.bottomMargin = surroundingMarginBottom;
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            int rowSpan = mAdapter.getRowSpan(i);
            View v = mAdapter.getView(i);

            //该View要放置到第几行
            nextRow = (rowSpan + nextRow) <= rowCount ? nextRow : 0;

            int maxLeftMagin = getMax(nextRow, rowSpan);
            params = new LayoutParams(params);
            if (orientation == HORIZONTAL) {
                params.topMargin = nextRow * (rowHeight + rowSpace) +
                        surroundingMarginTop;
                params.leftMargin = maxLeftMagin +
                        surroundingMarginLeft;
            } else {
                params.leftMargin = nextRow * (rowHeight + rowSpace) +
                        surroundingMarginLeft;
                params.topMargin = maxLeftMagin +
                        surroundingMarginLeft;
            }
            this.addView(v, params);

            //添加完成后，修改下个要添加的view的layout param
            v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            for (int j = nextRow; j < nextRow + rowSpan; j++) {
                leftMargin[j] = maxLeftMagin + v.getMeasuredWidth() + columnSpace;
            }

            //要把当前行填满(比上面一行宽)以后，才能去往下一行
            if (nextRow == 0 || leftMargin[nextRow] >= leftMargin[nextRow -1]) {
                nextRow = (rowSpan + nextRow) % rowCount;
            }
        }
    }

    private void initParams() {
        leftMargin = new int[rowCount];
        nextRow = 0;
    }

    private int getMax(int nextRow, int rowSpan) {
        int max = leftMargin[nextRow];
        for (int i = 1; i < rowSpan; i++) {
            if (leftMargin[nextRow + i] > max) {
                max = leftMargin[nextRow + i];
            }
        }
        return max;
    }

    public static abstract class MetroContainerAdapter {
        public abstract View getView(int position);
        public abstract int getCount();

        //position处的View占多少行(horizontal)或列(vertical)
        public abstract int getRowSpan(int position);

        //position处的View的type，不同的type对应不同布局文件的View
        public abstract int getType(int position);

        //指定type的View的layout resource id
        public abstract int getLayoutResId(int type);

        public abstract Object getItem(int position);
    }

    public int getExtraScrollSpace() {
        return extraScrollSpace;
    }
}
