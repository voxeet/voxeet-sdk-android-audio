package com.voxeet.audio2.devices;

import com.voxeet.promise.solve.Solver;

public class BluetoothDeviceConnectionWrapper {

    public final Solver<Boolean> solver;
    public boolean connect;

    public BluetoothDeviceConnectionWrapper(Solver<Boolean> solver, boolean connect) {
        this.solver = solver;
        this.connect = connect;
    }
}
