package com.jiaoyang.base.app;

import android.app.Application;

import com.jiaoyang.base.data.local.JiaoyangDatabaseHelper;
import com.jiaoyang.base.sys.AppConfigs;
import com.jiaoyang.base.sys.SystemConfigs;

public abstract class JiaoyangApplication extends Application {

    public static JiaoyangApplication sInstance;
    public static boolean sFirstTimeLaunche = false;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        sInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        fini();

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void init() {
        AppConfigs.initAppConfigs(getApplicationContext());
        SystemConfigs.initSystemConfigs(getApplicationContext());

        // Database
        JiaoyangDatabaseHelper.init(getApplicationContext());

        onInit();
    }

    public void fini() {
        onFini();

        JiaoyangDatabaseHelper.fini();
    }

    protected abstract void onInit();

    protected abstract void onFini();
}
