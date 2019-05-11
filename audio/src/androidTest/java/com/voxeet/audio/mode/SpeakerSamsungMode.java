package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.os.Build;

import com.voxeet.audio.AudioStackManager;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.VoxeetRunner;
import com.voxeet.audio.utils.AudioStackManagerUtils;
import com.voxeet.audio.utils.Fields;
import com.voxeet.audio.utils.ThreadUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

@RunWith(BlockJUnit4ClassRunner.class)
public class SpeakerSamsungMode {


    @Test
    public void testApplyOnlyMode() {
        Fields.setBuildBrand("samsung");

        SpeakerMode mode = Mockito.mock(SpeakerMode.class);
        final boolean[] invalid = {false};
        Answer invalidAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                invalid[0] = true;
                return null;
            }
        };

        //test to apply the speaker mode
        //we set the various environment specific code
        //for instance, we check on samsung that only the sub samsung method is called
        //and for any other, we check that the samsung method is not called
        if ("samsung".equalsIgnoreCase(Build.BRAND)) {
            doNothing().when(mode).applySamsung(true);
            doAnswer(invalidAnswer).when(mode).applyNonSamsung(true);

            doNothing().when(mode).applySamsung(false);
            doAnswer(invalidAnswer).when(mode).applyNonSamsung(false);
        } else {
            Assert.fail("invalid, expected samsung, having " + Build.BRAND);
        }

        try {
            mode.apply(true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            mode.apply(true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testApplyOnlyModeNonSamsung() {
        Fields.setBuildBrand("non+samsung");

        SpeakerMode mode = Mockito.mock(SpeakerMode.class);
        final boolean[] invalid = {false};
        Answer invalidAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                invalid[0] = true;
                return null;
            }
        };

        //test to apply the speaker mode
        //we set the various environment specific code
        //for instance, we check on samsung that only the sub samsung method is called
        //and for any other, we check that the samsung method is not called
        if ("samsung".equalsIgnoreCase(Build.BRAND)) {
            Assert.fail("invalid, unexpected samsung, having" + Build.BRAND);
            doNothing().when(mode).applySamsung(true);
            doAnswer(invalidAnswer).when(mode).applyNonSamsung(true);

            doNothing().when(mode).applySamsung(false);
            doAnswer(invalidAnswer).when(mode).applyNonSamsung(false);
        } else {
        }

        try {
            mode.apply(true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            mode.apply(true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testApplyOnlyFalse() {
        AudioManager manager = VoxeetRunner.app.getSystemService(AudioManager.class);

        SpeakerMode mode = new SpeakerMode(manager, new AudioFocusManager());

        mode.apply(false);

        ThreadUtils.waitFor(1000);

        int currentMode = manager.getMode();

        Assert.assertEquals(AudioManager.STREAM_VOICE_CALL, currentMode);
    }

}
