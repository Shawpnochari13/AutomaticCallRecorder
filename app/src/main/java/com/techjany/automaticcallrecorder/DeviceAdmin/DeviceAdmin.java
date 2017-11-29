package com.techjany.automaticcallrecorder.DeviceAdmin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by sandhya on 23-Aug-17.
 */

public class DeviceAdmin extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public void onEnabled(Context context, Intent intent) {
    }

    public void onDisabled(Context context, Intent intent) {
    }
}
