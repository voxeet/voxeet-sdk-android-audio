package com.voxeet.system.record;

import android.support.annotation.NonNull;

public interface IMicrophoneRecordStateListener {

    void onState(@NonNull MicrophoneRecordState state);

}
