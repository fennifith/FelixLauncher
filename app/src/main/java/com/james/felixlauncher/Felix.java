package com.james.felixlauncher;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.DetectedActivity;
import com.james.felixlauncher.data.AppData;
import com.james.felixlauncher.receivers.FenceReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Felix extends Application implements GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient client;
    private List<AppData> apps;
    private List<AppsChangedListener> listeners;
    private List<ActivityChangedListener> activityListeners;
    private boolean isLoading;

    private String activityKey;
    private boolean isHeadphones;
    private PendingIntent intent;

    @Override
    public void onCreate() {
        super.onCreate();

        intent = PendingIntent.getBroadcast(this, 0, new Intent(FenceReceiver.ACTION_FENCE_RECEIVER), 0);
        registerReceiver(new FenceReceiver(this), new IntentFilter(FenceReceiver.ACTION_FENCE_RECEIVER));

        client = new GoogleApiClient.Builder(this).addApi(Awareness.API).build();
        client.registerConnectionCallbacks(this);
        client.connect();

        apps = new ArrayList<>();
        listeners = new ArrayList<>();
        activityListeners = new ArrayList<>();
        loadApps();
    }

    public void loadApps() {
        apps.clear();
        isLoading = true;

        new Thread() {
            @Override
            public void run() {
                PackageManager manager = getPackageManager();
                if (manager == null) return;

                final List<AppData> apps = new ArrayList<>();
                List<ResolveInfo> infos = manager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
                for (ResolveInfo info : infos) {
                    apps.add(new AppData(Felix.this, info.loadLabel(manager).toString(), info.activityInfo.packageName));
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Felix.this.apps = apps;
                        isLoading = false;
                        onAppsChanged();
                    }
                });
            }
        }.start();
    }

    public void getWeather(ResultCallback<WeatherResult> callback) {
        if (client.isConnected() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            Awareness.SnapshotApi.getWeather(client).setResultCallback(callback);
    }

    public void getLocation(ResultCallback<LocationResult> callback) {
        if (client.isConnected() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            Awareness.SnapshotApi.getLocation(client).setResultCallback(callback);
    }

    public void getPlaces(ResultCallback<PlacesResult> callback) {
        if (client.isConnected() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            Awareness.SnapshotApi.getPlaces(client).setResultCallback(callback);
    }

    public boolean isHeadphones() {
        return isHeadphones;
    }

    public void setHeadphones(boolean isHeadphones) {
        this.isHeadphones = isHeadphones;
        onActivityChanged();
    }

    public String getActivityKey() {
        return activityKey;
    }

    public void setActivityKey(String activityKey) {
        this.activityKey = activityKey;
        onActivityChanged();
    }

    public boolean isLoading() {
        return isLoading;
    }

    private void onActivityChanged() {
        for (ActivityChangedListener listener : activityListeners) {
            listener.onActivityChanged();
        }
    }

    public List<AppData> getAppsForActivity(String activityKey) {
        List<AppData> apps = new ArrayList<>();

        if (activityKey != null) {
            for (AppData app : getApps()) {
                if (app.getOpened(this, activityKey) > 0) apps.add(app);
            }

            Collections.sort(apps, new ActivityComparator(this, activityKey));
        }

        return apps;
    }

    public List<AppData> getApps() {
        List<AppData> apps = new ArrayList<>();
        for (AppData app : this.apps) {
            if (!app.hide) apps.add(app);
        }

        return apps;
    }

    public List<AppData> getFavorites() {
        List<AppData> favorites = new ArrayList<>();
        for (AppData app : apps) {
            if (!app.hide && app.fav) favorites.add(app);
        }

        return favorites;
    }

    public List<AppData> getHidden() {
        List<AppData> hidden = new ArrayList<>();
        for (AppData app : apps) {
            if (app.hide) hidden.add(app);
        }

        return hidden;
    }

    public void onAppsChanged() {
        for (AppsChangedListener listener : listeners) {
            listener.onAppsChanged();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Awareness.FenceApi.updateFences(client, new FenceUpdateRequest.Builder()
                .addFence(FenceReceiver.KEY_DRIVING, DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE), intent)
                .addFence(FenceReceiver.KEY_BIKING, DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE), intent)
                .addFence(FenceReceiver.KEY_RUNNING, DetectedActivityFence.during(DetectedActivityFence.RUNNING), intent)
                .addFence(FenceReceiver.KEY_WALKING, DetectedActivityFence.during(DetectedActivityFence.WALKING, DetectedActivityFence.ON_FOOT), intent)
                .addFence(FenceReceiver.KEY_HEADPHONES, HeadphoneFence.during(HeadphoneState.PLUGGED_IN), intent)
                .build());

        Awareness.SnapshotApi.getDetectedActivity(client).setResultCallback(new ResultCallback<DetectedActivityResult>() {
            @Override
            public void onResult(DetectedActivityResult result) {
                if (result != null) {
                    switch (result.getActivityRecognitionResult().getMostProbableActivity().getType()) {
                        case DetectedActivity.IN_VEHICLE:
                            activityKey = FenceReceiver.KEY_DRIVING;
                            break;
                        case DetectedActivity.ON_BICYCLE:
                            activityKey = FenceReceiver.KEY_BIKING;
                            break;
                        case DetectedActivity.RUNNING:
                            activityKey = FenceReceiver.KEY_RUNNING;
                            break;
                        case DetectedActivity.WALKING:
                        case DetectedActivity.ON_FOOT:
                            activityKey = FenceReceiver.KEY_WALKING;
                            break;
                    }
                }
            }
        });

        Awareness.SnapshotApi.getHeadphoneState(client).setResultCallback(new ResultCallback<HeadphoneStateResult>() {
            @Override
            public void onResult(HeadphoneStateResult result) {
                if (result != null) {
                    isHeadphones = result.getHeadphoneState().getState() == HeadphoneState.PLUGGED_IN;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public void addListener(AppsChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AppsChangedListener listener) {
        listeners.add(listener);
    }

    public void addActivityListener(ActivityChangedListener listener) {
        activityListeners.add(listener);
    }

    public void removeActivityListener(ActivityChangedListener listener) {
        activityListeners.remove(listener);
    }

    public interface AppsChangedListener {
        void onAppsChanged();
    }

    public interface ActivityChangedListener {
        void onActivityChanged();
    }

    private class ActivityComparator implements Comparator<AppData> {

        private Context context;
        private String activityKey;

        ActivityComparator(Context context, String activityKey) {
            this.context = context;
            this.activityKey = activityKey;
        }

        @Override
        public int compare(AppData t1, AppData t2) {
            return (int) ((t2.getOpenScale(context, activityKey) * 100) - (t1.getOpenScale(context, activityKey) * 100));
        }
    }
}
