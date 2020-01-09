package com.voxeet.system.record;

import android.media.AudioRecord;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AudioRecordThread extends Thread {
    private ReentrantLock lock = new ReentrantLock();
    private final static String TAG = AudioRecordThread.class.getSimpleName();

    private CountDownLatch countDownLatch = null;

    @Nullable
    private AudioRecord audioRecord;

    @Nullable
    private AudioRecordSampleListener listener;

    @NonNull
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1);

    private volatile boolean keepAlive = true;
    private boolean activated;
    private byte[] emptyBytes;
    private boolean started = false;

    private AudioRecordThread(String name) {
        super(name);
        this.activated = true;
        this.emptyBytes = new byte[byteBuffer.capacity()];
    }

    public AudioRecordThread(String name, boolean activated,
                             @NonNull AudioRecord audioRecord,
                             @NonNull AudioRecordSampleListener listener,
                             @NonNull ByteBuffer byteBuffer) {
        this(name);

        Log.d("AudioRecordThread", "AudioRecordThread: creating a specific AudioRecordThread :: " + activated);
        this.activated = activated;

        setAudioRecord(audioRecord);
        this.listener = listener;
        this.byteBuffer = byteBuffer;
    }

    public void activate(boolean activated) {
        this.activated = activated;
    }

    @Override
    public void run() {
        started = true;
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        Log.d(TAG, "AudioRecordThread");
        MicrophoneRecord.assertTrue(null != audioRecord && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);

        long lastTime = System.nanoTime();
        while (keepAlive && null != listener) {
            AudioRecordSampleListener listener = this.listener;
            int bytesRead = 0;

            //if the mic is activated
            lock();
            if (activated) {
                bytesRead = audioRecord.read(byteBuffer, byteBuffer.capacity());
            } else {
                bytesRead = emptyBytes.length;
                byteBuffer.clear();
                byteBuffer.put(emptyBytes);
            }
            Log.d(TAG, "run: read " + bytesRead);
            unlock();

            if (bytesRead == byteBuffer.capacity()) {
                if (listener.isMicrophoneMuted()) {
                    byteBuffer.clear();
                    byteBuffer.put(emptyBytes);
                }
                // It's possible we've been shut down during the read, and stopRecording() tried and
                // failed to join this thread. To be a bit safer, try to avoid calling any native methods
                // in case they've been unregistered after stopRecording() returned.
                if (keepAlive) {
                    listener.onDataRead(bytesRead);
                    byte[] data = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.arrayOffset(),
                            byteBuffer.capacity() + byteBuffer.arrayOffset());
                    listener.onData(data);
                }
            } else {
                String errorMessage = "AudioRecord.read failed: " + bytesRead;
                if (1 == bytesRead) {
                    errorMessage += byteBuffer.get(0) + " " + byteBuffer.capacity();
                }
                Log.e(TAG, errorMessage);
                if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    keepAlive = false;
                    listener.onReadFailed(errorMessage);
                    //reportWebRtc  AudioRecordError(errorMessage);
                }
            }
        }

        try {
            if (audioRecord != null) {
                audioRecord.stop();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "AudioRecord.stop failed: " + e.getMessage());
        }
        started = false;
        if (null != countDownLatch) {
            countDownLatch.countDown();
        }
    }

    public void setAudioRecord(@NonNull AudioRecord audioRecord) {
        lock();
        this.audioRecord = audioRecord;
        unlock();
    }

    @Override
    public synchronized void start() {
        countDownLatch = new CountDownLatch(1);
        super.start();
    }

    // Stops the inner thread loop and also calls AudioRecord.stop().
    // Does not block the calling thread.
    public void stopThread() {
        Log.d(TAG, "stopThread");
        keepAlive = false;
        activated = false;

        try {
            if (null != countDownLatch && started) {
                countDownLatch.await(MicrophoneRecord.AUDIO_RECORD_THREAD_JOIN_TIMEOUT_MS, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            countDownLatch = null;
        }
    }

    public static interface AudioRecordSampleListener {

        boolean isMicrophoneMuted();

        void onDataRead(int read);

        void onData(@NonNull byte[] rawdata);

        void onReadFailed(@NonNull String s);
    }

    private void lock() {
        try {
            lock.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unlock() {
        try {
            if (lock.isLocked()) lock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}