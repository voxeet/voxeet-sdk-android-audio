package com.voxeet.system.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.system.log.Logging;

import java.nio.ByteBuffer;

public class MicrophoneRecord {
    private static final String TAG = MicrophoneRecord.class.getSimpleName();

    // Default audio data format is PCM 16 bit per sample.
    // Guaranteed to be supported by all devices.
    public static final int BITS_PER_SAMPLE = 16;

    public static final int DEFAULT_SAMPLE_RATE = 48000;

    // Requested size of each recorded buffer provided to the client.
    public static final int CALLBACK_BUFFER_SIZE_MS = 10;

    // Average number of callbacks per second.
    public static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;

    // We ask for a native buffer size of BUFFER_SIZE_FACTOR * (minimum required
    // buffer size). The extra space is allocated to guard against glitches under
    // high load.
    private static final int BUFFER_SIZE_FACTOR = 2;

    // The AudioRecordJavaThread is allowed to wait for successful call to join()
    // but the wait times out afther this amount of time.
    public static final long AUDIO_RECORD_THREAD_JOIN_TIMEOUT_MS = 2000;

    public static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;

    private final int audioSource;
    private final IMicrophoneInformationProvider informationProvider;
    private final AudioEffects effects;
    private IMicrophoneRecordStateListener stateListener;
    private MicrophoneRecordState state;

    @Nullable
    private ByteBuffer byteBuffer;

    @Nullable
    private AudioRecord audioRecord;

    @Nullable
    private AudioRecordThread audioThread;

    public static boolean activated = true;
    private boolean microphoneMute = false;
    private AudioRecordThread.AudioRecordSampleListener listenerAudioRecordSampleListener;

    /**
     * Audio thread which keeps calling ByteBuffer.read() waiting for audio
     * to be recorded. Feeds recorded data to the native counterpart as a
     * periodic sequence of callbacks using DataIsRecorded().
     * This thread uses a Process.THREAD_PRIORITY_URGENT_AUDIO priority.
     */

    public MicrophoneRecord(int audioSource,
                            @NonNull IMicrophoneRecordStateListener stateListener,
                            @NonNull IMicrophoneInformationProvider informationProvider,
                            @NonNull AudioRecordThread.AudioRecordSampleListener listenerAudioRecordSampleListener,
                            @Nullable AudioEffects effects) {
        this.stateListener = stateListener;

        this.listenerAudioRecordSampleListener = listenerAudioRecordSampleListener;
        this.effects = effects;
        this.informationProvider = informationProvider;
        this.audioSource = audioSource;
    }

    public int initRecording(int sampleRate, int channels) {
        setState(MicrophoneRecordState.INITIALIZING);
        Logging.d(TAG, "initRecording(sampleRate=" + sampleRate + ", channels=" + channels + ")");
        if (audioRecord != null) {
            informationProvider.reportInitError("InitRecording called twice without StopRecording.");
            setState(MicrophoneRecordState.INITIALIZED_FAILED);
            return -1;
        }
        final int bytesPerFrame = channels * (BITS_PER_SAMPLE / 8);
        final int framesPerBuffer = sampleRate / BUFFERS_PER_SECOND;
        byteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * framesPerBuffer);
        if (!(byteBuffer.hasArray())) {
            informationProvider.reportInitError("ByteBuffer does not have backing array.");
            setState(MicrophoneRecordState.INITIALIZED_FAILED);
            return -1;
        }

        Logging.d(TAG, "byteBuffer.capacity: " + byteBuffer.capacity());
        // Rather than passing the ByteBuffer with every callback (requiring
        // the potentially expensive GetDirectBufferAddress) we simply have the
        // the native class cache the address to the memory once.
        informationProvider.nativeBufferAddress(byteBuffer);

