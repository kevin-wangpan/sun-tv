package com.jiaoyang.tv.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jiaoyang.tv.util.Logger;

public class URLLoader {
    private static final Logger LOG = Logger.getLogger(URLLoader.class);

    private static final String API_VERSION = "1.0";

    private static final int CONNECTION_TIMEOUT = 4000;
    private static final int SO_TIMEOUT = 6000;

    private final Gson mGson;
    private String mCookie;

    public URLLoader() {
        mGson = new Gson();
        mCookie = null;
    }

    public <T> T loadObject(URLRequest request, Class<T> klass) {
        return loadObject(request.toString(), klass);
    }

    public Object loadObject(URLRequest request, Type type) {
        return loadObject(request.toString(), type);
    }

    public <T> T loadObject(String url, Class<T> klass) {
        String json = getJsonString(load(url));
        LOG.e("url=" + url);
        LOG.e("json=" + json);
        T resp = null;
        try {
            resp = mGson.fromJson(json, klass);
        } catch (JsonSyntaxException e) {
            LOG.warn("invalid json. err={}", e.toString());
        }
        return resp;
    }

    public Object loadObject(String url, Type type) {
        String json = getJsonString(load(url));
        LOG.d("url=" + url);
        LOG.d("json=" + json);
        BaseRspData resp;
        try {
            resp = mGson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            LOG.warn("invalid json. err={}", e.toString());
            resp = null;
        }

        if (resp == null || resp.failed) {
            LOG.warn("load object failed");
        }
        return resp;
    }

    public String load(String url) {
        LOG.debug("load {}.", url);

        String result = null;

        InputStream in = getStreamFromUrl(url);
        if (in != null) {
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(in, writer);
                result = writer.toString();
            } catch (IOException e) {
                LOG.warn("load failed. err={}", e.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LOG.verbose("data: {}", result);

        return result;
    }

    public void setCookie(String cookie) {
        mCookie = cookie;
    }

    private InputStream getStreamFromUrl(String url) {
        InputStream in = null;

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
            httpGet.setHeader("Accept-Encoding", "gzip,deflate");
            if (!(TextUtils.isEmpty(mCookie))) {
                httpGet.setHeader("Cookie", mCookie);
            }
            HttpClient client = new DefaultHttpClient(params);

            HttpResponse resp = client.execute(httpGet);
            int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = resp.getEntity();
                if (null != entity) {
                    Header header = entity.getContentEncoding();
                    if (header != null && header.getValue().equals("gzip")) {
                        in = new GZIPInputStream(entity.getContent());
                    } else {
                        in = entity.getContent();
                    }
                }
            } else {
                LOG.warn("Request HTTP resource failed. StatusCode={} Url={}", statusCode, url);
            }
        } catch (IOException e) {
            LOG.warn("Request HTTP resource failed. err={}", e.toString());
        } catch (IllegalStateException e) {
            LOG.warn("Request HTTP resource failed. url={} err={}", url, e.toString());
        }

        return in;
    }

    /**
     * 处理jsonp
     */
    private String getJsonString(String result) {
        String json = result;

        if (!TextUtils.isEmpty(json)) {
            Pattern pattern = Pattern.compile("^\\s*callback\\s*\\((.*)\\s*\\)\\s*$");
            Matcher m = pattern.matcher(result);
            if (m.find() && m.groupCount() > 0) {
                json = m.group(1);
            }
            // 兼容处理一些小运营商IPS劫持，修改json数据的问题
            if (json.contains(JSON_FILTER_SIGN)) {
                json = json.replaceAll(JSON_FILTER_REGEX, "");
            }
        }

        return json;
    }
    private static final String JSON_FILTER_SIGN = "<script>_guanggao_pub";
    private static final String JSON_FILTER_REGEX = "<script>_guanggao_pub.*?<\\/script>";

    public JSONObject loadJsonObject(URLRequest request) throws JSONException {
        String json = getJsonString(load(request.toString()));
        JSONObject object = null;
        if(null != json) {
            object = new JSONObject(json);
        }
        return object;
    }

    public Topic<Movie> loadTopic(URLRequest request, Type type) {
        String json = getJsonString(load(request.toString()));
        Topic<Movie> resp = null;
        try {
            resp = mGson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            LOG.warn("invalid json. err={}", e.toString());
        }
        return resp != null ? resp : null;
    }

}
