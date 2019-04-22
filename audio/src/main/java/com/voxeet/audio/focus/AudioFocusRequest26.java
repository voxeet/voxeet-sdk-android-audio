package com.voxeet.audio.focus;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AudioFocusRequest26 implements AudioFocusRequest {

    private final AudioAttributes playbackAttributes;

    private static Handler sHandler = new Handler();
    private final AudioManager.OnAudioFocusChangeListener focusRequest;
    private final android.media.AudioFocusRequest focusRequestBuilt;

    @RequiresApi(Build.VERSION_CODES.O)
    public AudioFocusRequest26() {
        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        focusRequest = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.d("AudioFocusRequest", "onAudioFocusChange: " + focusChange);
            }
        };

        focusRequestBuilt = new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(focusRequest, sHandler)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int requestAudioFocus(@NonNull AudioManager manager) {
        manager.setMode(AudioManager.MODE_IN_CALL);
        Log.d("AudioFocusRequest", "requestAudioFocus");
        manager.requestAudioFocus(null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN);

        return manager.requestAudioFocus(focusRequestBuilt);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int abandonAudioFocus(@NonNull AudioManager manager) {
        Log.d("AudioFocusRequest", "abandonAudioFocus");
        return manager.abandonAudioFocusRequest(focusRequestBuilt);
    }
}
