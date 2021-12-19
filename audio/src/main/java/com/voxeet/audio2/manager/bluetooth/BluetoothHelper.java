package com.voxeet.audio2.manager.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Opt;
import com.voxeet.audio2.manager.BluetoothHeadsetDeviceManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BluetoothHelper {

    private static final String TAG = BluetoothHelper.class.getSimpleName();

    public static boolean canFetchActiveDevice(@NonNull BluetoothHeadset headset) {
        return null != _getActiveDevice(headset);
    }

    public static boolean canSetActiveDevice(@NonNull BluetoothHeadset headset) {
        return null != _setActiveDevice(headset);
    }

    @SuppressLint("DiscouragedPrivateApi")
    @Nullable
    public static BluetoothDevice getActiveDevice(@NonNull BluetoothHeadset headset) {
        return __Opt.of(_getActiveDevice(headset)).then(m -> _invokeGetActiveDevice(headset, m)).orNull();
    }

    @SuppressLint("DiscouragedPrivateApi")
    public static boolean setActiveDevice(@NonNull BluetoothHeadset headset, @NonNull BluetoothDevice device) {
        return __Opt.of(_setActiveDevice(headset)).then(m -> _invokeSetActiveDevice(headset, m, device)).or(false);
    }


    @Nullable
    private static Method _setActiveDevice(@NonNull BluetoothHeadset headset) {
        try {
            Class<?> klass = headset.getClass();
            return klass.getDeclaredMethod("setActiveDevice", BluetoothDevice.class);
        } catch (SecurityException exception) {
            Log.e(TAG, BluetoothHeadsetDeviceManager.BLUETOOTH_CONNECT_EXCEPTION, exception);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Nullable
    private static Method _getActiveDevice(@NonNull BluetoothHeadset headset) {
        try {
            Class<?> klass = headset.getClass();
            return klass.getDeclaredMethod("getActiveDevice");
        } catch (SecurityException exception) {
            Log.e(TAG, BluetoothHeadsetDeviceManager.BLUETOOTH_CONNECT_EXCEPTION, exception);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Nullable
    private static BluetoothDevice _invokeGetActiveDevice(@NonNull BluetoothHeadset headset,
                                                          @NonNull Method getActiveDevice) {
        try {
            return (BluetoothDevice) getActiveDevice.invoke(headset);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SecurityException exception) {
            Log.e(TAG, BluetoothHeadsetDeviceManager.BLUETOOTH_CONNECT_EXCEPTION, exception);
        }
        return null;
    }

    private static boolean _invokeSetActiveDevice(@NonNull BluetoothHeadset headset,
                                                  @NonNull Method setActiveDevice,
                                                  @NonNull BluetoothDevice device) {
        try {
            return (boolean) setActiveDevice.invoke(headset, device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SecurityException exception) {
            Log.e(TAG, BluetoothHeadsetDeviceManager.BLUETOOTH_CONNECT_EXCEPTION, exception);
        }
        return false;
    }
}
