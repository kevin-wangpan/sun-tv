package com.jiaoyang.tv.util;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class ScreenUtil {

    public static Bitmap takeScreenshot(View view) {
        Bitmap screenshot = null;
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(false);

        Bitmap cache = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        view.getWindowVisibleDisplayFrame(frame);
        int statusHeight = frame.top;// Activity 的顶端， 就是状态栏的高度。
        screenshot = Bitmap.createBitmap(cache, 0, statusHeight, cache.getWidth(), cache.getHeight() - statusHeight);
        view.destroyDrawingCache();

        return screenshot;
    }

    public static int getScreenWidth(Context context) {
        int width = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        return width;
    }

    public static int getScreenHeight(Context context) {
        int height = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;
        return height;
    }
    
    public static int getDeviceRealWidth(Activity context) {
        int realWidth = 0;
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            realWidth = dm.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realWidth;
    }
    
    public static int getDeviceRealHeight(Activity context) {
        int realHeight = 0;
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            realHeight = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realHeight;
    }
    
}
