package com.voxeet.audio2.devices;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.focus.AudioFocusMode;
import com.voxeet.audio.mode.BluetoothMode;
import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
import com.voxeet.audio2.manager.BluetoothHeadsetDeviceManager;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.Solver;
import com.voxeet.promise.solve.ThenPromise;

public class BluetoothDevice extends MediaDevice<DeviceType> {
    private static Handler handler = new Handler(Looper.getMainLooper());

    private final __Call<BluetoothDeviceConnectionWrapper> waitForSolver;
    private final __Call<BluetoothDevice> setActive;
    private final BluetoothHeadsetDeviceManager bluetoothHeadsetDeviceManager;
    private android.bluetooth.BluetoothDevice bluetoothDeviceHolder;
    @NonNull
    private AudioManager audioManager;
    private AudioFocusManager audioFocusManagerCall = new AudioFocusManager(AudioFocusMode.CALL);

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
            @NonNull __Call<BluetoothDevice> setActive) {
        super(connectionState, deviceType, bluetoothDevice.getAddress(), deviceType, bluetoothDevice.getName());

        this.bluetoothHeadsetDeviceManager = bluetoothHeadsetDeviceManager;
        this.setActive = setActive;
        this.waitForSolver = waitForSolver;
        this.audioManager = audioManager;
        this.bluetoothDeviceHolder = bluetoothDevice;
        mode = new BluetoothMode(audioManager, audioFocusManagerCall);
        wrapper.apply(BluetoothDevice.this::setPlatformConnectionState);
    }

    @NonNull
    @Override
    protected Promise<Boolean> connect(@NonNull LastConnectionStateType lastConnectionStateType) {
        return new Promise<>(solver -> {
            setActive.apply(BluetoothDevice.this);
            setConnectionState(ConnectionState.CONNECTING, lastConnectionStateType);
            new Promise<Boolean>(second -> {
                postTimeout(second);
                Log.d(id(), "call for apply connect... sco:=" + bluetoothHeadsetDeviceManager.isSCOOn());
                // Apply modes no matter if SCO is already on or off - just a precautionary measures
                mode.apply(false)
                        .then(o -> {
                            Log.d(id(), "set bluetooth sco to true and start it");
                            audioManager.setBluetoothScoOn(true);
                            audioManager.startBluetoothSco();
                        })
                        .error(error -> {
                            //TODO manage ?
                        });
                // Wait for SCO ON only if it is OFF, otherwise BluetoothDeviceConnectionWrapper will not clear wrappers list
                if (!bluetoothHeadsetDeviceManager.isSCOOn()) {
                    waitForSolver.apply(new BluetoothDeviceConnectionWrapper(second, true));
                } else {
                    Log.d(id(), "sco already started... resolving");
                    cancelRunnable();
                    second.resolve(true);
                }
            }).then(result -> {
                cancelRunnable();
                Log.d(id(), "connect done result " + result);
                setConnectionState(ConnectionState.CONNECTED, lastConnectionStateType);
                solver.resolve(true);
            }).error(err -> {
                cancelRunnable();
                Log.d(id(), "connect done with error");
                cleanUpOnError(solver);
            });
        });
    }

    @NonNull
    @Override
    protected Promise<Boolean> disconnect(@NonNull LastConnectionStateType lastConnectionStateType) {
        return new Promise<>(solver -> {
            setConnectionState(ConnectionState.DISCONNECTING, lastConnectionStateType);
            new Promise<Boolean>(second -> {
                postTimeout(second);
                if (!ConnectionState.DISCONNECTED.equals(platformConnectionState)) {
                    Log.d(id(), "call for apply disconnect... sco:=" + bluetoothHeadsetDeviceManager.isSCOOn());
                    // Wait for SCO ON only if it is OFF, otherwise BluetoothDeviceConnectionWrapper will not clear wrappers list
                    if (bluetoothHeadsetDeviceManager.isSCOOn()) {
                        waitForSolver.apply(new BluetoothDeviceConnectionWrapper(second, false));
                    } else {
                        second.resolve(true);
                    }
                    // Apply modes no matter if SCO is already on or off - just a precautionary measures
                    Log.d(id(), "set bluetooth sco to false and stop it");
                    audioManager.setBluetoothScoOn(false);
                    audioManager.stopBluetoothSco();
                } else {
                    second.resolve(true);
                }
            }).then((ThenPromise<Boolean, Boolean>) aBoolean -> {
                cancelRunnable();
                Log.d(id(), "disconnect done");
                return mode.abandonAudioFocus();
            }).then(b -> {
                setConnectionState(ConnectionState.DISCONNECTED, lastConnectionStateType);
                solver.resolve(true);
            }).error(err -> {
                Log.d(id(), "disconnect done with error");
                cancelRunnable();
                cleanUpOnError(solver);
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

    private void cleanUpOnError(Solver<Boolean> solver) {
        final Runnable run = () -> {
            setConnectionState(ConnectionState.DISCONNECTED, LastConnectionStateType.PROGRAMMATIC);
            solver.resolve(true);
        };
        mode.abandonAudioFocus().then(aBoolean -> {
            run.run();
        }).error(error -> {
            error.printStackTrace();
            run.run();
        });
    }

    private void postTimeout(Solver<Boolean> second) {
        runnable = new Cancellable(() -> {
            Log.d(id(), "oops.... timedout after 8s, should not happen. system error ?");
            second.resolve(false);
        });
        handler.postDelayed(runnable, 8 * 1000);
    }

    public boolean isConnected() {
        return mode.isConnected();
    }

    public void update(@NonNull android.bluetooth.BluetoothDevice device) {
        this.bluetoothDeviceHolder = device;
    }

    public android.bluetooth.BluetoothDevice bluetoothDevice() {
        return bluetoothDeviceHolder;
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
