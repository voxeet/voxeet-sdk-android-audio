package com.voxeet.audio2.devices;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.ConnectionStatesEvent;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.promise.Promise;

public abstract class MediaDevice<TYPE> {

    @NonNull
    protected final String id;

    @Nullable
    protected TYPE holder;

    @NonNull
    private final IMediaDeviceConnectionState mediaDeviceConnectionState;

    @NonNull
    private final DeviceType deviceType;

    @NonNull
    protected ConnectionState connectionState;

    @NonNull
    protected ConnectionState platformConnectionState;

    protected MediaDevice(@NonNull IMediaDeviceConnectionState mediaDeviceConnectionState,
                          @NonNull DeviceType deviceType,
                          @NonNull String id) {
        this(mediaDeviceConnectionState, deviceType, id, null);
    }

    protected MediaDevice(@NonNull IMediaDeviceConnectionState mediaDeviceConnectionState,
                          @NonNull DeviceType deviceType,
                          @NonNull String id,
                          @Nullable TYPE holder) {
        connectionState = ConnectionState.DISCONNECTED;
        platformConnectionState = ConnectionState.CONNECTED;
        this.mediaDeviceConnectionState = mediaDeviceConnectionState;
        this.id = id;
        this.deviceType = deviceType;
        this.holder = holder;

    }

    @NonNull
    public String id() {
        return id;
    }

    @NonNull
    public DeviceType deviceType() {
        return deviceType;
    }

    void setConnectionState(@NonNull ConnectionState connectionState) {
        Log.d(MediaDevice.class.getSimpleName(), "setConnectionState: " + id() + " " + connectionState);
        this.connectionState = connectionState;
        mediaDeviceConnectionState.onConnectionState(new ConnectionStatesEvent(connectionState, platformConnectionState, this));
    }

    protected void setPlatformConnectionState(@NonNull ConnectionState platformConnectionState) {
        Log.d(MediaDevice.class.getSimpleName(), "setPlatformConnectionState: " + id() + " " + connectionState);
        this.platformConnectionState = platformConnectionState;
        mediaDeviceConnectionState.onConnectionState(new ConnectionStatesEvent(connectionState, platformConnectionState, this));
    }

    @NonNull
    public ConnectionState connectionState() {
        return connectionState;
    }

    @NonNull
    public ConnectionState platformConnectionState() {
        return platformConnectionState;
    }

    @NonNull
    protected abstract Promise<Boolean> connect();

    @NonNull
    protected abstract Promise<Boolean> disconnect();

}
