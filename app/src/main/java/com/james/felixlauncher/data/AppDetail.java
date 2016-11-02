package com.james.felixlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

public class AppDetail implements Parcelable {
    public static final Creator<AppDetail> CREATOR = new Creator<AppDetail>() {
        @Override
        public AppDetail createFromParcel(Parcel in) {
            return new AppDetail(in);
        }

        @Override
        public AppDetail[] newArray(int size) {
            return new AppDetail[size];
        }
    };

    public String label, name;
    public boolean fav, hide;
    public Drawable icon;

    public AppDetail(Context context, String label, String name) {
        this.label = label;
        this.name = name;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        fav = prefs.getBoolean(name + "-fav", false);
        hide = prefs.getBoolean(name + "-hide", false);
    }

    public void setFav(Context context, boolean fav) {
        this.fav = fav;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name + "-fav", fav).apply();
    }

    public void setHide(Context context, boolean hide) {
        this.hide = hide;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name + "-hide", hide).apply();
    }

    protected AppDetail(Parcel in) {
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
