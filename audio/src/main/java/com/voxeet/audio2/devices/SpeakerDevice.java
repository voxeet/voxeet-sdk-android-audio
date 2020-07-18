package com.voxeet.audio2.devices;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.ThenVoid;

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
            speakerMode.apply(true).then(aBoolean -> {
                Log.d(SpeakerDevice.class.getSimpleName(), "error");
                setConnectionState(ConnectionState.CONNECTED);
                solver.resolve(true);
            }).error(error -> {
                Log.d(SpeakerDevice.class.getSimpleName(), "error");
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.reject(error);
            });
        });
    }

    @NonNull
    @Override
    protected Promise<Boolean> disconnect() {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.DISCONNECTING);
            speakerMode.apply(false).then(aBoolean -> {
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
            }).error(error -> {
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(false);
            });
        });
    }
}
