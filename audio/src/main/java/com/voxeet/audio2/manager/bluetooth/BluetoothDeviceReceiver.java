package com.voxeet.audio2.manager.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import androidx.annotation.NonNull;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Call;


public class BluetoothDeviceReceiver {

    private final static String TAG = BluetoothDeviceReceiver.class.getSimpleName();
    private static final String ACTION_ACTIVE_DEVICE_CHANGED = "android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED";

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (null == action) action = "";

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "onReceive: with device ? " + device);
            if (null != device) {
                Log.d(TAG, "device := " + device.getAddress() + " " + device.getName());
            }
            switch (action) {
                case ACTION_ACTIVE_DEVICE_CHANGED:
                    Log.d(TAG, "onReceive: acl connected");
                    callback.apply(new BluetoothAction(BluetoothAction.Action.DEVICE_ACTIVE_CHANGED, device));
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "onReceive: acl connected");
                    callback.apply(new BluetoothAction(BluetoothAction.Action.DEVICE_CONNECTED, device));
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "onReceive: acl disconnected");
                    callback.apply(new BluetoothAction(BluetoothAction.Action.DEVICE_DISCONNECTED, device));
                    break;
                case AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED:
                    Log.d(TAG, "onReceive: sco audio state changed");

                    int state = intent.getIntExtra(android.media.AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                    switch (state) {
                        case android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED:
                            Log.d(TAG, "onReceive: sco audio state connected");
                            callback.apply(new BluetoothAction(BluetoothAction.Action.SCO_AUDIO_CONNECTED, device));
                            break;
                        case android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                            Log.d(TAG, "onReceive: sco audio state disconnected");
                            callback.apply(new BluetoothAction(BluetoothAction.Action.SCO_AUDIO_DISCONNECTED, device));
                            break;
                        case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                            Log.d(TAG, "onReceive: sco audio state connecting");
                            callback.apply(new BluetoothAction(BluetoothAction.Action.SCO_AUDIO_CONNECTING, device));
                    }
            }
        }
    };


    public void connect(@NonNull Context context) {
        context.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        context.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        Intent scoIntent = context.registerReceiver(receiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        context.registerReceiver(receiver, new IntentFilter(BluetoothDeviceReceiver.ACTION_ACTIVE_DEVICE_CHANGED));

        if (null != scoIntent) {
            receiver.onReceive(context, scoIntent);
        }
    }

    public boolean isKnownSCO(@NonNull Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

        if (null == intent) return false;
        int state = intent.getIntExtra(android.media.AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
        return state == android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED;

    }

    private final __Call<BluetoothAction> callback;

    public BluetoothDeviceReceiver(@NonNull __Call<BluetoothAction> callback) {
        this.callback = callback;
    }
}
