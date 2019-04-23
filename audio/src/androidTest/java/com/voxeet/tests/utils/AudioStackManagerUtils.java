package com.voxeet.tests.utils;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.voxeet.audio.AudioStackManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AudioStackManagerUtils {

    private AudioStackManagerUtils() {

    }

    public static AudioStackManager create(@NonNull final Application app) {
        CountDownLatch latch = new CountDownLatch(1);
        final AudioStackManager[] manager = {null};

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                manager[0] = new AudioStackManager(app);
            }
        });

        try {
            latch.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return manager[0];
    }
}
