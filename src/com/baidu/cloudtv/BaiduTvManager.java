package com.baidu.cloudtv;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class BaiduTvManager {

    
    public static String sendRequestPost(String url, String content) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost hp = new HttpPost(url);
        List params = new ArrayList();
        params.add(new BasicNameValuePair("content", content));
        hp.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

        HttpResponse response = client.execute(hp);
        int code = response.getStatusLine().getStatusCode();
        InputStream inStream = response.getEntity().getContent();// ���ص����

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return new String(data);
    }
}
