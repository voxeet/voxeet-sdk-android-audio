package com.voxeet.audio.mode;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;
import com.voxeet.audio.utils.Invoke;

public abstract class AbstractMode {
    protected AudioFocusManager audioFocusManger;
    protected AudioManager manager;

    private AbstractMode() {

    }

    public AbstractMode(@NonNull AudioManager manager,
                        @NonNull AudioFocusManager audioFocusManager) {
        this.manager = manager;
        this.audioFocusManger = audioFocusManager;
    }

    public abstract void apply(boolean speaker_state);

    public abstract void requestAudioFocus();

    public void abandonAudioFocus() {
        manager.setMode(android.media.AudioManager.MODE_NORMAL);
        audioFocusManger.abandonAudioFocus(manager);
        forceVolumeControlStream(Constants.STREAM_MUSIC);
    }

    protected void forceVolumeControlStream(int volumeMode) {
        Invoke.callVoidIntArg(manager, "forceVolumeControlStream", volumeMode);
    }
}
