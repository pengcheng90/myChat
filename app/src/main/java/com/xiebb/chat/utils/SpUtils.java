package com.xiebb.chat.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by baobiao on 2016/12/2.
 */

public class SpUtils {
    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_APPEND);
        sp.edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_APPEND);
        return sp.getString(key, "");
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_APPEND);
        sp.edit().putInt(key, value).commit();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_APPEND);
        return sp.getInt(key, 0);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_APPEND);
        sp.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_APPEND);
        return sp.getBoolean(key, false);
    }

    public static void reMove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_APPEND);
        sp.edit().remove(key).commit();
    }

}
