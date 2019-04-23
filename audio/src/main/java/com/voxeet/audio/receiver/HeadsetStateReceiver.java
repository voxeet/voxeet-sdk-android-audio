package com.voxeet.audio.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.voxeet.audio.machines.BluetoothHeadsetMachine;
import com.voxeet.audio.machines.WiredHeadsetMachine;
import com.voxeet.audio.machines.WiredInformation;
import com.voxeet.audio.mode.BluetoothMode;

public class HeadsetStateReceiver extends BroadcastReceiver {

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
            return;
        }

        String action = intent.getAction();
        if (null == action) action = "";
        switch (action) {
            case Intent.ACTION_HEADSET_PLUG:
                int state = intent.getIntExtra("state", -1);
                int has_mic = intent.getIntExtra("microphone", -1);

                WiredInformation information = new WiredInformation(has_mic > 0, state);

                wiredHeadsetMachine.connect(information);
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothHeadsetMachine.connect(device);
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                bluetoothHeadsetMachine.disconnect();
                break;
            case android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED:

                int l_state = intent.getIntExtra(android.media.AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                switch (l_state) {
                    case android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED:
                        bluetoothMode.requestAudioFocus();
                        break;
                    case android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                        bluetoothMode.abandonAudioFocus();
                }
        }
    }
}
