package com.james.felixlauncher.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.felixlauncher.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockFragment extends Fragment {

    TextView clock, time, date;
    BroadcastReceiver receiver;
    SimpleDateFormat clockFormat, timeFormat, dateFormat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clock, container, false);

        clock = (TextView) rootView.findViewById(R.id.clock);
        time = (TextView) rootView.findViewById(R.id.time);
        date = (TextView) rootView.findViewById(R.id.date);

        clockFormat = new SimpleDateFormat("HH:mm");
        timeFormat = new SimpleDateFormat("a");
        dateFormat = new SimpleDateFormat("EEE, MMM d");

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

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) getActivity().unregisterReceiver(receiver);
    }
}