        // Get the minimum buffer size required for the successful creation of
        // an AudioRecord object, in byte units.
        // Note that this size doesn't guarantee a smooth recording under load.
        final int channelConfig = channelCountToConfiguration(channels);
        int minBufferSize =
                AudioRecord.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            informationProvider.reportInitError("AudioRecord.getMinBufferSize failed: " + minBufferSize);
            setState(MicrophoneRecordState.INITIALIZED_FAILED);
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
            setState(MicrophoneRecordState.INITIALIZED);
            return framesPerBuffer;
        }

        try {
            audioRecord = new AudioRecord(audioSource, sampleRate, channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        } catch (IllegalArgumentException e) {
            informationProvider.reportInitError("AudioRecord ctor error: " + e.getMessage());
            releaseAudioResources();
            setState(MicrophoneRecordState.INITIALIZED_FAILED);
            return -1;
        }
        if (audioRecord == null || audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            informationProvider.reportInitError("Failed to create a new AudioRecord instance");
            releaseAudioResources();
            setState(MicrophoneRecordState.INITIALIZED_FAILED);
            return -1;
        }

        if (effects != null) {
            effects.enable(audioRecord.getAudioSessionId());
        }
        logMainParameters();
        logMainParametersExtended();
        setState(MicrophoneRecordState.INITIALIZED);
        return framesPerBuffer;
    }

    public boolean startRecording() {
        setState(MicrophoneRecordState.STARTING);
        Logging.d(TAG, "startRecording");
        if (null == byteBuffer) {
            setState(MicrophoneRecordState.STARTED_ERROR);
            setState(MicrophoneRecordState.STOPPED);
            return false;
        }

        if (!activated) {
            //if the mic is deactivated completely, we start but it will be empty
            audioThread = new AudioRecordThread("AudioRecordDeactivatedJavaThread", false,
                    audioRecord,
                    listenerAudioRecordSampleListener,
                    byteBuffer);
            audioThread.activate(microphoneMute);
            audioThread.start();
            return true;
        }

        assertTrue(audioRecord != null);
        assertTrue(audioThread == null);
        try {
            Log.d(TAG, "startRecording !");
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            informationProvider.reportStartError(AudioRecordStartErrorCode.AUDIO_RECORD_START_EXCEPTION,
                    "AudioRecord.startRecording failed: " + e.getMessage());
            setState(MicrophoneRecordState.STARTED_ERROR);
            setState(MicrophoneRecordState.STOPPED);
            return false;
        }
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            informationProvider.reportStartError(AudioRecordStartErrorCode.AUDIO_RECORD_START_STATE_MISMATCH,
                    "AudioRecord.startRecording failed - incorrect state :"
                            + audioRecord.getRecordingState());
            setState(MicrophoneRecordState.STARTED_ERROR);
            setState(MicrophoneRecordState.STOPPED);
            return false;
        }

        audioThread = new AudioRecordThread("AudioRecordDeactivatedJavaThread", false,
                audioRecord,
                listenerAudioRecordSampleListener,
                byteBuffer);

        audioThread.start();
        setState(MicrophoneRecordState.STARTED);
        return true;
    }

    public boolean stopRecording() {
        setState(MicrophoneRecordState.STOPPING);
        Logging.d(TAG, "stopRecording");
        assertTrue(audioThread != null);
        audioThread.stopThread();
        Log.d(TAG, "stopRecording: >>>>>>>>>>>>>>>>>>>>>>");
        Log.d(TAG, "stopRecording: >>>>>>>>>>>>>>>>>>>>>>");
        Log.d(TAG, "stopRecording: TODO STILL NEED TO JOIN THREAD ON STOP");
        Log.d(TAG, "stopRecording: <<<<<<<<<<<<<<<<<<<<<<");
        Log.d(TAG, "stopRecording: <<<<<<<<<<<<<<<<<<<<<<");
        /*if (!ThreadUtils.joinUninterruptibly(audioThread, AUDIO_RECORD_THREAD_JOIN_TIMEOUT_MS)) {
            Logging.e(TAG, "Join of AudioRecordJavaThread timed out");
            //WebRtcAudioUtils.logAudioState(TAG, context, audioManager);
        }*/
        audioThread = null;
        if (effects != null) {
            effects.release();
        }
        releaseAudioResources();
        setState(MicrophoneRecordState.STOPPED);
        return true;
    }

    public void logMainParameters() {
        if (audioRecord != null) {
            Log.d(TAG,
                    "AudioRecord: "
                            + "session ID: " + audioRecord.getAudioSessionId() + ", "
                            + "channels: " + audioRecord.getChannelCount() + ", "
                            + "sample rate: " + audioRecord.getSampleRate());
        }
    }

    public void logMainParametersExtended() {
        if (Build.VERSION.SDK_INT >= 23 && audioRecord != null) {
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
        if (null != audioThread) {
            audioThread.activate(microphoneMute);
        }
    }

    // Releases the native AudioRecord resources.
    private void releaseAudioResources() {
        Logging.d(TAG, "releaseAudioResources");
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }

    public MicrophoneRecordState state() {
        return state;
    }

    private void setState(@NonNull MicrophoneRecordState state) {
        this.state = state;
        stateListener.onState(state);
    }
}
