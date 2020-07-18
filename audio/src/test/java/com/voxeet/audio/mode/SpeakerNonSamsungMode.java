package com.voxeet.audio.mode;

import com.voxeet.audio.utils.Fields;

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
public class SpeakerNonSamsungMode {

    private AudioStackManager audioStackManager;

    @Before
    public void init() {
        Fields.setBuildBrand("well_not_samsung");
    }

    @Test
    public void testApplyOnlyMode() {
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
        doAnswer(invalidAnswer).when(mode).applySamsung(true);
        doNothing().when(mode).applyNonSamsung(true);

        doAnswer(invalidAnswer).when(mode).applySamsung(false);
        doNothing().when(mode).applyNonSamsung(false);

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

        Assert.assertFalse(invalid[0]);
    }

}
