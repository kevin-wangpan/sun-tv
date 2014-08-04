package com.baidu.cloudtv;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

import com.baidu.cloudtv.signurl.SignURL;

public class BaiduTvManager2 {
    
    //测试使用，正式需要继承DataTaskListner
    public static void getRealUrl(Context pContext){
        System.out.println("签名： " + getSignature(pContext, "com.jiaoyang.video"));
        final String ret = SignURL.signURL(pContext, BaiduConstant.contentId_test, BaiduConstant.channelId);
        System.out.println("ret : " + ret);
        new Thread(){
            public void run() {
                //打开下方注释，发送网络请求，请求真实url
                try {
                    byte[] videoPlayUrl= sendRequestPost(ret);
                    
                    String r = new String (videoPlayUrl);
                    System.out.println("r : " + r);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    
    private static byte[] sendRequestPost(String content) throws Exception{
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost hp = new HttpPost(BaiduConstant.BAIDUTV_VIDEO_URL);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("content", content));
        hp.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        
        HttpResponse response = client.execute(hp);  
        int code = response.getStatusLine().getStatusCode(); 
        InputStream inStream = response.getEntity().getContent();// 返回的数据

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }
    
    
    public static String SHA1(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            return toHexString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
  
    public static String toHexString(byte[] keyData) {
        if (keyData == null) {
            return null;
        }
        int expectedStringLen = keyData.length * 2;
        StringBuilder sb = new StringBuilder(expectedStringLen);
        for (int i = 0; i < keyData.length; i++) {
            String hexStr = Integer.toString(keyData[i] & 0x00FF,16);
            if (hexStr.length() == 1) {
                hexStr = "0" + hexStr;
            }
            sb.append(hexStr);
        }
        return sb.toString();
    }

    public static String getSignature(Context pContext, String pkgname) {
        String sha1 = "";
        //String pkgname = this.getPackageName();
        try {
            PackageInfo packageInfo = pContext.getPackageManager().getPackageInfo(
                    pkgname, PackageManager.GET_SIGNATURES);
            Signature[] signatures = packageInfo.signatures;
            StringBuilder builder = new StringBuilder();
            for (Signature signature : signatures) {
                builder.append(signature.toCharsString());
            }
            String signature = builder.toString();
             sha1 = SHA1(signature);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return sha1;
    }
}
