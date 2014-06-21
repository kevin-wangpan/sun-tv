package com.jiaoyang.tv.content;

import java.util.ArrayList;

import com.jiaoyang.base.caching.ImageFetcher;
import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.video.tv.R;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;

/**
 * 首页每个栏目的ViewPager的adapter
 */
public class JyPagerAdapter extends PagerAdapter {

    private static final int MOVIE_COUNT_PER_PAGE = 12;
    private ArrayList<Movie> movies;
    private MainActivity context;
    private OnFocusChangeListener focusListener;
    private OnClickListener clickListener;
    private ImageFetcher fetcher;

    public JyPagerAdapter (MainActivity ctx, OnFocusChangeListener focusListener,
            OnClickListener clickListener, ImageFetcher fetcher) {
        context = ctx;
        this.focusListener = focusListener;
        this.clickListener = clickListener;
        this.fetcher = fetcher;
    }

    public void setData(ArrayList<Movie> movies) {
        this.movies = movies;
    }

    @Override
    public int getCount() {
        if (movies == null) {
            return 0;
        }
        int count = 0;
        for (Movie m : movies) {
            count += m.getSize();
        }
        return count / MOVIE_COUNT_PER_PAGE + (count % MOVIE_COUNT_PER_PAGE == 0 ? 0 : 1);
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        MetroContainer metroContainer = (MetroContainer) inflater.inflate(R.layout.jy_metro_container, null);
        JyMetroAdapter adapter = new JyMetroAdapter(context, focusListener, clickListener, fetcher);
        adapter.setData(getDataOfPage(movies, position));
        metroContainer.setAdapter(adapter);
        container.addView(metroContainer);
        return metroContainer;
    }

    /**
     * 获取指定页的数据
     * @param movies 全部的数据
     * @param page 指定的页码
     */
    private ArrayList<Movie> getDataOfPage(ArrayList<Movie> movies, int page) {
        ArrayList<Movie> pagedMovies = new ArrayList<Movie>();
        int size = movies.size();
        int count = 0;
        for (int i = 0; i < size; i ++) {
            Movie m = movies.get(i);
            count += m.getSize();
            if (count <= page * MOVIE_COUNT_PER_PAGE) {
                continue;
            }
            if (count > (page + 1) * MOVIE_COUNT_PER_PAGE) {
                break;
            }
            if (count > page * MOVIE_COUNT_PER_PAGE &&
                    count <= (page + 1) * MOVIE_COUNT_PER_PAGE) {
                pagedMovies.add(m);
            }
            
        }
        return pagedMovies;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
