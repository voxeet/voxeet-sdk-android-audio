package com.voxeet.audio2.devices.description;

import androidx.annotation.NonNull;

public interface IMediaDeviceConnectionState {
    void onConnectionState(@NonNull ConnectionStatesEvent holder);
}
