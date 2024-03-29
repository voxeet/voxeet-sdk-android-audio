package com.voxeet.audio2.devices;

import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
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
    protected Promise<Boolean> connect(@NonNull LastConnectionStateType lastConnectionStateType) {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.CONNECTING, lastConnectionStateType);
            speakerMode.apply(true).then(aBoolean -> {
                Log.d(SpeakerDevice.class.getSimpleName(), "done");
                setConnectionState(ConnectionState.CONNECTED, lastConnectionStateType);
                solver.resolve(true);
            }).error(error -> {
                Log.d(SpeakerDevice.class.getSimpleName(), "error");
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
            speakerMode.apply(false).then(aBoolean -> {
                setConnectionState(ConnectionState.DISCONNECTED, lastConnectionStateType);
                solver.resolve(true);
            }).error(error -> {
                setConnectionState(ConnectionState.DISCONNECTED, lastConnectionStateType);
                solver.resolve(false);
            });
        });
    }
}
