package com.voxeet.audiosample.devices;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.voxeet.audio.utils.__Call;
import com.voxeet.audio2.devices.MediaDevice;
import com.voxeet.audiosample.R;

public class DeviceHolder extends RecyclerView.ViewHolder {
    private TextView device_id;
    private TextView device_connectivity_status;
    private TextView device_last_connectivity_status_origin;
    private TextView device_platform_connectivity_status;
    private Button connect;
    private MediaDevice mediaDevice;

    public DeviceHolder(@NonNull View itemView, @NonNull __Call<MediaDevice> callConnect) {
        super(itemView);

        device_id = itemView.findViewById(R.id.device_id);
        device_connectivity_status = itemView.findViewById(R.id.device_connectivity_status);
        device_last_connectivity_status_origin = itemView.findViewById(R.id.device_last_connectivity_status_origin);
        device_platform_connectivity_status = itemView.findViewById(R.id.device_platform_connectivity_status);
        connect = itemView.findViewById(R.id.connect);
        connect.setOnClickListener(v -> callConnect.apply(mediaDevice));
    }

    public void bind(MediaDevice mediaDevice) {
        this.mediaDevice = mediaDevice;
        device_id.setText(mediaDevice.id() + " (" + mediaDevice.deviceType() + ")");
        device_connectivity_status.setText(mediaDevice.connectionState().name());
        device_last_connectivity_status_origin.setText(mediaDevice.lastConnectionStateType().name());
        device_platform_connectivity_status.setText(mediaDevice.platformConnectionState().name());
    }
}
