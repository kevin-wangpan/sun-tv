package com.jiaoyang.base.engine;

import android.os.Handler;

public class JiaoyangTaskInfo {

    public static final int WAITING = 0;
    public static final int RUNNING = WAITING + 1;
    public static final int PAUSED = RUNNING + 1;
    public static final int SUCCESS = PAUSED + 1;
    public static final int FAILED = SUCCESS + 1;
    public static final int DELETED = FAILED + 1;

    public static final int READY = DELETED + 1;
    public static final int RELEASE = READY + 1;

    public static final int UPDATE_SPEED = RELEASE + 1;

    public static final int PLAYNEXT = UPDATE_SPEED + 1;

    public String mUrl;
    public int mTaskId;
    public Handler mHandler;
}
