package com.voxeet.audio;

import android.app.Application;

import androidx.test.runner.AndroidJUnitRunner;

public class VoxeetRunner extends AndroidJUnitRunner {

    public static Application app = null;

    @Override
    public void callApplicationOnCreate(Application app) {
        VoxeetRunner.app = app;

        super.callApplicationOnCreate(app);
    }
}