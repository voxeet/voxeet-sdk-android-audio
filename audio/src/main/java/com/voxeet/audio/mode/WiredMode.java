package com.voxeet.audio.mode;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusManagerAsync;
import com.voxeet.audio.utils.Constants;
import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.MediaDeviceHelper;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;

public class WiredMode extends AbstractMode {

    /**
     * A side effect of using the non stream music is that a main library used will collide with this
     * We need to set it for now, if it's an indesirable effect, please change to false to have the previous
     * behaviour
     * <p>
     * A future version of the library will have a proper way of handling those conflicts by making
     * device merges possible
     */
    public static boolean SetAsMusic = true;
    private final AudioFocusManager mediaFocusManager;
    private boolean isConnected;

    public WiredMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager, AudioFocusManager audioMediaFocusManagerCall) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_HEADSET);
        this.mediaFocusManager = audioMediaFocusManagerCall;
        isConnected = MediaDeviceHelper.isWiredHeadsetConnected(manager);
    }

    @Override
    public Promise<Boolean> apply(boolean speaker_state) {
        return new Promise<>(solver -> {
            manager.setSpeakerphoneOn(false);

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
            int requestFocus = this.requestFocus;
            if (isConnected() && WiredMode.SetAsMusic) {
                Log.d("WiredMode", "set as STREAM_MUSIC");
                requestFocus = Constants.STREAM_MUSIC;
            } else {
                Log.d("WiredMode", "keep STREAM_VOICE_CALL");
            }

            forceVolumeControlStream(requestFocus);

            if (isConnected() && !"samsung".equalsIgnoreCase(Build.BRAND)) {
                Log.d("WiredMode", "not a samsung device, we need to force to media only");

                int finalRequestFocus = requestFocus;
                Log.d("WiredMode", "requestAudioFocus requestFocus " + requestFocus);
                mediaFocusManager.requestAudioFocus(manager, requestFocus).then(integer -> {
                    forceVolumeControlStream(finalRequestFocus);
                    solver.resolve(true);
                }).error(solver::reject);
                return;
            }
            Log.d("WiredMode", "samsung, requesting audio focus and solving");

            Log.d("WiredMode", "requestAudioFocus requestFocus " + requestFocus);
            audioFocusManger.requestAudioFocus(manager, requestFocus).then(integer -> {
                solver.resolve(true);
            }).error(solver::reject);
        });
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Set the current WiredMode. This value needs to be updated by the parent's controller when required
     * @param plugged
     */
    public void setConnected(boolean plugged) {
        isConnected = plugged;
    }
}
