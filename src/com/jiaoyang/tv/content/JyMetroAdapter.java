package com.jiaoyang.tv.content;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.jiaoyang.tv.data.ChannelType;
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

    private static final int MOVIE_ID_EXTRA = 0;
    private static final int MOVIE_ID_MORE = 1;

    public static final int FIRST_VIEW_ID = 1;

    private static HashMap<String, Integer> MORE_PIC = new HashMap<String, Integer>();
    static {
        MORE_PIC.put(ChannelType.MOVIE, R.drawable.video_more_movie);
        MORE_PIC.put(ChannelType.TV, R.drawable.video_more_tv);
        MORE_PIC.put(ChannelType.VARIETY_SHOW, R.drawable.video_more_variety);
        MORE_PIC.put(ChannelType.ANIME, R.drawable.video_more_anim);
    }

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
        if (movie.id == MOVIE_ID_MORE) {
            v.setId(JyPagedFragment.VIEW_ID_MORE);
            v.setTag(movie.label);
            v.findViewById(R.id.title).setVisibility(View.GONE);
            ((ImageView)(v.findViewById(R.id.poster))).setImageResource(MORE_PIC.get(movie.label));
        } else if (movie.id == MOVIE_ID_EXTRA) {
            View search = v.findViewById(R.id.extra_search);
            View favorite = v.findViewById(R.id.extra_favorite);
            View history = v.findViewById(R.id.extra_history);
            if (search != null) {
                search.setOnClickListener(mClickListener);
                search.setOnFocusChangeListener(mFocusListener);
            }
            if (favorite != null) {
                favorite.setOnClickListener(mClickListener);
                favorite.setOnFocusChangeListener(mFocusListener);
            }
            if (history != null) {
                history.setOnClickListener(mClickListener);
                history.setOnFocusChangeListener(mFocusListener);
            }
        } else {
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
                if (movie.bitrate == null || movie.bitrate.equals("")) {
                    holder.bitrate.setVisibility(View.GONE);
                } else {
                    if (movie.bitrate.equalsIgnoreCase("720P")) {
                        holder.bitrate.setVisibility(View.VISIBLE);
                        holder.bitrate.setImageResource(R.drawable.detail_profile_720p);
                    } else if (movie.bitrate.equalsIgnoreCase("1080P")) {
                        holder.bitrate.setVisibility(View.VISIBLE);
                        holder.bitrate.setImageResource(R.drawable.detail_profile_1080p);
                    } else {
                        holder.bitrate.setVisibility(View.GONE);
                    }
                }
            }
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
        case Movie.LAYOUT_TYPE_EXTAR:
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
        case Movie.LAYOUT_TYPE_EXTAR:
            return R.layout.metro_item_extra;
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
