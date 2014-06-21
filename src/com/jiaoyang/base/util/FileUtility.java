package com.jiaoyang.base.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import com.jiaoyang.tv.util.Logger;

public class FileUtility {
    private static final Logger LOG = Logger.getLogger(FileUtility.class);

    public static synchronized boolean ensureDir(String path) {
        if (null == path) {
            return false;
        }

        boolean ret = false;

        File file = new File(path);
        if (!file.exists()) {
            try {
                ret = file.mkdirs();
            } catch (SecurityException e) {
                LOG.warn(e);
            }
        } else {
            ret = true;
        }

        return ret;
    }

    public static boolean isFileExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    public static void createDirectoryIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
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

    public static long getExistingFilesSize(String path) {
        long size = 0;
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] fileList = file.listFiles();
                if (fileList != null) {
                    for (int i = 0; i < fileList.length; i++) {
                        size += getExistingFilesSize(fileList[i].getPath());
                    }
                }
            } else {
                size = file.length();
            }
        }

        return size;
        // StatFs stat = new StatFs(path);
        // long blockSize = stat.
        // long availableBlocks = stat.getAvailableBlocks();
        // ret = (availableBlocks * blockSize) / 1024 / 1024;
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

    public static Bitmap creatBitmap(String path) {
        if (null == path) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        return BitmapFactory.decodeFile(path);
    }

    public static void saveBitmap(Bitmap bitmap, String path, String name) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        File file = new File(path + name);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();

            FileOutputStream out = null;
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmap(Bitmap bitmap, String path, String name, Bitmap.CompressFormat format) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        if (!bitmap.isRecycled()) {
            File file = new File(path + name);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();

                FileOutputStream out = null;
                out = new FileOutputStream(file);
                bitmap.compress(format, 100, out);
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFileAttrStrings(File file) {

        String result = "";

        Runtime runtime = Runtime.getRuntime();
        String command = "ls -l \"" + file.getAbsolutePath() + "\"";
        try {
            Process p = runtime.exec(command);

            Scanner scanner = new Scanner(p.getInputStream());
            result = scanner.nextLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 复制单个文件
     * 
     * @param from
     *            String 原文件路径 如：c:/fqf.txt
     * @param to
     *            String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    @SuppressLint("WorldReadableFiles")
    public static void copyFile(Context context, String from, String to) {

        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(from);
            if (oldfile.exists()) {
                inStream = new FileInputStream(from); // 读入原文件
                fs = context.openFileOutput(to, Context.MODE_WORLD_READABLE);
                byte[] buffer = new byte[1444];
                // int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    // System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
            }
        } catch (IOException e) {
            LOG.warn(e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从Raw文件ID复制到指定路径
     * 
     * @param context
     * @param resourceID
     * @param to
     */
    @SuppressLint("WorldReadableFiles")
    public static void copyFile(Context context, int resourceID, String to) {
        InputStream in = null;
        OutputStream out = null;
        try {
            byte[] buffer = new byte[4096];
            in = context.getResources().openRawResource(resourceID);
            out = new FileOutputStream(to, false);

            int n = in.read(buffer, 0, buffer.length);
            while (n >= 0) {
                out.write(buffer, 0, n);
                n = in.read(buffer, 0, buffer.length);
            }
            out.flush();
        } catch (NotFoundException e) {
            LOG.error("NotFoundException : " + e.getMessage());
        } catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            LOG.error("IOException : " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("IOException : " + e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.error("IOException : " + e.getMessage());
                }
            }
        }
    }

    /**
     * 复制整个文件夹内容
     * 
     * @param from
     *            String 原文件路径 如：c:/fqf
     * @param to
     *            String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public void copyFolder(String from, String to) {
        InputStream input = null;
        FileOutputStream output = null;
        try {
            (new File(to)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
            File a = new File(from);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (from.endsWith(File.separator)) {
                    temp = new File(from + file[i]);
                } else {
                    temp = new File(from + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    input = new FileInputStream(temp);
                    output = new FileOutputStream(to + "/" + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyFolder(from + "/" + file[i], to + "/" + file[i]);
                }
            }
        } catch (IOException e) {
            LOG.warn(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
