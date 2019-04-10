package com.voxeet.audio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Window;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.listeners.IAudioRouteListener;
import com.voxeet.audio.listeners.IMediaStateListener;
import com.voxeet.audio.listeners.ListenerHolder;
import com.voxeet.audio.machines.BluetoothHeadsetMachine;
import com.voxeet.audio.machines.WiredHeadsetMachine;
import com.voxeet.audio.machines.WiredInformation;
import com.voxeet.audio.mode.BluetoothMode;
import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.audio.mode.WiredMode;
import com.voxeet.audio.utils.Constants;
import com.voxeet.audio.utils.Invoke;

import java.util.ArrayList;
import java.util.List;

/**
 * Control and Manager the Audio states of the media
 */

public class AudioManager {
    private static final String TAG = AudioManager.class.getSimpleName();
    private WiredHeadsetMachine mWiredMachine;
    private BluetoothHeadsetMachine mBluetoothMachine;
    private Context mContext;

    private SpeakerMode speakerMode;
    private WiredMode wiredMode;
    private BluetoothMode bluetoothMode;

    private android.media.AudioManager mServiceAudioManager;

    private HeadsetStateReceiver mHeadsetStateReceiver;

    private BluetoothAdapter mBluetoothAdapter;

    private AudioRoute mOutputRoute = AudioRoute.ROUTE_PHONE;
    private ListenerHolder<IMediaStateListener> mMediaStateListeners = new ListenerHolder<>();

