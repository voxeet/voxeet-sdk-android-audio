package com.voxeet.audio2.devices;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.promise.Promise;

public class SpeakerDevice extends MediaDevice<DeviceType> {

    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager audioFocusManagerCall = new AudioFocusManager(AudioFocusMode.CALL);

    @NonNull
    private SpeakerMode speakerMode;

    public SpeakerDevice(
            @NonNull AudioManager audioManager,
            @NonNull IMediaDeviceConnectionState connectionState,
            @NonNull DeviceType deviceType,
            @NonNull String id) {
        super(connectionState, deviceType, id);

        this.audioManager = audioManager;
        speakerMode = new SpeakerMode(audioManager, audioFocusManagerCall);
    }

    @NonNull
    @Override
    protected Promise<Boolean> connect() {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.CONNECTING);
            speakerMode.apply(true);
            setConnectionState(ConnectionState.CONNECTED);
            solver.resolve(true);
        });
    }

    @NonNull
    @Override
    protected Promise<Boolean> disconnect() {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.DISCONNECTING);
            speakerMode.apply(false);
            setConnectionState(ConnectionState.DISCONNECTED);
            solver.resolve(true);
        });
    }
}
