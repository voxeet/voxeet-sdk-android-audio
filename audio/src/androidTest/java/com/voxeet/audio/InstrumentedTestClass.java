package com.voxeet.audio;

import com.voxeet.audio.audio_stack.TestRoutes;
import com.voxeet.audio.mode.SpeakerModeTest;
import com.voxeet.audio.mode.SpeakerSamsungMode;
import com.voxeet.audio.mode.TestModes;
import com.voxeet.audio.mode.speaker.TestSpeaker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestRoutes.class,
        TestSpeaker.class,
        SpeakerModeTest.class,
        SpeakerSamsungMode.class,
        TestModes.class,
})
public class InstrumentedTestClass {
}
