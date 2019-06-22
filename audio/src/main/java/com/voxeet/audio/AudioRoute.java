package com.voxeet.audio;

/**
 * Specific routes for audio devices on Android
 */

public enum AudioRoute {
    ROUTE_HEADSET(false),
    ROUTE_PHONE(true),
    ROUTE_SPEAKER(false),
    ROUTE_BLUETOOTH(false),
    ROUTE_MEDIA(false);

    private boolean mProximitySensor;

    AudioRoute(boolean proximity_sensor) {
        mProximitySensor = proximity_sensor;
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
            case 4:
                return ROUTE_MEDIA;
            default:
                return ROUTE_SPEAKER;
        }
    }
}