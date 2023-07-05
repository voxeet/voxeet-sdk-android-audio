package com.voxeet.audio2.devices;

import androidx.annotation.NonNull;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
import com.voxeet.promise.Promise;

public class MediaDeviceConnectionWrapper {

    public MediaDeviceConnectionWrapper() {
    }

    @NonNull
    public <TYPE> Promise<Boolean> connect(@NonNull MediaDevice<TYPE> mediaDevice,
                                           @NonNull LastConnectionStateType lastConnectionStateType) {
        return mediaDevice.connect(lastConnectionStateType);
    }

    @NonNull
    public <TYPE> Promise<Boolean> disconnect(@NonNull MediaDevice<TYPE> mediaDevice,
                                              @NonNull LastConnectionStateType lastConnectionStateType) {
        return new Promise<>(solver -> {
            if (ConnectionState.DISCONNECTED.equals(mediaDevice.connectionState())) {
                Log.d(mediaDevice.id(), "device already disconnected...");
                solver.resolve(true);
            } else {
                solver.resolve(mediaDevice.disconnect(lastConnectionStateType));
            }
        });
    }
}
