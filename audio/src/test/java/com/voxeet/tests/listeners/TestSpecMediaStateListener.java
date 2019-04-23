package com.voxeet.tests.listeners;

import com.voxeet.audio.listeners.IAudioRouteListener;
import com.voxeet.audio.listeners.IMediaStateListener;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestSpecMediaStateListener {

    @Test
    public void testImplementation() {
        final boolean[] called = {false, false, false};
        IMediaStateListener instance = new IMediaStateListener() {
            @Override
            public void onSpeakerChanged(boolean isEnabled) {
                called[0] = isEnabled;
            }

            @Override
            public void onHeadsetStateChange(boolean isPlugged) {
                called[1] = isPlugged;
            }

            @Override
            public void onBluetoothHeadsetStateChange(boolean isPlugged) {
                called[2] = isPlugged;
            }
        };

        instance.onSpeakerChanged(true);
        Assert.assertTrue(called[0]);
        Assert.assertFalse(called[1]);
        Assert.assertFalse(called[2]);

        instance.onHeadsetStateChange(true);
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
        Assert.assertFalse(called[2]);

        instance.onBluetoothHeadsetStateChange(true);
        Assert.assertTrue(called[0]);
        Assert.assertTrue(called[1]);
        Assert.assertTrue(called[2]);

        //TODO fuzz the cross possibilities
    }
}
