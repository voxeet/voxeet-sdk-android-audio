package com.voxeet.audio2.manager.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.audio.utils.__Call;

public class BluetoothDisconnectListener {

    @Nullable
    private final BluetoothAdapter bluetoothAdapter;
    @NonNull
    private final __Call<Boolean> closeBluetoothDevices;

    @Nullable
    private BroadcastReceiver bluetoothStateBroadcastReceiver;

    public BluetoothDisconnectListener(@NonNull Context context, @NonNull __Call<Boolean> closeBluetoothDevices) {
        this.closeBluetoothDevices = closeBluetoothDevices;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (null == bluetoothAdapter) return;

        bluetoothStateBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                // It means the user has changed his bluetooth state.
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    checkBluetoothStateOff();
                }
            }
        };

        context.registerReceiver(bluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    private void checkBluetoothStateOff() {
        if (null == bluetoothAdapter) return;

        switch (bluetoothAdapter.getState()) {
            case BluetoothAdapter.STATE_TURNING_OFF:
            case BluetoothAdapter.STATE_OFF:
                closeBluetoothDevices.apply(true);
                break;
            default:
        }
    }
}
