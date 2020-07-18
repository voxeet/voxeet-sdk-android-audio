package com.voxeet.audio.mode;

import com.voxeet.audio.VoxeetRunner;
import com.voxeet.audio.utils.AudioStackManagerUtils;
import com.voxeet.audio.utils.CheckFields;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestModes {

    private AudioStackManager audioStackManager;

    @Before
    public void init() {
        audioStackManager = AudioStackManagerUtils.create(VoxeetRunner.app);
    }

    @Test
    public void testSpeakerValid() {
        try {
            SpeakerMode mode = CheckFields.getField(audioStackManager, "speakerMode");
            Assert.assertNotNull(mode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNormalValid() {
        try {
            NormalMode mode = CheckFields.getField(audioStackManager, "normalMode");
            Assert.assertNotNull(mode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testBluetoothValid() {
        try {
            BluetoothMode mode = CheckFields.getField(audioStackManager, "bluetoothMode");
            Assert.assertNotNull(mode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testWiredValid() {
        try {
            WiredMode mode = CheckFields.getField(audioStackManager, "wiredMode");
            Assert.assertNotNull(mode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
