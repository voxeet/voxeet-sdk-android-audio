package com.voxeet.audio2.devices;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.ConnectionStatesEvent;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
import com.voxeet.promise.Promise;

public abstract class MediaDevice<TYPE> {

    @NonNull
    protected final String id;

    @Nullable
    protected final String name;

    @Nullable
    protected TYPE holder;

    @NonNull
    private final IMediaDeviceConnectionState mediaDeviceConnectionState;

    @NonNull
    private final DeviceType deviceType;

    @NonNull
    protected ConnectionState connectionState;

    @NonNull
    protected LastConnectionStateType lastConnectionStateType;

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
        this(mediaDeviceConnectionState, deviceType, id, holder, null);
    }

    protected MediaDevice(@NonNull IMediaDeviceConnectionState mediaDeviceConnectionState,
                          @NonNull DeviceType deviceType,
                          @NonNull String id,
                          @Nullable TYPE holder,
                          @Nullable String name) {
        connectionState = ConnectionState.DISCONNECTED;
        lastConnectionStateType = LastConnectionStateType.PROGRAMMATIC;
        platformConnectionState = ConnectionState.CONNECTED;
        this.mediaDeviceConnectionState = mediaDeviceConnectionState;
        this.id = id;
        this.name = name;
        this.deviceType = deviceType;
        this.holder = holder;

    }

    @NonNull
    public String id() {
        return id;
    }

    @NonNull
    public String name() {
        return null != name ? name : id();
    }

    @NonNull
    public DeviceType deviceType() {
        return deviceType;
    }

    void setConnectionState(@NonNull ConnectionState connectionState,
                            @NonNull LastConnectionStateType lastConnectionStateType) {
        Log.d(MediaDevice.class.getSimpleName(), "setConnectionState: " + id() + " " + connectionState + " " + lastConnectionStateType);
        this.connectionState = connectionState;
        this.lastConnectionStateType = lastConnectionStateType;
        mediaDeviceConnectionState.onConnectionState(new ConnectionStatesEvent(connectionState, platformConnectionState, this));
    }

    protected void setPlatformConnectionState(@NonNull ConnectionState platformConnectionState) {
        Log.d(MediaDevice.class.getSimpleName(), "setPlatformConnectionState: " + id() + " " + platformConnectionState);
        this.platformConnectionState = platformConnectionState;
        mediaDeviceConnectionState.onConnectionState(new ConnectionStatesEvent(connectionState, platformConnectionState, this));
    }

    @NonNull
    public ConnectionState connectionState() {
        return connectionState;
    }

    @NonNull
    public LastConnectionStateType lastConnectionStateType() {
        return lastConnectionStateType;
    }

    @NonNull
    public ConnectionState platformConnectionState() {
        return platformConnectionState;
    }

    @NonNull
    protected abstract Promise<Boolean> connect(@NonNull LastConnectionStateType lastConnectionStateType);

    @NonNull
    protected abstract Promise<Boolean> disconnect(@NonNull LastConnectionStateType lastConnectionStateType);

    @Override
    public String toString() {
        return "MediaDevice{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", deviceType=" + deviceType +
                ", connectionState=" + connectionState +
                ", lastConnectionStateTYpe=" + lastConnectionStateType +
                ", platformConnectionState=" + platformConnectionState +
                '}';
    }
}
