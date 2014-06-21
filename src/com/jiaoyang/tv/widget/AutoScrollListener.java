package com.jiaoyang.tv.widget;


import com.jiaoyang.tv.util.Logger;

import android.os.Build;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ListView;

public class AutoScrollListener implements OnItemSelectedListener {
    private final static int INIT_VALUE = -1;
    private final static int ERROR_VALUE = -2;
//    private final Logger LOG = Logger.getLogger("klilog");

    private int selectPos;
    int totalHeight = INIT_VALUE;
    int itemHeight = INIT_VALUE;
    int columnCount = INIT_VALUE;

    int horizontalSpacing = 0;
    int parentPadding = 0;
    
    private BaseOnScrollListener mScrollListener = new BaseOnScrollListener();
    private OnScrollListener mOnScrollListener;

    public AutoScrollListener setOnScrollListener(AbsListView parent, OnScrollListener listener){
        mOnScrollListener = listener;
        parent.setOnScrollListener(mOnScrollListener);
        return this;
    }
    
    private int getLineIndex(int pos, int columCount) {
        int line = pos / columCount;
        return line;
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, View view, int position, long id) {
//        LOG.i("***************************");

        if (columnCount == ERROR_VALUE) {
//            LOG.i("1 ERROR_VALUE");
            return;
        } else if (columnCount == INIT_VALUE) {
            // 初始化 columnCount
            if (parent instanceof GridView) {
                columnCount = ((GridView) parent).getNumColumns();
                if(Build.VERSION.SDK_INT >= 16){
                    horizontalSpacing = ((GridView)parent).getVerticalSpacing();
                }else{
                    try {
                        View view1 = ((GridView)parent).getChildAt(0);
                        View view2 = ((GridView)parent).getChildAt(columnCount);
                        horizontalSpacing = view2.getTop() - view1.getBottom();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                LOG.i("1 horizontalSpacing:"+horizontalSpacing);
            } else if (parent instanceof ListView) {
                columnCount = 1;
            } else {
                columnCount = ERROR_VALUE;
//                LOG.i("1 ERROR_VALUE");
                return;
            }
            parentPadding =  parent.getPaddingTop() + parent.getPaddingBottom();
        }

        int newPos = position;
        int oldLine = getLineIndex(selectPos, columnCount);
        int newLine = getLineIndex(newPos, columnCount);
//        LOG.i("2 selectPos:" + selectPos + ", newPos:" + newPos + ", oldLine:" + oldLine + ", newLine:"
//                + newLine);
        selectPos = newPos;

        // 焦点竖直方向没有移动
        if (newLine == oldLine) {
//            LOG.i("3 same line");
            return;
        }

        boolean moveDown = newLine > oldLine;

        int targetLine = moveDown ? newLine + 1 : newLine - 1;
        final int targetPos = targetLine * columnCount;

//        LOG.i("6 targetLine:" + targetLine + ", targetPos:" + targetPos);
        
//        if(targetLine < 0 || targetPos > parent.getCount() - 1){
//            return;
//        }


        try {
            int top = 0;
            if (moveDown) {
                totalHeight = parent.getMeasuredHeight();
                itemHeight = parent.getChildAt(0).getMeasuredHeight();
//                LOG.i("7 totalHeight:" + totalHeight + ", itemHeight:" + itemHeight);
                
                top = totalHeight - itemHeight - parentPadding - horizontalSpacing;
            }
            final int offset = top;
//            LOG.i("8 moveDown:" + moveDown + ", offset:" + offset);

            parent.postDelayed(new Runnable(){

                @Override
                public void run() {
                  smoothScrollToPositionFromTopWithBugWorkAround((AbsListView)parent, targetPos, offset, -1);
                }
                
            }, 0);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    
    public class BaseOnScrollListener implements OnScrollListener{
        private int position, offset, duration;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                view.setOnScrollListener(mOnScrollListener);
                if(duration < 0){
                    view.smoothScrollToPositionFromTop(position, offset);
                }else{
                    view.smoothScrollToPositionFromTop(position, offset, duration);
                }
            }
            if(mOnScrollListener != null){
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(mOnScrollListener != null){
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
        
        private void setScrollParams(int position, int offset, int duration){
            this.position = position;
            this.offset = offset;
            this.duration = duration;
        }
    }

    /**
     * 解决android系统滚动BUG
     * 来源：http://stackoverflow.com/questions/14479078/smoothscrolltopositionfromtop-is-not-always-working-like-it-should
     * @Title: smoothScrollToPositionFromTopWithBugWorkAround
     * @param listView
     * @param position
     * @param offset
     * @param duration
     * @return void
     * @date 2013-12-11 上午10:44:29
     */
    private void smoothScrollToPositionFromTopWithBugWorkAround(final AbsListView listView,
            final int position,
            final int offset,
            final int duration) {

        // the bug workaround involves listening to when it has finished scrolling, and then
        // firing a new scroll to the same position.

        // the bug is the case that sometimes smooth Scroll To Position sort of misses its intended position.
        // more info here : https://code.google.com/p/android/issues/detail?id=36062
        if(duration < 0){
            listView.smoothScrollToPositionFromTop(position, offset);
        }else{
            listView.smoothScrollToPositionFromTop(position, offset, duration);
        }
        
        mScrollListener.setScrollParams(position, offset, duration);
        listView.setOnScrollListener(mScrollListener);
    }
}
