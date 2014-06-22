package com.jiaoyang.tv.content;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jiaoyang.base.data.MovieType;
import com.jiaoyang.tv.DetailActivity;
import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.tv.content.JyMetroAdapter.ViewHolder;
import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.tv.data.HomePage;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.video.tv.R;

/**
 * 分页显示数据的fragment
 */
public class JyPagedFragment extends HomePageFragment implements OnFocusChangeListener, OnClickListener {

    private static final Logger LOG = Logger.getLogger(JyPagedFragment.class);

    private ImageButton mRefreshBtn;
    private ProgressBar mLoadingProgress;
    private View mReloadLayout;
    private ViewPager mPager;

    private LoadDataTask mLoadDataTask;

    public static final float FOCUS_SCALE_FACTOR = 1.05f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_homepage, container, false);
        initViews(root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (HttpDataFetcher.getInstance().getHomePage() == null) {
            startLoadData();
        } else {
            fillData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLoadData();
    }

    private void initViews(View root) {

        mLoadingProgress = (ProgressBar) root.findViewById(R.id.loading_progress);
        mPager = (ViewPager) root.findViewById(R.id.homepage_viewpager);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            
            @Override
            public void onPageSelected(int position) {
                setSliderBarSelectedPage(position);
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        mReloadLayout = root.findViewById(R.id.empty_view);
        mReloadLayout.setVisibility(View.GONE);

        mRefreshBtn = (ImageButton) mReloadLayout.findViewById(R.id.empty_reload);
        mRefreshBtn.setVisibility(View.GONE);

    }

    private void setSliderBarSelectedPage(int page) {
        SliderBarFragment slider = ((MainActivity)getActivity()).getSliderBarFragment();
        if (slider != null) {
            slider.setCurrentPage(page); //初始化以后，肯定显示的是第一页的内容
        }
    }

    public int getCurrentPage() {
        if (mPager == null) {
            return 0;
        } else {
            return mPager.getCurrentItem();
        }
    }

    private void startLoadData() {
        stopLoadData();
        mLoadDataTask = new LoadDataTask();
        mLoadDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void stopLoadData() {
        if (mLoadDataTask != null && !mLoadDataTask.isCancelled()) {
            mLoadDataTask.cancel(true);
            mLoadDataTask = null;
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            mReloadLayout.setVisibility(View.GONE);
            mLoadingProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... args) {
            return loadHomePage();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!isCancelled()) {
                if (result) {
                    fillData();
                } else {
                    mReloadLayout.setVisibility(View.VISIBLE);

                }

                mLoadingProgress.setVisibility(View.GONE);
            }

        }
    }

    private boolean loadHomePage() {
        try {
            HomePage homePage = HttpDataFetcher.getInstance().getHomePage();
            if (homePage == null) {
                HttpDataFetcher.getInstance().loadHomePage();
                homePage = HttpDataFetcher.getInstance().getHomePage();
            }
            return homePage != null;
        } catch (Exception e) {
            LOG.warn(e);
            showToast(R.string.get_data_failed, Toast.LENGTH_LONG);
            return false;
        }

    }

    private void fillData() {
        JyPagerAdapter adapter = new JyPagerAdapter((MainActivity) getActivity(), this, this, getImageFetcher());
        adapter.setData(JyUtils.transferToArrayList(HttpDataFetcher.getInstance().getHomePage()));
        mPager.setAdapter(adapter);
        ((MainActivity)getActivity()).getSliderBarFragment().setTotalPages(adapter.getCount());
        focusView();
    }

    @Override
    public void onFocusChange(View view, boolean focused) {
        Object o = view.getTag();
        if (focused) {
            if (o != null && o instanceof ViewHolder) {
                ((ViewHolder) o).title.setSelected(true);
            }
            view.animate().scaleX(FOCUS_SCALE_FACTOR)
                    .scaleY(FOCUS_SCALE_FACTOR)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                refreshFocusShadow();
                            }
                        })
                    .start();
        } else {
            if (o != null && o instanceof ViewHolder) {
                ((ViewHolder) o).title.setSelected(false);
            }
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
        }
    }

    @Override
    public void onClick(View view) {
        Object o = view.getTag();
        if (o == null || !(o instanceof ViewHolder)) {
            //
        } else {
            Movie movie = ((ViewHolder) o).movie;
            if (movie == null) {
                return;
            }
            if (movie.price > 0 && !Util.isSupportedDevice()) {
                showNotSurportDialog();
                return;
            }
            if (MovieType.isShortVideo(movie.type)) {
                // playShortVideo(movie);
            } else {
                Bundle arguments = new Bundle();
                arguments.putInt("id", movie.id);
                arguments.putInt("type", movie.type);
                arguments.putString("title", movie.title);
                arguments.putInt("productId", movie.productId);
                startActivity(DetailActivity.class, arguments);
            }
        }
    }

    private void startActivity(Class<DetailActivity> activityClass, Bundle arguments) {
        Intent intent = new Intent(getActivity(), activityClass);
        if (arguments != null) {
            intent.putExtras(arguments);
        }
        startActivity(intent);
    }

    private void showNotSurportDialog() {
        showSipleDialog(R.string.tip, R.string.tips_unsurport_for_play);
    }

    @Override
    public int getFragmentType() {
        return FRAGMENT_TYPE_RECOMMENDATION;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        if (!enter || nextAnim == 0) {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);

        anim.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {
                RootRelativeLayout.sDrawFocusShadow = false;
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                RootRelativeLayout.sDrawFocusShadow = true;
                refreshFocusShadow();
            }
        });

        return anim;
    }

    private void refreshFocusShadow() {
        Activity a = getActivity();
        if (a != null && a instanceof MainActivity) {
            ((MainActivity)a).refreshFocusShadow();
        }
    }

    @Override
    protected int getFirstViewId() {
        return JyMetroAdapter.FIRST_VIEW_ID;
    }

    @Override
    protected int getLastViewId() {
        //TODO
        return 0;
    }

    protected void flipPager(int page) {
        mPager.setCurrentItem(page);
    }
}
