package com.voxeet.audio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;

import com.voxeet.audio.focus.AudioFocusManager;
import com.voxeet.audio.listeners.IAudioRouteListener;
import com.voxeet.audio.listeners.IMediaStateListener;
import com.voxeet.audio.listeners.ListenerHolder;
import com.voxeet.audio.machines.AbstractMachine;
import com.voxeet.audio.machines.BluetoothHeadsetMachine;
import com.voxeet.audio.machines.WiredHeadsetMachine;
import com.voxeet.audio.machines.WiredInformation;
import com.voxeet.audio.mode.AbstractMode;
import com.voxeet.audio.mode.BluetoothMode;
import com.voxeet.audio.mode.NormalMode;
import com.voxeet.audio.mode.SpeakerMode;
import com.voxeet.audio.mode.WiredMode;
import com.voxeet.audio.receiver.HeadsetStateReceiver;
import com.voxeet.audio.utils.Constants;
import com.voxeet.audio.utils.Invoke;

import java.util.ArrayList;
import java.util.List;

/**
 * Control and Manager the Audio states of the media
 */

public class AudioStackManager {
    private static final String TAG = AudioStackManager.class.getSimpleName();
    private WiredHeadsetMachine mWiredMachine;
    private BluetoothHeadsetMachine mBluetoothMachine;
    private Context mContext;

    private SpeakerMode speakerMode;
    private WiredMode wiredMode;
    private BluetoothMode bluetoothMode;
    private NormalMode normalMode;

    private AbstractMode currentMode;

    private android.media.AudioManager mServiceAudioManager;

    private HeadsetStateReceiver mHeadsetStateReceiver;

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

    private AudioFocusManager audioFocusManager;
    private boolean enabled;

    public void notifyAudioRoute() {
        mAudioRouteListeners.notif();
    }

    public void disable() {
        enabled = false;
        mHeadsetStateReceiver.enable(enabled);
    }

    public void enable() {
        enabled = true;
        mHeadsetStateReceiver.enable(enabled);
    }

    private AudioStackManager() {
        audioFocusManager = new AudioFocusManager();
    }

    public AudioStackManager(Context context) {
        this();

        mContext = context;

        mServiceAudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        speakerMode = new SpeakerMode(mServiceAudioManager, audioFocusManager);
        wiredMode = new WiredMode(mServiceAudioManager, audioFocusManager);
        bluetoothMode = new BluetoothMode(mServiceAudioManager, audioFocusManager);
        normalMode = new NormalMode(mServiceAudioManager, audioFocusManager);


        // set the current model to default
        currentMode = normalMode;

        mBluetoothMachine = new BluetoothHeadsetMachine(context, mMediaStateListeners, this, mServiceAudioManager, bluetoothMode);
        mWiredMachine = new WiredHeadsetMachine(mMediaStateListeners, this, mServiceAudioManager, wiredMode);

        mHeadsetStateReceiver = new HeadsetStateReceiver(mWiredMachine,
                mBluetoothMachine,
                bluetoothMode);

        mWiredMachine.warmup();
        mBluetoothMachine.warmup();

        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        context.registerReceiver(mHeadsetStateReceiver, new IntentFilter(android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
    }

    public void stop() {
        mContext.unregisterReceiver(mHeadsetStateReceiver);
        mBluetoothMachine.enable(false);
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
        return currentMode.getAudioRoute();
    }


    /**
     * Set the current route for the manager
     *
     * @param route set the valid audio route
     */
    public boolean setOutputRoute(@NonNull AudioRoute route) {
        switch (route) {
            case ROUTE_SPEAKER:
                if (!isWiredHeadsetOn() && !isBluetoothHeadsetConnected()) {
                    setSpeakerMode(true);
                    notifyAudioRoute();
                    return true;
                }
                break;
            case ROUTE_BLUETOOTH:
                if (isBluetoothHeadsetConnected()) mBluetoothMachine.enable(true);
                break;
            case ROUTE_HEADSET:
            case ROUTE_PHONE:
                setSpeakerMode(false);
                break;
            default:
                break;
        }

        notifyAudioRoute();
        return false;
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
        if (isWiredHeadsetOn()) {
            wiredMode.apply(speaker);
        } else if (isBluetoothHeadsetConnected()) {
            bluetoothMode.apply(speaker);
        } else {
            speakerMode.apply(speaker);
        }

        mMediaStateListeners.notif(new ListenerHolder.Callback<IMediaStateListener>() {
            @Override
            public void apply(IMediaStateListener listener) {
                listener.onSpeakerChanged(speaker);
            }
        });

        checkOutputRoute();
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
        return wiredMode.isConnected();
    }
    
    public void checkOutputRoute() {
        if (isBluetoothHeadsetConnected()) {
            bluetoothMode.requestAudioFocus();
            currentMode = bluetoothMode;
        } else if (isWiredHeadsetOn()) {
            wiredMode.requestAudioFocus();
            currentMode = wiredMode;
        } else if (null != mServiceAudioManager && mServiceAudioManager.isSpeakerphoneOn()) {
            speakerMode.requestAudioFocus();
            currentMode = speakerMode;
        } else {
            normalMode.requestAudioFocus();
            currentMode = normalMode;
        }
    }

    public AudioStackManager requestAudioFocus() {
        if (null != currentMode) currentMode.requestAudioFocus();
        return this;
    }

    public AudioStackManager abandonAudioFocus() {
        if (null != currentMode) currentMode.abandonAudioFocus();
        return this;
    }

    public AudioStackManager configureVolumeStream(int requestFocus, int abandonFocus) {
        bluetoothMode.configureVolumeStream(requestFocus, abandonFocus);
        normalMode.configureVolumeStream(requestFocus, abandonFocus);
        speakerMode.configureVolumeStream(requestFocus, abandonFocus);
        wiredMode.configureVolumeStream(requestFocus, abandonFocus);

        return this;
    }

    @Nullable
    public Ringtone getSystemRingtone() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (null == uri) return null;
        return RingtoneManager.getRingtone(mContext, uri);
    }

    private int getUiSoundsStreamType() {
        return Invoke.callReturnIntVoidArg(mServiceAudioManager, "getUiSoundsStreamType", Constants.STREAM_SYSTEM);
    }
}
