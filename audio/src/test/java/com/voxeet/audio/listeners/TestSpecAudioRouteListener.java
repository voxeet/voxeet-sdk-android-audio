package com.voxeet.audio.listeners;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestSpecAudioRouteListener {

    @Test
    public void testImplementation() {
        final boolean[] called = {false};
        IAudioRouteListener instance = new IAudioRouteListener() {
            @Override
            public void onAudioRouteChanged() {
                called[0] = true;
            }
        };

        instance.onAudioRouteChanged();

        Assert.assertTrue(called[0]);
    }
}
