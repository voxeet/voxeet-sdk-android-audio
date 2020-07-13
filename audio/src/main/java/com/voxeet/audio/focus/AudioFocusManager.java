package com.voxeet.audio.focus;


import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

public class AudioFocusManager implements AudioFocusRequest{
    private final AudioFocusRequest audioFocus;

    public AudioFocusManager(@NonNull AudioFocusMode mode) {
        AudioFocusManagerAsync.start();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocus = new AudioFocusRequest26(mode);
        } else {
            audioFocus = new AudioFocusRequest8(mode);
        }
    }

    @Override
    public int requestAudioFocus(@NonNull AudioManager manager, int audioFocusVolumeType) {
        return audioFocus.requestAudioFocus(manager, audioFocusVolumeType);
    }

    @Override
    public int abandonAudioFocus(AudioManager service) {
        return audioFocus.abandonAudioFocus(service);
    }


}
