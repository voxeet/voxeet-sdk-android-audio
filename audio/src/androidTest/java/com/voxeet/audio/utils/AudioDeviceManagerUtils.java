package com.voxeet.audio.utils;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.voxeet.audio2.AudioDeviceManager;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.promise.Promise;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AudioDeviceManagerUtils {

    private static AudioDeviceManager manager;

    private AudioDeviceManagerUtils() {

    }

    public static AudioDeviceManager create(@NonNull final Application app, __Call<Promise<List<MediaDevice>>> update) {
        if (null != manager) return manager;

        final CountDownLatch latch = new CountDownLatch(1);
        final AudioDeviceManager[] manager = {null};

        new Handler(Looper.getMainLooper()).post(() -> {
            manager[0] = new AudioDeviceManager(app, update);
            latch.countDown();
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AudioDeviceManagerUtils.manager = manager[0];
        return manager[0];
    }
}
