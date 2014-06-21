package com.jiaoyang.base.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Timer;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.FloatMath;

/**
 * 通用方法
 * 
 * @author SPM
 * 
 */
public class CommonUtils {
	/**
	 * 内容格式化的通用方法
	 * 
	 * @author SPM
	 * 
	 */
	public static class Format {
		/**
		 * 通过dip获取相应的px值
		 * 
		 * @param context
		 * @param dip
		 * @return
		 */
		public static int formatDipToPx(Context context, int dip) {
			DisplayMetrics dm = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay()
					.getMetrics(dm);
			return (int) FloatMath.ceil(dip * dm.density);
		}

		/**
		 * 获取格式化播放时长字符串[HH:mm:ss]
		 * 
		 * @param mCurSec
		 *            时长(单位：秒)
		 * @return HH:mm:ss
		 */
		public static String getPlayTimeFormat(int mCurSec) {
			if (mCurSec <= 0) {
				return "00:00:00";
			}
			StringBuffer formattedTime = new StringBuffer();
			int hour = mCurSec / 60 / 60;
			int leftSec = mCurSec % (60 * 60);
			int min = leftSec / 60;
			int sec = leftSec % 60;

			formattedTime.append(hour < 10 ? "0" : "").append(hour).append(":");
			formattedTime.append(min < 10 ? "0" : "").append(min).append(":");
			formattedTime.append(sec < 10 ? "0" : "").append(sec);
			return formattedTime.toString();
		}
	}

	/**
	 * 线程相关的通用方法
	 * 
	 * @author SPM
	 * 
	 */
	public static class TreadTask {
		/**
		 * 停止并释放一个timer
		 * 
		 * @param timer
		 */
		public static void stopTimer(Timer timer) {
			if (timer != null) {
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}
	}

	/**
	 * <p>
	 * 通过反射实现toString方法
	 * <p>
	 * 
	 * @param obj
	 * @return
	 */
	public static final <T> String toStringByReflection(T obj) {
		String toString = "";
		StringBuilder sb = new StringBuilder();
		Class<?> clazz = obj.getClass();
		Field[] fields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		sb.append(clazz.getName()).append("@")
				.append(Integer.toHexString(obj.hashCode())).append("[")
				.append("\n\r");
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Object value = null;
			try {
				value = field.get(obj);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			sb.append(field.getName()).append("=").append(value).append(",")
					.append("\n\r");
		}
		int lastIndex = sb.length() - 3;
		toString = sb.substring(0, lastIndex);
		toString = toString.concat("]");
		return toString;
	}
}
