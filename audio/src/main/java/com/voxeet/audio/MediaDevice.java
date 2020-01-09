package com.voxeet.audio;

import com.voxeet.audio.utils.Annotate;

/**
 * Specific routes for audio devices on Android
 *
 * The possible routes are :
 * - ROUTE_HEADSET
 * - ROUTE_PHONE
 * - ROUTE_SPEAKER
 * - ROUTE_BLUETOOTH
 * - ROUTE_MEDIA
 */
@Annotate
public enum MediaDevice {
    ROUTE_HEADSET(false),
    ROUTE_PHONE(true),
    ROUTE_SPEAKER(false),
    ROUTE_BLUETOOTH(false),
    ROUTE_MEDIA(false);

    private boolean mProximitySensor;

    MediaDevice(boolean proximity_sensor) {
        mProximitySensor = proximity_sensor;
    }

    public boolean useProximitySensor() {
        return mProximitySensor;
    }

    public static MediaDevice valueOf(int value) {
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