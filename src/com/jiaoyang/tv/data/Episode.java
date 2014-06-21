package com.jiaoyang.tv.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.util.SparseArray;

import com.jiaoyang.base.misc.JiaoyangConstants.PlayProfile;
import com.jiaoyang.tv.data.Episode.Part.URL;
import com.jiaoyang.tv.util.Logger;

public class Episode implements Serializable {
    private static final Logger LOG = Logger.getLogger(Episode.class);

    private static final long serialVersionUID = 1135217010470114423L;

    public static class Part implements Serializable {
        private static final long serialVersionUID = 9006812340349124143L;

        public static class URL implements Serializable {
            private static final long serialVersionUID = 4487969993405265591L;

            public int profile;
            public String url;
            public long file_size;
        }

        public int index;
        public int id;
        private URL[] urls;
        public String screen_shot = "";
        private URL mLowDeviceURL;// 低端机的Mp4播放地址

        public Part() {
        }

        public Part(int size) {
            urls = new URL[size];
        }

        public void addURL(URL url, int pos) {
            if (pos < urls.length && pos >= 0) {
                urls[pos] = url;
            } else {
                LOG.warn("out of bounds");
            }

        }

        private transient Map<Integer, URL> mURLMap;

        public URL getURLByProfile(int profile) {
            ensureMap();
            if (profile == PlayProfile.LOW_PROFILE) {
                return mLowDeviceURL;
            }
            return mURLMap.get(profile);
        }

        public Set<Integer> getProfiles() {
            ensureMap();

            return mURLMap.keySet();
        }

        public List<URL> getURLS() {
            ensureMap();

            return new ArrayList<Episode.Part.URL>(mURLMap.values());
        }

        @SuppressLint("UseSparseArrays")
        void ensureMap() {
            if (mURLMap == null) {
                mURLMap = new HashMap<Integer, URL>(urls.length);
                for (URL url : urls) {
//                    if (url.profile >= PlayProfile.SMOOTH_PROFILE && url.profile <= PlayProfile.HIGH_PROFILE) {
                    if (url.profile >= PlayProfile.SMOOTH_PROFILE && url.profile <= PlayProfile.SUPER_PROFILE) {
                        mURLMap.put(url.profile, url);
                    } else if (url.profile == PlayProfile.LOW_PROFILE) {
                        mLowDeviceURL = url;
                    }
                }
            }
        }
    }

    public int index;
    public String label;
    public String title;
    public Part[] parts;
    public boolean advance;

    private transient SparseArray<Part> mPartMap;

    public Episode() {
    }

    public Episode(int size) {
        parts = new Part[size];
    }

    public void addPart(Part part, int pos) {
        if (pos < parts.length && pos >= 0) {
            parts[pos] = part;
        } else {
            LOG.warn("out of bounds");
        }
    }

    public Part getPartByIndex(int index) {
        if (mPartMap == null) {
            mPartMap = new SparseArray<Part>(parts.length);
            for (Part part : parts) {
                mPartMap.append(part.index, part);
            }
        }

        return mPartMap.get(index);
    }

    public boolean isSupportPlay() {
        Part part = getPartByIndex(0);
        if (part != null) {
            part.ensureMap();

            return part.mURLMap == null ? false : part.mURLMap.size() > 0;
        }

        return false;
    }
    
    /**
     * 根据profile得到episode的大小
     * @Title: getSizeByProfile
     * @param pf
     * @return
     * @return long
     * @date 2013-12-30 上午10:55:05
     */
    public long getSizeByProfile(int pf){
        long size = 0;
        for(Part p : parts){
            URL pURL = p.getURLByProfile(pf);
            if(pURL == null){
                Set<Integer> profiles = p.getProfiles();
                int _profile = PlayProfile.BASE_PROFILE;
                if (profiles.contains(PlayProfile.SUPER_PROFILE)){
                    _profile = PlayProfile.SUPER_PROFILE;
                } else if (profiles.contains(PlayProfile.HIGH_PROFILE)) {
                    _profile = PlayProfile.HIGH_PROFILE;
                } else if (profiles.contains(PlayProfile.BASE_PROFILE)) {
                    _profile = PlayProfile.BASE_PROFILE;
                } else if (profiles.contains(PlayProfile.SMOOTH_PROFILE)) {
                    _profile = PlayProfile.SMOOTH_PROFILE;
                }
                pURL = p.getURLByProfile(_profile);
            }
            if(pURL == null){
                continue;
            }
            size += pURL.file_size;
        }
        return size;
    }
}
