package com.voxeet.audio2.devices;

import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.MediaMode;
import com.voxeet.audio.mode.NormalMode;
import com.voxeet.audio.mode.WiredMode;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;

public class WiredDevice extends MediaDevice<DeviceType> {

    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager audioFocusManagerCall = new AudioFocusManager(AudioFocusMode.CALL);
    private AudioFocusManager audioMediaFocusManagerCall = new AudioFocusManager(AudioFocusMode.MEDIA);

    @NonNull
    private NormalMode normalMode;
    private MediaMode mediaMode;

    @NonNull
    private WiredMode mode;


    private boolean isWiredPlugged = false;

    public WiredDevice(
            @NonNull AudioManager audioManager,
            @NonNull IMediaDeviceConnectionState connectionState,
            @NonNull DeviceType deviceType,
            @NonNull String id,
            @NonNull __Call<PlatformDeviceConnectionWrapper> afterBuild) {
        super(connectionState, deviceType, id);

        this.audioManager = audioManager;
        normalMode = new NormalMode(audioManager, audioFocusManagerCall);
        mediaMode = new MediaMode(audioManager, audioMediaFocusManagerCall);
        mode = new WiredMode(audioManager, audioFocusManagerCall);
        setWiredMode(mode.isConnected());
        afterBuild.apply(connectionState1 -> isWiredPlugged = ConnectionState.CONNECTED.equals(connectionState1));

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
            mode.apply(false).then((ThenPromise<Boolean, Boolean>) aBoolean -> {
                return normalMode.abandonAudioFocus();
            }).then(aBoolean -> {
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
            }).error(error -> {
                error.printStackTrace();
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(false);
            });
        });
    }

    @NonNull
    @Override
    public DeviceType deviceType() {
        return isWiredMode() ? DeviceType.WIRED_HEADSET : super.deviceType();
    }

    private boolean isWiredMode() {
        return isWiredPlugged || mode.isConnected();
    }

    public boolean isConnected() {
        return mode.isConnected();
    }

    public void setWiredMode(boolean plugged) {
        isWiredPlugged = plugged;
    }
}
