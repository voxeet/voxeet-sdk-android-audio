package com.voxeet.audio.focus;


import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

public class AudioFocusManager implements AudioFocusRequest{
    private final AudioFocusRequest audioFocus;

    public AudioFocusManager() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocus = new AudioFocusRequest26();
        } else {
            audioFocus = new AudioFocusRequest8();
        }
    }

    @Override
    public int requestAudioFocus(@NonNull AudioManager manager) {
        return audioFocus.requestAudioFocus(manager);
    }

    @Override
    public int abandonAudioFocus(AudioManager service) {
        return audioFocus.abandonAudioFocus(service);
    }


}
