package com.voxeet.audio.focus;

import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.utils.Log;
import com.voxeet.promise.Promise;

public class AudioFocusRequest8 implements AudioFocusRequest {

    private final AudioFocusMode mode;
    private AudioManager.OnAudioFocusChangeListener focusRequest;

    public AudioFocusRequest8(AudioFocusMode mode) {
        this.mode = mode;
        Log.d("AudioFocusRequest8", "ctor : this mode does not use the mode " + mode);
        focusRequest = focusChange -> {

        };
    }

    @Override
    public Promise<Integer> requestAudioFocus(@NonNull AudioManager manager, int audioFocusVolumeType) {
        return new Promise<>(solver -> {
            int result = manager.requestAudioFocus(focusRequest,
                    audioFocusVolumeType, //AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN);

            solver.resolve(result);
        });
    }

    @Override
    public Promise<Integer> abandonAudioFocus(@NonNull AudioManager manager) {
        return new Promise<>(solver -> {
            int result = manager.abandonAudioFocus(focusRequest);

            solver.resolve(result);
        });
    }
}
