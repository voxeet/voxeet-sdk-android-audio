[![N|Solid](http://p9qjk1ag09i2wv1d248nvwk14mt.wpengine.netdna-cdn.com/wp-content/uploads/2015/12/Voxeet-logo-tagline.svg)](https://app.voxeet.com)

This Android library helps managing the various event and feature around external peripherals, sound outputs

# install

```gradle
dependencies {
  //add this one if you want to use the logic-only SDK
  compile ('com.voxeet.sdk:audio:1.0.3') {
    transitive = true
  }
}
```

# Roadmap

- add more features


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