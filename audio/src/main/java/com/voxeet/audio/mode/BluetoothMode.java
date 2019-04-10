package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class BluetoothMode extends AbstractMode {
    public BluetoothMode(@NonNull AudioManager manager) {
        super(manager);
    }

    @Override
    public void apply(boolean speaker_state) {
        manager.setSpeakerphoneOn(false);
        manager.setMode(MODE_IN_COMMUNICATION);
    }
}
