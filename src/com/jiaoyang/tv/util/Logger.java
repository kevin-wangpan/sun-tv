package com.jiaoyang.tv.util;

import android.util.Log;

import com.jiaoyang.tv.app.JiaoyangTvApplication;
import com.jiaoyang.video.tv.BuildConfig;

public final class Logger {

    private String tag = "JiaoYang";
    private final boolean needLog;

    public Logger() {
        needLog = BuildConfig.DEBUG ||
                JiaoyangTvApplication.sBuildVersion != JiaoyangTvApplication.BUILD_VERSION.FORMAL;
    }

    public static Logger getLogger(String tag) {
        Logger l = new Logger();
        l.tag = tag;
        return l;
    }

    public static Logger getLogger(Class classTag) {
        return getLogger(classTag.getName());
    }

    public void debug(String format, Object... arguments) {
        if (needLog) {
            d(tag, String.format(format.replaceAll("\\{\\}", "%s"), arguments));
        }
    }

    public void debug(String msg, Throwable tr) {
        d(tag, msg, tr);
    }

    public void debug(String msg) {
        d(tag, msg);
    }

    public void debug(Throwable tr) {
        d(tag, "", tr);
    }

    public void d(String msg) {
        d(tag, msg);
    }

    public void d(String tag, String msg) {
        if (needLog) {
            Log.d(tag, msg);
        }
    }

    public void d(String tag, String msg, Throwable ex) {
        if (needLog) {
            Log.d(tag, msg, ex);
        }
    }

    public void error(String format, Object... arguments) {
        e(tag, String.format(format.replaceAll("\\{\\}", "%s"), arguments));
    }

    public void error(String msg, Throwable tr) {
        e(tag, msg, tr);
    }

    public void error(String msg) {
        e(tag, msg);
    }

    public void error(Throwable tr) {
        e(tag, "", tr);
    }

    public void e(String msg) {
        e(tag, msg);
    }

    public void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public void e(String tag, String msg, Throwable ex) {
        Log.e(tag, msg, ex);
    }

    public void info(String format, Object... arguments) {
        if (needLog) {
            i(tag, String.format(format.replaceAll("\\{\\}", "%s"), arguments));
        }
    }

    public void info(String msg, Throwable tr) {
        i(tag, msg, tr);
    }

    public void info(String msg) {
        i(tag, msg);
    }

    public void i(String msg) {
        i(tag, msg);
    }

    public void i(String tag, String msg) {
        if (needLog) {
            Log.i(tag, msg);
        }
    }

    public void i(String tag, String msg, Throwable ex) {
        if (needLog) {
            Log.i(tag, msg, ex);
        }
    }

    public void verbose(String format, Object... arguments) {
        if (needLog) {
            v(tag, String.format(format.replaceAll("\\{\\}", "%s"), arguments));
        }
    }

    public void verbose(String msg, Throwable tr) {
        v(tag, msg, tr);
    }

    public void verbose(String msg) {
        v(tag, msg);
    }

    public void verbose(Throwable tr) {
        v(tag, "", tr);
    }

    public void v(String msg) {
        v(tag, msg);
    }

    public void v(String tag, String msg) {
        if (needLog) {
            Log.v(tag, msg);
        }
    }

    public void v(String tag, String msg, Throwable ex) {
        if (needLog) {
            Log.v(tag, msg, ex);
        }
    }

    public void warn(String format, Object... arguments) {
        if (needLog) {
            w(tag, String.format(format.replaceAll("\\{\\}", "%s"), arguments));
        }
    }

    public void warn(String msg, Throwable tr) {
        w(tag, msg, tr);
    }

    public void warn(String msg) {
        w(tag, msg);
    }

    public void warn(Throwable tr) {
        w(tag, "", tr);
    }

    public void w(String msg) {
        w(tag, msg);
    }

    public void w(String tag, String msg) {
        if (needLog) {
            Log.w(tag, msg);
        }
    }

    public void w(String tag, String msg, Throwable ex) {
        if (needLog) {
            Log.w(tag, msg, ex);
        }
    }
    
    public static final String TAG_LUKE = "luke";
    
}
