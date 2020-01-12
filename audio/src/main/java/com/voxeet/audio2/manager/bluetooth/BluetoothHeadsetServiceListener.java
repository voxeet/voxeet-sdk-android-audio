package com.voxeet.audio2.manager.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Call;
import com.voxeet.audio.utils.__Opt;

public class BluetoothHeadsetServiceListener {

    private static final String TAG = BluetoothHeadsetServiceListener.class.getSimpleName();
    private BluetoothAdapter bluetoothAdapter;

    @NonNull
    private __Call<BluetoothHeadsetServiceListener> update;

    @Nullable
    private BluetoothHeadset bluetoothHeadsetProfile;

    private BluetoothHeadset.ServiceListener bluetoothServiceListener;

    private BluetoothHeadsetServiceListener() {
        this.update = (r) -> {
        };

        bluetoothServiceListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (proxy instanceof BluetoothHeadset) {
                    Log.d(TAG, "bluetooth service connected");

                    BluetoothHeadsetServiceListener.this.bluetoothHeadsetProfile = (BluetoothHeadset) proxy;
                    update.apply(BluetoothHeadsetServiceListener.this);
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                Log.d(TAG, "bluetooth service disconnected");
                BluetoothHeadsetServiceListener.this.bluetoothHeadsetProfile = null;

                update.apply(BluetoothHeadsetServiceListener.this);
            }
        };
    }

    public BluetoothHeadsetServiceListener(@NonNull __Call<BluetoothHeadsetServiceListener> update) {
        this();


        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        } catch (VerifyError exception) {
            Log.e(TAG, "BluetoothHeadsetMachine: VerifyError exception for this device. Please report", exception);
            bluetoothAdapter = null;
            exception.printStackTrace();
        }
        this.update = update;
    }

    public void connect(@NonNull Context context) {

        try {
            if (null != bluetoothAdapter) {
                Log.d(TAG, "acquiring BluetoothProfile.HEADSET...");
                bluetoothAdapter.getProfileProxy(context, bluetoothServiceListener, BluetoothProfile.HEADSET);
            }
        } catch (SecurityException exception) {
            Log.e(TAG, "security exception occurred...", exception);
            exception.printStackTrace();
        }
    }

    @Nullable
    public BluetoothHeadset bluetoothHeadset() {
        return bluetoothHeadsetProfile;
    }

    @Nullable
    public boolean isConnected() {
        return null != bluetoothHeadsetProfile;
    }

    @Nullable
    public BluetoothDevice getActiveDevice() {
        return __Opt.of(bluetoothHeadsetProfile).then(BluetoothHelper::getActiveDevice).orNull();
    }

    @Nullable
    public boolean setActiveDevice(@NonNull BluetoothDevice device) {
        return __Opt.of(bluetoothHeadsetProfile).then(h -> BluetoothHelper.setActiveDevice(h, device)).or(false);
    }

    public boolean canFetchAndSetActiveDevices() {
        return __Opt.of(bluetoothHeadsetProfile).then(h -> BluetoothHelper.canFetchActiveDevice(h) && BluetoothHelper.canSetActiveDevice(h)).or(false);
    }
}
