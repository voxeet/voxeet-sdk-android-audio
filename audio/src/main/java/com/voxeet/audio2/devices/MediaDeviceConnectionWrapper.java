package com.voxeet.audio2.devices;

import android.support.annotation.NonNull;

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
    public Promise<Boolean> connect(@NonNull MediaDevice mediaDevice) {
        return mediaDevice.connect();
    }

    @NonNull
    public Promise<Boolean> disconnect(@NonNull MediaDevice mediaDevice) {
        return mediaDevice.disconnect();
    }
}
