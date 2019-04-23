package com.voxeet.audio.focus;

import android.media.AudioManager;
import android.support.annotation.NonNull;

public class AudioFocusRequest8 implements AudioFocusRequest {

    private AudioManager.OnAudioFocusChangeListener focusRequest;

    public AudioFocusRequest8() {
        focusRequest = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {

            }
        };
    }
    @Override
    public int requestAudioFocus(@NonNull AudioManager manager, int audioFocusVolumeType) {
        return manager.requestAudioFocus(focusRequest,
                audioFocusVolumeType, //AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    @Override
    public int abandonAudioFocus(@NonNull AudioManager manager) {
        return manager.abandonAudioFocus(focusRequest);
    }
}
