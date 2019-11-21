package com.voxeet.system.record;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.ByteBuffer;

public class MicrophoneRecord {
    private static final String TAG = MicrophoneRecord.class.getSimpleName();

    // Default audio data format is PCM 16 bit per sample.
    // Guaranteed to be supported by all devices.
    private static final int BITS_PER_SAMPLE = 16;

    // Requested size of each recorded buffer provided to the client.
    private static final int CALLBACK_BUFFER_SIZE_MS = 10;

    // Average number of callbacks per second.
    private static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;

    // We ask for a native buffer size of BUFFER_SIZE_FACTOR * (minimum required
    // buffer size). The extra space is allocated to guard against glitches under
    // high load.
    private static final int BUFFER_SIZE_FACTOR = 2;

    // The AudioRecordJavaThread is allowed to wait for successful call to join()
    // but the wait times out afther this amount of time.
    private static final long AUDIO_RECORD_THREAD_JOIN_TIMEOUT_MS = 2000;

    public static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;

    private final Context context;
    private final AudioManager audioManager;
    private final int audioSource;

    private long nativeAudioRecord;

    @Nullable
    private ByteBuffer byteBuffer;

    @Nullable
    private AudioRecord audioRecord;

    @Nullable
    private AudioRecordThread audioThread;

    private volatile boolean microphoneMute;

    @Nullable
    private final SamplesReadyCallback audioSamplesReadyCallback;

    private final boolean isAcousticEchoCancelerSupported;
    private final boolean isNoiseSuppressorSupported;

    public static boolean activated;// = true;

    /**
     * Audio thread which keeps calling ByteBuffer.read() waiting for audio
     * to be recorded. Feeds recorded data to the native counterpart as a
     * periodic sequence of callbacks using DataIsRecorded().
     * This thread uses a Process.THREAD_PRIORITY_URGENT_AUDIO priority.
     */


    @CalledByNative
    WebRtcAudioRecord(Context context, AudioManager audioManager) {
        this(context, audioManager, DEFAULT_AUDIO_SOURCE, null /* errorCallback */,
                null /* audioSamplesReadyCallback */, WebRtcAudioEffects.isAcousticEchoCancelerSupported(),
                WebRtcAudioEffects.isNoiseSuppressorSupported());
    }

    public WebRtcAudioRecord(Context context, AudioManager audioManager, int audioSource,
                             @Nullable AudioRecordErrorCallback errorCallback,
                             @Nullable SamplesReadyCallback audioSamplesReadyCallback,
                             boolean isAcousticEchoCancelerSupported, boolean isNoiseSuppressorSupported) {
        if (isAcousticEchoCancelerSupported && !WebRtcAudioEffects.isAcousticEchoCancelerSupported()) {
            throw new IllegalArgumentException("HW AEC not supported");
        }
        if (isNoiseSuppressorSupported && !WebRtcAudioEffects.isNoiseSuppressorSupported()) {
            throw new IllegalArgumentException("HW NS not supported");
        }
        this.context = context;
        this.audioManager = audioManager;
        this.audioSource = audioSource;
        this.errorCallback = errorCallback;
        this.audioSamplesReadyCallback = audioSamplesReadyCallback;
        this.isAcousticEchoCancelerSupported = isAcousticEchoCancelerSupported;
        this.isNoiseSuppressorSupported = isNoiseSuppressorSupported;
    }

    @CalledByNative
    private int initRecording(int sampleRate, int channels) {
        Logging.d(TAG, "initRecording(sampleRate=" + sampleRate + ", channels=" + channels + ")");
        if (audioRecord != null) {
            reportWebRtcAudioRecordInitError("InitRecording called twice without StopRecording.");
            return -1;
        }
        final int bytesPerFrame = channels * (BITS_PER_SAMPLE / 8);
        final int framesPerBuffer = sampleRate / BUFFERS_PER_SECOND;
        byteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * framesPerBuffer);
        if (!(byteBuffer.hasArray())) {
            reportWebRtcAudioRecordInitError("ByteBuffer does not have backing array.");
            return -1;
        }
        Logging.d(TAG, "byteBuffer.capacity: " + byteBuffer.capacity());
        // Rather than passing the ByteBuffer with every callback (requiring
        // the potentially expensive GetDirectBufferAddress) we simply have the
        // the native class cache the address to the memory once.
        nativeCacheDirectBufferAddress(nativeAudioRecord, byteBuffer);

