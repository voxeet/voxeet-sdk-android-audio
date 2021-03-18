package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusManagerAsync;
import com.voxeet.audio.utils.Constants;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class BluetoothMode extends AbstractMode {

    public BluetoothMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_BLUETOOTH);
    }

    @Override
    public Promise<Boolean> apply(boolean speaker_state) {
        return new Promise<>(solver -> {
            manager.setSpeakerphoneOn(false);
            AudioFocusManagerAsync.setMode(manager, MODE_IN_COMMUNICATION, "BluetoothMode")
                    .then((ThenPromise<Boolean, Boolean>) aBoolean -> {
                        //forceVolumeControlStream(Constants.STREAM_BLUETOOTH_SCO | requestFocus);
                        return requestAudioFocus();
                    })
                    .then(o -> {
                        solver.resolve(true);
                    }).error(solver::reject);
        });
    }

    @Override
    public Promise<Boolean> requestAudioFocus() {
        return new Promise<>(solver -> {
            forceVolumeControlStream(Constants.STREAM_BLUETOOTH_SCO | requestFocus);
            audioFocusManger.requestAudioFocus(manager, Constants.STREAM_BLUETOOTH_SCO | requestFocus)
                    .then(integer -> {
                        solver.resolve(true);
                    }).error(solver::reject);

        });
    }

    @Override
    public boolean isConnected() {
        return true; //TODO check for current bluetooth state?
    }
}
