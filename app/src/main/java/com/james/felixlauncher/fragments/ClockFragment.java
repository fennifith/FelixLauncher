package com.james.felixlauncher.fragments;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
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
import com.james.felixlauncher.adapters.AppIconAdapter;
import com.james.felixlauncher.data.AppDetail;
import com.james.felixlauncher.data.WeatherCondition;
import com.james.felixlauncher.receivers.FenceReceiver;
import com.james.felixlauncher.views.SquareImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClockFragment extends CustomFragment implements Felix.ActivityChangedListener {

    private static final String
            FORMAT_CLOCK = "h:mm",
            FORMAT_CLOCK_24H = "kk:mm",
            FORMAT_TIME = "a",
            FORMAT_DATE = "EEEE, MMMM d";

    private TextView clock, time, date;
    private TimeReceiver receiver;
    private SimpleDateFormat clockFormat, timeFormat, dateFormat;

    private View activity;
    private SquareImageView activityImage;
    private AppIconAdapter activityAdapter;

    private View weather;
    private SquareImageView weatherImage;
    private TextView weatherText, weatherTemperature;

    private View headphones;
    private AppIconAdapter headphonesAdapter;

    private View alarm;
    private TextView alarmText;
    private AlarmManager alarmManager;

    private Felix felix;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clock, container, false);

        felix = (Felix) getContext().getApplicationContext();
        felix.addActivityListener(this);

        clock = (TextView) rootView.findViewById(R.id.clock);
        time = (TextView) rootView.findViewById(R.id.time);
        date = (TextView) rootView.findViewById(R.id.date);

        activity = rootView.findViewById(R.id.activity);
        activityImage = (SquareImageView) rootView.findViewById(R.id.activityImage);
        RecyclerView activityRecycler = (RecyclerView) rootView.findViewById(R.id.activityRecycler);
        activityRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        activityAdapter = new AppIconAdapter(getContext(), getContext().getPackageManager(), new ArrayList<AppDetail>());
        activityRecycler.setAdapter(activityAdapter);

        weather = rootView.findViewById(R.id.weather);
        weatherImage = (SquareImageView) rootView.findViewById(R.id.weatherImage);
        weatherText = (TextView) rootView.findViewById(R.id.weatherText);
        weatherTemperature = (TextView) rootView.findViewById(R.id.temperature);

        headphones = rootView.findViewById(R.id.headphones);
        RecyclerView headphonesRecycler = (RecyclerView) rootView.findViewById(R.id.headphonesRecycler);
        headphonesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        headphonesAdapter = new AppIconAdapter(getContext(), getContext().getPackageManager(), new ArrayList<AppDetail>());
        headphonesRecycler.setAdapter(headphonesAdapter);

        alarm = rootView.findViewById(R.id.alarm);
        alarmText = (TextView) rootView.findViewById(R.id.alarmText);
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        clockFormat = new SimpleDateFormat(FORMAT_CLOCK, Locale.getDefault());
        timeFormat = new SimpleDateFormat(FORMAT_TIME, Locale.getDefault());
        dateFormat = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());

        Date current = new Date();
        clock.setText(clockFormat.format(current));
        time.setText(timeFormat.format(current));
        date.setText(dateFormat.format(current));

        receiver = new TimeReceiver();
        getActivity().registerReceiver(receiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS));
                else startActivity(new Intent(AlarmClock.ACTION_SET_ALARM));
            }
        });

        rootView.findViewById(R.id.weatherButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/?q=weather")));
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        felix.removeActivityListener(this);
        super.onDestroy();
        if (receiver != null) getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onSelect() {
        Context context = getContext();
        if (context == null) return;

        if (felix != null && activity != null && activityImage != null && activityAdapter != null) {
            String activityKey = felix.getActivityKey();

            if (activityKey != null) {
                switch (activityKey) {
                    case FenceReceiver.KEY_DRIVING:
                        activityImage.setImageResource(R.drawable.ic_driving);
                        break;
                    case FenceReceiver.KEY_BIKING:
                        activityImage.setImageResource(R.drawable.ic_biking);
                        break;
                    case FenceReceiver.KEY_RUNNING:
                        activityImage.setImageResource(R.drawable.ic_running);
                        break;
                    case FenceReceiver.KEY_WALKING:
                        activityImage.setImageResource(R.drawable.ic_walking);
                        break;
                }

                List<AppDetail> apps = new ArrayList<>();
                for (AppDetail app : felix.getAppsForActivity(activityKey)) {
                    if (apps.size() >= 10) break;
                    else apps.add(app);
                }


                activity.setVisibility(apps.size() > 0 ? View.VISIBLE : View.GONE);
                activityAdapter.setApps(apps);
            } else activity.setVisibility(View.GONE);

        }

        felix.getWeather(new ResultCallback<WeatherResult>() {
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

        if (headphones != null && headphonesAdapter != null) {
            if (felix.isHeadphones()) {
                List<AppDetail> apps = new ArrayList<>();
                for (AppDetail app : felix.getAppsForActivity(FenceReceiver.KEY_HEADPHONES)) {
                    if (apps.size() >= 10) break;
                    else apps.add(app);
                }

                headphones.setVisibility(apps.size() > 0 ? View.VISIBLE : View.GONE);
                headphonesAdapter.setApps(apps);
            } else headphones.setVisibility(View.GONE);
        }

        if (alarm != null && alarmText != null) {
            Date current = new Date();
            if (alarmManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && alarmManager.getNextAlarmClock() != null) {
                Date alarmDate = new Date(alarmManager.getNextAlarmClock().getTriggerTime());
                if (current.before(alarmDate)) alarm.setVisibility(View.VISIBLE);
                alarmText.setText(clockFormat.format(alarmDate));
                alarmText.setVisibility(View.VISIBLE);
            } else {
                alarm.setVisibility(isEvening() ? View.VISIBLE : View.GONE);
                alarmText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityChanged() {
        onSelect();
    }

    private boolean isEvening() {
        try {
            return new SimpleDateFormat(FORMAT_CLOCK_24H, Locale.getDefault()).parse(DateFormat.format(FORMAT_CLOCK_24H, new Date()).toString()).after(new SimpleDateFormat(FORMAT_CLOCK_24H, Locale.getDefault()).parse("18:00"));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(Intent.ACTION_TIME_TICK)) {
                Date current = new Date();
                clock.setText(clockFormat.format(current));
                time.setText(timeFormat.format(current));
                date.setText(dateFormat.format(current));

                if (alarmManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && alarmManager.getNextAlarmClock() != null) {
                    Date alarmDate = new Date(alarmManager.getNextAlarmClock().getTriggerTime());
                    if (current.before(alarmDate)) alarm.setVisibility(View.VISIBLE);
                    alarmText.setText(clockFormat.format(alarmDate));
                    alarmText.setVisibility(View.VISIBLE);
                } else {
                    alarm.setVisibility(isEvening() ? View.VISIBLE : View.GONE);
                    alarmText.setVisibility(View.GONE);
                }
            }
        }
    }
}
