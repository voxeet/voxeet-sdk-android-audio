package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class WiredMode extends AbstractMode {

    public WiredMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager);
    }

    @Override
    public void apply(boolean speaker_state) {
        manager.setSpeakerphoneOn(false);
        manager.setMode(MODE_IN_COMMUNICATION);
        forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        audioFocusManger.requestAudioFocus(manager);
    }
}
