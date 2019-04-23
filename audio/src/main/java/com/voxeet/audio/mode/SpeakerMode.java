package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.voxeet.audio.AudioRoute;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;

import static android.media.AudioManager.MODE_CURRENT;
import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class SpeakerMode extends AbstractMode {

    public SpeakerMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, AudioRoute.ROUTE_SPEAKER);
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

    private void applyNonSamsung(boolean speaker_state) {
        manager.setMode(MODE_IN_COMMUNICATION);
        manager.setSpeakerphoneOn(speaker_state);
        //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        requestAudioFocus();
    }

    private void applySamsung(boolean speaker_state) {
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
