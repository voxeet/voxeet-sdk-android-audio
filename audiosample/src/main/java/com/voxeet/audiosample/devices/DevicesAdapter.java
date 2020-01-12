package com.voxeet.audiosample.devices;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.voxeet.audio.utils.__Call;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audiosample.R;

import java.util.ArrayList;
import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DeviceHolder> {

    private final __Call<MediaDevice> connect;
    private List<MediaDevice> list;

    public DevicesAdapter(@NonNull __Call<MediaDevice> connect) {
        list = new ArrayList<>();
        this.connect = connect;
    }

    public void update(@NonNull List<MediaDevice> devices) {
        this.list = devices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new DeviceHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.devices_list_device, viewGroup, false),
                connect);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder deviceHolder, int index) {
        deviceHolder.bind(list.get(index));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
