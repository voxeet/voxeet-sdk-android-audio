package com.voxeet.audio2.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
import com.voxeet.promise.Promise;

public interface AudioDeviceManagerProxy {

    @NonNull
    Promise<MediaDevice> current();

    @NonNull
    Promise<Boolean> connect(@NonNull MediaDevice mediaDevice, @NonNull LastConnectionStateType lastConnectionStateType);

    @NonNull
    Promise<Boolean> disconnect(@NonNull MediaDevice mediaDevice, @NonNull LastConnectionStateType lastConnectionStateType);
}
