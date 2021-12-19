package com.voxeet.audio.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public final class Validate {

    public static boolean hasBluetoothPermissions(@NonNull Context context) {
        String permission = Manifest.permission.BLUETOOTH;
        return hasPermissionInManifest(context, permission)
                && !deniedPermission(context, permission);
    }

    private static boolean deniedPermission(Context context, String permission) {
        final int GRANTED = PackageManager.PERMISSION_GRANTED;

        return ContextCompat.checkSelfPermission(context, permission) != GRANTED
                || context.checkCallingOrSelfPermission(permission) != GRANTED;

    }

    private static boolean hasPermissionInManifest(@NonNull Context context,
                                                   @NonNull String permission_to_check) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = null;
            if (packageInfo != null) {
                requestedPermissions = packageInfo.requestedPermissions;
            }


            if (null != requestedPermissions) {
                for (String permission : requestedPermissions) {
                    if (permission.equals(permission_to_check)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }
}
