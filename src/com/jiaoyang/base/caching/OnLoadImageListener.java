package com.jiaoyang.base.caching;

import android.graphics.Bitmap;

public interface OnLoadImageListener {

    public final static int LOAD_SUCCEED = 1;
    public final static int LOAD_FAILED = 2;

    public void onLoadCompleted(Bitmap bmp);
}
