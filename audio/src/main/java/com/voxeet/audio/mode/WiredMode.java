package com.voxeet.audio.mode;

import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.voxeet.audio.AudioRoute;
import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.utils.Constants;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;

public class WiredMode extends AbstractMode {

    public WiredMode(@NonNull AudioManager manager, @NonNull AudioFocusManager audioFocusManager) {
        super(manager, audioFocusManager, AudioRoute.ROUTE_HEADSET);
    }

    @Override
    public void apply(boolean speaker_state) {
        manager.setSpeakerphoneOn(false);
        manager.setMode(MODE_IN_COMMUNICATION);
        forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
    }

    @Override
    public void requestAudioFocus() {
        forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        audioFocusManger.requestAudioFocus(manager);
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
