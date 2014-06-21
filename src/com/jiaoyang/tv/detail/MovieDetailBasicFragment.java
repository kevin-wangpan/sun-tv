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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiaoyang.base.data.MovieType;
import com.jiaoyang.base.util.StringEx;
import com.jiaoyang.tv.BaseImageAdapter;
import com.jiaoyang.tv.JyBaseFragment;
import com.jiaoyang.tv.data.Episode;
import com.jiaoyang.tv.data.EpisodeList;
import com.jiaoyang.tv.data.Movie;
import com.jiaoyang.tv.player.EpisodeAdapterUtil;
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
    private EpisodeList mEpisodeList;
    private int mMovieType;
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

    public void updateMovieDetailInfo(Movie info) {
        mMovie = info;
        mMovieType = info.type;

        updateBasicInfo();
    }

    public void updateEpisodesInfo(EpisodeList info) {
        mEpisodeList = info;
        updateEpisodeInfo();
        mMovieCover.setFocusable(true);
        mMovieCover.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                Log.e("wangpan", "focus:" + mMovieCover.requestFocus());
            }
        }, 100);
    }

    private void initializeView(View root) {
        mMovieCover = (ImageView) root
                .findViewById(R.id.iv_intermediatepage_movie_cover);
        mMoviePlay = (ImageView) root.findViewById(R.id.iv_play);
        mMovieCover.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (mMoviePlay == null) {
                    return;
                }
                if (hasFocus) {
                    mMoviePlay.setVisibility(View.VISIBLE);
                    mMoviePlay.setBackgroundResource(R.drawable.jy_play_focused);
                    mMoviePlay.setImageResource(R.drawable.jy_detail_play);
                } else {
                    mMoviePlay.setVisibility(View.INVISIBLE);
                }
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
    }

    private void updateBasicInfo() {
        if (mViewCreated && mMovie != null) {
            mTitle.setText(StringEx.fromHtml(mMovie.title));
            updatePosterView();
            updateMovieProfile();
            updateDirectorView();
            updateActors();
            mLanguage.setText("语言：" + escapeText(mMovie.label));
            mArea.setText("地区：" + escapeText(mMovie.label));
            mTime.setText("年代：" + escapeText(mMovie.year));
            mPoints.setText("看点：" + escapeText(mMovie.label));
            updateVideoIntroduce();
        }
    }

    private void updateEpisodeInfo() {
        if (mViewCreated && mEpisodeList != null) {
            EpisodeSelectorAdapter adapter = new EpisodeSelectorAdapter();
            adapter.setData(mEpisodeList.episodes);
            mEpisodeSelector.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    playMovie();
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
        }
    }

    private void updateMovieProfile() {
        if (mMovie.bitrate == null || mMovie.bitrate.equals("")) {
            mMovieProfile.setVisibility(View.GONE);
        } else {
            if (mMovie.bitrate.equalsIgnoreCase("720P")) {
                mMovieProfile.setVisibility(View.VISIBLE);
                mMovieProfile.setImageResource(R.drawable.detail_profile_720p);
            } else if (mMovie.bitrate.equalsIgnoreCase("1080P")) {
                mMovieProfile.setVisibility(View.VISIBLE);
                mMovieProfile.setImageResource(R.drawable.detail_profile_1080p);
            }
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
        if (mMovieType == MovieType.DOCUMENTARY) {
            mDirector.setVisibility(View.GONE);
        } else {
            if (TextUtils.isEmpty(mMovie.directorName)) {
                String actor = escapeText(StringEx.join(mMovie.actors));
                String actorName = mMovie.actorName;
                actor = actorName + " : " + actor;
                // mDirectorLabel.setText(styleText(actor));
                mDirector.setText(actor);
            } else {
                String director = escapeText(StringEx.join(mMovie.directors));
                String directorName = mMovie.directorName;
                director = directorName + " : " + director;
                // mDirectorLabel.setText(styleText(director));
                mDirector.setText(director);
            }
        }
    }

    private void updateActors() {
        if (mMovieType == MovieType.VARIETY_SHOW
                || mMovieType == MovieType.DOCUMENTARY
                || mMovieType == MovieType.OPEN_COURSES) {
            mActors.setVisibility(View.GONE);
        } else {
            String artist = escapeText(StringEx.join(mMovie.actors));
            artist = mMovie.actorName + " : " + artist;
            // mArtistLabel.setText(styleText(artist));
            mActors.setText(artist);
        }
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

    private void playMovie() {
        // 付费频道，有权限播放
        if (true) {
            EpisodeList episodeList = mEpisodeList;
            if (episodeList != null) {
                Episode episodeInfo = null;
                if (false /*mPlayRecord != null*/) {
//                    episodeInfo = episodeList
//                            .getEpisodeByIndex(mPlayRecord.index);
                } else {
                    if (mEpisodeList.episodes != null && mEpisodeList.episodes.length > 0) {
                        episodeInfo = mEpisodeList.episodes[0];
                    }
                }

                VideoInfoManager.prepareVideoInfo(episodeList,
                        episodeInfo.index, 0, false, true);
                if (mMovie != null) {
                    PlayerAdapter.getInstance().play(getActivity(), mMovie.getPosterUrl(), mMovie.bitrate);
                }
            } else {
                showDialog("提示", "无法播放该视频");
            }
        } else {
            if (mEpisodeList != null) {
                Episode episodeInfo = null;
                int partIndex = 0;

                if (false/*mPlayRecord != null*/) {
//                    episodeInfo = mEpisodeList
//                            .getEpisodeByIndex(mPlayRecord.index);
//                    partIndex = mPlayRecord.partindex;
                } else {
                    if (mEpisodeList.episodes != null && mEpisodeList.episodes.length > 0) {
                        episodeInfo = mEpisodeList.episodes[0];
                    }
                }

                VideoInfoManager
                        .prepareVideoInfo(
                                mEpisodeList,
                                episodeInfo.index,
                                partIndex,
                                false);

                if (mMovie != null) {
                    PlayerAdapter.getInstance().play(getActivity(), mMovie.getPosterUrl(), mMovie.bitrate);
                }
            }
        }
    }

    private class EpisodeSelectorAdapter extends BaseImageAdapter {

        private Episode[] episodes;

        public void setData(Episode[] episodes) {
            this.episodes = episodes;
        }

        @Override
        public int getCount() {
            if (episodes == null) {
                return 0;
            }
            return episodes.length;
        }

        @Override
        public Episode getItem(int position) {
            if (episodes == null || position < 0 || position >= episodes.length) {
                return null;
            }
            return episodes[position];
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
