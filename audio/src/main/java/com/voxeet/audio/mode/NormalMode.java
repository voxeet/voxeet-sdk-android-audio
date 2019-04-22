package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;

public class NormalMode extends AbstractMode {

    public NormalMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager);
    }

    @Override
    public void apply(boolean speaker_state) {
        forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        audioFocusManger.requestAudioFocus(manager);
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        audioFocusManger.requestAudioFocus(manager);
    }
}
