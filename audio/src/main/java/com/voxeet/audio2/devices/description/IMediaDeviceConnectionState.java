package com.voxeet.audio2.devices.description;

import android.support.annotation.NonNull;

public interface IMediaDeviceConnectionState {
    void onConnectionState(@NonNull ConnectionStatesEvent holder);
}
