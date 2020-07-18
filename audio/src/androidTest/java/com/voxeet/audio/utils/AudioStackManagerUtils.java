package com.voxeet.audio.utils;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AudioStackManagerUtils {

    private static AudioStackManager manager;

    private AudioStackManagerUtils() {

    }

    public static AudioStackManager create(@NonNull final Application app) {
        if (null != manager) return manager;

        final CountDownLatch latch = new CountDownLatch(1);
        final AudioStackManager[] manager = {null};

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                manager[0] = new AudioStackManager(app);
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AudioStackManagerUtils.manager = manager[0];
        return manager[0];
    }
}
