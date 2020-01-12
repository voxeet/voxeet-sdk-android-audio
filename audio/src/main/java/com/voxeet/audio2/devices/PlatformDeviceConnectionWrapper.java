package com.voxeet.audio2.devices;

import android.support.annotation.NonNull;

import com.voxeet.audio2.devices.description.ConnectionState;

public interface PlatformDeviceConnectionWrapper {

    void setPlatformConnectionState(@NonNull ConnectionState connectionState);
}
