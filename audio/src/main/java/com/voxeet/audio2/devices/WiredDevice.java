package com.voxeet.audio2.devices;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.WiredMode;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.ThenVoid;

public class WiredDevice extends MediaDevice<DeviceType> {

    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager audioFocusManagerCall = new AudioFocusManager(AudioFocusMode.CALL);

    @NonNull
    private WiredMode mode;

    public WiredDevice(
            @NonNull AudioManager audioManager,
            @NonNull IMediaDeviceConnectionState connectionState,
            @NonNull DeviceType deviceType,
            @NonNull String id,
            @NonNull __Call<PlatformDeviceConnectionWrapper> afterBuild) {
        super(connectionState, deviceType, id);

        this.audioManager = audioManager;
        mode = new WiredMode(audioManager, audioFocusManagerCall);
        afterBuild.apply(connectionState1 -> WiredDevice.this.platformConnectionState = connectionState1);
    }

    @NonNull
    @Override
    protected Promise<Boolean> connect() {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.CONNECTING);
            mode.apply(false).then(aBoolean -> {
                setConnectionState(ConnectionState.CONNECTED);
                solver.resolve(true);
            }).error(error -> {
                error.printStackTrace();
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
            mode.apply(false).then(aBoolean -> {
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
            }).error(error -> {
                error.printStackTrace();
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(false);
            });
        });
    }

    public boolean isConnected() {
        return mode.isConnected();
    }
}
