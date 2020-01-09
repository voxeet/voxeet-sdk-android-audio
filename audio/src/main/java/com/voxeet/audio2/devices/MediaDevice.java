package com.voxeet.audio2.devices;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.promise.Promise;

public abstract class MediaDevice<TYPE> {

    @NonNull
    protected final String id;

    @Nullable
    protected final TYPE holder;

    @NonNull
    private final IMediaDeviceConnectionState mediaDeviceConnectionState;

    @NonNull
    private final DeviceType deviceType;

    @NonNull
    protected ConnectionState connectionState;

    protected MediaDevice(@NonNull IMediaDeviceConnectionState mediaDeviceConnectionState,
                          @NonNull DeviceType deviceType,
                          @NonNull String id) {
        connectionState = ConnectionState.DISCONNECTED;
        this.mediaDeviceConnectionState = mediaDeviceConnectionState;
        this.id = id;
        this.deviceType = deviceType;
        this.holder = null;
    }

    protected MediaDevice(@NonNull IMediaDeviceConnectionState mediaDeviceConnectionState,
                          @NonNull DeviceType deviceType,
                          @NonNull String id,
                          @NonNull TYPE holder) {
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

    protected void setConnectionState(@NonNull ConnectionState connectionState) {
        Log.d(MediaDevice.class.getSimpleName(), "setConnectionState: " + id() + " " + connectionState);
        this.connectionState = connectionState;
        mediaDeviceConnectionState.onConnectionState(this, connectionState);
    }

    @NonNull
    public ConnectionState connectionState() {
        return connectionState;
    }

    protected abstract Promise<Boolean> connect();

    protected abstract Promise<Boolean> disconnect();

}
