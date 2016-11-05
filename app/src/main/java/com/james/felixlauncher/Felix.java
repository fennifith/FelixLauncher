package com.james.felixlauncher;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.james.felixlauncher.data.AppDetail;

import java.util.ArrayList;
import java.util.List;

public class Felix extends Application {

    private GoogleApiClient client;
    private List<AppDetail> apps;
    private List<AppsChangedListener> listeners;

    @Override
    public void onCreate() {
        super.onCreate();
        client = new GoogleApiClient.Builder(this).addApi(Awareness.API).build();
        client.connect();

        apps = new ArrayList<>();
        listeners = new ArrayList<>();
        loadApps();
    }

    private void loadApps() {
        apps.clear();
        new Thread() {
            @Override
            public void run() {
                PackageManager manager = getPackageManager();
                if (manager == null) return;

                List<ResolveInfo> infos = manager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
                for (ResolveInfo info : infos) {
                    apps.add(new AppDetail(Felix.this, info.loadLabel(manager).toString(), info.activityInfo.packageName));
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        for (AppsChangedListener listener : listeners) {
                            onAppsChanged();
                        }
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

    public void getHeadphoneState(ResultCallback<HeadphoneStateResult> callback) {
        if (client.isConnected())
            Awareness.SnapshotApi.getHeadphoneState(client).setResultCallback(callback);
    }

    public List<AppDetail> getApps() {
        List<AppDetail> apps = new ArrayList<>();
        for (AppDetail app : this.apps) {
            if (!app.hide) apps.add(app);
        }

        return apps;
    }

    public List<AppDetail> getFavorites() {
        List<AppDetail> favorites = new ArrayList<>();
        for (AppDetail app : apps) {
            if (!app.hide && app.fav) favorites.add(app);
        }

        return favorites;
    }

    public List<AppDetail> getHidden() {
        List<AppDetail> hidden = new ArrayList<>();
        for (AppDetail app : apps) {
            if (app.hide) hidden.add(app);
        }

        return hidden;
    }

    public void addListener(AppsChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AppsChangedListener listener) {
        listeners.add(listener);
    }

    public void onAppsChanged() {
        for (AppsChangedListener listener : listeners) {
            listener.onAppsChanged();
        }
    }

    public interface AppsChangedListener {
        void onAppsChanged();
    }
}
