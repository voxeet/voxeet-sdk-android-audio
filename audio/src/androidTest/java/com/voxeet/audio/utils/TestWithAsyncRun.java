package com.voxeet.audio.utils;

import android.os.Handler;
import android.os.Looper;

import com.voxeet.audio.VoxeetRunner;
import com.voxeet.audio2.AudioDeviceManager;
import com.voxeet.promise.Promise;

import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestWithAsyncRun {

    protected AudioDeviceManager audioStackManager;

    protected void internalInit() {
        audioStackManager = AudioDeviceManagerUtils.create(VoxeetRunner.app, update -> {

        });
    }

    protected <T> T runWith(Promise<T> promise) {
        return runWith(promise, 0);
    }

    protected <T> T runWith(Promise<T> promise, long after) {
        final CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] localError = {null};

        try {
            if (after > 0) Thread.sleep(after);
        } catch (Exception e) {

        }
        AtomicReference<T> result = new AtomicReference<>();

        new Handler(Looper.getMainLooper()).post(() -> {
            promise.then(t -> {
                result.set(t);
                latch.countDown();
            }).error(error -> {
                error.printStackTrace();
                localError[0] = error;
                latch.countDown();
            });
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (null != localError[0]) {
            localError[0].printStackTrace();
            Assert.fail(localError[0].getMessage());
        }

        return result.get();
    }
}
