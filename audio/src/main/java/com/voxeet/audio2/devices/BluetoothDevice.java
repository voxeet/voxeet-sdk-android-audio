package com.voxeet.audio2.devices;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
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
    private static Handler handler = new Handler(Looper.getMainLooper());

    private final __Call<BluetoothDeviceConnectionWrapper> waitForSolver;
    private final __Call<BluetoothDevice> setActive;
    private final BluetoothHeadsetDeviceManager bluetoothHeadsetDeviceManager;
    private final __Call<BluetoothDevice> onDisconnected;
    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager audioFocusManagerCall = new AudioFocusManager(AudioFocusMode.CALL);

    private NormalMode normalMode;
    @NonNull
    private BluetoothMode mode;
    private Cancellable runnable;

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
        normalMode = new NormalMode(audioManager, audioFocusManagerCall);
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
                postTimeout(second);
                Log.d(id(), "call for apply connect... sco:=" + bluetoothHeadsetDeviceManager.isSCOOn());
                if (!bluetoothHeadsetDeviceManager.isSCOOn()) {
                    waitForSolver.apply(new BluetoothDeviceConnectionWrapper(second, true));
                    mode.apply(false);
                    normalMode.apply(false);
                    audioManager.setBluetoothScoOn(true);
                    audioManager.startBluetoothSco();
                } else {
                    Log.d(id(), "sco already started... resolving");
                    cancelRunnable();
                    second.resolve(true);
                }
            }).then(b -> {
                cancelRunnable();
                Log.d(id(), "connect done");
                setConnectionState(ConnectionState.CONNECTED);
                solver.resolve(true);
            }).error(err -> {
                cancelRunnable();
                Log.d(id(), "connect done with error");
                setConnectionState(ConnectionState.DISCONNECTED);
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
                postTimeout(second);
                if (!ConnectionState.DISCONNECTED.equals(platformConnectionState)) {
                    Log.d(id(), "call for apply disconnect... sco:=" + bluetoothHeadsetDeviceManager.isSCOOn());
                    if (bluetoothHeadsetDeviceManager.isSCOOn()) {
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
                cancelRunnable();
                Log.d(id(), "disconnect done");
                mode.abandonAudioFocus();
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
                onDisconnected.apply(BluetoothDevice.this);
            }).error(err -> {
                cancelRunnable();
                Log.d(id(), "disconnect done with error");
                mode.abandonAudioFocus();
                setConnectionState(ConnectionState.DISCONNECTED);
                solver.resolve(true);
            });
        });
    }

    private void cancelRunnable() {
        if (null != runnable) {
            runnable.cancel = true;
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    private void postTimeout(Solver<Boolean> second) {
        runnable = new Cancellable(() -> {
            Log.d(id(), "oops.... timedout after 15s, should not happen. system error ?");
            second.resolve(false);
        });
        handler.postDelayed(runnable, 15 * 1000);
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

    private class Cancellable implements Runnable {
        public boolean cancel = false;
        private Runnable run;

        public Cancellable(Runnable run) {
            this.run = run;
        }

        @Override
        public void run() {
            if (!cancel) run.run();
        }
    }

}
