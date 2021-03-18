package com.voxeet.audio.audio_stack;

import com.voxeet.audio.utils.TestWithAsyncRun;
import com.voxeet.audio2.AudioDeviceManager;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.DeviceType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestRoutes extends TestWithAsyncRun {

    @Before
    public void init() {
        internalInit();
    }

    @Test
    public void testAvailableRoutes() {
        final List<MediaDevice> routes = runWith(audioStackManager.enumerateDevices());

        int expected_size = 2;

        Assert.assertTrue(expected_size <= routes.size());
    }

    @Test
    public void testSpeaker() {
        final List<MediaDevice> routes = runWith(audioStackManager.enumerateDevices());
        for (MediaDevice route : routes) {
            if (DeviceType.EXTERNAL_SPEAKER.equals(route.deviceType())) return;
        }
        Assert.fail("EXTERNAL_SPEAKER not found but expected");
    }

    @Test
    public void testHeadsetOrPhone() {
        final List<MediaDevice> routes = runWith(audioStackManager.enumerateDevices());
        boolean isWiredHeadsetOn = audioStackManager.isWiredConnected();

        for (MediaDevice route : routes) {
            if (isWiredHeadsetOn && DeviceType.WIRED_HEADSET.equals(route.deviceType())) return;
        }
        if (isWiredHeadsetOn) {
            Assert.fail("ROUTE_HEADSET not found but expected");
        }
    }

    @Test
    public void testHeadsetValid() {
        AudioDeviceManager audioStackManager = mock(AudioDeviceManager.class);
        when(audioStackManager.isWiredConnected()).thenReturn(true);
        when(audioStackManager.enumerateDevices()).thenCallRealMethod();

        final List<MediaDevice> routes = runWith(audioStackManager.enumerateDevices());

        for (MediaDevice route : routes) {
            if (DeviceType.WIRED_HEADSET.equals(route.deviceType())) return;
        }
        Assert.fail("ROUTE_HEADSET not found but expected");
    }
}
