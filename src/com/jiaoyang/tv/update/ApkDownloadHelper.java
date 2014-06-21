package com.jiaoyang.tv.update;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.jiaoyang.tv.util.Logger;

/**
 * apk下载helper
 * 
 * 
 */
public class ApkDownloadHelper {
    private static final Logger LOG = Logger.getLogger(ApkDownloadHelper.class);
    private static final int TIMEOUT_IN_MILL = 4000;

    /**
     * 获取要下载的文件长度
     * 
     * @param url
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static long getLength(String url) throws ClientProtocolException, IOException {
        long len = 0;
        HttpEntity entity = getEntity(url);
        if (entity != null) {
            len = entity.getContentLength();
        }
        return len;
    }

    public static InputStream getStreamFromUrl(String url) throws ClientProtocolException, IOException {
        InputStream is = null;
        HttpEntity entity = getEntity(url);
        if (entity != null) {
            is = entity.getContent();
        }
        return is;
    }

    private static HttpEntity getEntity(String url) throws ClientProtocolException, IOException {
        if (url == null) {
            return null;
        }
        String errorMessage = "";
        HttpEntity entity = null;

        HttpGet httpGet = new HttpGet(url);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                TIMEOUT_IN_MILL);
        HttpClient client = new DefaultHttpClient(httpParameters);
        HttpResponse resp = null;
        resp = client.execute(httpGet);
        int statusCode = resp.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK == statusCode) {
            entity = resp.getEntity();
        } else {
            errorMessage = "Request HTTP resource failed. Response Code = "
                    + statusCode;
            LOG.warn(errorMessage);
        }

        return entity;
    }
    

}
