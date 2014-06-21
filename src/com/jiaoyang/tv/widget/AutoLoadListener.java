package com.jiaoyang.tv.widget;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 滚动至列表底部，读取下一页数据
 */
public class AutoLoadListener implements OnScrollListener {

	public interface AutoLoadCallBack {
		void execute();
	}

	private AutoLoadCallBack mCallback;

	public AutoLoadListener(AutoLoadCallBack callback) {
		this.mCallback = callback;
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	    if(firstVisibleItem + visibleItemCount == totalItemCount){
            mCallback.execute();
	    }
	}
}
