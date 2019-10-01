package com.voxeet.audio.focus;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.utils.Log;

public class AudioFocusRequest8 implements AudioFocusRequest {

    private final AudioFocusMode mode;
    private AudioManager.OnAudioFocusChangeListener focusRequest;

    public AudioFocusRequest8(AudioFocusMode mode) {
        this.mode = mode;
        Log.d("AudioFocusRequest8", "ctor : this mode does not use the mode " + mode);
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
