package com.voxeet.audio2.manager;

import android.support.annotation.NonNull;

import com.voxeet.promise.Promise;

import java.util.List;

public interface IDeviceManager<TYPE> {
    @NonNull
    Promise<List<TYPE>> enumerateDevices();
}
