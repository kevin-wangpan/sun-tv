package com.jiaoyang.tv.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.net.Uri;
import android.util.Pair;

import com.jiaoyang.tv.util.Logger;

public class URLRequest {
    private static final Logger LOG = Logger.getLogger(URLRequest.class);

    public static final int TYPE_STANDARD = 0;
    public static final int TYPE_EXTRA = TYPE_STANDARD + 1;

    private String mUrl;
    private int mType;

    public URLRequest(String url) {
        this(url, TYPE_STANDARD);
    }

    public URLRequest(String url, int type) {
        mUrl = url;
        mType = type;
    }

    public void appendQueryParameter(String key, String value) {
        if (mType == TYPE_STANDARD) {
            mUrl = Uri.parse(mUrl).buildUpon().appendQueryParameter(key, value).build().toString();
        } else {
            try {
                mUrl = mUrl + key + "," + URLEncoder.encode(value, "UTF-8") + "/";
            } catch (UnsupportedEncodingException e) {
                LOG.warn("encode url failed. err={}", e.toString());

                mUrl = mUrl + key + "," + value + "/";
            }
        }
    }

    public void appendQueryParameter(String key, int value) {
        appendQueryParameter(key, Integer.toString(value));
    }

    public void appendQueryParameters(ParameterList parameters) {
        for (Pair<String, String> param : parameters) {
            appendQueryParameter(param.first, param.second);
        }
    }

    @Override
    public String toString() {
        return mUrl;
    }
}
