package com.jiaoyang.tv.data;

import java.io.Serializable;

import android.util.SparseArray;

import com.jiaoyang.tv.util.Logger;

public class EpisodeList implements Serializable, Cloneable{
    private static final Logger LOG = Logger.getLogger(EpisodeList.class);

    private static final long serialVersionUID = 1L;
    public int id;
    public int type;
    public String title;
    public String label;
    public int productId;
    public String gcid;
    public String cid;
    
    public String posterUrl;// 影片图片地址

    /**
     *  1. 网格剧集列表（电视剧，动漫） 2. 单行剧集列表（综艺等）3. 无剧集列表（单集且完结）
     */
    public int displayType2;
    private boolean downloadable;
    public Episode[] episodes;

    private transient SparseArray<Episode> mEpisodeMap;

    public EpisodeList() {
    }

    public EpisodeList(int size) {
        episodes = new Episode[size];
    }
    
    public void setDownloadable(boolean enable){
        downloadable = enable;
    }
    
    public void addEpisode(Episode episode, int pos) {
        if (pos < episodes.length && pos >= 0) {
            episodes[pos] = episode;
        } else {
            LOG.debug("out of bounds");
        }
    }

    public Episode getEpisodeByIndex(int index) {
        if (mEpisodeMap == null) {
            mEpisodeMap = new SparseArray<Episode>(episodes.length);
            for (Episode episode : episodes) {
                mEpisodeMap.append(episode.index, episode);
            }
        }

        return mEpisodeMap.get(index);
    }

    public Episode getEpisodeByPartId(int partId) {
        Episode episode = null;
        for (int i = 0; i < episodes.length; i++) {
            Episode temp = episodes[i];
            if (partId == temp.parts[0].id) {
                episode = temp;
                break;
            }
        }
        return episode;
    }

    public boolean isSupportPlay() {
        Episode episode = null;
        if (episodes != null && episodes.length > 0) {
            episode = episodes[0];
        }

        return episode == null ? false : episode.isSupportPlay();
    }
    
    public boolean isSupportDownload(){
        return hasDownloadEpisode() && downloadable;
    }
    
    private boolean hasDownloadEpisode() {
        if (episodes == null || episodes.length <= 0) {
            return false;
        }
        for (Episode episode : episodes) {
            if (!episode.advance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EpisodeList clone() {
        EpisodeList res = null;
        try {
            res = (EpisodeList) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return res;
    }
    
    
}
