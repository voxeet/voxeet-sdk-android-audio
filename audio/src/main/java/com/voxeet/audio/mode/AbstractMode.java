package com.voxeet.audio.mode;

import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusManagerAsync;
import com.voxeet.audio.utils.Constants;
import com.voxeet.audio.utils.Invoke;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;

public abstract class AbstractMode {
    protected AudioFocusManager audioFocusManger;
    protected AudioManager manager;
    private MediaDevice audioRoute;
    protected int requestFocus = Constants.STREAM_VOICE_CALL;
    private int abandonFocus = Constants.STREAM_MUSIC;

    private AbstractMode() {

    }

    public AbstractMode(@NonNull AudioManager manager,
                        @NonNull AudioFocusManager audioFocusManager,
                        @NonNull MediaDevice audioRoute) {
        this();

        this.manager = manager;
        this.audioFocusManger = audioFocusManager;
        this.audioRoute = audioRoute;
    }

    public abstract Promise<Boolean> apply(boolean speaker_state);

    public abstract Promise<Boolean> requestAudioFocus();

    public abstract boolean isConnected();

    public Promise<Boolean> abandonAudioFocus() {
        return new Promise<>(solver -> AudioFocusManagerAsync.setMode(manager, AudioManager.MODE_NORMAL, "AbstractMode")
                .then((ThenPromise<Boolean, Integer>) aBoolean -> audioFocusManger.abandonAudioFocus(manager))
                .then(integer -> {
                    forceVolumeControlStream(abandonFocus);
                    solver.resolve(true);
                }).error(solver::reject));
    }

    protected void forceVolumeControlStream(int volumeMode) {
        Invoke.callVoidIntArg(manager, "forceVolumeControlStream", volumeMode);
    }

    public final MediaDevice getAudioRoute() {
        return audioRoute;
    }

    public void configureVolumeStream(int requestFocus, int abandonFocus) {
        this.requestFocus = requestFocus;
        this.abandonFocus = abandonFocus;
    }
}
