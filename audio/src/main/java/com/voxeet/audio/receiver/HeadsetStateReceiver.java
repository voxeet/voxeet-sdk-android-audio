package com.voxeet.audio.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.voxeet.audio.utils.Log;

import com.voxeet.audio.machines.BluetoothHeadsetMachine;
import com.voxeet.audio.machines.WiredHeadsetMachine;
import com.voxeet.audio.machines.WiredInformation;
import com.voxeet.audio.mode.BluetoothMode;


public class HeadsetStateReceiver extends BroadcastReceiver {

    private final static String TAG = HeadsetStateReceiver.class.getSimpleName();

    private WiredHeadsetMachine wiredHeadsetMachine;
    private BluetoothHeadsetMachine bluetoothHeadsetMachine;
    private BluetoothMode bluetoothMode;
    private boolean enabled;

    private HeadsetStateReceiver() {
        enabled = true;
    }

    public HeadsetStateReceiver(@NonNull WiredHeadsetMachine wiredHeadsetMachine,
                                @NonNull BluetoothHeadsetMachine bluetoothHeadsetMachine,
                                @NonNull BluetoothMode bluetoothMode) {
        this();

        this.wiredHeadsetMachine = wiredHeadsetMachine;
        this.bluetoothHeadsetMachine = bluetoothHeadsetMachine;
        this.bluetoothMode = bluetoothMode;
    }

    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!enabled) {
            Log.d(TAG, "onReceive: unable to comply, the onReceive is receive while being disabled");
            return;
        } else {
            Log.d(TAG, "onReceive: complying, the onReceive event is received while being activated");
        }

        String action = intent.getAction();
        if (null == action) action = "";
        switch (action) {
            case Intent.ACTION_HEADSET_PLUG:
                Log.d(TAG, "onReceive: headset plug");
                int state = intent.getIntExtra("state", -1);
                int has_mic = intent.getIntExtra("microphone", -1);

                WiredInformation information = new WiredInformation(has_mic > 0, state);

                wiredHeadsetMachine.connect(information);
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                Log.d(TAG, "onReceive: acl connected");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothHeadsetMachine.connect(device);
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                Log.d(TAG, "onReceive: acl disconnected");
                bluetoothHeadsetMachine.disconnect();
                break;
            case android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED:
                Log.d(TAG, "onReceive: sco audio state changed");

                int l_state = intent.getIntExtra(android.media.AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                switch (l_state) {
                    case android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED:
                        Log.d(TAG, "onReceive: sco audio state connected");
                        bluetoothMode.requestAudioFocus();
                        break;
                    case android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                        Log.d(TAG, "onReceive: sco audio state disconnected");
                        bluetoothMode.abandonAudioFocus();
                }
        }
    }
}
