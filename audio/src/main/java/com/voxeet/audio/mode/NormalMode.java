package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.promise.Promise;

public class NormalMode extends AbstractMode {

    public NormalMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_PHONE);
    }

    @Override
    public Promise<Boolean> apply(boolean speaker_state) {
        return new Promise<>(solver -> {
            manager.setSpeakerphoneOn(false);
            solver.resolve(requestAudioFocus());
        });
    }

    @Override
    public Promise<Boolean> requestAudioFocus() {
        return new Promise<>(solver -> {
            forceVolumeControlStream(requestFocus);
            audioFocusManger.requestAudioFocus(manager, requestFocus).then(integer -> {
                solver.resolve(true);
            }).error(solver::reject);
        });
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
