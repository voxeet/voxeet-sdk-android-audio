package com.voxeet.audio.mode;

import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.voxeet.audio.MediaDevice;
import com.voxeet.audio.focus.AudioFocusManager;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class WiredMode extends AbstractMode {

    public WiredMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, MediaDevice.ROUTE_HEADSET);
    }

    @Override
    public void apply(boolean speaker_state) {
        manager.setSpeakerphoneOn(false);
        manager.setMode(MODE_IN_COMMUNICATION);
        //forceVolumeControlStream(Constants.STREAM_VOICE_CALL);

        requestAudioFocus();
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(requestFocus);
        audioFocusManger.requestAudioFocus(manager, requestFocus);
    }

    @Override
    public boolean isConnected() {
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
}
