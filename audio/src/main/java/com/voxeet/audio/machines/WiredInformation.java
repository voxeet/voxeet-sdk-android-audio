package com.voxeet.audio.machines;

public class WiredInformation {

    private boolean hasMic;
    private int state;


    private WiredInformation() {

    }

    public WiredInformation(boolean hasMic, int state) {
        this();

        this.hasMic = hasMic;
        this.state = state;
    }

    public boolean isPlugged() {
        return state == 1;
    }
}
