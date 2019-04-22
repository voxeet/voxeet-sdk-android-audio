package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.AudioRoute;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class BluetoothMode extends AbstractMode {

    public BluetoothMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, AudioRoute.ROUTE_BLUETOOTH);
    }

    @Override
    public void apply(boolean speaker_state) {
        manager.setSpeakerphoneOn(false);
        manager.setMode(MODE_IN_COMMUNICATION);
        forceVolumeControlStream(Constants.STREAM_BLUETOOTH_SCO);
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(Constants.STREAM_BLUETOOTH_SCO);
        audioFocusManger.requestAudioFocus(manager);
    }

    @Override
    public boolean isConnected() {
        return true; //TODO check for current bluetooth state?
    }
}
