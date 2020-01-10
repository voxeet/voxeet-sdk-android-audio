package com.voxeet.audio2.devices.description;

import android.support.annotation.NonNull;

import com.voxeet.audio2.devices.MediaDevice;

public interface IMediaDeviceConnectionState {
    void onConnectionState(@NonNull MediaDevice typeAbstractDevice,
                           @NonNull ConnectionState connectionState);
}
