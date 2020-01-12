package com.voxeet.audio2.devices;

import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.BluetoothMode;
import com.voxeet.audio.mode.MediaMode;
import com.voxeet.audio.mode.NormalMode;
import com.voxeet.audio.mode.WiredMode;
import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.manager.BluetoothHeadsetDeviceManager;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.PromiseExec;
import com.voxeet.promise.solve.PromiseSolver;
import com.voxeet.promise.solve.Solver;

public class BluetoothDevice extends MediaDevice<android.bluetooth.BluetoothDevice> {

    private final __Call<BluetoothDeviceConnectionWrapper> waitForSolver;
    private final __Call<BluetoothDevice> setActive;
    private final BluetoothHeadsetDeviceManager bluetoothHeadsetDeviceManager;
    private final __Call<BluetoothDevice> onDisconnected;
    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager audioFocusManagerCall = new AudioFocusManager(AudioFocusMode.CALL);

    @NonNull
    private BluetoothMode mode;

    public BluetoothDevice(
            @NonNull AudioManager audioManager,
            @NonNull IMediaDeviceConnectionState connectionState,
            @NonNull DeviceType deviceType,
            @NonNull BluetoothHeadsetDeviceManager bluetoothHeadsetDeviceManager,
            @NonNull android.bluetooth.BluetoothDevice bluetoothDevice,
            @NonNull __Call<PlatformDeviceConnectionWrapper> wrapper,
            @NonNull __Call<BluetoothDeviceConnectionWrapper> waitForSolver,
            @NonNull __Call<BluetoothDevice> setActive,
            @NonNull __Call<BluetoothDevice> onDisconnected) {
        super(connectionState, deviceType, bluetoothDevice.getAddress(), bluetoothDevice);

        this.bluetoothHeadsetDeviceManager = bluetoothHeadsetDeviceManager;
        this.setActive = setActive;
        this.waitForSolver = waitForSolver;
        this.audioManager = audioManager;
        this.onDisconnected = onDisconnected;
        mode = new BluetoothMode(audioManager, audioFocusManagerCall);
        wrapper.apply(BluetoothDevice.this::setPlatformConnectionState);
    }

    @NonNull
    @Override
    protected Promise<Boolean> connect() {
        return new Promise<>(solver -> {
            setActive.apply(BluetoothDevice.this);
            setConnectionState(ConnectionState.CONNECTING);
            new Promise<Boolean>(second -> {
                Log.d(id(), "call for apply connect...");
                if(!bluetoothHeadsetDeviceManager.isSCOOn()) {
                    waitForSolver.apply(new BluetoothDeviceConnectionWrapper(second, false));
                    mode.apply(false);
                    audioManager.setBluetoothScoOn(true);
                    audioManager.startBluetoothSco();
                } else {
                    second.resolve(true);
                }
            }).then(b -> {
                Log.d(id(), "connect done");
                setConnectionState(ConnectionState.CONNECTED);
                solver.resolve(true);
            }).error(err -> {
                Log.d(id(), "connect done with error");
                setConnectionState(ConnectionState.CONNECTED);
                solver.resolve(true);
            });
        });
    }

    @NonNull
    @Override
    protected Promise<Boolean> disconnect() {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.DISCONNECTING);
            new Promise<Boolean>(second -> {
                if (!ConnectionState.DISCONNECTED.equals(platformConnectionState)) {
                    Log.d(id(), "call for apply disconnect...");
                    if(bluetoothHeadsetDeviceManager.isSCOOn()) {
                        waitForSolver.apply(new BluetoothDeviceConnectionWrapper(second, false));
                        audioManager.setBluetoothScoOn(false);
                        audioManager.stopBluetoothSco();
                    } else {
                        second.resolve(true);
                    }
                } else {
                    second.resolve(true);
                }
            }).then(b -> {
                Log.d(id(), "disconnect done");
                mode.abandonAudioFocus();
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
                onDisconnected.apply(BluetoothDevice.this);
            }).error(err -> {
                Log.d(id(), "disconnect done with error");
                mode.abandonAudioFocus();
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
            });
        });
    }

    public boolean isConnected() {
        return mode.isConnected();
    }

    public void update(@NonNull android.bluetooth.BluetoothDevice device) {
        this.holder = device;
    }

    public android.bluetooth.BluetoothDevice bluetoothDevice() {
        return holder;
    }
}
