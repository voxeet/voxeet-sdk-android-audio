package com.voxeet.audio.focus;

import android.media.AudioManager;
import android.support.annotation.NonNull;

public interface AudioFocusRequest {

    int requestAudioFocus(@NonNull AudioManager manager, int audioFocusVolumeType);

    int abandonAudioFocus(@NonNull AudioManager manager);

}
