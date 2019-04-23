package com.voxeet.tests;

import com.voxeet.tests.modes.speaker.TestSpeaker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        Test.class,
        TestSpeaker.class
})
public class InstrumentedTestClass {
}
