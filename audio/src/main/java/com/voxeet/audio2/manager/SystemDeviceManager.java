package com.voxeet.audio2.manager;

import android.support.annotation.NonNull;

import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.NormalMediaDevice;
import com.voxeet.audio2.devices.SpeakerDevice;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.system.SystemAudioManager;
import com.voxeet.promise.Promise;

import java.util.ArrayList;
import java.util.List;

public class SystemDeviceManager implements IDeviceManager<MediaDevice> {

    private final SpeakerDevice speakerDevice;
    private final NormalMediaDevice normalMediaDevice;

    private SystemAudioManager systemAudioManager;

    private ArrayList<MediaDevice> list;

    public SystemDeviceManager(@NonNull SystemAudioManager systemAudioManager,
                               @NonNull IMediaDeviceConnectionState connectionState) {
        this.systemAudioManager = systemAudioManager;

        this.speakerDevice = new SpeakerDevice(systemAudioManager.audioManager(),
                connectionState,
                DeviceType.EXTERNAL_SPEAKER,
                "external_speaker");
        this.normalMediaDevice = new NormalMediaDevice(systemAudioManager.audioManager(),
                connectionState,
                DeviceType.NORMAL_MEDIA,
                "normal_media");

        this.list = new ArrayList<>();
        list.add(speakerDevice);
        list.add(normalMediaDevice);
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateDevices() {
        return enumerateTypedDevices();
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateTypedDevices() {
        return Promise.resolve(list);
    }

    @Override
    public boolean isWorking() {
        return true;
    }
}
