package com.voxeet.audio.enums;

import com.voxeet.audio.MediaDevice;

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
        MediaDevice phone = MediaDevice.ROUTE_PHONE;

        Assert.assertTrue(phone.useProximitySensor());
    }

    @Test
    public void checkProximitySensorDisabled() {
        List<MediaDevice> routes = Arrays.asList(MediaDevice.ROUTE_BLUETOOTH,
                MediaDevice.ROUTE_HEADSET,
                MediaDevice.ROUTE_SPEAKER);

        for (MediaDevice route : routes) {
            Assert.assertFalse(route.useProximitySensor());
        }
    }


    @Test
    public void checkValueHeadset() {
        Assert.assertEquals(MediaDevice.valueOf(0), MediaDevice.ROUTE_HEADSET);
    }

    @Test
    public void checkValuePhone() {
        Assert.assertEquals(MediaDevice.valueOf(1), MediaDevice.ROUTE_PHONE);
    }

    @Test
    public void checkValueSpeaker() {
        Assert.assertEquals(MediaDevice.valueOf(2), MediaDevice.ROUTE_SPEAKER);
    }

    @Test
    public void checkValueBluetooth() {
        Assert.assertEquals(MediaDevice.valueOf(3), MediaDevice.ROUTE_BLUETOOTH);
    }

    @Test
    public void checkValueMedia() {
        Assert.assertEquals(MediaDevice.valueOf(4), MediaDevice.ROUTE_MEDIA);
    }

    @Test
    public void checkValueDefault() {
        int i = Integer.MIN_VALUE;
        while (i < 0) {
            if(!MediaDevice.ROUTE_SPEAKER.equals(MediaDevice.valueOf(i))) {
                Assert.fail("Excepted speaker but no...");
            }
            i++;
        }

        i = 5;
        while (i < Integer.MAX_VALUE) {
            if(!MediaDevice.ROUTE_SPEAKER.equals(MediaDevice.valueOf(i))) {
                Assert.fail("Excepted speaker but no...");
            }
            i++;
        }
    }


}
