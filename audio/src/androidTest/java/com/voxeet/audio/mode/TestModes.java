package com.voxeet.audio.mode;

import com.voxeet.audio.utils.TestWithAsyncRun;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.DeviceType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestModes extends TestWithAsyncRun {

    @Before
    public void init() {
        internalInit();
    }

    @Test
    public void testSpeakerValid() {
        List<MediaDevice> devices = runWith(audioStackManager.enumerateDevices(DeviceType.EXTERNAL_SPEAKER));
        Assert.assertFalse(0 == devices.size());
    }

    @Test
    public void testNormalValid() {
        List<MediaDevice> devices = runWith(audioStackManager.enumerateDevices(DeviceType.NORMAL_MEDIA));
        Assert.assertFalse(0 == devices.size());
    }

    @Test
    public void testWiredValid() {
        List<MediaDevice> devices = runWith(audioStackManager.enumerateDevices(DeviceType.EXTERNAL_SPEAKER));
        boolean connected = audioStackManager.isWiredConnected();
        if (connected) {
            Assert.assertTrue(1 == devices.size());
        } else {
            Assert.assertFalse(0 == devices.size());
        }
    }
}
