package com.voxeet.audio2.manager.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BluetoothAction {

    public final Action action;
    public final BluetoothDevice device;

    public BluetoothAction(@NonNull Action action,
                           @Nullable BluetoothDevice device) {
        this.action = action;
        this.device = device;
    }

    @Override
    public String toString() {
        return "BluetoothAction{" +
                "action=" + action +
                ", device=" + device +
                '}';
    }

    public enum Action {
        DEVICE_CONNECTED,
        DEVICE_DISCONNECTED,
        SCO_AUDIO_DISCONNECTED,
        SCO_AUDIO_CONNECTING,
        DEVICE_ACTIVE_CHANGED, SCO_AUDIO_CONNECTED
    }
}
