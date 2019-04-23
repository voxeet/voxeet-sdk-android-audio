package com.voxeet.tests.modes;

import com.voxeet.audio.AudioStackManager;
import com.voxeet.audio.mode.BluetoothMode;
import com.voxeet.audio.mode.NormalMode;
import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.audio.mode.WiredMode;
import com.voxeet.tests.VoxeetRunner;
import com.voxeet.tests.utils.AudioStackManagerUtils;
import com.voxeet.tests.utils.CheckFields;

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
