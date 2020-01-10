package com.voxeet.audiosample;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.voxeet.audio.utils.__Opt;
import com.voxeet.audio2.AudioDeviceManager;
import com.voxeet.audio2.devices.MediaDeviceHelper;
import com.voxeet.audio2.devices.description.DeviceType;
import com.voxeet.audio2.devices.MediaDevice;
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
    private View speaker_on;
    private View speaker_off;
    private View internal_call;
    private View internal_media;
    private TextView speaker_on_state;
    private TextView speaker_off_state;
    private TextView internal_call_state;
    private TextView internal_media_state;
    private TextView wired_headsets_state;
    private boolean enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speaker_on = findViewById(R.id.speaker_on);
        speaker_off = findViewById(R.id.speaker_off);
        internal_call = findViewById(R.id.internal_call);
        internal_media = findViewById(R.id.internal_media);
        speaker_on_state = findViewById(R.id.speaker_on_state);
        speaker_off_state = findViewById(R.id.speaker_off_state);
        internal_call_state = findViewById(R.id.internal_call_state);
        internal_media_state = findViewById(R.id.internal_media_state);
        wired_headsets_state = findViewById(R.id.wired_headsets_state);


        audioDeviceManager = new AudioDeviceManager(this,
                this::onNewDevices);

        stream_type = findViewById(R.id.stream_type);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;

        handler = new Handler();
        handler.post(runnable);
        if (shouldAskPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 0x20);
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
        updateViews();
    }

    private void updateViews() {
        if (!enabled) {
            speaker_on.setEnabled(false);
            speaker_off.setEnabled(false);
            internal_call.setEnabled(false);
            internal_media.setEnabled(false);
        } else {
            speaker_on.setEnabled(true);
            speaker_off.setEnabled(true);
            internal_call.setEnabled(true);
            internal_media.setEnabled(true);

            //little scenario here
            List<MediaDevice> wired_headsets = audioDeviceManager.filter(known_devices, DeviceType.WIRED_HEADSET);
            List<MediaDevice> external_speaker = audioDeviceManager.filter(known_devices, DeviceType.EXTERNAL_SPEAKER);
            List<MediaDevice> internal_speaker = audioDeviceManager.filter(known_devices, DeviceType.INTERNAL_SPEAKER);
            List<MediaDevice> normal_media = audioDeviceManager.filter(known_devices, DeviceType.NORMAL_MEDIA);
            List<MediaDevice> bluetooth = audioDeviceManager.filter(known_devices, DeviceType.BLUETOOTH);
            List<MediaDevice> usb = audioDeviceManager.filter(known_devices, DeviceType.USB);

            speaker_on_state.setText(MediaDeviceHelper.hasConnected(external_speaker) ? "true":"false");
            speaker_off_state.setText(MediaDeviceHelper.hasConnected(internal_speaker) ? "true":"false");
            internal_call_state.setText(MediaDeviceHelper.hasConnected(internal_speaker) ? "true":"false");
            wired_headsets_state.setText(MediaDeviceHelper.hasConnected(wired_headsets) ? "true":"false");
            internal_media_state.setText(MediaDeviceHelper.hasConnected(normal_media) ? "true":"false");

            if(MediaDeviceHelper.hasConnected(external_speaker)) {
                speaker_on.setEnabled(false);
                speaker_off.setEnabled(true);
                internal_call.setEnabled(true);
                internal_media.setEnabled(true);
            } else if(MediaDeviceHelper.hasConnected(internal_speaker)) {
                speaker_on.setEnabled(true);
                speaker_off.setEnabled(false);
                internal_call.setEnabled(false);
                internal_media.setEnabled(true);
            } else if(MediaDeviceHelper.hasConnected(wired_headsets)) {
                speaker_on.setEnabled(true);
                speaker_off.setEnabled(false);
                internal_call.setEnabled(false);
                internal_media.setEnabled(true);
            } else if(MediaDeviceHelper.hasConnected(bluetooth)) {
                speaker_on.setEnabled(true);
                speaker_off.setEnabled(true);
                internal_call.setEnabled(true);
                internal_media.setEnabled(true);
            } else if(MediaDeviceHelper.hasConnected(usb)) {
                speaker_on.setEnabled(true);
                speaker_off.setEnabled(true);
                internal_call.setEnabled(true);
                internal_media.setEnabled(true);
            } else { //normal_media
                speaker_on.setEnabled(true);
                speaker_off.setEnabled(true);
                internal_call.setEnabled(true);
                internal_media.setEnabled(false);
                internal_media_state.setText("true // force default");
            }
        }

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
