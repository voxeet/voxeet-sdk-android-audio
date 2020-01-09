package com.voxeet.audio2.manager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.MediaDeviceConnectionWrapper;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.Solver;

import java.util.ArrayList;
import java.util.List;

public class ConnectScheduler {
    private static final String TAG = ConnectScheduler.class.getSimpleName();
    @NonNull
    private final MediaDeviceConnectionWrapper connectionWrapper;
    private List<IOHolder> mediaDevices = new ArrayList<>();
    private IOHolder currenting;

    private IOHolder current = null;

    public ConnectScheduler() {
        connectionWrapper = MediaDeviceConnectionWrapper.unique();
    }

    public void pushConnect(MediaDevice mediaDevice, Solver<Boolean> solver) {
        checkDisconnectPrevious(mediaDevice);
        mediaDevices.add(new IOHolder(true, mediaDevice, solver));

        tryIO();
    }

    public void pushDisconnect(MediaDevice mediaDevice, Solver<Boolean> solver) {
        checkDisconnectPrevious(mediaDevice);
        mediaDevices.add(new IOHolder(false, mediaDevice, solver));

        tryIO();
    }

    private void checkDisconnectPrevious(MediaDevice mediaDevice) {
        if (null != current && !current.id().equals(mediaDevice.id())) {
            Log.d(TAG, "ConnectSchedumer // checkDisconnectPrevious: push disconnecting current " + current.id());
            mediaDevices.add(new IOHolder(false, current.mediaDevice, null));
        }
    }

    private void tryIO() {
        Log.d(TAG, "tryIO: currenting:=" + currenting + " mediaDevices:=" + mediaDevices.size());
        if (null == currenting && mediaDevices.size() > 0) {
            IOHolder holder = mediaDevices.get(0);
            mediaDevices.remove(0);
            currenting = holder;

            Promise<Boolean> promise;
            if (holder.connect) promise = connectionWrapper.connect(holder.mediaDevice);
            else promise = connectionWrapper.disconnect(holder.mediaDevice);

            final Solver<Boolean> solver = currenting.solver;

            promise.then(result -> {
                Log.d(TAG, "ConnectSchedumer // tryIO: " + result);
                if (holder.connect) {
                    Log.d(TAG, "ConnectSchedumer // tryIO: setting the current device connected");
                    current = holder;
                } else if (null != current && holder.id().equals(current.id())) {
                    Log.d(TAG, "ConnectSchedumer // tryIO: removing the current device connected");
                    current = null;
                }
                if (solver != null) {
                    solver.resolve(result);
                }
                currenting = null;
                tryIO();
            }).error(error -> {
                if (solver != null) {
                    solver.reject(error);
                }
                error.printStackTrace();
                currenting = null;
                tryIO();
            });
        }
    }

    @Nullable
    public MediaDevice current() {
        IOHolder holder = current;
        return null != holder ? holder.mediaDevice : null;
    }

    private class IOHolder {
        public boolean connect;

        @NonNull
        public MediaDevice mediaDevice;

        @Nullable
        public Solver<Boolean> solver;

        public IOHolder(boolean connect, @NonNull MediaDevice mediaDevice, @Nullable Solver<Boolean> solver) {
            this.connect = connect;
            this.mediaDevice = mediaDevice;
            this.solver = solver;
        }

        @NonNull
        public String id() {
            return mediaDevice.id();
        }
    }
}
