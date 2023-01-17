package com.voxeet.audio2.devices;

import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.MediaMode;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
import com.voxeet.promise.Promise;

public class NormalMediaDevice extends MediaDevice<DeviceType> {

    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager focusManager = new AudioFocusManager(AudioFocusMode.MEDIA);

    @NonNull
    private MediaMode mode;

    public NormalMediaDevice(
            @NonNull AudioManager audioManager,
            @NonNull IMediaDeviceConnectionState connectionState,
            @NonNull DeviceType deviceType,
            @NonNull String id) {
        super(connectionState, deviceType, id);

        this.audioManager = audioManager;
        mode = new MediaMode(audioManager, focusManager);
    }

    @NonNull
    @Override
    protected Promise<Boolean> connect(@NonNull LastConnectionStateType lastConnectionStateType) {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.CONNECTING, lastConnectionStateType);
            mode.apply(false).then(aBoolean -> {
                setConnectionState(ConnectionState.CONNECTED, lastConnectionStateType);
                solver.resolve(true);
            }).error(error -> {
                setConnectionState(ConnectionState.DISCONNECTED, lastConnectionStateType);
                solver.reject(error);
            });
        });
    }

    @NonNull
    @Override
    protected Promise<Boolean> disconnect(@NonNull LastConnectionStateType lastConnectionStateType) {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.DISCONNECTING, lastConnectionStateType);
            mode.apply(false).then(aBoolean -> {
                setConnectionState(ConnectionState.DISCONNECTED, lastConnectionStateType);
                solver.resolve(true);
            }).error(error -> {
                setConnectionState(ConnectionState.DISCONNECTED, lastConnectionStateType);
                solver.resolve(false);
            });
        });
    }
}
