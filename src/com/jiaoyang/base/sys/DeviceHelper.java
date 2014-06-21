package com.jiaoyang.base.sys;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;

import com.jiaoyang.tv.util.Logger;

/**
 * 
 * 设备帮助类</br>
 * 在获取屏幕宽、高和密度的时候尽量用带Activity参数的方法，该方法安全性和准确性更高</br>
 * 以后在Activity里获取屏幕信息不需要预先调用loadScreenInfo方法，直接调用对应带Activity参数的方法即可：</br>
 * 如：</br>
 * 获取屏幕高度，调用getScreenHeight(Activity activity)即可</br>
 * 在非Activity里面获取屏幕信息，必须先调用loadScreenInfo方法</br>
 */
public class DeviceHelper {
    private static final Logger LOG = Logger.getLogger(DeviceHelper.class);

    private static DisplayMetrics sDisplayMetrics = null;
    
    /**
     * 
     * 加载屏幕参数信息
     * @Title: loadScreenInfo
     * @param activity
     * @return
     * @return boolean 加载成功返回true，加载失败返回false
     * @date 2014-4-28 下午1:56:32
     */
    public static boolean loadScreenInfo(Activity activity) {
        if (sDisplayMetrics == null) {
            sDisplayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(sDisplayMetrics);

            LOG.info("ScreenInfo [width={} height={} density={} densityDpi={} xdpi={} ydpi={} scaledDensity={}]",
                    sDisplayMetrics.widthPixels,
                    sDisplayMetrics.heightPixels,
                    sDisplayMetrics.density,
                    sDisplayMetrics.densityDpi,
                    sDisplayMetrics.xdpi,
                    sDisplayMetrics.ydpi,
                    sDisplayMetrics.scaledDensity);
        }
        
        return sDisplayMetrics != null;
    }
    
    private static boolean isDisplayMetricsNotNull() {
        return sDisplayMetrics != null;
    }
    
    public static int getScreenWidth() {
        if(isDisplayMetricsNotNull()) return sDisplayMetrics.widthPixels;
        return 0;
    }

    public static int getScreenWidth(Activity activity) {
        if(loadScreenInfo(activity)) return sDisplayMetrics.widthPixels;
        return 0;
    }

    public static int getScreenHeight() {
        if(isDisplayMetricsNotNull()) return sDisplayMetrics.heightPixels;
        return 0;
    }
    
    public static int getScreenHeight(Activity activity) {
        if(loadScreenInfo(activity)) return sDisplayMetrics.heightPixels;
        return 0;
    }
    
    public static float getDensity() {
        if(isDisplayMetricsNotNull()) return sDisplayMetrics.density;
        return 0.0f;
    }

    public static float getDensity(Activity activity) {
        if(loadScreenInfo(activity)) return sDisplayMetrics.density;
        return 0.0f;
    }

    public static boolean isScreenLocked(Context context) {
        return ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
    }
    
    public static DisplayMetrics getDisplayMetrics() {
        return sDisplayMetrics;
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        loadScreenInfo(activity);
        return sDisplayMetrics;
    }

    public static boolean isWifiOk(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        return wm != null && wm.isWifiEnabled();
    }

    public static String getCpuInfo() {
        String cpuInfo = null;

        String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
        cpuInfo = shellExec(args);
        if (cpuInfo != null) {
            cpuInfo = cpuInfo.replaceAll(" ", "");
            cpuInfo = cpuInfo.replaceAll("\t", "");
            cpuInfo = cpuInfo.replaceAll("\n", " ");
        }

        return cpuInfo;
    }

    private static String shellExec(String[] args) {
        String result = null;

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            process = processBuilder.start();
            baos = new ByteArrayOutputStream();
            is = process.getInputStream();
            int read = -1;
            while (-1 != (read = is.read())) {
                baos.write(read);
            }
            result = new String(baos.toByteArray());
        } catch (Exception e) {
            LOG.warn("shell exec failed. err={}", e.getMessage());

            result = null;
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                LOG.warn("shell exec close failed. err={}", e.getMessage());
            }
        }

        return result;
    }

    // 获取peerid
    public static String getPeerid(Context context) {
        String peerId = null;

        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wm != null && wm.getConnectionInfo() != null) {
            String mac = wm.getConnectionInfo().getMacAddress();
            mac += "004V";
            peerId = mac.replaceAll(":", "");
            peerId = peerId.replaceAll(",", "");
            peerId = peerId.replaceAll("[.]", "");
            peerId = peerId.toUpperCase(Locale.US);
        }

        return peerId;
    }
}
