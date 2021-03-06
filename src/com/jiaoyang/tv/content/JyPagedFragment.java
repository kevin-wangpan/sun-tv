package com.jiaoyang.tv.content;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
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

import com.jiaoyang.tv.DetailActivity;
import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.tv.content.JyMetroAdapter.ViewHolder;
import com.jiaoyang.tv.data.HomePageData;
import com.jiaoyang.tv.data.HttpDataFetcher;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.tv.util.Logger;
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

    private int mHomePageIndex; //首页的第几个tab页
    private LoadDataTask mLoadDataTask;

    public static final float FOCUS_SCALE_FACTOR = 1.05f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mHomePageIndex = args.getInt(NaviControlFragment.HOME_PAGE_TAB_INDEX_KEY, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_homepage, container, false);
        initViews(root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (HttpDataFetcher.getInstance().getHomePage(mHomePageIndex) == null) {
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
            HomePageData homePage = HttpDataFetcher.getInstance().getHomePage(mHomePageIndex);
            if (homePage == null) {
                HttpDataFetcher.getInstance().loadHomePage();
                homePage = HttpDataFetcher.getInstance().getHomePage(mHomePageIndex);
            }
            return homePage != null;
        } catch (Exception e) {
            LOG.warn(e);
            showToast(R.string.get_data_failed, Toast.LENGTH_LONG);
            return false;
        }

    }

    private void fillData() {
        MainActivity activity = (MainActivity) getActivity();
        HomePageData data = HttpDataFetcher.getInstance().getHomePage(mHomePageIndex);
        if (data == null) {
            Log.e("jiaoyang", "page data is null!");
            startLoadData();
            return;
        }
        if (mHomePageIndex == 0) {
            //第一个tab时，更新导航上各个tab的名字
            for (int i = 0; i < NaviControlFragment.HOME_PAGE_TAB_COUNT; i++) {
                HomePageData pageData = HttpDataFetcher.getInstance().getHomePage(i);
                if (pageData != null && !TextUtils.isEmpty(pageData.title)) {
                    activity.getNaviControlFragment().updateTabTitle(i, pageData.title);
                }
            }
        }
        JyPagerAdapter adapter = new JyPagerAdapter(activity, this, this, getImageFetcher());
        adapter.setData(JyUtils.transferToArrayList(data));
        mPager.setAdapter(adapter);
        activity.getSliderBarFragment().setTotalPages(adapter.getCount());
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
            DetailActivity.startDetailActivity(getActivity(), movie.mid, movie.title);
        }
    }

    @Override
    public int getFragmentType() {
        return mHomePageIndex;
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
