package com.jiaoyang.base.sys;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;

public class FileSystem {

    public static boolean ensureDir(String path) {
        File file = new File(path);

        return file.mkdirs() || file.isDirectory();
    }

    public static boolean isFileExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    public static boolean ensureFile(String path) {
        if (null == path) {
            return false;
        }

        boolean ret = false;

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static boolean deleteFile(String path) {
        if (path == null)
            return false;
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        return file.delete();

    }

    public static String getSDCardDir() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static boolean isSDCardExist() {
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState());
    }

    // 获取sdcard剩余空间大小单位是MB
    public static long getSdCardAvailaleSize() {
        long ret = 0;
        String strSDCardPath = getSDCardDir();
        if (null != strSDCardPath) {
            StatFs stat = new StatFs(strSDCardPath);
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            ret = (availableBlocks * blockSize) / 1024 / 1024;
        }
        return ret;
    }

    public static long getAvailableExternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getTotalExternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        Environment.getExternalStorageState();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }
}
