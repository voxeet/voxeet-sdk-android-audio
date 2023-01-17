package com.voxeet.audio2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.voxeet.audio.machines.WiredInformation;
import com.voxeet.audio.utils.Log;
import com.voxeet.audio.utils.__Call;


public class WiredHeadsetStateReceiver extends BroadcastReceiver {

    private final static String TAG = WiredHeadsetStateReceiver.class.getSimpleName();
    private final __Call<WiredInformation> update;

    public WiredHeadsetStateReceiver(@NonNull __Call<WiredInformation> update) {
        this.update = update;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (null == action) action = "";
        switch (action) {
            case Intent.ACTION_HEADSET_PLUG:
                int state = intent.getIntExtra("state", -1);
                int has_mic = intent.getIntExtra("microphone", -1);
                Log.d(TAG, "onReceive: headset plug " + ((state==0)? "DISCONNECTED" : "CONNECTED"));

                WiredInformation information = new WiredInformation(has_mic > 0, state);
                update.apply(information);

                break;
        }
    }
}
