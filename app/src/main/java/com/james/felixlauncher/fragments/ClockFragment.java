package com.james.felixlauncher.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.ResultCallback;
import com.james.felixlauncher.Felix;
import com.james.felixlauncher.R;
import com.james.felixlauncher.data.WeatherCondition;
import com.james.felixlauncher.views.SquareImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockFragment extends CustomFragment {

    private static final String
            FORMAT_CLOCK = "kk:mm",
            FORMAT_TIME = "a",
            FORMAT_DATE = "EEE, MMM d";

    private TextView clock, time, date;
    private BroadcastReceiver receiver;
    private SimpleDateFormat clockFormat, timeFormat, dateFormat;

    private View weather;
    private SquareImageView weatherImage;
    private TextView weatherText, weatherTemperature;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clock, container, false);

        clock = (TextView) rootView.findViewById(R.id.clock);
        time = (TextView) rootView.findViewById(R.id.time);
        date = (TextView) rootView.findViewById(R.id.date);

        weather = rootView.findViewById(R.id.weather);
        weatherImage = (SquareImageView) rootView.findViewById(R.id.weatherImage);
        weatherText = (TextView) rootView.findViewById(R.id.weatherText);
        weatherTemperature = (TextView) rootView.findViewById(R.id.temperature);

        clockFormat = new SimpleDateFormat(FORMAT_CLOCK, Locale.getDefault());
        timeFormat = new SimpleDateFormat(FORMAT_TIME, Locale.getDefault());
        dateFormat = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());

        Date current = new Date();
        clock.setText(clockFormat.format(current));
        time.setText(timeFormat.format(current));
        date.setText(dateFormat.format(current));

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches(Intent.ACTION_TIME_TICK)) {
                    Date current = new Date();
                    clock.setText(clockFormat.format(current));
                    time.setText(timeFormat.format(current));
                    date.setText(dateFormat.format(current));
                }
            }
        };

        getActivity().registerReceiver(receiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        rootView.findViewById(R.id.alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AlarmClock.ACTION_SET_ALARM));
            }
        });

        rootView.findViewById(R.id.weatherButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather")));
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onSelect() {
        Context context = getContext();
        if (context == null) return;

        ((Felix) context.getApplicationContext()).getWeather(new ResultCallback<WeatherResult>() {
            @Override
            public void onResult(@NonNull WeatherResult weatherResult) {
                if (weatherResult.getStatus().isSuccess() && weatherImage != null && weatherText != null) {
                    Weather weather = weatherResult.getWeather();

                    WeatherCondition condition = new WeatherCondition(weather.getConditions()[0]);
                    weatherImage.setImageResource(condition.getDrawable());
                    weatherText.setText(condition.getTitle(getContext()));
                    weatherTemperature.setText(String.format(getString(R.string.temperature), weather.getTemperature(Weather.CELSIUS), weather.getTemperature(Weather.FAHRENHEIT)));
                    ClockFragment.this.weather.setVisibility(View.VISIBLE);
                } else {
                    if (weather != null) {
                        weather.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.weather_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
