package com.voxeet.audio;

/**
 * Specific routes for audio devices on Android
 */

public enum AudioRoute {
    ROUTE_HEADSET(0, false),
    ROUTE_PHONE(1, true),
    ROUTE_SPEAKER(2, false),
    ROUTE_BLUETOOTH(3, false);

    private int mValue;
    private boolean mProximitySensor;

    AudioRoute(int value, boolean proximity_sensor) {
        mValue = value;
        mProximitySensor = proximity_sensor;
    }

    public int value() {
        return mValue;
    }

    public boolean useProximitySensor() {
        return mProximitySensor;
    }

    public static AudioRoute valueOf(int value) {
        switch (value) {
            case 0:
                return ROUTE_HEADSET;
            case 1:
                return ROUTE_PHONE;
            case 2:
                return ROUTE_SPEAKER;
            case 3:
                return ROUTE_BLUETOOTH;
            default:
                return ROUTE_SPEAKER;
        }
    }
}