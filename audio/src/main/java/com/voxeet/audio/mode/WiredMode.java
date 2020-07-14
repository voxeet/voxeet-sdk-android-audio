package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusManagerAsync;
import com.voxeet.audio2.devices.MediaDeviceHelper;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class WiredMode extends AbstractMode {

    public WiredMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_HEADSET);
    }

    @Override
    public void apply(boolean speaker_state) {
        manager.setSpeakerphoneOn(false);
        AudioFocusManagerAsync.setMode(manager, MODE_IN_COMMUNICATION, "WiredMode");
        //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);

        requestAudioFocus();
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(requestFocus);
        audioFocusManger.requestAudioFocus(manager, requestFocus);
    }

    @Override
    public boolean isConnected() {
        return MediaDeviceHelper.isWiredHeadsetConnected(manager);
    }
}
