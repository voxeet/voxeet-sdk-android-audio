package com.voxeet.audio2.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.audio.utils.Log;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audio2.devices.MediaDeviceConnectionWrapper;
import com.voxeet.audio2.devices.description.LastConnectionStateType;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectScheduler {
    private static final String TAG = ConnectScheduler.class.getSimpleName();
    @NonNull
    private final MediaDeviceConnectionWrapper connectionWrapper;
    private List<IOHolder> mediaDevices = new ArrayList<>();

    private IOHolder current = null;
    private boolean locked = false;
    private List<Solver<ConnectScheduler>> waitFors = new CopyOnWriteArrayList<>();

    public ConnectScheduler() {
        connectionWrapper = MediaDeviceConnectionWrapper.unique();
    }

    public void pushConnect(@NonNull MediaDevice mediaDevice,
                            @NonNull LastConnectionStateType lastConnectionStateType,
                            @NonNull Solver<Boolean> solver) {
        cancelAwaitingConnect();
        checkDisconnectPrevious(mediaDevice);
        mediaDevices.add(new IOHolder(true, mediaDevice, lastConnectionStateType, solver));

        tryIO();
    }

    public void pushDisconnect(@NonNull MediaDevice mediaDevice,
                               @NonNull LastConnectionStateType lastConnectionStateType,
                               @NonNull Solver<Boolean> solver) {
        checkDisconnectPrevious(mediaDevice);
        mediaDevices.add(new IOHolder(false, mediaDevice, lastConnectionStateType, solver));

        tryIO();
    }

    private void checkDisconnectPrevious(MediaDevice mediaDevice) {
        if (null != current && !current.id().equals(mediaDevice.id())) {
            Log.d(TAG, "ConnectSchedumer // checkDisconnectPrevious: push disconnecting current " + current.id());
            mediaDevices.add(new IOHolder(false, current.mediaDevice, LastConnectionStateType.PROGRAMMATIC, null));
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
        } else if (!locked) {
            flushWaitFors();
        }
    }

    private void flushWaitFors() {
        for (Solver<ConnectScheduler> solver : waitFors) {
            try {
                solver.resolve(this);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        waitFors.clear();
    }

    @Nullable
    public MediaDevice current() {
        IOHolder holder = current;
        return null != holder ? holder.mediaDevice : null;
    }

    public Promise<ConnectScheduler> waitFor() {
        if (!locked) return Promise.resolve(this);
        return new Promise<>(solver -> waitFors.add(solver));
    }

    public boolean isLocked() {
        return locked;
    }

    private void cancelAwaitingConnect() {
        int index = 0;
        while (index < mediaDevices.size()) {
            IOHolder holder = mediaDevices.get(index);
            if (holder.connect && null != holder.solver) {
                Promise.reject(holder.solver, new CancellationException("canceled"));
                mediaDevices.remove(index);
            } else {
                index++;
            }
        }
    }

    private class IOHolder {
        public boolean connect;

        @NonNull
        public MediaDevice mediaDevice;

        @NonNull
        public LastConnectionStateType lastConnectionStateType;

        @Nullable
        public Solver<Boolean> solver;

        public IOHolder(boolean connect,
                        @NonNull MediaDevice mediaDevice,
                        @NonNull LastConnectionStateType lastConnectionStateType,
                        @Nullable Solver<Boolean> solver) {
            this.connect = connect;
            this.lastConnectionStateType = lastConnectionStateType;
            this.mediaDevice = mediaDevice;
            this.solver = solver;
        }

        @NonNull
        public String id() {
            return mediaDevice.id();
        }
    }
}
