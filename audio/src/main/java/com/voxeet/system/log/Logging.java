package com.voxeet.system.log;

import android.util.Log;

public class Logging {

    private static boolean enable = false;

    public void set(boolean enable) {
        Logging.enable = enable;
    }

    public static void d(String tag, String s) {
        Log.d(tag, s);
    }
}
