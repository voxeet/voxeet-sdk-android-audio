package com.voxeet.audio.listeners;

public interface IMediaStateListener {
    void onSpeakerChanged(boolean isEnabled);

    void onHeadsetStateChange(boolean isPlugged);

    void onBluetoothHeadsetStateChange(boolean isPlugged);
}