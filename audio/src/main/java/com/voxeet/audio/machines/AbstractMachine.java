package com.voxeet.audio.machines;

import android.util.Log;

import com.voxeet.audio.AudioManager;
import com.voxeet.audio.listeners.IMediaStateListener;
import com.voxeet.audio.listeners.ListenerHolder;

public abstract class AbstractMachine<CONNECT_CLASS> {
    protected ListenerHolder<IMediaStateListener> listenerHolder;
    protected AudioManager audioManager;
    protected android.media.AudioManager manager;

    private AbstractMachine() {

    }

    AbstractMachine(ListenerHolder<IMediaStateListener> listenerHolder, AudioManager audioManager, android.media.AudioManager manager){
        this();

        this.listenerHolder = listenerHolder;
        this.audioManager = audioManager;
        this.manager = manager;
    }

    public abstract void connect(CONNECT_CLASS connect);

    public abstract void disconnect();

    public abstract void stop();

    public abstract boolean isConnected();

    public abstract void warmup();

    public abstract void enable(boolean isEnabled);

    public void requestAudioFocus() {
        Log.d(getClass().getSimpleName(), "requestAudioFocus from machine");
        audioManager.requestAudioFocus();
    }
}
