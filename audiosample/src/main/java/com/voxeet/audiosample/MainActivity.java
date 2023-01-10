package com.voxeet.audiosample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.sample.oboe.hellooboe.PlaybackEngine;
import com.voxeet.audio.utils.__Opt;
import com.voxeet.audio2.AudioDeviceManager;
import com.voxeet.audio2.devices.BluetoothDevice;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.MediaDeviceHelper;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audiosample.devices.DevicesAdapter;
import com.voxeet.promise.Promise;
import com.voxeet.promise.PromiseInOut;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.promise.solve.ThenValue;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AudioSample";
    private AudioDeviceManager audioDeviceManager;

    private TextView stream_type;

    private Handler handler;
    private Runnable runnable = () -> {
        updateView();
        if (null != handler) handler.postDelayed(this.runnable, 5000);
    };
    private List<MediaDevice> known_devices;
    private boolean isResumed = false;

    private TextView speaker_on_state;
    private TextView speaker_off_state;
    private TextView internal_call_state;
    private TextView internal_media_state;
    private TextView wired_headsets_state;
    private TextView active_bluetooth_device;
    private RecyclerView devices_list;
    private boolean enabled = false;
    private DevicesAdapter devicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlaybackEngine.create();

        speaker_on_state = findViewById(R.id.speaker_on_state);
        speaker_off_state = findViewById(R.id.speaker_off_state);
        internal_call_state = findViewById(R.id.internal_call_state);
        internal_media_state = findViewById(R.id.internal_media_state);
        wired_headsets_state = findViewById(R.id.wired_headsets_state);
        active_bluetooth_device = findViewById(R.id.active_bluetooth_device);
        devices_list = findViewById(R.id.devices_list);

        //set standard layout manager
        devices_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        devicesAdapter = new DevicesAdapter(this::connectDevice);
        devices_list.setAdapter(devicesAdapter);
        audioDeviceManager = new AudioDeviceManager(this,
                this::onNewDevices);

        stream_type = findViewById(R.id.stream_type);
    }

    private void connectDevice(@NonNull MediaDevice mediaDevice) {
        audioDeviceManager.connect(mediaDevice).then(b -> {
            Log.d(TAG, "direct call done");
        }).error(Throwable::printStackTrace);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;

        PlaybackEngine.setToneOn(true);

        handler = new Handler();
        handler.post(runnable);

        List<String> permissions = new ArrayList();
        permissions.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        List<String> toAsk = new ArrayList();

        for (String permission : permissions) {
            if (shouldAskPermission(permission)) toAsk.add(permission);
        }

        if (toAsk.size() > 0) {
            ActivityCompat.requestPermissions(this, toAsk.toArray(new String[0]), 0x20);
        }

        audioDeviceManager.enumerateDevices().then(devices -> {
            if (isResumed) {
                updateCapabilities(__Opt.of(devices).or(new ArrayList<>()));
            }
        }).error(Throwable::printStackTrace);
        updateCapabilities(known_devices);

    }

    @Override
    protected void onPause() {
        PlaybackEngine.setToneOn(false);
        isResumed = false;
        handler.removeCallbacks(runnable);
        handler = null;
        super.onPause();
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
        oneInternal().then(audioDeviceManager::connect)
                .then((ThenPromise<Boolean, List<MediaDevice>>) r -> audioDeviceManager.enumerateDevices())
                .then(r -> {
                    Toast.makeText(MainActivity.this, "onResume: speaker undone ?! " + audioDeviceManager.systemAudioManager().audioManager().isSpeakerphoneOn(), Toast.LENGTH_SHORT).show();
                    enable();
                })
                .error(Throwable::printStackTrace);
    }

    public void disable() {
        this.enabled = false;
        updateViews();
    }

    public void enable() {
        this.enabled = true;
        updateViews();
    }

    public boolean shouldAskPermission(String permission) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
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
        return audioDeviceManager.enumerateDevices()
                .then((ThenValue<List<MediaDevice>, MediaDevice>) (mediaDevices) -> {
                    if (null == mediaDevices) return null;

                    for (MediaDevice device : audioDeviceManager.filter(mediaDevices, deviceType)) {
                        if (deviceType.equals(device.deviceType())) {
                            return device;
                        }
                    }

                    throw new IllegalStateException("Expected speaker...");
                });
    }

    private void updateView() {
        int type = getVolumeControlStream();
        Log.d(TAG, "updateView: " + type);
        stream_type.setText("type : " + type);
    }

    private void updateCapabilities(@NonNull List<MediaDevice> devices) {
        if (null == known_devices) enabled = true;
        this.known_devices = devices;
        devicesAdapter.update(__Opt.of(known_devices).or(new ArrayList<>()));
        updateViews();
    }

    private void updateViews() {
        //little scenario here
        List<MediaDevice> wired_headsets = audioDeviceManager.filter(known_devices, DeviceType.WIRED_HEADSET);
        List<MediaDevice> external_speaker = audioDeviceManager.filter(known_devices, DeviceType.EXTERNAL_SPEAKER);
        List<MediaDevice> internal_speaker = audioDeviceManager.filter(known_devices, DeviceType.INTERNAL_SPEAKER);
        List<MediaDevice> normal_media = audioDeviceManager.filter(known_devices, DeviceType.NORMAL_MEDIA);
        List<MediaDevice> bluetooth = audioDeviceManager.filter(known_devices, DeviceType.BLUETOOTH);
        List<MediaDevice> usb = audioDeviceManager.filter(known_devices, DeviceType.USB);

        speaker_on_state.setText(MediaDeviceHelper.hasConnected(external_speaker) ? "true" : "false");
        speaker_off_state.setText(MediaDeviceHelper.hasConnected(internal_speaker) ? "true" : "false");
        internal_call_state.setText(MediaDeviceHelper.hasConnected(internal_speaker) ? "true" : "false");
        wired_headsets_state.setText(MediaDeviceHelper.hasConnected(wired_headsets) ? "true" : "false");
        internal_media_state.setText(MediaDeviceHelper.hasConnected(normal_media) ? "true" : "false");

        BluetoothDevice active = audioDeviceManager.bluetoothHeadsetDeviceManager().active();
        active_bluetooth_device.setText(__Opt.of(active).then(MediaDevice::id).or(""));

        devicesAdapter.notifyDataSetChanged();

        com.voxeet.audio.utils.Log.enable(true);
        if (null != known_devices) {
            audioDeviceManager.dump(known_devices);
        }
    }

    private void onNewDevices(@NonNull Promise<List<MediaDevice>> devices) {
        devices.then(r -> {
            Toast.makeText(MainActivity.this, "onResume: internal media done ?! false==" + audioDeviceManager.systemAudioManager().audioManager().isSpeakerphoneOn(), Toast.LENGTH_SHORT).show();
            updateCapabilities(__Opt.of(r).or(new ArrayList<>()));
        }).error(Throwable::printStackTrace);
    }
}
