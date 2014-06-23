package com.jiaoyang.tv.player;

import com.jiaoyang.tv.data.Movie;

public class VideoInfoManager {
    private static VideoInfoManager sInstance = null;

    private Movie mMovie;
    private int mIndex;

    public static void prepareVideoInfo(
            Movie mMovie, int index) {
        if (sInstance == null) {
            sInstance = new VideoInfoManager();
        }

        sInstance.init(mMovie, index);
    }

    public static VideoInfoManager getInstance() {
        return sInstance;
    }

    public int getIndex() {
        return mIndex;
    }

    private void init(Movie movie,
            int index) {
        mMovie = movie;
        mIndex = index;
    }

    public Movie getMovie() {
        return mMovie;
    }
}
