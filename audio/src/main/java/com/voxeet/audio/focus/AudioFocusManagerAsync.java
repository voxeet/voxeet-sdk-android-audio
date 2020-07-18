package com.voxeet.audio.focus;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.voxeet.audio.utils.Log;
import com.voxeet.promise.Promise;

public class AudioFocusManagerAsync {

    private static HandlerThread FOCUS_MANAGER = new HandlerThread("FOCUS_MANAGER");
    private static Handler handler;

    private AudioFocusManagerAsync() {

    }

    public static void start() {
        if (null == handler) {
            FOCUS_MANAGER.start();
            handler = new Handler(Looper.getMainLooper());//FOCUS_MANAGER.getLooper());
        }
    }

    public static Promise<Boolean> setMode(@NonNull AudioManager manager, int mode, @NonNull String tag) {
        return new Promise<>(solver -> {
            Log.d(tag, "mode starting");
            post(() -> manager.setMode(mode), () -> {
                Log.d(tag, "mode set");
                solver.resolve(true);
            });
        });
    }

    public static boolean post(@NonNull Runnable runnable, @NonNull Runnable finished) {
        Log.d(AudioFocusManagerAsync.class.getSimpleName(), "posting -->");
        if (null != handler) {
            handler.post(() -> {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                    //TODO report error
                }

                finished.run();
                Log.d(AudioFocusManagerAsync.class.getSimpleName(), "done -->");
            });
            return true;
        } else {
            runnable.run();
            finished.run();
            Log.d(AudioFocusManagerAsync.class.getSimpleName(), "done -->");
            return false;
        }
    }

}
