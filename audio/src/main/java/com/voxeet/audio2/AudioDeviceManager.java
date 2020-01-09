package com.voxeet.audio2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.manager.ConnectScheduler;
import com.voxeet.audio2.manager.IDeviceManager;
import com.voxeet.audio2.manager.SystemDeviceManager;
import com.voxeet.audio2.system.SystemAudioManager;
import com.voxeet.promise.Promise;

import java.util.ArrayList;
import java.util.List;

public final class AudioDeviceManager implements IDeviceManager<MediaDevice> {

    private ConnectScheduler connectScheduler;
    private static final String TAG = AudioDeviceManager.class.getSimpleName();
    private SystemAudioManager systemAudioManager;
    private SystemDeviceManager systemDeviceManager;

    private AudioDeviceManager() {

    }

    public AudioDeviceManager(@NonNull Context context) {
        systemAudioManager = new SystemAudioManager(context);
        systemDeviceManager = new SystemDeviceManager(systemAudioManager, this::onConnectionState);
        connectScheduler = new ConnectScheduler();
    }

    @NonNull
    public SystemAudioManager systemAudioManager() {
        return systemAudioManager;
    }

    @NonNull
    public SystemDeviceManager systemDeviceManager() {
        return systemDeviceManager;
    }

    public void dump(@NonNull List<MediaDevice> list) {
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Log.d(TAG, "enumeraDevices");
        for (MediaDevice device : list) {
            Log.d(TAG, "> > > > > > > > > > > > > >");
            Log.d(TAG, "device " + device.id() + " " + device.connectionState());
            Log.d(TAG, "< < < < < < < < < < < < < <");
        }
        Log.d(TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateDevices() {
        return new Promise<>((resolve, reject) -> Promise.all(systemDeviceManager.enumerateDevices())
                .then((result, solver) -> {
                    if (null == result) {
                        resolve.call(new ArrayList<>());
                        return;
                    }
                    List<MediaDevice> list = new ArrayList<>();
                    for (List<MediaDevice> mediaDevices : result) {
                        if (null != mediaDevices) list.addAll(mediaDevices);
                    }

                    dump(list);

                    resolve.call(list);
                })
                .error(reject::call));
    }

    @NonNull
    public Promise<List<MediaDevice>> enumerateDevices(@NonNull DeviceType deviceType) {
        return new Promise<>(solver -> enumerateDevices().then(devices -> {
            if (null == devices) devices = new ArrayList<>();
            List<MediaDevice> result = new ArrayList<>();
            for (MediaDevice device : devices) {
                if (deviceType.equals(device.deviceType())) result.add(device);
            }
            solver.resolve(result);
        }).error(solver::reject));
    }

    @NonNull
    public Promise<Boolean> connect(MediaDevice mediaDevice) {
        return new Promise<>((solver) -> connectScheduler.pushConnect(mediaDevice, solver));
    }

    @NonNull
    public Promise<Boolean> disconnect(MediaDevice mediaDevice) {
        return new Promise<>((solver) -> connectScheduler.pushDisconnect(mediaDevice, solver));
    }

    @Nullable
    public MediaDevice current() {
        return connectScheduler.current();
    }

    private void onConnectionState(@NonNull MediaDevice mediaDevice,
                                   @NonNull ConnectionState connectionState) {
        switch (connectionState) {
            case DISCONNECTED:
            case DISCONNECTING:
            case CONNECTING:
            case CONNECTED:
        }
    }
}
