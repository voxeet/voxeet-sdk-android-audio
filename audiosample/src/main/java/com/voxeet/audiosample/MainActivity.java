package com.voxeet.audiosample;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.voxeet.audio2.AudioDeviceManager;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.promise.PromiseInOut;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.promise.solve.ThenValue;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AudioSample";
    private AudioDeviceManager audioDeviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioDeviceManager = new AudioDeviceManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldAskPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 0x20);
        }
    }

    public void internalMedia(View view) {
        disable();
        oneMedia().then(audioDeviceManager::connect)
                .then((ThenPromise<Boolean, List<MediaDevice>>) r -> audioDeviceManager.enumerateDevices())
                .then(r -> {
                    Toast.makeText(MainActivity.this, "onResume: internal media done ?! false==" + audioDeviceManager.systemAudioManager().audioManager().isSpeakerphoneOn(), Toast.LENGTH_SHORT).show();
                    enable();
                })
                .error(Throwable::printStackTrace);
    }


    public void internalCall(View view) {
        disable();
        oneInternal().then(audioDeviceManager::connect)
                .then((ThenPromise<Boolean, List<MediaDevice>>) r -> audioDeviceManager.enumerateDevices())
                .then(r -> {
                    Toast.makeText(MainActivity.this, "onResume: internal voice call done ?! false==" + audioDeviceManager.systemAudioManager().audioManager().isSpeakerphoneOn(), Toast.LENGTH_SHORT).show();
                    enable();
                })
                .error(Throwable::printStackTrace);
    }


    public void speakerOn(View view) {
        disable();
        oneSpeaker().then(audioDeviceManager::connect)
                .then((ThenPromise<Boolean, List<MediaDevice>>) r -> audioDeviceManager.enumerateDevices())
                .then(r -> {
                    Toast.makeText(MainActivity.this, "onResume: speaker done ?! " + audioDeviceManager.systemAudioManager().audioManager().isSpeakerphoneOn(), Toast.LENGTH_SHORT).show();
                    enable();
                })
                .error(Throwable::printStackTrace);
    }


    public void speakerOff(View view) {
        disable();
        oneSpeaker().then(audioDeviceManager::disconnect)
                .then((ThenPromise<Boolean, List<MediaDevice>>) r -> audioDeviceManager.enumerateDevices())
                .then(r -> {
                    Toast.makeText(MainActivity.this, "onResume: speaker undone ?! " + audioDeviceManager.systemAudioManager().audioManager().isSpeakerphoneOn(), Toast.LENGTH_SHORT).show();
                    enable();
                })
                .error(Throwable::printStackTrace);
    }

    public void disable() {
        findViewById(R.id.speaker_on).setEnabled(false);
        findViewById(R.id.speaker_off).setEnabled(false);
        findViewById(R.id.internal_call).setEnabled(false);
        findViewById(R.id.internal_media).setEnabled(false);
    }

    public void enable() {
        findViewById(R.id.speaker_on).setEnabled(true);
        findViewById(R.id.speaker_off).setEnabled(true);
        findViewById(R.id.internal_call).setEnabled(true);
        findViewById(R.id.internal_media).setEnabled(true);
    }

    public boolean shouldAskPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
                return true; //force a state where it has been asked hehe
            } else {
                return true;
            }
        }
        return false;
    }

    private PromiseInOut<List<MediaDevice>, MediaDevice> oneSpeaker() {
        return oneOfType(DeviceType.EXTERNAL_SPEAKER);
    }

    private PromiseInOut<List<MediaDevice>, MediaDevice> oneInternal() {
        return oneOfType(DeviceType.INTERNAL_SPEAKER);
    }

    private PromiseInOut<List<MediaDevice>, MediaDevice> oneMedia() {
        return oneOfType(DeviceType.NORMAL_MEDIA);
    }

    private PromiseInOut<List<MediaDevice>, MediaDevice> oneOfType(DeviceType deviceType) {
        return audioDeviceManager.enumerateDevices(deviceType)
                .then((ThenValue<List<MediaDevice>, MediaDevice>) (mediaDevices) -> {
                    if (null == mediaDevices) return null;

                    for (MediaDevice device : mediaDevices) {
                        if (deviceType.equals(device.deviceType())) {
                            return device;
                        }
                    }

                    throw new IllegalStateException("Expected speaker...");
                });
    }
}
