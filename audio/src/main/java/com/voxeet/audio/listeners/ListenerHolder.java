package com.voxeet.audio.listeners;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ListenerHolder<INTERFACE_TYPE> {
    private List<INTERFACE_TYPE> list;
    private Callback<INTERFACE_TYPE> callback;

    public ListenerHolder() {
        list = new ArrayList<>();
    }

    public ListenerHolder(Callback<INTERFACE_TYPE> callback) {
        this();

        callback(callback);
    }

    public void notif() {
        if(null != callback) notif(callback);
    }

    public void notif(Callback<INTERFACE_TYPE> callback) {
        if(null != callback) {
            for (INTERFACE_TYPE listener : list) {
                callback.apply(listener);
            }
        }
    }

    public List<INTERFACE_TYPE> list() {
        return list;
    }

    public void callback(Callback<INTERFACE_TYPE> callback) {
        this.callback = callback;
    }

    public void register(@NonNull INTERFACE_TYPE listener) {
        if(!list.contains(listener)) list.add(listener);
    }

    public void unregister(@NonNull INTERFACE_TYPE listener) {
        list.remove(listener);
    }

    public interface Callback<INTERFACE_TYPE> {
        void apply(INTERFACE_TYPE listener);
    }
}
