package com.voxeet.audio;

import com.voxeet.audio.audio_stack.TestRoutes;
import com.voxeet.audio.mode.TestModes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestRoutes.class,
        TestModes.class,
})
public class InstrumentedTestClass {
}
