package com.james.felixlauncher.data;

import android.content.Context;
import android.support.annotation.DrawableRes;

import com.google.android.gms.awareness.state.Weather;
import com.james.felixlauncher.R;

import java.util.ArrayList;
import java.util.List;

public class WeatherCondition {

    private int condition;

    public WeatherCondition(int condition) {
        this.condition = condition;
    }

    public int getCondition() {
        return condition;
    }

    @DrawableRes
    public int getDrawable() {
        switch (condition) {
            case Weather.CONDITION_CLEAR:
                return R.drawable.ic_sun;
            case Weather.CONDITION_FOGGY:
                return R.drawable.ic_partial_sun;
            case Weather.CONDITION_CLOUDY:
                return R.drawable.ic_light_cloud;
            case Weather.CONDITION_HAZY:
                return R.drawable.ic_partial_sun;
            case Weather.CONDITION_ICY:
                return R.drawable.ic_snow_hail;
            case Weather.CONDITION_RAINY:
                return R.drawable.ic_umbrella;
            case Weather.CONDITION_SNOWY:
                return R.drawable.ic_snow_hail;
            case Weather.CONDITION_STORMY:
                return R.drawable.ic_cloud;
            case Weather.CONDITION_WINDY:
                return R.drawable.ic_cloud;
            default:
                return android.R.drawable.stat_notify_error;
        }
    }

    public String getTitle(Context context) {
        switch (condition) {
            case Weather.CONDITION_CLEAR:
                return context.getString(R.string.weather_clear);
            case Weather.CONDITION_FOGGY:
                return context.getString(R.string.weather_foggy);
            case Weather.CONDITION_CLOUDY:
                return context.getString(R.string.weather_cloudy);
            case Weather.CONDITION_HAZY:
                return context.getString(R.string.weather_hazy);
            case Weather.CONDITION_ICY:
                return context.getString(R.string.weather_icy);
            case Weather.CONDITION_RAINY:
                return context.getString(R.string.weather_rainy);
            case Weather.CONDITION_SNOWY:
                return context.getString(R.string.weather_snowy);
            case Weather.CONDITION_STORMY:
                return context.getString(R.string.weather_stormy);
            case Weather.CONDITION_WINDY:
                return context.getString(R.string.weather_windy);
            default:
                return "";
        }
    }

    public static List<WeatherCondition> getConditions(int[] conditions) {
        List<WeatherCondition> list = new ArrayList<>();
        for (int condition : conditions) {
            list.add(new WeatherCondition(condition));
        }

        return list;
    }

}
