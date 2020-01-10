package com.voxeet.audio2.devices;

import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.voxeet.audio2.devices.description.ConnectionState;

import java.util.List;

public class MediaDeviceHelper {
    public static boolean isWiredHeadsetConnected(@NonNull AudioManager manager) {
        if (Build.VERSION.SDK_INT >= 23) {
            AudioDeviceInfo[] audioDevices = manager.getDevices(android.media.AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                switch (deviceInfo.getType()) {
                    case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                    case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                        return true;
                }
            }
        }
        return manager.isWiredHeadsetOn();
    }

    public static boolean hasConnected(List<MediaDevice> list) {
        if (null == list) return false;
        for (MediaDevice device : list) {
            if (ConnectionState.CONNECTED.equals(device.connectionState)) return true;
        }
        return false;
    }
}
