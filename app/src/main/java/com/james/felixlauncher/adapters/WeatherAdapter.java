package com.james.felixlauncher.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.felixlauncher.R;
import com.james.felixlauncher.data.WeatherCondition;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private Context context;
    private List<WeatherCondition> conditions;

    public WeatherAdapter(Context context, List<WeatherCondition> conditions) {
        this.context = context;
        this.conditions = conditions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_weather, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeatherCondition condition = conditions.get(position + 1);
        ((AppCompatImageView) holder.v.findViewById(R.id.image)).setImageResource(condition.getDrawable());
        ((TextView) holder.v.findViewById(R.id.text)).setText(condition.getTitle(context));
    }

    @Override
    public int getItemCount() {
        return conditions.size() - 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
