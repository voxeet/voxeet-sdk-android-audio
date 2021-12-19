package com.voxeet.audio2.devices;

import androidx.annotation.NonNull;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.promise.Promise;

public class MediaDeviceConnectionWrapper {
    private static boolean called = false;

    private MediaDeviceConnectionWrapper() {

    }

    @NonNull
    public static MediaDeviceConnectionWrapper unique() {
        if (called) throw new IllegalStateException("Only one accepted");
        called = true;
        return new MediaDeviceConnectionWrapper();
    }

    @NonNull
    public <TYPE> Promise<Boolean> connect(@NonNull MediaDevice<TYPE> mediaDevice) {
        return mediaDevice.connect();
    }

    @NonNull
    public <TYPE> Promise<Boolean> disconnect(@NonNull MediaDevice<TYPE> mediaDevice) {
        return new Promise<>(solver -> {
            if (ConnectionState.DISCONNECTED.equals(mediaDevice.connectionState())) {
                Log.d(mediaDevice.id(), "device already disconnected...");
                solver.resolve(true);
            } else {
                solver.resolve(mediaDevice.disconnect());
            }
        });
    }
}
