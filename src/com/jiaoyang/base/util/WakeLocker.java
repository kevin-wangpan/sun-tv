package com.jiaoyang.base.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.jiaoyang.tv.util.Logger;

/**
 * 保持屏幕高亮
 * @author SPM
 *
 */
public class WakeLocker {
    private static final Logger LOG = Logger.getLogger(WakeLocker.class);
    private static WakeLock mWakeLock;
    
    public static void acquire(Context context){
        if (mWakeLock == null) {  
            PowerManager powerManager = (PowerManager)(context.getSystemService(Context.POWER_SERVICE));  
            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, context.getPackageName());  
        }
        mWakeLock.acquire();
        LOG.info("WakeLocker.acquire()");
    }
    
    public static void release() {  
        if(mWakeLock != null && mWakeLock.isHeld()){  
            mWakeLock.release();  
            LOG.info("WakeLocker.release()");
        }  
    }  
}
