package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;

public class NormalMode extends AbstractMode {

    public NormalMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_PHONE);
    }

    @Override
    public void apply(boolean speaker_state) {
        manager.setSpeakerphoneOn(false);
        requestAudioFocus();
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(requestFocus);
        audioFocusManger.requestAudioFocus(manager, requestFocus);
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
