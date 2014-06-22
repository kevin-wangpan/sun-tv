package com.jiaoyang.tv.detail;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.jiaoyang.base.caching.ImageCache;
import com.jiaoyang.base.caching.ImageFetcher;
import com.jiaoyang.tv.JyBaseFragment;
import com.jiaoyang.tv.app.JiaoyangTvApplication;
import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.tv.data.EpisodeList;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.video.tv.R;
import com.jiaoyang.tv.util.Logger;

/**
 * 电影详细页,又称中间页
 * 
 * @author admin
 * 
 */
public class DetailFragment extends JyBaseFragment {
    private static final Logger LOG = Logger.getLogger(DetailFragment.class);

    private View mContentView;

    private MovieDetailBasicFragment mDetailBasicView;

    private ProgressBar mEpisodeProgress;
    private View mReloadContainer;
    private ImageButton mReloadButton;

    private LoadMovieDetailTask mLoadMovieDetailTask;
    private boolean mFirstCreated = true;

    private EpisodeList mEpisodes;
    private Movie mMovieDetailInfo;
    private int mMovieType;
    private int mMovieId;

    private boolean mIsLoading = true;
    
    public boolean isLoading() {
        return mIsLoading;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mFirstCreated) {
            retrieveDataFromIntent();
            fillViews();
            mFirstCreated = false;

            mLoadMovieDetailTask = new LoadMovieDetailTask();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mLoadMovieDetailTask.execute();
            } else {
                mLoadMovieDetailTask
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            fillViews();
            fillDataToViews();
            updateEpisodeList();
        }

    }

    private void fillDataToViews() {
        mContentView.setVisibility(View.VISIBLE);
        mEpisodeProgress.setVisibility(View.GONE);
        populateViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getImageFetcher() != null) {
            getImageFetcher().setExitTasksEarly(false);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (getImageFetcher() != null) {
            getImageFetcher().setExitTasksEarly(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getImageFetcher() != null) {
            getImageFetcher().setExitTasksEarly(true);
        }

        cancelTasks();
    }

    protected ImageCache getImageCache() {
        ImageCache cache = null;

        JiaoyangTvApplication ca = (JiaoyangTvApplication) getActivity().getApplication();
        if (ca != null) {
            cache = ca.getImageCache();
        }

        return cache;
    }

    protected ImageFetcher getImageFetcher() {
        return getImageFetcher(R.drawable.poster_default);
    }

    protected void finish() {
        getFragmentManager().popBackStack();
    }

    public void cancelTasks() {
        if (mLoadMovieDetailTask != null) {
            mLoadMovieDetailTask.cancel(true);
            mLoadMovieDetailTask = null;
        }
    }

    private void fillViews() {
        mReloadContainer = getView().findViewById(R.id.empty_view);
        mReloadButton = (ImageButton) getView().findViewById(R.id.empty_reload);
        mReloadButton.setVisibility(View.VISIBLE);
        mReloadButton.setOnClickListener(mOnClickListener);
        mEpisodeProgress = (ProgressBar) getView().findViewById(
                R.id.pd_episode_loading);

        mContentView = getView().findViewById(R.id.detail_container);
        mDetailBasicView = new MovieDetailBasicFragment();
        mDetailBasicView.setArguments(getArguments());
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_container, mDetailBasicView).commit();

    }

    private void retrieveDataFromIntent() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieId = arguments.getInt("id");
            mMovieType = arguments.getInt("type");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void reload() {
        mReloadContainer.setVisibility(View.GONE);
        mReloadButton.setFocusable(false);
        mContentView.setVisibility(View.GONE);

        cancelTasks();
        mLoadMovieDetailTask = new LoadMovieDetailTask();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mLoadMovieDetailTask.execute();
        } else {
            mLoadMovieDetailTask
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void populateViews() {
        if (mMovieDetailInfo != null) {
            mReloadContainer.setVisibility(View.GONE);
            mDetailBasicView.updateMovieDetailInfo(mMovieDetailInfo);
        }
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            final int id = v.getId();
            switch (id) {
            case R.id.empty_reload:
                reload();
                break;

            default:
                break;
            }
        }
    };

    private void updateEpisodeList() {
        mDetailBasicView.updateEpisodesInfo(mEpisodes);
    }

    private class LoadMovieDetailTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mEpisodeProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mMovieDetailInfo = HttpDataFetcher.getInstance().getMovieDetail(mMovieType, mMovieId);

                if (!isCancelled()) {
                    publishProgress();

                    if (mMovieDetailInfo != null) {
                        mEpisodes = HttpDataFetcher.getInstance().getMovieEpisodes(mMovieType,
                                mMovieId);
                    }
                }


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (!isCancelled()) {
                if (mMovieDetailInfo != null) {
                    LOG.debug("load movie detail info success.");

                    populateViews();

                    mContentView.setVisibility(View.VISIBLE);
                } else {
                    mReloadContainer.setVisibility(View.VISIBLE);
                    mReloadButton.setFocusable(true);
                    mReloadButton.requestFocus();
                    mEpisodeProgress.setVisibility(View.GONE);
                    mContentView.setVisibility(View.GONE);
                }

            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!isCancelled()) {
                mEpisodeProgress.setVisibility(View.GONE);
                mIsLoading = false;
                
                if (mEpisodes != null && mMovieDetailInfo != null) {
                    LOG.debug("load movie multi episode detail info success.");

                    updateEpisodeList();
                } else {
                    mReloadContainer.setVisibility(View.VISIBLE);
                    mReloadButton.setFocusable(true);
                    mReloadButton.requestFocus();
                    mContentView.setVisibility(View.GONE);
                }
                
            }
        }
    }

    protected void showToast(String text, int duration) {
        Context context = getActivity();
        if (context != null)
            Util.showToast(context, text, duration);
    }

    protected void showToast(int resId, int duration) {
        showToast(getString(resId), duration);
    }

}
