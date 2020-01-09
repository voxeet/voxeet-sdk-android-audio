package com.voxeet.audiosample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.voxeet.system.record.AudioRecordStartErrorCode;
import com.voxeet.system.record.AudioRecordThread;
import com.voxeet.system.record.IMicrophoneInformationProvider;
import com.voxeet.system.record.IMicrophoneRecordStateListener;
import com.voxeet.system.record.MicrophoneRecord;
import com.voxeet.system.record.MicrophoneRecordState;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivityMicrophone extends AppCompatActivity {

    private AudioRecordThread.AudioRecordSampleListener samplerListener = new AudioRecordThread.AudioRecordSampleListener() {
        @Override
        public boolean isMicrophoneMuted() {
            return false;
        }

        @Override
        public void onDataRead(int read) {
            Log.d("MainActivity", "onDataRead: " + read);
        }

        @Override
        public void onData(@NonNull byte[] rawdata) {
            Log.d("MainActivity", "onData: " + Arrays.toString(rawdata));
        }

        @Override
        public void onReadFailed(@NonNull String s) {

        }
    };

    private IMicrophoneInformationProvider provider = new IMicrophoneInformationProvider() {
        @Override
        public boolean isNoiseSuppressorSupported() {
            return false;
        }

        @Override
        public boolean isAcousticEchoCancelerSupported() {
            return false;
        }

        @Override
        public void reportInitError(@NonNull String string) {
            Log.d("MainActivity", "reportInitError: " + string);
        }

        @Override
        public void reportStartError(@NonNull AudioRecordStartErrorCode audioRecordStartStateMismatch,
                                     @NonNull String string) {
            Log.d("MainActivity", "reportStartError: " + audioRecordStartStateMismatch + " " + string);
        }

        @Override
        public void nativeBufferAddress(@NonNull ByteBuffer byteBuffer) {
            Log.d("MainActivity", "nativeBufferAddress: " + byteBuffer);
        }
    };

    private IMicrophoneRecordStateListener stateListener = new IMicrophoneRecordStateListener() {

        @Override
        public void onState(@NonNull MicrophoneRecordState state) {
            Toast.makeText(MainActivityMicrophone.this, "state : " + state, Toast.LENGTH_SHORT).show();
        }
    };

    @NonNull
    private MicrophoneRecord microphoneRecord = new MicrophoneRecord(MicrophoneRecord.DEFAULT_AUDIO_SOURCE,
            stateListener,
            provider,
            samplerListener,
            null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_microphone);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public boolean shouldAskPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                return true; //force a state where it has been asked hehe
            } else {
                return true;
            }
        }
        return false;
    }

    public void onStartMic(@NonNull View view) {
        if (shouldAskPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0x20);
        } else {
            microphoneRecord.initRecording(MicrophoneRecord.DEFAULT_SAMPLE_RATE, 1);
            microphoneRecord.startRecording();
        }
    }

    public void onStopMic(@NonNull View view) {
        microphoneRecord.stopRecording();
    }
}
