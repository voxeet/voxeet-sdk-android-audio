package com.voxeet.audio.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

    private ThreadUtils() {

    }

    public static void waitFor(final int milliseconds) {
        final CountDownLatch latch = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(milliseconds);
                } catch (Exception e) {

                }
                latch.countDown();
            }
        }).run();

        try {
            latch.await(milliseconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
