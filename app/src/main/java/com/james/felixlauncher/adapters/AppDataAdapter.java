package com.james.felixlauncher.adapters;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.james.felixlauncher.R;
import com.james.felixlauncher.data.AppData;
import com.james.felixlauncher.views.SquareImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppDataAdapter extends RecyclerView.Adapter<AppDataAdapter.ViewHolder> {

    private List<AppData> list;
    private List<AppData> filteredList;
    private boolean grid;
    private Listener listener;
    private PackageManager manager;
    private Activity activity;

    public AppDataAdapter(final Activity activity, PackageManager manager, List<AppData> list) {
        this.list = new ArrayList<>();
        this.list.addAll(list);

        this.manager = manager;
        this.activity = activity;

        filteredList = new ArrayList<>();
        filteredList.addAll(list);

        Collections.sort(filteredList, new Comparator<AppData>() {
            @Override
            public int compare(AppData t1, AppData t2) {
                return t1.label.compareToIgnoreCase(t2.label);
            }
        });

        Collections.sort(filteredList, new TimeComparator());
    }

    public interface Listener {
        void onChange();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setGrid(boolean grid) {
        this.grid = grid;
    }

    public void setList(List<AppData> list) {
        this.list.clear();
        this.list.addAll(list);

        filteredList.clear();
        filteredList.addAll(list);

        Collections.sort(filteredList, new Comparator<AppData>() {
            @Override
            public int compare(AppData t1, AppData t2) {
                return t1.label.compareToIgnoreCase(t2.label);
            }
        });

        Collections.sort(filteredList, new TimeComparator());

        notifyDataSetChanged();
    }

    public List<AppData> getList() {
        List<AppData> list = new ArrayList<>();
        list.addAll(this.list);
        return list;
    }

    @Override
    public AppDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(grid ? R.layout.item_icon : R.layout.item_list, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AppDataAdapter.ViewHolder holder, int position) {
        if (holder.t != null && holder.t.isAlive()) holder.t.interrupt();

        AppData app = filteredList.get(position);

        holder.v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

        TextView title = holder.v.findViewById(R.id.name);
        title.setText(app.label);

        TextView subtitle = holder.v.findViewById(R.id.extra);
        subtitle.setText(app.getDescription());

        if (app.icon != null)
            ((SquareImageView) holder.v.findViewById(R.id.image)).setImageDrawable(app.icon);
        else {
            holder.t = new Thread() {
                @Override
                public void run() {
                    try {
                        filteredList.get(holder.getAdapterPosition()).icon = manager.getApplicationIcon(filteredList.get(holder.getAdapterPosition()).name);
                    } catch (PackageManager.NameNotFoundException e) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ((SquareImageView) holder.v.findViewById(R.id.image)).setImageDrawable(filteredList.get(holder.getAdapterPosition()).icon);
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
                activity.startActivity(
                        filteredList.get(holder.getAdapterPosition()).getIntent(activity, manager),
                        ActivityOptionsCompat.makeScaleUpAnimation(v, (int) v.getX() - (v.getWidth() / 2), (int) v.getY() - (v.getHeight() / 2), v.getWidth(), v.getHeight()).toBundle()
                );
            }
        });

        holder.v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                final BottomSheetDialog dialog = new BottomSheetDialog(v.getContext());
                View sheet = activity.getLayoutInflater().inflate(R.layout.bottom_sheet, null);

                AppData app = filteredList.get(holder.getAdapterPosition());
                if (app.fav)
                    ((TextView) sheet.findViewById(R.id.fav_text)).setText(R.string.favorites_remove);
                if (app.hide)
                    ((TextView) sheet.findViewById(R.id.hide_text)).setText(R.string.set_visible);

                if (app.icon != null) ((ImageView) sheet.findViewById(R.id.icon)).setImageDrawable(app.icon);
                ((TextView) sheet.findViewById(R.id.title)).setText(app.label);

                sheet.findViewById(R.id.sheet_fav).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppData app = filteredList.get(holder.getAdapterPosition());
                        app.setFav(activity, !app.fav);
                        listener.onChange();

                        dialog.dismiss();
                    }
                });

                sheet.findViewById(R.id.sheet_hide).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppData app = filteredList.get(holder.getAdapterPosition());
                        app.setHide(activity, !app.hide);
                        listener.onChange();

                        dialog.dismiss();
                    }
                });

                sheet.findViewById(R.id.sheet_play).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + filteredList.get(holder.getAdapterPosition()).name)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + filteredList.get(holder.getAdapterPosition()).name)));
                        }

                        dialog.dismiss();
                    }
                });

                sheet.findViewById(R.id.sheet_settings).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + filteredList.get(holder.getAdapterPosition()).name));
                            activity.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            activity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                        }

                        dialog.dismiss();
                    }
                });

                dialog.setContentView(sheet);
                dialog.show();
                return false;
            }
        });
    }

    public void search(String text) {
        filteredList.clear();
        if (text == null) {
            filteredList.addAll(list);

            Collections.sort(filteredList, new Comparator<AppData>() {
                public int compare(AppData v1, AppData v2) {
                    return v1.label.compareTo(v2.label);
                }
            });

            notifyDataSetChanged();
            return;
        }

        for (AppData detail : list) {
            if (detail.label.toLowerCase().contains(text.toLowerCase())) filteredList.add(detail);
        }

        Collections.sort(filteredList, new Comparator<AppData>() {
            public int compare(AppData v1, AppData v2) {
                return v1.label.compareTo(v2.label);
            }
        });

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View v;
        Thread t;

        ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

    private class TimeComparator implements Comparator<AppData> {

        private Date time;
        private SimpleDateFormat format;

        TimeComparator() {
            format = new SimpleDateFormat(AppData.TIME_FORMAT, Locale.getDefault());
            try {
                time = format.parse(DateFormat.format(AppData.TIME_FORMAT, new Date().getTime()).toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int compare(AppData t1, AppData t2) {
            if (time != null && t1.time != null && t2.time != null) {
                try {
                    return format.parse(t1.time).compareTo(time) - format.parse(t2.time).compareTo(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (t1.time != null && t2.time == null) return -1;
            else if (t1.time == null && t2.time != null) return 1;
            else return 0;
        }
    }
}
