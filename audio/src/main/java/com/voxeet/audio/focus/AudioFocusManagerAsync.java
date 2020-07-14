package com.voxeet.audio.focus;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import com.voxeet.audio.utils.Log;

public class AudioFocusManagerAsync {

    private static HandlerThread FOCUS_MANAGER = new HandlerThread("FOCUS_MANAGER");
    private static Handler handler;

    private AudioFocusManagerAsync() {

    }

    public static void start() {
        if (null == handler) {
            FOCUS_MANAGER.start();
            handler = new Handler(FOCUS_MANAGER.getLooper());
        }
    }

    public static void setMode(@NonNull AudioManager manager, int mode, @NonNull String tag) {
        Log.d(tag, "mode starting");
        post(() -> manager.setMode(mode), () -> {
            Log.d(tag, "mode set");
        });
    }

    public static boolean post(@NonNull Runnable runnable, @NonNull Runnable finished) {
        if (null != handler) {
            handler.post(() -> {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    //TODO report error
                }

                finished.run();
            });
            return true;
        } else {
            runnable.run();
            finished.run();
            return false;
        }
    }

}
