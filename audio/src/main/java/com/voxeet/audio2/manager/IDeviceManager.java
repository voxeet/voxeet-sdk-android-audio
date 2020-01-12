package com.voxeet.audio2.manager;

import android.support.annotation.NonNull;

import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.promise.Promise;

import java.util.List;

public interface IDeviceManager<TYPE extends MediaDevice> {
    @NonNull
    Promise<List<MediaDevice>> enumerateDevices();

    @NonNull
    Promise<List<TYPE>> enumerateTypedDevices();

    boolean isWorking();
}
