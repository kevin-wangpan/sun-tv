package com.jiaoyang.tv.app;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.jiaoyang.base.data.local.CrashInfo;
import com.jiaoyang.base.data.local.CrashInfoDao;
import com.jiaoyang.base.sys.AppConfigs;
import com.jiaoyang.tv.util.Logger;
import com.jiaoyang.video.tv.BuildConfig;

public class CrashHandler implements UncaughtExceptionHandler {
    private static final Logger LOG = Logger.getLogger(CrashHandler.class);

    public static final String ERROR_REPORT_URL = "http://abugreport.sandai.net/cgi-bin/error_report";

    private final Application mApplication;
    private final Context mContext;
    private Handler mUIHandler;
    private Thread mUiThread;

    public CrashHandler(Application app) {
        mApplication = app;
        mContext = app.getApplicationContext();
        mUIHandler = new Handler();
        mUiThread = Thread.currentThread();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LOG.error(ex);

        // path为绝对路径
        saveCrashInfo2File(ex);

        CrashInfoDao crashInfoDao = new CrashInfoDao(mContext);
        CrashInfo crashInfo = new CrashInfo();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (ex != null) {
            ex.printStackTrace(pw);
            if(BuildConfig.DEBUG){
                ex.printStackTrace();
            }
        }
        crashInfo.log = sw.toString();
        crashInfo.time = AppConfigs.DEFAULT_DATE_FORMATTER.format(new Date());
        crashInfo.version = AppConfigs.appVersion;
        if (crashInfoDao.insert(crashInfo) == -1) {
            LOG.warn("failed to save crash info into database.");
        }
        LOG.error("handled uncaught exception,  process will be killed soon.");

        if (Thread.currentThread() != mUiThread) {
            mUIHandler.post(new Runnable() {

                @Override
                public void run() {
                    mApplication.onTerminate();
                }
            });
        } else {
            mApplication.onTerminate();
        }
    }

    private void saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String fileName = "crash.log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/TDDOWNLOAD/crash/";
                (new File(path)).mkdirs();
                FileOutputStream fos = new FileOutputStream(path + fileName + ".zip");
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ZipOutputStream zos = new ZipOutputStream(bos);// 压缩包
                ZipEntry ze = new ZipEntry(fileName);// 这是压缩包名里的文件名
                zos.putNextEntry(ze);// 写入新的 ZIP 文件条目并将流定位到条目数据的开始处
                byte[] b = sb.toString().getBytes();
                if (b != null && b.length > 0) {
                    zos.write(b, 0, b.length);
                }
                bos.flush();
                zos.flush();
                zos.close();
                bos.close();
                fos.close();
            }
        } catch (Exception e) {
            LOG.warn(e);
        }
    }

}
