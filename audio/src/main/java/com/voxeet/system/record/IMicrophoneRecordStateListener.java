package com.voxeet.system.record;

import androidx.annotation.NonNull;

public interface IMicrophoneRecordStateListener {

    void onState(@NonNull MicrophoneRecordState state);

}
