package com.jiaoyang.tv.detail;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiaoyang.base.util.StringEx;
import com.jiaoyang.tv.BaseImageAdapter;
import com.jiaoyang.tv.JyBaseFragment;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.tv.player.PlayerAdapter;
import com.jiaoyang.tv.player.VideoInfoManager;
import com.jiaoyang.video.tv.R;

public class MovieDetailBasicFragment extends JyBaseFragment {

    private ImageView mMovieCover;
    private ImageView mMovieProfile;
    private ImageView mMoviePlay;
    private TextView mTitle;
    private TextView mActors;
    private TextView mDirector;
    private TextView mLanguage;
    private TextView mArea;
    private TextView mTime;
    private TextView mPoints;
    private TextView mMovieIntro;

    private ImageView mArrowUp;
    private ImageView mArrowDown;
    private GridView mEpisodeSelector;

    private Movie mMovie;
    private boolean mViewCreated;


    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.movie_detail_basic, container, false);
        initializeView(root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mViewCreated = false;
    }

    public void updateMovieDetailInfo(Movie movie) {
        mMovie = movie;
        updateBasicInfo();
        updateEpisodeInfo();
        mMoviePlay.requestFocus();
    }

    private void initializeView(View root) {
        mMovieCover = (ImageView) root
                .findViewById(R.id.iv_intermediatepage_movie_cover);
        mMoviePlay = (ImageView) root.findViewById(R.id.iv_play);
        mMoviePlay.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (mMoviePlay == null) {
                    return;
                }
                if (hasFocus) {
                    mMoviePlay.setBackgroundResource(R.drawable.jy_play_focused);
                    mMoviePlay.setImageResource(R.drawable.jy_detail_play);
                    mEpisodeSelector.setSelection(-1);
                } else {
                    mMoviePlay.setBackgroundColor(0xff);
                    mMoviePlay.setImageResource(android.R.color.transparent);
                }
            }
        });
        mMoviePlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: 播放记录
                playMovie(0);
            }
        });
        mMovieProfile = (ImageView) root.findViewById(R.id.iv_intermediatepage_movie_profile);
        mTitle = (TextView) root.findViewById(R.id.tv_movie_name);
        mActors = (TextView) root
                .findViewById(R.id.tv_actors);
        mDirector = (TextView) root
                .findViewById(R.id.tv_director);
        mLanguage = (TextView) root.findViewById(R.id.tv_language);
        mArea = (TextView) root.findViewById(R.id.tv_area);
        mTime = (TextView) root.findViewById(R.id.tv_time);
        mPoints = (TextView) root.findViewById(R.id.tv_points);
        mMovieIntro = (TextView) root.findViewById(R.id.tv_movie_intro);

        mArrowUp = (ImageView) root.findViewById(R.id.arrow_up);
        mArrowDown = (ImageView) root.findViewById(R.id.arrow_down);
        mEpisodeSelector = (GridView) root.findViewById(R.id.episode_selector_gridview);

        mViewCreated = true;

        updateBasicInfo();
        updateEpisodeInfo();
        mMoviePlay.requestFocus();
    }

    private void updateBasicInfo() {
        if (mViewCreated && mMovie != null) {
            mTitle.setText(StringEx.fromHtml(mMovie.title));
            updatePosterView();
            updateDirectorView();
            mLanguage.setText("语言：" + escapeText(mMovie.language));
            mArea.setText("地区：" + escapeText(mMovie.area));
            mTime.setText("年代：" + escapeText(mMovie.time));
            mPoints.setText("看点：" + escapeText(mMovie.points));
            updateVideoIntroduce();
        }
    }

    private void updateEpisodeInfo() {
        if (mViewCreated && mMovie != null) {
            EpisodeSelectorAdapter adapter = new EpisodeSelectorAdapter();
            adapter.setData(mMovie.videos.length);
            mEpisodeSelector.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    playMovie(position);
                }
            });
            mEpisodeSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    updateArrowsVisibility();
                }

                private void updateArrowsVisibility() {
                    //TODO
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {}
            });
            mEpisodeSelector.setAdapter(adapter);
            mArrowUp.setVisibility(View.VISIBLE);
            mArrowDown.setVisibility(View.VISIBLE);
        }
    }

    private void updateVideoIntroduce() {
        if (mMovie != null) {
            String str = mMovie.intro;
            mMovieIntro.setText("剧情简介：" + Html.fromHtml(str));
        }
    }

    private void updatePosterView() {
        String imageUrl = mMovie.getPosterUrl();
        if (!(StringEx.isNullOrEmpty(imageUrl))) {
            getImageFetcher().loadImage(imageUrl, mMovieCover);
        }
    }

    private void updateDirectorView() {
        mDirector.setText(escapeText(mMovie.info));
    }

    /**
     * 将空字符串转换为"未知"
     */
    private String escapeText(String str) {
        if (TextUtils.isEmpty(str)) {
            str = getResources().getString(R.string.unknown);
        }

        return str;
    }

    private void showDialog(String title, String msg) {
        Builder builder = new Builder(getActivity());
        builder.setTitle(title).setMessage(msg);
        builder.setPositiveButton("确定", null);
        Dialog dialog = builder.create();
        dialog.show();
    }

    public Bitmap getMoviePoster() {
        try {
            Drawable d = mMovieCover.getDrawable();
            if (d instanceof BitmapDrawable) {
                return ((BitmapDrawable) d).getBitmap();
            } else if (d instanceof TransitionDrawable) {
                return ((BitmapDrawable) (((TransitionDrawable) d)
                        .getDrawable(1))).getBitmap();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void playMovie(int position) {
        if (mMovie != null) {
                PlayerAdapter.getInstance().play(getActivity(), mMovie.videos[position], mMovie.title);
        } else {
            showDialog("提示", "无法播放该视频");
        }
    }

    private class EpisodeSelectorAdapter extends BaseImageAdapter {

        private int episodeCount;

        public void setData(int episodesCount) {
            this.episodeCount = episodesCount;
        }

        @Override
        public int getCount() {
            return episodeCount;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.jy_episode_item, null);
            }
            ((TextView)convertView).setText("" + (position + 1));
            return convertView;
        }
    }
}
