package com.james.felixlauncher.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.awareness.fence.FenceState;
import com.james.felixlauncher.Felix;

public class FenceReceiver extends BroadcastReceiver {

    public static final String ACTION_FENCE_RECEIVER = "com.james.felixlauncher.ACTION_FENCE_RECEIVER";

    public static final String KEY_DRIVING = "driving";
    public static final String KEY_BIKING = "biking";
    public static final String KEY_RUNNING = "running";
    public static final String KEY_WALKING = "walking";
    public static final String KEY_HEADPHONES = "headphones";

    private Felix felix;

    public FenceReceiver(Context context) {
        felix = (Felix) context.getApplicationContext();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), ACTION_FENCE_RECEIVER)) {
            FenceState state = FenceState.extract(intent);

            switch (state.getFenceKey()) {
                case KEY_DRIVING:
                case KEY_BIKING:
                case KEY_RUNNING:
                case KEY_WALKING:
                    felix.setActivityKey(state.getCurrentState() == FenceState.TRUE ? state.getFenceKey() : null);
                    break;
                case KEY_HEADPHONES:
                    felix.setHeadphones(state.getCurrentState() == FenceState.TRUE);
                    break;
            }
        }
    }
}
