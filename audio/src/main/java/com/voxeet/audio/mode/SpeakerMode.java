package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;

import static android.media.AudioManager.MODE_CURRENT;
import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class SpeakerMode extends AbstractMode {

    public SpeakerMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_SPEAKER);
    }

    @Override
    public void apply(boolean speaker_state) {
        if("samsung".equalsIgnoreCase(Build.BRAND)) {
            applySamsung(speaker_state);
        } else {
            applyNonSamsung(speaker_state);
        }
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

    void applyNonSamsung(boolean speaker_state) {
        manager.setMode(MODE_IN_COMMUNICATION);
        manager.setSpeakerphoneOn(speaker_state);
        //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        requestAudioFocus();
    }

    void applySamsung(boolean speaker_state) {
        if (speaker_state) {
            // route audio to back speaker
            manager.setSpeakerphoneOn(true);
            manager.setMode(MODE_CURRENT);
            //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        } else {
            // route audio to earpiece
            manager.setSpeakerphoneOn(speaker_state);
            manager.setMode(MODE_IN_COMMUNICATION);
            //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        }

        requestAudioFocus();
    }
}