    private ListenerHolder<IAudioRouteListener> mAudioRouteListeners = new ListenerHolder<>(new ListenerHolder.Callback<IAudioRouteListener>() {
        @Override
        public void apply(IAudioRouteListener listener) {
            try {
                listener.onAudioRouteChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    private AudioFocusManager manager;
    private boolean enabled;

    public void notifyAudioRoute() {
        mAudioRouteListeners.notif();
    }

    public void requestAudioFocus() {
        forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        manager.requestAudioFocus(mServiceAudioManager);
    }

    public void abandonAudioFocusRequest() {
        mServiceAudioManager.setMode(android.media.AudioManager.MODE_NORMAL);
        manager.abandonAudioFocus(mServiceAudioManager);
        forceVolumeControlStream(Constants.STREAM_MUSIC);
    }

    public void disable() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
    }

    private class HeadsetStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            if (!enabled) {
                Log.d(TAG, "onReceive: the AudioManager is disabled, nothing to do for action received");
                return;
            }

            String action = intent.getAction();
            if (null == action) action = "";
            switch (action) {
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", -1);
                    int has_mic = intent.getIntExtra("microphone", -1);

                    WiredInformation information = new WiredInformation(has_mic > 0, state);

                    mWiredMachine.connect(information);
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mBluetoothMachine.connect(device);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mBluetoothMachine.disconnect();
                    break;
                case android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED:

                    int l_state = intent.getIntExtra(android.media.AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                    Log.d(TAG, "Audio SCO: " + android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);

                    switch (l_state) {
                        case android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED:
                        case android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                            mBluetoothMachine.requestAudioFocus();
                    }
            }
        }
    }

    private AudioManager() {
        manager = new AudioFocusManager();
        mHeadsetStateReceiver = new HeadsetStateReceiver();
    }

    public AudioManager(Context context) {
        this();

        mContext = context;

        mServiceAudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        mBluetoothMachine = new BluetoothHeadsetMachine(context, mMediaStateListeners, this, mServiceAudioManager);
        mWiredMachine = new WiredHeadsetMachine(mMediaStateListeners, this, mServiceAudioManager);

        mWiredMachine.warmup();
        mBluetoothMachine.warmup();

        speakerMode = new SpeakerMode(mServiceAudioManager);
        wiredMode = new WiredMode(mServiceAudioManager);
        bluetoothMode = new BluetoothMode(mServiceAudioManager);

        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
    }

    public void stop() {
        setBluetooth(false);

        mContext.unregisterReceiver(mHeadsetStateReceiver);

        mBluetoothMachine.stop();
    }

    /**
     * Get the current available routes
     *
     * @return a non null route
     */
    @NonNull
    public List<AudioRoute> availableRoutes() {
        List<AudioRoute> routes = new ArrayList<>();

        routes.add(AudioRoute.ROUTE_SPEAKER);

        if (isWiredHeadsetOn()) {
            routes.add(AudioRoute.ROUTE_HEADSET);
        } else {
            routes.add(AudioRoute.ROUTE_PHONE);
        }

        if (isBluetoothHeadsetConnected()) {
            routes.add(AudioRoute.ROUTE_BLUETOOTH);
        }

        return routes;
    }

    /**
     * Retrieve the current audio route defined in this manager
     *
     * @return a non null audio route
     */
    @NonNull
    public AudioRoute outputRoute() {
        return mOutputRoute;
    }


    /**
     * Set the current route for the manager
     *
     * @param route set the valid audio route
     */
    public boolean setOutputRoute(@NonNull AudioRoute route) {

        switch (route) {
            case ROUTE_BLUETOOTH:
                if (isBluetoothHeadsetConnected()) {
                    setBluetooth(true);
                }
                break;
            case ROUTE_HEADSET:
            case ROUTE_PHONE:
                setSpeakerMode(false);
                break;
            case ROUTE_SPEAKER:
                if (isWiredHeadsetOn() || isBluetoothHeadsetConnected()) {
                    return false;
                }
                setSpeakerMode(true);
                break;
            default:
                break;
        }

        notifyAudioRoute();
        return true;
    }

    /**
     * @see android.app.Activity#setVolumeControlStream(int)
     */
    public void setVolumeControlStream(Window window, int streamType) {
        try {
            window.setVolumeControlStream(streamType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSpeakerMode(final boolean speaker) {
        Log.d(TAG, "setSpeakerMode: " + isWiredHeadsetOn() + " " + isBluetoothHeadsetConnected());
        if (isWiredHeadsetOn()) {
            wiredMode.apply(speaker);
            forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        } else if (isBluetoothHeadsetConnected()) {
            bluetoothMode.apply(speaker);
            forceVolumeControlStream(Constants.STREAM_BLUETOOTH_SCO);
        } else {
            speakerMode.apply(speaker);
            forceVolumeControlStream(Constants.STREAM_VOICE_CALL);
        }

        mMediaStateListeners.notif(new ListenerHolder.Callback<IMediaStateListener>() {
            @Override
            public void apply(IMediaStateListener listener) {
                listener.onSpeakerChanged(speaker);
            }
        });

        checkOutputRoute();
    }

    /**
     * Start the audio manager in bluetooth mode
     * <p>
     * Can lead to a non-bluetooth state if a crash occured internally (android 5.0)
     *
     * @param isEnabled true if it should start
     */
    public void setBluetooth(boolean isEnabled) {
        mBluetoothMachine.enable(isEnabled);
    }

    public void registerAudioRouteListener(@NonNull IAudioRouteListener listener) {
        mAudioRouteListeners.register(listener);
    }

    public void unregisterAudioRouteListener(@NonNull IAudioRouteListener listener) {
        mAudioRouteListeners.unregister(listener);
    }

    /**
     * Register a valid listener to this manager
     *
     * @param listener a persisted listener
     */
    public void registerMediaState(@NonNull IMediaStateListener listener) {
        mMediaStateListeners.register(listener);
    }

    /**
     * Remove a listener from the list of listeners
     *
     * @param listener a valid listener, if not currently listening, no crash reported
     */
    public void unregisterMediaState(@NonNull IMediaStateListener listener) {
        mMediaStateListeners.unregister(listener);
    }

    /**
     * Get the current default sound type
     *
     * @return the saved value
     */
    public int getDefaultSoundStreamType() {
        return Constants.STREAM_MUSIC;
    }

    /**
     * Set the default sound back to the default one
     */
    public void resetDefaultSoundType() {
        forceVolumeControlStream(Constants.STREAM_MUSIC);
    }

    /**
     * Force a given volume type
     *
     * @param type a valid type from the android.media.AudioManager class
     */
    public void forceVolumeControlStream(int type) {
        Invoke.callVoidIntArg(mServiceAudioManager, "forceVolumeControlStream", type);
    }

    /**
     * Check for bluetooth headset
     *
     * @return true if at least one device is properly connected
     */
    public boolean isBluetoothHeadsetConnected() {
        return mBluetoothMachine.isConnected();
    }

    public boolean isWiredHeadsetOn() {
        if (Build.VERSION.SDK_INT >= 23) {
            AudioDeviceInfo[] audioDevices = mServiceAudioManager.getDevices(android.media.AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d(TAG, "isWiredHea-*dsetOn: headphone connected");
                    return true;
                }
            }
        }
        return mServiceAudioManager.isWiredHeadsetOn();
    }

    /**
     * Check for wired or bluetooth headset
     *
     * @return true if at least one device is properly connected
     */
    public boolean isHeadphonesPlugged() {
        return isWiredHeadsetOn() || isBluetoothHeadsetConnected();
    }

    public void checkOutputRoute() {
        if (isBluetoothHeadsetConnected()) {
            mOutputRoute = AudioRoute.ROUTE_BLUETOOTH;
        } else if (isWiredHeadsetOn()) {
            mOutputRoute = AudioRoute.ROUTE_HEADSET;
        } else if (null != mServiceAudioManager && mServiceAudioManager.isSpeakerphoneOn()) {
            mOutputRoute = AudioRoute.ROUTE_SPEAKER;
        } else {
            mOutputRoute = AudioRoute.ROUTE_PHONE;
        }

        requestAudioFocus();
    }

    private int getUiSoundsStreamType() {
        return Invoke.callReturnIntVoidArg(mServiceAudioManager, "getUiSoundsStreamType", Constants.STREAM_SYSTEM);
    }
}
