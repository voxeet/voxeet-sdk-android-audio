package com.voxeet.audio2.manager;

import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio.utils.__Opt;
import com.voxeet.audio2.AudioDeviceManager;
import com.voxeet.audio2.devices.BluetoothDevice;
import com.voxeet.audio2.devices.BluetoothDeviceConnectionWrapper;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.PlatformDeviceConnectionWrapper;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.IMediaDeviceConnectionState;
import com.voxeet.audio2.manager.bluetooth.BluetoothAction;
import com.voxeet.audio2.manager.bluetooth.BluetoothDeviceReceiver;
import com.voxeet.audio2.manager.bluetooth.BluetoothDisconnectListener;
import com.voxeet.audio2.manager.bluetooth.BluetoothHeadsetServiceListener;
import com.voxeet.audio2.system.SystemAudioManager;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.promise.solve.ThenVoid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BluetoothHeadsetDeviceManager implements IDeviceManager<BluetoothDevice> {

    public static final String BLUETOOTH_CONNECT_EXCEPTION = "requestDevices: having security exception, did you prompt the user with BLUETOOTH_CONNECT permission on Android 12+?";

    private static final String TAG = BluetoothHeadsetDeviceManager.class.getSimpleName();
    private final __Call<List<BluetoothDevice>> connectivityUpdate;
    private final IMediaDeviceConnectionState connectionState;
    private final BluetoothDeviceReceiver bluetoothDeviceReceiver;
    private final Context context;

    @NonNull
    private final BluetoothDisconnectListener bluetoothDisconnectListener;

    private BluetoothDevice active;
    private Runnable runnable;
    private Handler handler;
    private final AudioDeviceManager audioDeviceManager;

    private SystemAudioManager systemAudioManager;
    private BluetoothHeadsetServiceListener bluetoothHeadsetServiceListener;

    private ArrayList<BluetoothDevice> list;

    private HashMap<String, PlatformDeviceConnectionWrapper> wrappers = new HashMap<>();
    private HashMap<String, BluetoothDeviceConnectionWrapper> waitingConnectivity = new HashMap<>();
    private boolean sco_connected = false;

    public BluetoothHeadsetDeviceManager(
            @NonNull Context context,
            @NonNull AudioDeviceManager audioDeviceManager,
            @NonNull SystemAudioManager systemAudioManager,
            @NonNull __Call<List<BluetoothDevice>> connectivityUpdate,
            @NonNull IMediaDeviceConnectionState connectionState) {
        this.context = context;
        this.audioDeviceManager = audioDeviceManager;
        this.systemAudioManager = systemAudioManager;
        this.connectivityUpdate = connectivityUpdate;
        this.connectionState = connectionState;

        this.list = new ArrayList<>();
        bluetoothHeadsetServiceListener = new BluetoothHeadsetServiceListener(this::onNewBluetoothServiceConnectivity);
        bluetoothDeviceReceiver = new BluetoothDeviceReceiver(this::onNewBluetoothDeviceState);
        bluetoothHeadsetServiceListener.connect(context);
        bluetoothDeviceReceiver.connect(context);
        handler = null;
        runnable = () -> {
            try {
                RequestDevices requestDevices = requestDevices();
                if (requestDevices.hasNew) {
                    connectivityUpdate.apply(requestDevices.list);
                }

                android.bluetooth.BluetoothDevice device = bluetoothHeadsetServiceListener.getActiveDevice();
                updateActiveDevice(device);
            } catch (Exception exception) {
                Log.e(TAG, "Exception in handler", exception);
            }
            if (null != handler) handler.postDelayed(runnable, 5000);
        };

        bluetoothDisconnectListener = new BluetoothDisconnectListener(context, this::closeBluetoothDevices);
    }

    public boolean canFetchAndSetActiveDevices() {
        return bluetoothHeadsetServiceListener.canFetchAndSetActiveDevices();
    }

    public boolean isConnected() {
        return false;
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateDevices() {
        return new Promise<>(solver -> solver.resolve(new ArrayList<>(devices())));
    }

    @NonNull
    @Override
    public Promise<List<BluetoothDevice>> enumerateTypedDevices() {
        return new Promise<>(solver -> solver.resolve(devices()));
    }

    @Override
    public boolean isWorking() {
        return bluetoothHeadsetServiceListener.isConnected();
    }

    public BluetoothDevice active() {
        return active;
    }

    private void onNewBluetoothServiceConnectivity(@NonNull BluetoothHeadsetServiceListener listener) {
        Log.d(TAG, "onNewBluetoothServiceConnectivity :: " + listener.isConnected());
        connectivityUpdate.apply(devices());
        if (listener.isConnected()) {
            if (null == handler) {
                handler = new Handler(Looper.getMainLooper());
                handler.post(runnable);
            }
        } else {
            if (null != handler) {
                handler.removeCallbacks(runnable);
                handler = null;
            }
        }
    }

    private void onNewBluetoothDeviceState(@NonNull BluetoothAction bluetoothAction) {
        Log.d(TAG, "onNewBluetoothDeviceState " + bluetoothAction);
        BluetoothDevice device = matching(list, bluetoothAction.device);

        BluetoothDeviceConnectionWrapper waitingCall = __Opt.of(device).then(d -> waitingConnectivity.get(d.id())).orNull();
        if (BluetoothAction.Action.DEVICE_DISCONNECTED.equals(bluetoothAction.action)) {
            Log.d(TAG, "the device is being disconnected...");
            Log.d(TAG, "exists ? " + device);

            if (null != device) {
                switch (__Opt.of(device.connectionState()).or(ConnectionState.DISCONNECTED)) {
                    case CONNECTED:
                    case CONNECTING:
                        Log.d(TAG, "oops... we need to stop this !");
                        //audioDeviceManager.disconnect(device);
                        break;
                    case DISCONNECTING:
                    case DISCONNECTED:
                    default:
                        Log.d(TAG, "state is already shutting down...");
                }
            } else {
                Log.d(TAG, "the corresponding device in list does not exists");
            }
        }

        Set<Map.Entry<String, BluetoothDeviceConnectionWrapper>> set = waitingConnectivity.entrySet();

        Log.d("BluetoothHeadsetdeviceManager", "" + bluetoothAction.action);
        switch (bluetoothAction.action) {
            case SCO_AUDIO_CONNECTED:
                sco_connected = true;
                for (Map.Entry<String, BluetoothDeviceConnectionWrapper> entry : set) {
                    Log.d(TAG, "having awaiting sco on... resolving");
                    if (!entry.getValue().connect) {
                        Promise.reject(entry.getValue().solver, new IllegalStateException("oops"));
                    } else {
                        entry.getValue().solver.resolve(true);
                    }
                }
                waitingConnectivity.clear();
                break;
            case SCO_AUDIO_DISCONNECTED:
                sco_connected = false;
                for (Map.Entry<String, BluetoothDeviceConnectionWrapper> entry : set) {
                    Log.d(TAG, "having awaiting sco off... resolving");
                    if (entry.getValue().connect) {
                        Promise.reject(entry.getValue().solver, new IllegalStateException("oops"));
                    } else {
                        entry.getValue().solver.resolve(true);
                    }
                }
                waitingConnectivity.clear();
        }

        String id = __Opt.of(bluetoothAction.device).then(android.bluetooth.BluetoothDevice::getAddress).or("");
        PlatformDeviceConnectionWrapper wrapper = wrappers.get(id);
        Log.d(TAG, "firing up to " + wrapper + " the event " + bluetoothAction.action);
        if (null != wrapper) {
            switch (bluetoothAction.action) {
                case DEVICE_ACTIVE_CHANGED:
                    if (active != null && device == null) {
                        Log.d(TAG, "switch to disconnected device ... disconnect");
                        audioDeviceManager.disconnect(active)
                                .then((ThenVoid<Boolean>) done -> Log.d(TAG, "disconnect done for this device"))
                                .error(Throwable::printStackTrace);
                    }
                    active = device;
                    break;
                case DEVICE_CONNECTED:
                    wrapper.setPlatformConnectionState(ConnectionState.CONNECTED);
                    break;
                case DEVICE_DISCONNECTED:
                    wrapper.setPlatformConnectionState(ConnectionState.DISCONNECTED);
                    if (null != device) {
                        audioDeviceManager.current()
                                .then((ThenPromise<MediaDevice, Boolean>) mediaDevice -> {
                                    if (null == mediaDevice) return Promise.resolve(true);
                                    if (mediaDevice.id().equals(device.id()))
                                        return audioDeviceManager.disconnect(device);
                                    return Promise.resolve(true);
                                })
                                .then(result -> {
                                    Log.d(TAG, "disconnecting the connect device because it was the main one");
                                }).error(Throwable::printStackTrace);
                    }
                    break;
                default:
            }
        }

        connectivityUpdate.apply(devices());
    }

    private List<BluetoothDevice> devices() {
        return requestDevices().list;
    }

    private RequestDevices requestDevices() {
        boolean hasNew = false;
        BluetoothHeadset headset = bluetoothHeadsetServiceListener.bluetoothHeadset();
        List<android.bluetooth.BluetoothDevice> devices = new ArrayList<>();
        try {
            devices = __Opt.of(headset).then(BluetoothHeadset::getConnectedDevices).or(new ArrayList<>());
        } catch (SecurityException exception) {
            Log.e(TAG, BLUETOOTH_CONNECT_EXCEPTION, exception);
        }

        for (android.bluetooth.BluetoothDevice device : devices) {
            BluetoothDevice in_list = matching(list, device);
            if (null != in_list) {
                in_list.update(device);
            } else {
                hasNew = true;
                in_list = new BluetoothDevice(systemAudioManager.audioManager(),
                        connectionState, DeviceType.BLUETOOTH,
                        this,
                        device,
                        wrapper -> wrappers.put(device.getAddress(), wrapper),
                        wrapper -> waitingConnectivity.put(device.getAddress(), wrapper),
                        this::setActiveDevice,
                        this::onDisconnected);
                list.add(in_list);
            }
        }

        return new RequestDevices(hasNew, list);
    }

    private void onDisconnected(@NonNull BluetoothDevice bluetoothDevice) {

    }

    private void setActiveDevice(@NonNull BluetoothDevice device) {
        if (null != bluetoothHeadsetServiceListener) {
            Log.d(TAG, "setActive device to " + device);
            bluetoothHeadsetServiceListener.setActiveDevice(device.bluetoothDevice());
        }
    }

    @Nullable
    private BluetoothDevice matching(List<BluetoothDevice> list, @Nullable android.bluetooth.BluetoothDevice device) {
        if (null == device) return null;

        for (BluetoothDevice in_list : list) {
            if (__Opt.of(in_list.id()).or("").equals(device.getAddress())) {
                return in_list;
            }
        }
        return null;
    }

    public boolean isSCOOn() {
        if (bluetoothDeviceReceiver.isKnownSCO(context)) return true;
        return sco_connected;
    }

    private class RequestDevices {
        public boolean hasNew = false;
        public List<BluetoothDevice> list;

        public RequestDevices(boolean hasNew, List<BluetoothDevice> list) {
            this.hasNew = hasNew;
            this.list = list;
        }
    }

    private void updateActiveDevice(@Nullable android.bluetooth.BluetoothDevice device) {
        if (null != device) {
            BluetoothHeadsetDeviceManager.this.active = matching(list, device);
        } else {
            BluetoothHeadsetDeviceManager.this.active = null;
        }
    }

    private void closeBluetoothDevices(@Nullable Boolean /*unused*/ disconnect) {
        List<BluetoothDevice> devices = devices();
        for (BluetoothDevice device : devices) {
            if (null == device) continue;

            boolean need_disconnection = false;
            switch (device.connectionState()) {
                case CONNECTED:
                case CONNECTING:
                    need_disconnection = true;
                    break;
                default:
            }

            if (need_disconnection) {
                try {
                    audioDeviceManager.disconnect(device).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
