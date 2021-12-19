package com.voxeet.audio.mode;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusManagerAsync;
import com.voxeet.audio2.devices.MediaDeviceHelper;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;

public class WiredMode extends AbstractMode {

    public WiredMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_HEADSET);
    }

    @Override
    public Promise<Boolean> apply(boolean speaker_state) {
        return new Promise<>(solver -> {
            manager.setSpeakerphoneOn(false);
            //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
            AudioFocusManagerAsync.setMode(manager, MODE_IN_COMMUNICATION, "WiredMode")
                    .then((ThenPromise<Boolean, Boolean>) aBoolean -> requestAudioFocus())
                    .then(o -> {
                        solver.resolve(true);
                    })
                    .error(solver::reject);
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
        return MediaDeviceHelper.isWiredHeadsetConnected(manager);
    }
}
