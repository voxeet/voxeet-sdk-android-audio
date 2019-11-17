package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.AudioRoute;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;

public class MediaMode extends AbstractMode {

    public MediaMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, AudioRoute.ROUTE_MEDIA);
    }

    @Override
    public void apply(boolean speaker_state) {
        requestAudioFocus();
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(Constants.STREAM_MUSIC);

        //commenting this one - requesting audio focus for media with Spotify "pause" the music
        //audioFocusManger.requestAudioFocus(manager, Constants.STREAM_MUSIC);
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
