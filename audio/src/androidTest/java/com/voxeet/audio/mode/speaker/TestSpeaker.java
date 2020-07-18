package com.voxeet.audio.mode.speaker;

import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.audio.VoxeetRunner;
import com.voxeet.audio.utils.AudioStackManagerUtils;
import com.voxeet.audio.utils.CheckFields;

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
            SpeakerMode speaker = CheckFields.getField(audioStackManager, "speakerMode");
            Assert.assertNotNull(speaker);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
