package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusManagerAsync;
import com.voxeet.audio.utils.Log;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.PromiseSolver;
import com.voxeet.promise.solve.Solver;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.promise.solve.ThenVoid;

import static android.media.AudioManager.MODE_CURRENT;
import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class SpeakerMode extends AbstractMode {

    public SpeakerMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_SPEAKER);
    }

    @Override
    public Promise<Boolean> apply(boolean speaker_state) {
        if ("samsung".equalsIgnoreCase(Build.BRAND)) {
            Log.d("SpeakerMode", "apply samsung");
            return applySamsung(speaker_state);
        } else {
            Log.d("SpeakerMode", "apply non samsung");
            return applyNonSamsung(speaker_state);
        }
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

    Promise<Boolean> applyNonSamsung(boolean speaker_state) {
        return new Promise<>(solver -> AudioFocusManagerAsync.setMode(manager, MODE_IN_COMMUNICATION, "SpeakerMode").then((ThenPromise<Boolean, Boolean>) aBoolean -> {
            manager.setSpeakerphoneOn(speaker_state);
            //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
            return requestAudioFocus();
        }).then(o -> {
            solver.resolve(true);
        }).error(solver::reject));
    }

    Promise<Boolean> applySamsung(boolean speaker_state) {
        return new Promise<>(solver -> {
            Promise<Boolean> mode;
            if (speaker_state) {
                // route audio to back speaker
                manager.setSpeakerphoneOn(true);
                mode = AudioFocusManagerAsync.setMode(manager, MODE_CURRENT, "SpeakerMode");
                //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
            } else {
                // route audio to earpiece
                manager.setSpeakerphoneOn(speaker_state);
                mode = AudioFocusManagerAsync.setMode(manager, MODE_IN_COMMUNICATION, "SpeakerMode");
                //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
            }

            mode.then((ThenPromise<Boolean, Boolean>) aBoolean -> requestAudioFocus())
                    .then(aBoolean -> {
                        solver.resolve(true);
                    }).error(solver::reject);
        });
    }
}
