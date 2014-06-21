package com.jiaoyang.base.sys;

import android.content.Context;
import android.os.Build;

import com.jiaoyang.base.util.StringEx;

public class SystemConfigs {
    public static String deviceName = Build.PRODUCT;
    public static String deviceModel = Build.MODEL;
    public static String deviceManufacturer = Build.MANUFACTURER;
    public static String osVersion = Build.VERSION.RELEASE;
    public static int osVersion_INT = Build.VERSION.SDK_INT;
    public static String cpuInfo = DeviceHelper.getCpuInfo();
    public static String peerId = StringEx.Empty;

    public static int ScreenWidthPixels;
    public static int ScreenHeightPixels;

    public static String getSystemConfigs() {
        StringBuilder sb = new StringBuilder();
        sb.append("CPU_ABI:" + Build.CPU_ABI);
        sb.append("CPU_ABI2:" + Build.CPU_ABI2);
        sb.append("DEVICE:" + Build.DEVICE);
        sb.append("DISPLAY:" + Build.DISPLAY);
        sb.append("HARDWARE:" + Build.HARDWARE);
        sb.append("ID:" + Build.ID);
        sb.append("MANUFACTURER:" + Build.MANUFACTURER);
        sb.append("MODEL:" + Build.MODEL);
        sb.append("PRODUCT:" + Build.PRODUCT);
        sb.append("RADIO:" + Build.RADIO);
        sb.append("TYPE:" + Build.TYPE);
        sb.append("OS.VERSION:" + Build.VERSION.RELEASE);

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CPU_ABI:" + Build.CPU_ABI);
        sb.append("CPU_ABI2:" + Build.CPU_ABI2);
        sb.append("DEVICE:" + Build.DEVICE);
        sb.append("DISPLAY:" + Build.DISPLAY);
        sb.append("HARDWARE:" + Build.HARDWARE);
        sb.append("ID:" + Build.ID);
        sb.append("MANUFACTURER:" + Build.MANUFACTURER);
        sb.append("MODEL:" + Build.MODEL);
        sb.append("PRODUCT:" + Build.PRODUCT);
        sb.append("RADIO:" + Build.RADIO);
        sb.append("TYPE:" + Build.TYPE);
        sb.append("OS.VERSION:" + Build.VERSION.RELEASE);

        return sb.toString();
    }

    public static void initSystemConfigs(Context context) {
        peerId = DeviceHelper.getPeerid(context);
        if (osVersion_INT == 0) {
            try {
                osVersion_INT = Integer.valueOf(Build.VERSION.SDK).intValue();
            } catch (NumberFormatException e) {
            }
        }
    }
}
