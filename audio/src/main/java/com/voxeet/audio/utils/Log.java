package com.voxeet.audio.utils;

import android.support.annotation.NonNull;

public class Log {
    public static void d(@NonNull String tag, @NonNull String line) {
        android.util.Log.d("AudioSDK", tag+" :: " + line);
    }

    public static void e(String tag, String s) {
        android.util.Log.e("AudioSDK",tag+" :: " + s);
    }
}
