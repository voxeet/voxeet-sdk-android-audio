[![N|Solid](http://p9qjk1ag09i2wv1d248nvwk14mt.wpengine.netdna-cdn.com/wp-content/uploads/2015/12/Voxeet-logo-tagline.svg)](https://app.voxeet.com)

This Android library helps managing the various event and feature around external peripherals, sound outputs

# install

```gradle
dependencies {
  //add this one if you want to use the logic-only SDK
  compile ('com.voxeet.sdk:audio:2.2.0') {
    transitive = true
  }
}
```

# Usage

## Instance

```
AudioDeviceManager manager = new AudioDeviceManager(Context, ListenerForUpdates);
```

## Enumerate devices

```
Promise<List<MediaDevice>> devices = manager.enumerateDevices();
```

## Enumerate devices for a specific type

```
Promise<List<MediaDevice>> devices = manager.enumerateDevices(DeviceType);
```

## Filter devices

```
List<MediaDevice> filtered = manager.filter(List<MediaDevice>, DeviceType);
```

## Connect to a device

```
Promise<Boolean> connection = manager.connect(MediaDevice);
```

## Disonnect from a device

```
Promise<Boolean> connection = manager.disconnect(MediaDevice);
```

## Current device

```
Promise<MediaDevice> current = manager.current();
```

# Models

## DeviceType

```
DeviceType {
    INTERNAL_SPEAKER,
    EXTERNAL_SPEAKER,
    BLUETOOTH,
    NORMAL_MEDIA,
    WIRED_HEADSET, USB
}
```

## MediaDevice

### id()

System's id for the wrapped device

### deviceType()

Type of the current device

### connectionState()

Current connection state from the SDK point of view

### platformConnectionState()

Current connection state from a platform point of view (a device can be connected to the platform but not used)

## ConnectionState

```
ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING;
}
```

# Testing

## Running

Ensure a device is plugged in the current desktop (multiple devices for more integration testing purposes)

And run the following command :

```
./gradlew :audio:connectedCheck test
```

## Check the results

The various test implementation results can be found in the following folders. Use the direct links to access your own results :

- [Unit Tests](./audio/build/reports/tests/testDebugUnitTest/index.html)
- [Integration Tests](./audio/build/reports/androidTests/connected/index.html)
- [Code Coverage](./audio/build/reports/coverage/debug/index.html)

# Changelogs

v2.x :
  - asynchronous and safe use of various device manipulation

v1.0.5 :
  - address issue on some HTC devices where the `getDefaultAdapter()` for Bluetooth would trigger a VerifyError

v1.0.4 :
  - Improve Bluetooth management with a fix in the constructor to use the warmup method