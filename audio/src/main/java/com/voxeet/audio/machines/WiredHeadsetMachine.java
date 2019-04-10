package com.voxeet.audio.machines;

import com.voxeet.audio.AudioManager;
import com.voxeet.audio.listeners.IMediaStateListener;
import com.voxeet.audio.listeners.ListenerHolder;

public class WiredHeadsetMachine extends AbstractMachine<WiredInformation> {

    private WiredInformation connect;

    public WiredHeadsetMachine(ListenerHolder<IMediaStateListener> listenerHolder, AudioManager audioManager, android.media.AudioManager manager) {
        super(listenerHolder, audioManager, manager);
    }

    @Override
    public void connect(final WiredInformation connect) {

        this.connect = connect;

        if (connect.isPlugged()) {
            audioManager.setSpeakerMode(false);
            audioManager.checkOutputRoute();

            audioManager.notifyAudioRoute();
        } else {
            audioManager.checkOutputRoute();
            audioManager.notifyAudioRoute();
        }

        listenerHolder.notif(new ListenerHolder.Callback<IMediaStateListener>() {
            @Override
            public void apply(IMediaStateListener listener) {
                listener.onHeadsetStateChange(connect.isPlugged());
            }
        });
    }

    @Override
    public void disconnect() {
        audioManager.checkOutputRoute();
        audioManager.notifyAudioRoute();

        listenerHolder.notif(new ListenerHolder.Callback<IMediaStateListener>() {
            @Override
            public void apply(IMediaStateListener listener) {
                listener.onHeadsetStateChange(false);
            }
        });
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isConnected() {
        return null != connect && connect.isPlugged();
    }

    @Override
    public void warmup() {

    }

    @Override
    public void enable(boolean isEnabled) {

    }
}
