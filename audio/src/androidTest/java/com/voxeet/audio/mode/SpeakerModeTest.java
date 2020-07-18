package com.voxeet.audio.mode;

import android.media.AudioManager;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.VoxeetRunner;
import com.voxeet.audio.utils.AudioStackManagerUtils;
import com.voxeet.audio.utils.ThreadUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class SpeakerModeTest {

    private AudioStackManager audioStackManager;

    @Before
    public void init() {
        audioStackManager = AudioStackManagerUtils.create(VoxeetRunner.app);
    }


    @Test
    public void testConnected() {
        AudioManager manager = VoxeetRunner.app.getSystemService(AudioManager.class);

        com.voxeet.audio.mode.SpeakerMode mode = new com.voxeet.audio.mode.SpeakerMode(manager, new AudioFocusManager());

        Assert.assertTrue(mode.isConnected());
    }

    @Test
    public void testSamsungCalled() {
        AudioManager manager = VoxeetRunner.app.getSystemService(AudioManager.class);

        com.voxeet.audio.mode.SpeakerMode mode = new com.voxeet.audio.mode.SpeakerMode(manager, new AudioFocusManager());

        mode.requestAudioFocus().execute();

        ThreadUtils.waitFor(1000);

        int currentMode = manager.getMode();

        Assert.assertEquals(AudioManager.STREAM_VOICE_CALL, currentMode);
    }

    @Test
    public void testApplyOnlyMode() {
        AudioManager manager = VoxeetRunner.app.getSystemService(AudioManager.class);

        com.voxeet.audio.mode.SpeakerMode mode = new com.voxeet.audio.mode.SpeakerMode(manager, new AudioFocusManager());

        mode.apply(true);

        ThreadUtils.waitFor(1000);

        int currentMode = manager.getMode();

        Assert.assertEquals(AudioManager.STREAM_VOICE_CALL, currentMode);
    }

    @Test
    public void testApplyOnlyFalse() {
        AudioManager manager = VoxeetRunner.app.getSystemService(AudioManager.class);

        com.voxeet.audio.mode.SpeakerMode mode = new com.voxeet.audio.mode.SpeakerMode(manager, new AudioFocusManager());

        mode.apply(false);

        ThreadUtils.waitFor(1000);

        int currentMode = manager.getMode();

        Assert.assertEquals(AudioManager.STREAM_VOICE_CALL, currentMode);
    }

}
