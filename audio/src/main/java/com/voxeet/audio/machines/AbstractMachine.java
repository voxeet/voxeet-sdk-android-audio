package com.voxeet.audio.machines;

import com.voxeet.audio.AudioStackManager;
import com.voxeet.audio.listeners.IMediaStateListener;
import com.voxeet.audio.listeners.ListenerHolder;
import com.voxeet.audio.mode.AbstractMode;
import com.voxeet.audio.utils.Log;

public abstract class AbstractMachine<CONNECT_CLASS> {
    protected  AbstractMode audioMode;
    protected ListenerHolder<IMediaStateListener> listenerHolder;
    protected AudioStackManager audioManager;
    protected android.media.AudioManager manager;

    private AbstractMachine() {

    }

    AbstractMachine(ListenerHolder<IMediaStateListener> listenerHolder,
                    AudioStackManager audioManager,
                    android.media.AudioManager manager,
                    AbstractMode mode){
        this();

        this.listenerHolder = listenerHolder;
        this.audioManager = audioManager;
        this.manager = manager;
        this.audioMode = mode;
    }

    public abstract void connect(CONNECT_CLASS connect);

    public abstract void disconnect();

    public abstract void stop();

    public abstract boolean isConnected();

    public abstract void warmup();

    public abstract void enable(boolean isEnabled);

    public void requestAudioFocus() {
        Log.d(getClass().getSimpleName(), "requestAudioFocus from machine");
        audioMode.requestAudioFocus();
    }
}
