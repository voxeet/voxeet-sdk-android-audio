package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

public abstract class AbstractMode {
    protected AudioManager manager;

    private AbstractMode() {

    }

    public AbstractMode(@NonNull AudioManager manager) {
        this.manager = manager;
    }

    public abstract void apply(boolean speaker_state);
}
