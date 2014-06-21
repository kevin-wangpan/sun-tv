package com.jiaoyang.base.sys;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jiaoyang.tv.util.Logger;

public class NetworkHelper {

    private static final Logger LOG = Logger.getLogger(NetworkHelper.class);

    public static final int TIMEOUT_IN_MILL = 4000;
    public static final int GEY_DATA_TIMEOUT = 6000;

    public static boolean isWifiAvailable(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifiInfo.isConnectedOrConnecting();
    }

    public static Date getLastModified(String urlStr) {
        Date lastModified = null;
        HttpURLConnection urlConnection = null;
        try {
            final URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            long milliseconds = urlConnection.getLastModified();
            lastModified = new Date(milliseconds);
            urlConnection.disconnect();

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return lastModified;
    }

    /**
     * 发送字节流到服务器
     * 
     * @param serverUrl
     *            服务器地址
     * @param data
     *            要发送的字节流数据
     * @throws Exception
     */
    public static void upload(String serverUrl, byte[] data, Map<String, String> requestParams) throws Exception {
        try {
            URL url = new URL(serverUrl);
            // 打开连接
            HttpURLConnection conn;
            conn = (HttpURLConnection) url.openConnection();
            // 设置提交方式
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            // post方式不能使用缓存
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            // 设置连接超时时间
            conn.setConnectTimeout(6 * 1000);
            // 配置本次连接的Content-Type
            conn.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
            // 维持长连接

            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置浏览器编码
            conn.setRequestProperty("Charset", "UTF-8");
            // 应服务器方的要求......
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            // 将请求参数数据向服务器端发送

            dos.write(data);

            dos.flush();
            dos.close();
            if (conn.getResponseCode() == 200) {
                // 获得服务器端输出流
                conn.getInputStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            LOG.error(ex);
        }
        return null;
    }
    
    public static boolean checkNetworkConnection(String urlStr) {
        boolean isAvailable = false;
        try {
            URL url = new URL(urlStr);

            URLConnection conn = url.openConnection();
            if (conn.getReadTimeout() > 3000) {
                isAvailable = false;
            } else {
                isAvailable = true;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return isAvailable;
    }
    
}
