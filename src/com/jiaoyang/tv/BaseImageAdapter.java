package com.jiaoyang.tv;

import android.widget.BaseAdapter;

import com.jiaoyang.base.caching.ImageFetcher;

public abstract class BaseImageAdapter extends BaseAdapter {

    protected ImageFetcher mImageFetcher;

    public void setImageFetcher(ImageFetcher fetcher) {
        this.mImageFetcher = fetcher;
    }

    public ImageFetcher getImageFetcher() {

        return mImageFetcher;
    }
}
