package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

import static android.media.AudioManager.MODE_CURRENT;
import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class SpeakerMode extends AbstractMode {
    public SpeakerMode(@NonNull AudioManager manager) {
        super(manager);
    }

    @Override
    public void apply(boolean speaker_state) {
        if("samsung".equalsIgnoreCase(Build.BRAND)) {
            applySamsung(speaker_state);
        } else {
            applyNonSamsung(speaker_state);
        }
    }

    private void applyNonSamsung(boolean speaker_state) {
        manager.setMode(MODE_IN_COMMUNICATION);
        manager.setSpeakerphoneOn(speaker_state);
    }

    private void applySamsung(boolean speaker_state) {
        if (speaker_state) {
            // route audio to back speaker
            manager.setSpeakerphoneOn(true);
            manager.setMode(MODE_CURRENT);
        } else {
            // route audio to earpiece
            manager.setSpeakerphoneOn(speaker_state);
            manager.setMode(MODE_IN_COMMUNICATION);
        }
    }
}
