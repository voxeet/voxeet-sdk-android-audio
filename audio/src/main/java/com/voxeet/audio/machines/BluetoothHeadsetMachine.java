package com.voxeet.audio.machines;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import com.voxeet.audio.utils.Log;

import com.voxeet.audio.AudioStackManager;
import com.voxeet.audio.listeners.IMediaStateListener;
import com.voxeet.audio.listeners.ListenerHolder;
import com.voxeet.audio.mode.AbstractMode;
import com.voxeet.audio.utils.Validate;

import static android.media.AudioManager.STREAM_MUSIC;

public class BluetoothHeadsetMachine extends AbstractMachine<BluetoothDevice> {
    private final static String TAG = BluetoothHeadsetMachine.class.getSimpleName();

    private final Context mContext;
    private Handler handler = new Handler();

    private Runnable runnableBluetoothSco = new Runnable() {
        @Override
        public void run() {
            requestAudioFocus();
            try {
                manager.startBluetoothSco();
                manager.setBluetoothScoOn(true);
                //checkForBluetoothDeviceAtStartUp();
                //isBluetoothScoStarted = true;
            } catch (Exception e) {
                e.printStackTrace();
                isBluetoothScoStarted = false;
            }
        }
    };

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHeadsetListener mBluetoothHeadsetListener;

    private BluetoothHeadset mCurrentBluetoothHeadset;
    private boolean isBluetoothScoStarted;

    public BluetoothHeadsetMachine(Context context,
                                   ListenerHolder<IMediaStateListener> listener,
                                   AudioStackManager audioManager,
                                   android.media.AudioManager manager,
                                   AbstractMode audioMode) {
        super(listener, audioManager, manager, audioMode);

        mContext = context;

        mBluetoothHeadsetListener = new BluetoothHeadsetListener();

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }catch (VerifyError exception) {
            Log.d(TAG, "BluetoothHeadsetMachine: VerifyError exception for this device. Please report");
            mBluetoothAdapter = null;
            exception.printStackTrace();
        }
    }

    @Override
    public void connect(BluetoothDevice connect) {
        updateBluetoothHeadsetConnectivity(connect);
    }

    @Override
    public void disconnect() {
        updateBluetoothHeadsetConnectivity(null);
    }

    @Override
    public void stop() {
        if (mCurrentBluetoothHeadset != null && mBluetoothAdapter != null) {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mCurrentBluetoothHeadset);
        }
    }

    @Override
    public boolean isConnected() {
        checkForBluetoothDeviceAtStartUp();

        Log.d(TAG, "isConnected: " + isBluetoothScoStarted);

        return isBluetoothScoStarted;
    }

    @Override
    public void warmup() {
        if (mBluetoothAdapter != null) {
            try {
                mBluetoothAdapter.getProfileProxy(mContext, mBluetoothHeadsetListener, BluetoothProfile.HEADSET);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        checkForBluetoothDeviceAtStartUp();
    }

    @Override
    public void enable(boolean isEnabled) {
        Log.d(TAG, "enable: bluetooth " + isEnabled);
        if(audioManager.isEnabled()) {
            audioManager.forceVolumeControlStream(STREAM_MUSIC);//STREAM_VOICE_CALL);

            audioManager.checkOutputRoute();
            try {
                if (isEnabled) {
                    startBluetoothSco();
                } else {
                    stopBluetoothSco();
                }
            } catch (NullPointerException e) { // Workaround for lollipop 5.0
                Log.d(TAG, "No bluetooth headset connected");
            }
        }else{
            Log.d(TAG, "Calling enable while manager is disabled");
        }
    }


    private void checkForBluetoothDeviceAtStartUp() {
        Log.d(TAG, "checkForBluetoothDeviceAtStartUp");
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                    && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
                Log.d(TAG, "checkForBluetoothDeviceAtStartUp: bluetooth connected");
                isBluetoothScoStarted = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "checkForBluetoothDeviceAtStartUp: exception thrown, have you the BLUETOOTH permission?");
            e.printStackTrace();
        }
    }

    private void startBluetoothSco() {
        handler.postDelayed(runnableBluetoothSco, 3000);
    }

    private void stopBluetoothSco() {
        handler.removeCallbacks(runnableBluetoothSco);
        if (isBluetoothScoStarted) {
            try {
                manager.setBluetoothScoOn(false);
                manager.stopBluetoothSco();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isBluetoothScoStarted = false;
        }
    }

    private void updateBluetoothHeadsetConnectivity(@Nullable BluetoothDevice device) {
        Log.d(TAG, "updateBluetoothHeadsetConnectivity: " + device);
        if (null != mCurrentBluetoothHeadset) {
            if (Validate.hasBluetoothPermissions(mContext)) {
                isBluetoothScoStarted = mCurrentBluetoothHeadset.getConnectedDevices().size() > 0;
                isBluetoothScoStarted |= null != device;
            } else {
                Log.e(TAG, "onServiceConnected: BLUETOOTH PERMISSION MISSING");
            }

            listenerHolder.notif(new ListenerHolder.Callback<IMediaStateListener>() {
                @Override
                public void apply(IMediaStateListener listener) {
                    listener.onBluetoothHeadsetStateChange(isBluetoothScoStarted);
                }
            });

            Log.d(TAG, "onServiceConnected: isBluetoothScoStarted = " + isBluetoothScoStarted);

            if(audioManager.isEnabled()) {
                startBluetoothSco();
                audioManager.checkOutputRoute();
                audioManager.notifyAudioRoute();
            } else {
                Log.d(TAG, "not starting sco, checking or notify... disabled");
            }
        }
    }


    private class BluetoothHeadsetListener implements BluetoothHeadset.ServiceListener {
        @Override
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (bluetoothProfile instanceof BluetoothHeadset) {
                mCurrentBluetoothHeadset = (BluetoothHeadset) bluetoothProfile;
            }

            updateBluetoothHeadsetConnectivity(null);
        }

        @Override
        public void onServiceDisconnected(int i) {
            mCurrentBluetoothHeadset = null;

            isBluetoothScoStarted = false;
            Log.d(TAG, "onServiceDisconnected: isBluetoothScoStarted = true");
            manager.stopBluetoothSco();

            listenerHolder.notif(new ListenerHolder.Callback<IMediaStateListener>() {
                @Override
                public void apply(IMediaStateListener listener) {
                    listener.onBluetoothHeadsetStateChange(false);
                }
            });

            audioManager.checkOutputRoute();
        }
    }
}
