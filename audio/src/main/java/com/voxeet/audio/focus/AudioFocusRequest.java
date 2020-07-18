package com.voxeet.audio.focus;

import android.media.AudioManager;
import android.support.annotation.NonNull;

import com.voxeet.promise.Promise;

public interface AudioFocusRequest {

    Promise<Integer> requestAudioFocus(@NonNull AudioManager manager, int audioFocusVolumeType);

    Promise<Integer> abandonAudioFocus(@NonNull AudioManager manager);

}
