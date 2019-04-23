package com.voxeet.tests.modes.speaker;

import com.voxeet.audio.AudioStackManager;
import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.tests.VoxeetRunner;
import com.voxeet.tests.utils.AudioStackManagerUtils;
import com.voxeet.tests.utils.CheckFields;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestSpeaker {

    private AudioStackManager audioStackManager;

    @Before
    public void init() {
        audioStackManager = AudioStackManagerUtils.create(VoxeetRunner.app);
    }

    @Test
    public void testSpeakerValid() {
        try {
            SpeakerMode speaker = (SpeakerMode) CheckFields.getField(audioStackManager, "speakerMode");
            Assert.assertNotNull(speaker);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
