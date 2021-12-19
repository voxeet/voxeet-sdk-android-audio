package com.voxeet.audio2.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import androidx.annotation.NonNull;

public class SystemAudioManager {
    private Context context;
    private AudioManager audioManager;

    private BroadcastReceiver audioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    private SystemAudioManager() {

    }

    public SystemAudioManager(@NonNull Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public AudioManager audioManager() {
        return audioManager;
    }

    public void init() {
    }
}
