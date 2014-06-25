package com.jiaoyang.tv.content;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiaoyang.tv.JyBaseFragment;
import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.video.tv.R;

public class SliderBarFragment extends JyBaseFragment {
    private static final int PER_PAGE_COUNT = 6; //一次显示多少页

    private ImageView leftIndicator;
    private ImageView rightIndicator;
    private ViewPager viewPager;
    private MyPagerAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sliderbar_fragment, container, false);
        initViews(v);
        return v;
    }

    private void initViews(View v) {
        leftIndicator = (ImageView) v.findViewById(R.id.left_indicator);
        rightIndicator = (ImageView) v.findViewById(R.id.right_indicator);
        viewPager = (ViewPager) v.findViewById(R.id.sliderbar_viewpager);
        pagerAdapter = new MyPagerAdapter((MainActivity) getActivity());
        viewPager.setAdapter(pagerAdapter);
    }

    public void setTotalPages(int totalPages) {
        getView().setVisibility(totalPages > 0 ? View.VISIBLE : View.INVISIBLE);
        leftIndicator.setVisibility(View.INVISIBLE);
        rightIndicator.setVisibility(totalPages > PER_PAGE_COUNT ? View.VISIBLE : View.INVISIBLE);
        pagerAdapter.setTotalPages(totalPages);
        pagerAdapter.notifyDataSetChanged();
    }

    private void initPageIndex() {
        HomePageFragment fragment = ((MainActivity)getActivity()).getCurrentFragment();
        if (fragment != null && fragment instanceof JyPagedFragment) {
            setCurrentPage(((JyPagedFragment)fragment).getCurrentPage());
        }
    }

    /**
     * 内容页滚动后，更新下面的sliderbar
     */
    public void setCurrentPage(int page) {
        if (pagerAdapter.getCount() == 0) {
            return;
        }
        int current = page / PER_PAGE_COUNT;
        viewPager.setCurrentItem(current);
        View currentItemView = viewPager.getChildAt(current);
        if (currentItemView == null) {
            return;
        }
        for (int i = 1; i <= PER_PAGE_COUNT; i ++) {
            View numberItem = currentItemView.findViewById(i);
            if (numberItem != null) {
                numberItem.setSelected(i == page % PER_PAGE_COUNT + 1);
            }
        }
        if (page <= PER_PAGE_COUNT) {
            leftIndicator.setVisibility(View.INVISIBLE);
        } else {
            leftIndicator.setVisibility(View.VISIBLE);
        }
        if (page > (viewPager.getAdapter().getCount() - 1) * PER_PAGE_COUNT) {
            rightIndicator.setVisibility(View.INVISIBLE);
        } else {
            rightIndicator.setVisibility(View.VISIBLE);
        }
    }
    private class MyPagerAdapter extends PagerAdapter {

        private int totalPages;
        private MainActivity context;

        public MyPagerAdapter (MainActivity ctx) {
            context = ctx;
        }

        public void setTotalPages(int total) {
            this.totalPages = total;
        }

        @Override
        public int getCount() {
            return totalPages / PER_PAGE_COUNT + (totalPages % PER_PAGE_COUNT == 0 ? 0 : 1);
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.sliderbar_per_page, null);
            for (int i = 1; i <= PER_PAGE_COUNT && position * PER_PAGE_COUNT + i <= totalPages; i++) {
                final int pageIndex = position * PER_PAGE_COUNT + i;
                LinearLayout numContainer = (LinearLayout) inflater.inflate(R.layout.sliderbar_per_number, null);
                TextView textView = (TextView) numContainer.getChildAt(0);
                textView.setText("" + pageIndex);
                textView.setFocusable(true);
                textView.setId(i);
                textView.setOnFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            if (pageIndex <= PER_PAGE_COUNT) {
                                leftIndicator.setVisibility(View.INVISIBLE);
                            } else {
                                leftIndicator.setVisibility(View.VISIBLE);
                            }
                            if (pageIndex > (viewPager.getAdapter().getCount() - 1) * PER_PAGE_COUNT) {
                                rightIndicator.setVisibility(View.INVISIBLE);
                            } else {
                                rightIndicator.setVisibility(View.VISIBLE);
                            }
                            context.getCurrentFragment().flipPager(pageIndex - 1);
                        }
                    }
                });
                if (i == 1) {
                    textView.setOnKeyListener(new OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getRepeatCount() == 1 &&
                                    viewPager.getCurrentItem() >= 1) {
                                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                            }
                            return false;
                        }
                    });
                } else if (i == PER_PAGE_COUNT) {
                    textView.setOnKeyListener(new OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getRepeatCount() == 1 &&
                                    viewPager.getCurrentItem() < viewPager.getAdapter().getCount()) {
                                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                            }
                            return false;
                        }
                    });
                }
                linearLayout.addView(numContainer);
            }
            container.addView(linearLayout);
            return linearLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            initPageIndex();
        }
    }
}
