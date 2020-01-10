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
    public <TYPE> Promise<Boolean> connect(@NonNull MediaDevice<TYPE> mediaDevice) {
        return mediaDevice.connect();
    }

    @NonNull
    public <TYPE> Promise<Boolean> disconnect(@NonNull MediaDevice<TYPE> mediaDevice) {
        return mediaDevice.disconnect();
    }
}
