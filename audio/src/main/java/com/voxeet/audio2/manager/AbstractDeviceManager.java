package com.voxeet.audio2.manager;

import android.support.annotation.NonNull;

import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;

public abstract class AbstractDeviceManager<TYPE> implements IDeviceManager<TYPE> {

    @NonNull
    private IMediaDeviceConnectionState connectionState;

    private AbstractDeviceManager() {
        connectionState = (typeAbstractDevice, connectionState) -> {
        };
    }

    protected AbstractDeviceManager(@NonNull IMediaDeviceConnectionState connectionState) {
        this();
        this.connectionState = connectionState;
    }
}
