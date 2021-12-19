package com.voxeet.audio.focus;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.voxeet.audio.utils.Log;
import com.voxeet.promise.Promise;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AudioFocusRequest26 implements AudioFocusRequest {

    private final AudioAttributes playbackAttributes;

    private static Handler sHandler = new Handler();
    private final AudioManager.OnAudioFocusChangeListener focusRequest;
    private final android.media.AudioFocusRequest focusRequestBuilt;
    private final AudioFocusMode mode;

    @RequiresApi(Build.VERSION_CODES.O)
    public AudioFocusRequest26(AudioFocusMode mode) {

        int usage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
        int content = AudioAttributes.CONTENT_TYPE_SPEECH;
        this.mode = mode;
        switch (mode) {
            case MEDIA:
                usage = AudioAttributes.USAGE_MEDIA;
                content = AudioAttributes.CONTENT_TYPE_MUSIC;
                break;
            default:
        }
        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(usage)
                .setContentType(content)
                .build();

        focusRequest = focusChange -> Log.d("AudioFocusRequest", "onAudioFocusChange: " + focusChange);

        focusRequestBuilt = new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(focusRequest, sHandler)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Promise<Integer> requestAudioFocus(@NonNull AudioManager manager, int audioFocusVolumeType) {
        return new Promise<>(solver -> {
            int new_mode = AudioFocusMode.CALL.equals(mode) ?
                    AudioManager.MODE_IN_COMMUNICATION :
                    AudioManager.MODE_NORMAL; //MODE_IN_CALL)
            AudioFocusManagerAsync.setMode(manager, new_mode, "AudioFocusRequest").then(aBoolean -> {
                Log.d("AudioFocusRequest", "requestAudioFocus");
                manager.requestAudioFocus(null,
                        audioFocusVolumeType, //AudioManager.STREAM_VOICE_CALL,
                        AudioManager.AUDIOFOCUS_GAIN);

                solver.resolve(manager.requestAudioFocus(focusRequestBuilt));
            }).error(solver::reject);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Promise<Integer> abandonAudioFocus(@NonNull AudioManager manager) {
        return new Promise<>(solver -> {
            Log.d("AudioFocusRequest", "abandonAudioFocus");
            int result = manager.abandonAudioFocusRequest(focusRequestBuilt);
            solver.resolve(result);
        });
    }
}
