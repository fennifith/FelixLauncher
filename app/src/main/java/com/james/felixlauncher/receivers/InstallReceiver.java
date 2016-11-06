package com.james.felixlauncher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.james.felixlauncher.Felix;

public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ((Felix) context.getApplicationContext()).loadApps();
    }
}
