package com.voxeet.audio.machines;

import com.voxeet.audio.AudioStackManager;
import com.voxeet.audio.listeners.IMediaStateListener;
import com.voxeet.audio.listeners.ListenerHolder;
import com.voxeet.audio.mode.AbstractMode;

public class WiredHeadsetMachine extends AbstractMachine<WiredInformation> {

    private WiredInformation connect;

    public WiredHeadsetMachine(ListenerHolder<IMediaStateListener> listenerHolder,
                               AudioStackManager audioManager,
                               android.media.AudioManager manager,
                               AbstractMode audioMode) {
        super(listenerHolder, audioManager, manager, audioMode);
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
