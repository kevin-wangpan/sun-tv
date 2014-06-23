package com.jiaoyang.tv.content;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiaoyang.base.caching.ImageFetcher;
import com.jiaoyang.base.caching.OnLoadImageListener;
import com.jiaoyang.tv.MainActivity;
import com.jiaoyang.tv.content.MetroContainer.MetroContainerAdapter;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.video.tv.R;

/**
 * 每个栏目的每一页的metro adapter
 */
public class JyMetroAdapter extends MetroContainerAdapter {

    private LayoutInflater mInflater;
    private Context mContext;

    private OnFocusChangeListener mFocusListener;
    private OnClickListener mClickListener;
    private ImageFetcher mImageFetcher;

    public static final int FIRST_VIEW_ID = 1;

    private ArrayList<Movie> mMovies = new ArrayList<Movie>();

    public JyMetroAdapter(Context context,
            OnFocusChangeListener focusListener, OnClickListener clickListener,
            ImageFetcher fetcher) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFocusListener = focusListener;
        mClickListener = clickListener;
        mImageFetcher = fetcher;
    }

    public void setData(ArrayList<Movie> movies) {
        mMovies = movies;
    }

    @Override
    public View getView(int position) {
        View v = mInflater.inflate(getLayoutResId(getType(position)), null);
        populateView(v, position);
        return v;
    }

    private void populateView(View v, int position) {
        Movie m = getItem(position);
        v.setId(position + FIRST_VIEW_ID);
        v.setOnFocusChangeListener(mFocusListener);
        v.setOnClickListener(mClickListener);
        setupViewHolder(v, m);
    }

    private void setupViewHolder(View v, Movie movie) {
        if (v == null) {
            return;
        }
        ViewHolder holder = new ViewHolder();
        holder.poster = (ImageView) v.findViewById(R.id.poster);
        holder.bitrate = (ImageView) v.findViewById(R.id.bitrate);
        holder.title = (TextView) v.findViewById(R.id.title);
        holder.movie = movie;
        v.setTag(holder);

        if (holder.title != null) {
            holder.title.setText(movie.title);
        }
        if (holder.poster != null) {
            mImageFetcher.setImageLoadListener(new OnLoadImageListener() {
                @Override
                public void onLoadCompleted(Bitmap bmp) {
                    if (mContext != null && mContext instanceof MainActivity) {
                        ((MainActivity) mContext).refreshFocusShadow();
                    }
                }
            });
            mImageFetcher.loadImage(movie.getPosterUrl(), holder.poster);
        }
        if (holder.bitrate != null) {
            holder.bitrate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    @Override
    public int getRowSpan(int position) {
        int type = getType(position);
        switch(type) {
        case Movie.LAYOUT_TYPE_NORMAL:
            return 1;
        case Movie.LAYOUT_TYPE_LANDSCAPE:
            return 1;
        case Movie.LAYOUT_TYPE_PORTRAIT:
            return 2;
        default:
            return 1;
        }
    }

    @Override
    public int getType(int position) {
        return getItem(position).layoutType;
    }

    @Override
    public int getLayoutResId(int type) {
        switch (type) {
        case Movie.LAYOUT_TYPE_NORMAL:
            return R.layout.metro_item_normal;
        case Movie.LAYOUT_TYPE_LANDSCAPE:
            return R.layout.metro_item_landscape;
        case Movie.LAYOUT_TYPE_PORTRAIT:
            return R.layout.metro_item_portrait;
        default:
            return 0;
        }
    }

    @Override
    public Movie getItem(int position) {
        return mMovies.get(position);
    }

    static class ViewHolder {
        public ImageView poster;
        public ImageView bitrate;
        public TextView title;
        public Movie movie;
    }

}
