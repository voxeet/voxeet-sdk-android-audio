package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.AudioRoute;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;
import com.voxeet.audio.utils.Invoke;

public abstract class AbstractMode {
    protected AudioFocusManager audioFocusManger;
    protected AudioManager manager;
    private AudioRoute audioRoute;
    protected int requestFocus = Constants.STREAM_VOICE_CALL;
    private int abandonFocus = Constants.STREAM_MUSIC;

    private AbstractMode() {

    }

    public AbstractMode(@NonNull AudioManager manager,
                        @NonNull AudioFocusManager audioFocusManager,
                        @NonNull AudioRoute audioRoute) {
        this();

        this.manager = manager;
        this.audioFocusManger = audioFocusManager;
        this.audioRoute = audioRoute;
    }

    public abstract void apply(boolean speaker_state);

    public abstract void requestAudioFocus();

    public abstract boolean isConnected();

    public void abandonAudioFocus() {
        manager.setMode(android.media.AudioManager.MODE_NORMAL);
        audioFocusManger.abandonAudioFocus(manager);
        forceVolumeControlStream(abandonFocus);
    }

    protected void forceVolumeControlStream(int volumeMode) {
        Invoke.callVoidIntArg(manager, "forceVolumeControlStream", volumeMode);
    }

    public final AudioRoute getAudioRoute() {
        return audioRoute;
    }

    public void configureVolumeStream(int requestFocus, int abandonFocus) {
        this.requestFocus = requestFocus;
        this.abandonFocus = abandonFocus;
    }
}
