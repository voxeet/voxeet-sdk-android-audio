package com.voxeet.audio.audio_stack;

import com.voxeet.audio.AudioRoute;
import com.voxeet.audio.AudioStackManager;
import com.voxeet.audio.VoxeetRunner;
import com.voxeet.audio.utils.AudioStackManagerUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestRoutes {

    private AudioStackManager audioStackManager;

    @Before
    public void init() {
        audioStackManager = AudioStackManagerUtils.create(VoxeetRunner.app);
    }

    @Test
    public void testAvailableRoutes() {
        List<AudioRoute> routes = audioStackManager.availableRoutes();

        int expected_size = 2;
        if (audioStackManager.isBluetoothHeadsetConnected()) expected_size++;

        Assert.assertEquals(expected_size, routes.size());
    }

    @Test
    public void testSpeaker() {
        List<AudioRoute> routes = audioStackManager.availableRoutes();
        for (AudioRoute route : routes) {
            if (AudioRoute.ROUTE_SPEAKER.equals(route)) return;
        }
        Assert.fail("ROUTE_SPEAKER not found but expected");
    }

    @Test
    public void testHeadsetOrPhone() {
        List<AudioRoute> routes = audioStackManager.availableRoutes();
        boolean isWiredHeadsetOn = audioStackManager.isWiredHeadsetOn();

        for (AudioRoute route : routes) {
            if (isWiredHeadsetOn && AudioRoute.ROUTE_HEADSET.equals(route)) return;
            else if (!isWiredHeadsetOn && AudioRoute.ROUTE_PHONE.equals(route)) return;
        }
        if (isWiredHeadsetOn) {
            Assert.fail("ROUTE_HEADSET not found but expected");
        } else {
            Assert.fail("ROUTE_PHONE not found but expected");
        }
    }

    @Test
    public void testHeadsetValid() {
        AudioStackManager audioStackManager = mock(AudioStackManager.class);
        when(audioStackManager.isWiredHeadsetOn()).thenReturn(true);
        when(audioStackManager.availableRoutes()).thenCallRealMethod();

        List<AudioRoute> routes = audioStackManager.availableRoutes();
        for (AudioRoute route : routes) {
            if (AudioRoute.ROUTE_HEADSET.equals(route)) return;
        }
        Assert.fail("ROUTE_HEADSET not found but expected");
    }

    @Test
    public void testHeadsetValidWithoutPhone() {
        AudioStackManager audioStackManager = mock(AudioStackManager.class);
        when(audioStackManager.isWiredHeadsetOn()).thenReturn(true);
        when(audioStackManager.availableRoutes()).thenCallRealMethod();

        List<AudioRoute> routes = audioStackManager.availableRoutes();
        for (AudioRoute route : routes) {
            if (AudioRoute.ROUTE_PHONE.equals(route)) {
                Assert.fail("ROUTE_PHONE found but not expected");
            }
        }
    }

    @Test
    public void testBluetoothhInRouteIfPresent() {
        AudioStackManager audioStackManager = mock(AudioStackManager.class);
        when(audioStackManager.isBluetoothHeadsetConnected()).thenReturn(true);
        when(audioStackManager.availableRoutes()).thenCallRealMethod();

        List<AudioRoute> routes = audioStackManager.availableRoutes();
        for (AudioRoute route : routes) {
            if (AudioRoute.ROUTE_BLUETOOTH.equals(route)) return;
        }
        Assert.fail("ROUTE_PHONE found but not expected");
    }

    @Test
    public void testBluetoothhInRouteIfNotPresent() {
        AudioStackManager audioStackManager = mock(AudioStackManager.class);
        when(audioStackManager.isBluetoothHeadsetConnected()).thenReturn(false);
        when(audioStackManager.availableRoutes()).thenCallRealMethod();

        List<AudioRoute> routes = audioStackManager.availableRoutes();
        for (AudioRoute route : routes) {
            if (AudioRoute.ROUTE_BLUETOOTH.equals(route)) {
                Assert.fail("ROUTE_PHONE found but not expected");
            }
        }
    }
}
