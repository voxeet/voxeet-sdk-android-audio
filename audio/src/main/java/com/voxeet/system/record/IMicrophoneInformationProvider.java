package com.voxeet.system.record;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public interface IMicrophoneInformationProvider {
    boolean isNoiseSuppressorSupported();

    boolean isAcousticEchoCancelerSupported();

    void reportInitError(@NonNull String string);

    void reportStartError(@NonNull AudioRecordStartErrorCode audioRecordStartStateMismatch,
                          @NonNull String string);

    void nativeBufferAddress(@NonNull ByteBuffer byteBuffer);
}
