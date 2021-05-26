package com.voxeet.audio2.manager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.voxeet.audio.machines.WiredInformation;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.PlatformDeviceConnectionWrapper;
import com.voxeet.audio2.devices.WiredDevice;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.receiver.WiredHeadsetStateReceiver;
import com.voxeet.audio2.system.SystemAudioManager;
import com.voxeet.promise.Promise;

import java.util.ArrayList;
import java.util.List;

public class WiredHeadsetDeviceManager implements IDeviceManager<MediaDevice> {

    private final WiredDevice device;
    private final __Call<List<MediaDevice>> connectivityUpdate;

    private PlatformDeviceConnectionWrapper devicePlatformDeviceConnectionWrapper = (s) -> {
    };

    private SystemAudioManager systemAudioManager;
    private WiredHeadsetStateReceiver wiredHeadsetStateReceiver;

    private ArrayList<MediaDevice> list;

    public WiredHeadsetDeviceManager(
            @NonNull Context context,
            @NonNull SystemAudioManager systemAudioManager,
            @NonNull __Call<List<MediaDevice>> connectivityUpdate,
            @NonNull IMediaDeviceConnectionState connectionState) {
        this.systemAudioManager = systemAudioManager;
        this.connectivityUpdate = connectivityUpdate;

        this.device = new WiredDevice(systemAudioManager.audioManager(),
                connectionState,
                DeviceType.WIRED_HEADSET,
                "wired_headset",
                wrapper -> devicePlatformDeviceConnectionWrapper = wrapper);

        this.list = new ArrayList<>();
        list.add(device);
        wiredHeadsetStateReceiver = new WiredHeadsetStateReceiver(this::onNewWiredInformation);
        context.registerReceiver(wiredHeadsetStateReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    public boolean isConnected() {
        return device.isConnected();
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateDevices() {
        return Promise.resolve(list);
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateTypedDevices() {
        return enumerateDevices();
    }

    @Override
    public boolean isWorking() {
        return true;
    }

    private void onNewWiredInformation(@NonNull WiredInformation newWiredInformation) {
        if (newWiredInformation.isPlugged()) {
            devicePlatformDeviceConnectionWrapper.setPlatformConnectionState(ConnectionState.CONNECTED);
        } else {
            devicePlatformDeviceConnectionWrapper.setPlatformConnectionState(ConnectionState.DISCONNECTED);
        }
        connectivityUpdate.apply(list);
    }
}
