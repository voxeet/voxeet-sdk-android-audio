package com.voxeet.audio2.devices;

import android.support.annotation.NonNull;

public interface IMediaDeviceConnectionState {
    void onConnectionState(@NonNull MediaDevice typeAbstractDevice,
                           @NonNull ConnectionState connectionState);
}
