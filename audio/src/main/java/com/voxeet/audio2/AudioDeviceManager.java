package com.voxeet.audio2;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio.utils.__Opt;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.WiredDevice;
import com.voxeet.audio2.devices.description.ConnectionState;
import com.voxeet.audio2.devices.description.ConnectionStatesEvent;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
import com.voxeet.audio2.manager.AudioDeviceManagerProxy;
import com.voxeet.audio2.manager.BluetoothHeadsetDeviceManager;
import com.voxeet.audio2.manager.ConnectScheduler;
import com.voxeet.audio2.manager.IDeviceManager;
import com.voxeet.audio2.manager.SystemDeviceManager;
import com.voxeet.audio2.manager.WiredHeadsetDeviceManager;
import com.voxeet.audio2.system.SystemAudioManager;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenVoid;

import java.util.ArrayList;
import java.util.List;

public class AudioDeviceManager implements IDeviceManager<MediaDevice> {

    private __Call<Promise<List<MediaDevice>>> update;
    private ConnectScheduler connectScheduler;
    private static final String TAG = AudioDeviceManager.class.getSimpleName();
    private SystemAudioManager systemAudioManager;
    private SystemDeviceManager systemDeviceManager;
    private WiredHeadsetDeviceManager wiredHeadsetDeviceManager;
    private BluetoothHeadsetDeviceManager bluetoothHeadsetDeviceManager;

    private AudioDeviceManagerProxy audioDeviceManagerProxy = new AudioDeviceManagerProxy() {
        @NonNull
        @Override
        public Promise<MediaDevice> current() {
            return AudioDeviceManager.this.current();
        }

        @NonNull
        @Override
        public Promise<Boolean> connect(@NonNull MediaDevice mediaDevice, @NonNull LastConnectionStateType lastConnectionStateType) {
            return new Promise<>((solver) -> connectScheduler.waitFor()
                    .then((ThenVoid<ConnectScheduler>) connectScheduler -> connectScheduler.pushConnect(mediaDevice, lastConnectionStateType, solver))
                    .error(Throwable::printStackTrace));
        }

        @NonNull
        @Override
        public Promise<Boolean> disconnect(@NonNull MediaDevice mediaDevice, @NonNull LastConnectionStateType lastConnectionStateType) {
            return new Promise<>((solver) -> connectScheduler.waitFor()
                    .then((ThenVoid<ConnectScheduler>) connectScheduler -> connectScheduler.pushDisconnect(mediaDevice, lastConnectionStateType, solver))
                    .error(Throwable::printStackTrace));
        }
    };

    private AudioDeviceManager() {

    }

    public AudioDeviceManager(@NonNull Context context,
                              @NonNull __Call<Promise<List<MediaDevice>>> update) {
        systemAudioManager = new SystemAudioManager(context);
        this.update = update;

        systemDeviceManager = new SystemDeviceManager(systemAudioManager, this::onConnectionState);
        wiredHeadsetDeviceManager = new WiredHeadsetDeviceManager(context, systemAudioManager, this::onWiredHeadsetDeviceConnected, this::onConnectionState);
        bluetoothHeadsetDeviceManager = new BluetoothHeadsetDeviceManager(context, audioDeviceManagerProxy, systemAudioManager, (r) -> this.sendUpdate(), this::onConnectionState);
        connectScheduler = new ConnectScheduler();
    }

    @NonNull
    public SystemAudioManager systemAudioManager() {
        return systemAudioManager;
    }

    @NonNull
    public SystemDeviceManager systemDeviceManager() {
        return systemDeviceManager;
    }

    @NonNull
    public BluetoothHeadsetDeviceManager bluetoothHeadsetDeviceManager() {
        return bluetoothHeadsetDeviceManager;
    }

