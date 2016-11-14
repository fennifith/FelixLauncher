package com.james.felixlauncher.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.james.felixlauncher.Felix;
import com.james.felixlauncher.receivers.FenceReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AppData implements Parcelable {

    private static final String KEY_OPENED = "opened";
    private static final String KEY_FAVORITE = "fav";
    private static final String KEY_HIDDEN = "hide";
    private static final String KEY_TIME = "time";
    private static final String KEY_DATE = "date";

    public static final String TIME_FORMAT = "kk:mm";
    private static final String TIME_FORMAT_READABLE = "h:mm";
    private static final String DATE_FORMAT = "EEEE, MMMM d";

    public static final Creator<AppData> CREATOR = new Creator<AppData>() {
        @Override
        public AppData createFromParcel(Parcel in) {
            return new AppData(in);
        }

        @Override
        public AppData[] newArray(int size) {
            return new AppData[size];
        }
    };

    public String label, name, time, date;
    public boolean fav, hide;
    public Drawable icon;

    public AppData(Context context, String label, String name) {
        this.label = label;
        this.name = name;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        fav = prefs.getBoolean(getKey(KEY_FAVORITE), false);
        hide = prefs.getBoolean(getKey(KEY_HIDDEN), false);
        time = prefs.getString(getKey(KEY_TIME), null);
        date = prefs.getString(getKey(KEY_DATE), null);
    }

    public String getDescription() {
        String description = null;

        if (time != null && date != null) {
            try {
                description = DateFormat.format(TIME_FORMAT_READABLE, new SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).parse(time)) + " • " + date;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return description != null ? description : name;
    }

    public Intent getIntent(final Context context, PackageManager manager) {
        Felix felix = (Felix) context.getApplicationContext();

        time = DateFormat.format(TIME_FORMAT, Calendar.getInstance().getTime()).toString();
        date = DateFormat.format(DATE_FORMAT, Calendar.getInstance().getTime()).toString();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(getKey(KEY_TIME), time).putString(getKey(KEY_DATE), date).putInt(getKey(KEY_OPENED), prefs.getInt(getKey(KEY_OPENED), 0) + 1).apply();

        String key = felix.getActivityKey();
        if (key != null)
            prefs.edit().putInt(getKey(key), prefs.getInt(getKey(key), 0) + 1).apply();

        if (felix.isHeadphones())
            prefs.edit().putInt(getKey(FenceReceiver.KEY_HEADPHONES), prefs.getInt(getKey(FenceReceiver.KEY_HEADPHONES), 0) + 1).apply();

        return manager.getLaunchIntentForPackage(name);
    }

    private String getKey(String key) {
        return name + "-" + key;
    }

    public float getOpenScale(Context context, String key) {
        int opened = getOpened(context);
        if (opened > 0)
            return (float) getOpened(context, key) / opened;
        else return 0;
    }

    public int getOpened(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(getKey(key), 0);
    }

    public int getOpened(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(getKey(KEY_OPENED), 0);
    }

    public void setFav(Context context, boolean fav) {
        this.fav = fav;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(getKey(KEY_FAVORITE), fav).apply();
    }

    public void setHide(Context context, boolean hide) {
        this.hide = hide;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(getKey(KEY_HIDDEN), hide).apply();
    }

    protected AppData(Parcel in) {
        label = in.readString();
        name = in.readString();
        fav = in.readInt() == 1;
        hide = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(label);
        out.writeString(name);
        out.writeInt(fav ? 1 : 0);
        out.writeInt(hide ? 1 : 0);
    }
}
