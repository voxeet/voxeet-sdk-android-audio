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

    private IOHolder current = null;
    private boolean locked = false;

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
        if (!locked && mediaDevices.size() > 0) {
            locked = true;
            IOHolder holder = mediaDevices.get(0);
            mediaDevices.remove(0);
            Log.d(TAG, "STARTING --> " + holder.mediaDevice.id());

            Promise<Boolean> promise;
            if (holder.connect) promise = connectionWrapper.connect(holder.mediaDevice);
            else promise = connectionWrapper.disconnect(holder.mediaDevice);

            final Solver<Boolean> solver = holder.solver;

            if (holder.connect) {
                current = holder;
            } else if (null != current && holder.id().equals(current.id())) {
                current = null;
            }

            promise.then(result -> {
                if (solver != null) {
                    solver.resolve(result);
                }

                Log.d(TAG, "done for " + holder.mediaDevice.id() + " " + holder.mediaDevice.connectionState());
                locked = false;
                tryIO();
            }).error(error -> {
                if (solver != null) {
                    solver.reject(error);
                }
                error.printStackTrace();

                Log.d(TAG, "done for " + holder.mediaDevice.id() + " " + holder.mediaDevice.connectionState());
                locked = false;
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
