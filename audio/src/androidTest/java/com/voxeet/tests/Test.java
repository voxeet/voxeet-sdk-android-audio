package com.voxeet.tests;

import android.app.Application;
import android.util.Log;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(BlockJUnit4ClassRunner.class)
public class Test {
    @Before
    public void before() {

    }


    @org.junit.Test
    public void test() {
        final CountDownLatch latch = new CountDownLatch(1);

        Application app = VoxeetRunner.app;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                }catch (Exception e){

                }
                latch.countDown();
            }
        });
        thread.start();

        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    
}
