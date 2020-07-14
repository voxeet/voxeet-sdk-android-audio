package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusManagerAsync;
import com.voxeet.audio.utils.Constants;
import com.voxeet.audio.utils.Invoke;

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

    public abstract void apply(boolean speaker_state);

    public abstract void requestAudioFocus();

    public abstract boolean isConnected();

    public void abandonAudioFocus() {
        AudioFocusManagerAsync.setMode(manager, android.media.AudioManager.MODE_NORMAL, "AbstractMode");
        audioFocusManger.abandonAudioFocus(manager);
        forceVolumeControlStream(abandonFocus);
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
