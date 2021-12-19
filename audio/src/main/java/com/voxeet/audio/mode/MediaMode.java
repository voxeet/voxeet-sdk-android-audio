package com.voxeet.audio.mode;

import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;
import com.voxeet.promise.Promise;

public class MediaMode extends AbstractMode {

    public MediaMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_MEDIA);
    }

    @Override
    public Promise<Boolean> apply(boolean speaker_state) {
        return requestAudioFocus();
    }

    @Override
    public Promise<Boolean> requestAudioFocus() {
        return new Promise<>(solver -> {
            forceVolumeControlStream(Constants.STREAM_MUSIC);
            //commenting this one - requesting audio focus for media with Spotify "pause" the music
            //audioFocusManger.requestAudioFocus(manager, Constants.STREAM_MUSIC);

            solver.resolve(true);
        });
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
