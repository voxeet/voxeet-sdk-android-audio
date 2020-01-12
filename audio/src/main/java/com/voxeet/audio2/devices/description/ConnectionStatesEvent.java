package com.voxeet.audio2.devices.description;

import com.voxeet.audio2.devices.MediaDevice;

public class ConnectionStatesEvent {
    public ConnectionState connectionState;
    public ConnectionState platformConnectionState;
    public MediaDevice device;

    public ConnectionStatesEvent(ConnectionState connectionState, ConnectionState platformConnectionState, MediaDevice device) {
        this.connectionState = connectionState;
        this.platformConnectionState = platformConnectionState;
        this.device = device;
    }
}
