package com.voxeet.audio.enums;

import com.voxeet.audio.AudioRoute;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(BlockJUnit4ClassRunner.class)
public class SpecAudioRoute {

    @Test
    public void checkProximitySensorEnabled() {
        AudioRoute phone = AudioRoute.ROUTE_PHONE;

        Assert.assertTrue(phone.useProximitySensor());
    }

    @Test
    public void checkProximitySensorDisabled() {
        List<AudioRoute> routes = Arrays.asList(AudioRoute.ROUTE_BLUETOOTH,
                AudioRoute.ROUTE_HEADSET,
                AudioRoute.ROUTE_SPEAKER);

        for (AudioRoute route : routes) {
            Assert.assertFalse(route.useProximitySensor());
        }
    }


    @Test
    public void checkValueHeadset() {
        Assert.assertEquals(AudioRoute.valueOf(0), AudioRoute.ROUTE_HEADSET);
    }

    @Test
    public void checkValuePhone() {
        Assert.assertEquals(AudioRoute.valueOf(1), AudioRoute.ROUTE_PHONE);
    }

    @Test
    public void checkValueSpeaker() {
        Assert.assertEquals(AudioRoute.valueOf(2), AudioRoute.ROUTE_SPEAKER);
    }

    @Test
    public void checkValueBluetooth() {
        Assert.assertEquals(AudioRoute.valueOf(3), AudioRoute.ROUTE_BLUETOOTH);
    }

    @Test
    public void checkValueDefault() {
        int i = Integer.MIN_VALUE;
        while (i < 0) {
            if(!AudioRoute.ROUTE_SPEAKER.equals(AudioRoute.valueOf(i))) {
                Assert.fail("Excepted speaker but no...");
            }
            i++;
        }

        i = 4;
        while (i < Integer.MAX_VALUE) {
            if(!AudioRoute.ROUTE_SPEAKER.equals(AudioRoute.valueOf(i))) {
                Assert.fail("Excepted speaker but no...");
            }
            i++;
        }
    }


}