    public void dump(@NonNull List<MediaDevice> list) {
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Log.d(TAG, "enumeraDevices");
        for (MediaDevice device : list) {
            Log.d(TAG, "device " + device.id() + " " + device.deviceType()  + " " + device.connectionState() + " " + device.lastConnectionStateType() + " " + device.platformConnectionState());
        }
        Log.d(TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateDevices() {
        return enumerateTypedDevices();
    }

    @NonNull
    @Override
    public Promise<List<MediaDevice>> enumerateTypedDevices() {
        return new Promise<>((resolve, reject) -> Promise.all(
                systemDeviceManager.enumerateDevices(),
                wiredHeadsetDeviceManager.enumerateDevices(),
                bluetoothHeadsetDeviceManager.enumerateDevices()
        ).then(result -> {
            List<MediaDevice> list = new ArrayList<>();
            for (List<MediaDevice> mediaDevices : __Opt.of(result).or(new ArrayList<>())) {
                if (null != mediaDevices) list.addAll(mediaDevices);
            }

            dump(list);

            resolve.call(list);
        }).error(reject::call));
    }

    @Override
    public boolean isWorking() {
        return true;
    }

    @NonNull
    public Promise<List<MediaDevice>> enumerateDevices(@NonNull DeviceType deviceType) {
        return new Promise<>(solver -> enumerateDevices().then(devices -> {
            solver.resolve(filter(devices, deviceType));
        }).error(solver::reject));
    }


    @NonNull
    public List<MediaDevice> filter(@NonNull List<MediaDevice> devices, @NonNull DeviceType deviceType) {
        List<MediaDevice> result = new ArrayList<>();
        for (MediaDevice device : __Opt.of(devices).or(new ArrayList<>())) {
            if (deviceType.equals(device.deviceType())) result.add(device);
        }
        return result;
    }

    public boolean isLockedForConnectivity() {
        return connectScheduler.isLocked();
    }

    @NonNull
    public Promise<Boolean> connect(@NonNull MediaDevice mediaDevice) {
        return audioDeviceManagerProxy.connect(mediaDevice, LastConnectionStateType.PROGRAMMATIC);
    }

    @NonNull
    public Promise<Boolean> disconnect(@NonNull MediaDevice mediaDevice) {
        return audioDeviceManagerProxy.disconnect(mediaDevice, LastConnectionStateType.PROGRAMMATIC);
    }

    /**
     * Gets the current device which this library is connected to. The device can be null
     *
     * @return the promise to resolve
     */
    @Deprecated
    @NonNull
    public Promise<MediaDevice> current() {
        return new Promise<>(solver -> connectScheduler.waitFor().then(result -> {
            solver.resolve(connectScheduler.current());
        }).error(solver::reject));
    }

    /**
     * Will try to check for headset devices connected. This behavior aligns with https://developer.android.com/guide/topics/media-apps/volume-and-earphones
     * where it's indicated that wired headset will take precedence over the current route
     * TODO : check for an event indicating that the route changes ? Not without any "what if?"
     *
     * @param mediaDevices the list of media devices which are updated, normally called by the headset manager
     */
    private void onWiredHeadsetDeviceConnected(@NonNull List<MediaDevice> mediaDevices) {
        MediaDevice headset = null;


        for (MediaDevice in_list : mediaDevices) {
            if (null == in_list) continue;

            if (in_list instanceof WiredDevice) {
                headset = in_list;
            }
        }

        if (null == headset) return;

        boolean platformConnected = ConnectionState.CONNECTED.equals(headset.platformConnectionState());

        Promise<Boolean> promise;
        if (platformConnected) {
            Log.d(TAG, "onWiredDeviceConnected ? connected : will attempt to connect (forced)");
            MediaDevice finalHeadset = headset;
            promise = new Promise<>(solver -> enumerateDevices().then(devices -> {
                List<MediaDevice> current_attemps = new ArrayList<>();

                for (MediaDevice device : devices) {
                    boolean connected = ConnectionState.CONNECTED.equals(device.connectionState());
                    boolean connecting = ConnectionState.CONNECTING.equals(device.connectionState());
                    boolean media = DeviceType.NORMAL_MEDIA.equals(device.deviceType());

                    //wired_headset and internal_speaker are merged in new versions
                    boolean wired_headset = DeviceType.WIRED_HEADSET.equals(device.deviceType());
                    boolean internal_speaker = DeviceType.INTERNAL_SPEAKER.equals(device.deviceType());

                    Log.d(TAG, device.id() + " " + device.deviceType() + " " + connected + " " + connecting + " " + media + " " + wired_headset + " " + internal_speaker);
                    if (!(media || wired_headset || internal_speaker) && (connected || connecting)) {
                        current_attemps.add(device);
                    }
                }

                if (current_attemps.size() > 0) {
                    Log.d(TAG, "some in-call devices where already pending, we are then connecting the wired headset");
                    solver.resolve(audioDeviceManagerProxy.connect(finalHeadset, LastConnectionStateType.SYSTEM));
                } else {
                    Log.d(TAG, "no devices where active, we skip connecting to wire, we are in normal mode, WARNING : it currently does not check the queue, please act accordingly");
                    solver.resolve(true);
                }
            }).error(error -> solver.resolve(false)));
        } else {
            Log.d(TAG, "onWiredDeviceConnected ? disconnected : will disconnect");
            promise = audioDeviceManagerProxy.disconnect(headset, LastConnectionStateType.SYSTEM);
        }

        promise.then(aBoolean -> {
            Log.d(TAG, "onWiredDeviceConnected :: connection change result " + aBoolean);
            //sending device update
            sendUpdate();
        }).error(error -> Log.e(TAG, "onWiredDeviceConnected :: connection change result with error", error));
    }

    private void onConnectionState(@NonNull ConnectionStatesEvent holder) {
        sendUpdate();
    }

    public boolean isWiredConnected() {
        return wiredHeadsetDeviceManager.isConnected();
    }

    /**
     * Send a device update to the registered clients
     * <p>
     * TODO improve the sendUpdate method by checking the previous computation state of all devices
     * and with the new one to avoid possibly sending twice+ the same event
     */
    private void sendUpdate() {
        this.update.apply(enumerateDevices());
    }
}
