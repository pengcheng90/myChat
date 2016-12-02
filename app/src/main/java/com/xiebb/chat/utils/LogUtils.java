package com.xiebb.chat.utils;

import android.util.Log;

/**
 * Created by baobiao on 2016/12/2.
 */

public class LogUtils {
    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int LEVLE = 0;

    public static void v(String tag, String msg) {
        if (VERBOSE >= LEVLE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG >= LEVLE) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (INFO >= LEVLE) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (WARN >= LEVLE) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR >= LEVLE) {
            Log.e(tag, msg);
        }
    }

}