        // Get the minimum buffer size required for the successful creation of
        // an AudioRecord object, in byte units.
        // Note that this size doesn't guarantee a smooth recording under load.
        final int channelConfig = channelCountToConfiguration(channels);
        int minBufferSize =
                AudioRecord.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            reportWebRtcAudioRecordInitError("AudioRecord.getMinBufferSize failed: " + minBufferSize);
            return -1;
        }
        Logging.d(TAG, "AudioRecord.getMinBufferSize: " + minBufferSize);

        // Use a larger buffer size than the minimum required when creating the
        // AudioRecord instance to ensure smooth recording under load. It has been
        // verified that it does not increase the actual recording latency.
        int bufferSizeInBytes = Math.max(BUFFER_SIZE_FACTOR * minBufferSize, byteBuffer.capacity());
        Logging.d(TAG, "bufferSizeInBytes: " + bufferSizeInBytes);

        if (!activated) {
            //bypass the current state to make sure the micro is being deactivated
            return framesPerBuffer;
        }

        try {
            audioRecord = new AudioRecord(audioSource, sampleRate, channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        } catch (IllegalArgumentException e) {
            reportWebRtcAudioRecordInitError("AudioRecord ctor error: " + e.getMessage());
            releaseAudioResources();
            return -1;
        }
        if (audioRecord == null || audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            reportWebRtcAudioRecordInitError("Failed to create a new AudioRecord instance");
            releaseAudioResources();
            return -1;
        }

        effects.enable(audioRecord.getAudioSessionId());
        logMainParameters();
        logMainParametersExtended();
        return framesPerBuffer;
    }

    @CalledByNative
    private boolean startRecording() {
        Logging.d(TAG, "startRecording");

        if (!activated) {
            //if the mic is deactivated completely, we start but it will be empty
            audioThread = new AudioRecordThread("AudioRecordDeactivatedJavaThread", false);
            audioThread.start();
            return true;
        }

        assertTrue(audioRecord != null);
        assertTrue(audioThread == null);
        try {
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            reportWebRtcAudioRecordStartError(AudioRecordStartErrorCode.AUDIO_RECORD_START_EXCEPTION,
                    "AudioRecord.startRecording failed: " + e.getMessage());
            return false;
        }
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            reportWebRtcAudioRecordStartError(AudioRecordStartErrorCode.AUDIO_RECORD_START_STATE_MISMATCH,
                    "AudioRecord.startRecording failed - incorrect state :"
                            + audioRecord.getRecordingState());
            return false;
        }
        audioThread = new AudioRecordThread("AudioRecordJavaThread");
        audioThread.start();
        return true;
    }

    private boolean stopRecording() {
        Logging.d(TAG, "stopRecording");
        assertTrue(audioThread != null);
        audioThread.stopThread();
        if (!ThreadUtils.joinUninterruptibly(audioThread, AUDIO_RECORD_THREAD_JOIN_TIMEOUT_MS)) {
            Logging.e(TAG, "Join of AudioRecordJavaThread timed out");
            WebRtcAudioUtils.logAudioState(TAG, context, audioManager);
        }
        audioThread = null;
        effects.release();
        releaseAudioResources();
        return true;
    }

    private void logMainParameters() {
        Log.d(TAG,
                "AudioRecord: "
                        + "session ID: " + audioRecord.getAudioSessionId() + ", "
                        + "channels: " + audioRecord.getChannelCount() + ", "
                        + "sample rate: " + audioRecord.getSampleRate());
    }

    private void logMainParametersExtended() {
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d(TAG,
                    "AudioRecord: "
                            // The frame count of the native AudioRecord buffer.
                            + "buffer size in frames: " + audioRecord.getBufferSizeInFrames());
        }
    }

    // Helper method which throws an exception  when an assertion has failed.
    static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    private int channelCountToConfiguration(int channels) {
        return (channels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
    }

    // Sets all recorded samples to zero if |mute| is true, i.e., ensures that
    // the microphone is muted.
    public void setMicrophoneMute(boolean mute) {
        Log.w(TAG, "setMicrophoneMute(" + mute + ")");
        microphoneMute = mute;
    }

    // Releases the native AudioRecord resources.
    private void releaseAudioResources() {
        Logging.d(TAG, "releaseAudioResources");
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }
}
