package com.voxeet.audio2.devices;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.MediaMode;
import com.voxeet.audio.mode.NormalMode;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;

public class NormalDevice extends MediaDevice<DeviceType> {

    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager audioFocusManagerCall = new AudioFocusManager(AudioFocusMode.CALL);
    private AudioFocusManager audioMediaFocusManagerCall = new AudioFocusManager(AudioFocusMode.MEDIA);

    @NonNull
    private NormalMode normalMode;
    private MediaMode mediaMode;

    public NormalDevice(
            @NonNull AudioManager audioManager,
            @NonNull IMediaDeviceConnectionState connectionState,
            @NonNull DeviceType deviceType,
            @NonNull String id) {
        super(connectionState, deviceType, id);

        this.audioManager = audioManager;
        normalMode = new NormalMode(audioManager, audioFocusManagerCall);
        mediaMode = new MediaMode(audioManager, audioMediaFocusManagerCall);
    }

    @NonNull
    @Override
    protected Promise<Boolean> connect() {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.CONNECTING);
            normalMode.apply(false).then(aBoolean -> {
                setConnectionState(ConnectionState.CONNECTED);
                solver.resolve(true);
            }).error(error -> {
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.reject(error);
            });
        });
    }

    @NonNull
    @Override
    protected Promise<Boolean> disconnect() {
        return new Promise<>(solver -> {
            if (ConnectionState.DISCONNECTED.equals(connectionState)) {
                solver.resolve(true);
                return;
            }
            setConnectionState(ConnectionState.DISCONNECTING);
            //normalMode.apply(false);
            normalMode.abandonAudioFocus().then((ThenPromise<Boolean, Boolean>) aBoolean -> mediaMode.apply(false)).then(o -> {
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
            }).error(solver::reject);
        });
    }

    @NonNull
    @Override
    public DeviceType deviceType() {
        if (MediaDeviceHelper.isWiredHeadsetConnected(audioManager) && ConnectionState.CONNECTED.equals(connectionState)) {
            return DeviceType.WIRED_HEADSET;
        }
        return super.deviceType();
    }
}
