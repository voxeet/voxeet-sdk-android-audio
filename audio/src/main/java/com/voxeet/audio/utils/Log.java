package com.voxeet.audio.utils;

import androidx.annotation.NonNull;

public class Log {
    private static boolean enable = false;

    public static void enable(boolean enable) {
        Log.enable = enable;
    }

    public static void d(@NonNull String tag, @NonNull String line) {
        if (!enable) return;
        android.util.Log.d("AudioSDK", tag + " :: " + line);
    }

    public static void e(String tag, String s) {
        if (!enable) return;
        android.util.Log.e("AudioSDK", tag + " :: " + s);
    }

    public static void e(String tag, String s, Throwable e) {
        if (!enable) return;
        android.util.Log.e("AudioSDK", tag + " :: " + s, e);
    }
}
