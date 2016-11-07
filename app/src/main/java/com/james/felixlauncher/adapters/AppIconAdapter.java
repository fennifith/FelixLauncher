package com.james.felixlauncher.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.felixlauncher.R;
import com.james.felixlauncher.data.AppDetail;
import com.james.felixlauncher.views.SquareImageView;

import java.util.List;

public class AppIconAdapter extends RecyclerView.Adapter<AppIconAdapter.ViewHolder> {

    private Context context;
    private PackageManager manager;
    private List<AppDetail> apps;

    public AppIconAdapter(Context context, PackageManager manager, List<AppDetail> apps) {
        this.context = context;
        this.manager = manager;
        this.apps = apps;
    }

    public void setApps(List<AppDetail> apps) {
        this.apps = apps;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_icon, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppDetail app = apps.get(position);

        if (app.icon != null)
            ((SquareImageView) holder.v.findViewById(R.id.image)).setImageDrawable(app.icon);
        else {
            holder.t = new Thread() {
                @Override
                public void run() {
                    try {
                        apps.get(holder.getAdapterPosition()).icon = manager.getApplicationIcon(apps.get(holder.getAdapterPosition()).name);
                    } catch (PackageManager.NameNotFoundException e) {
                        return;
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ((SquareImageView) holder.v.findViewById(R.id.image)).setImageDrawable(apps.get(holder.getAdapterPosition()).icon);
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }
            };
            holder.t.start();
        }

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(
                        apps.get(holder.getAdapterPosition()).getIntent(context, manager),
                        ActivityOptionsCompat.makeScaleUpAnimation(v, (int) v.getX() - (v.getWidth() / 2), (int) v.getY() - (v.getHeight() / 2), v.getWidth(), v.getHeight()).toBundle()
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View v;
        Thread t;

        ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
