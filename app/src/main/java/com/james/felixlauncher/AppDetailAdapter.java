package com.james.felixlauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AppDetailAdapter extends RecyclerView.Adapter<AppDetailAdapter.ViewHolder> {

    private ArrayList<AppDetail> list;
    private ArrayList<AppDetail> filteredList;
    private boolean grid;
    private Listener listener;
    private PackageManager manager;
    private Activity activity;

    public AppDetailAdapter(final Activity activity, PackageManager manager, ArrayList<AppDetail> list) {
        this.list = new ArrayList<>();
        this.list.addAll(list);

        this.manager = manager;
        this.activity = activity;

        filteredList = new ArrayList<>();
        filteredList.addAll(list);

        Collections.sort(filteredList, new Comparator<AppDetail>() {
            public int compare(AppDetail v1, AppDetail v2) {
                return v1.label.compareTo(v2.label);
            }
        });
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

    public void setList(ArrayList<AppDetail> list) {
        this.list.clear();
        this.list.addAll(list);

        filteredList.clear();
        filteredList.addAll(list);

        Collections.sort(filteredList, new Comparator<AppDetail>() {
            public int compare(AppDetail v1, AppDetail v2) {
                return v1.label.compareTo(v2.label);
            }
        });

        notifyDataSetChanged();
    }

    public ArrayList<AppDetail> getList() {
        ArrayList<AppDetail> list = new ArrayList<>();
        list.addAll(this.list);
        return list;
    }

    @Override
    public AppDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(grid ? R.layout.icon_item : R.layout.list_item, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AppDetailAdapter.ViewHolder holder, int position) {
        if (holder.t != null && holder.t.isAlive()) holder.t.interrupt();

        holder.v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

        TextView title = (TextView) holder.v.findViewById(R.id.name);
        title.setText(filteredList.get(position).label);
        title.setTextColor(SettingsActivity.getPrimaryTextColor(activity));

        TextView subtitle = (TextView) holder.v.findViewById(R.id.extra);
        subtitle.setText(filteredList.get(position).name);
        subtitle.setTextColor(SettingsActivity.getSecondaryTextColor(activity));

        if (filteredList.get(holder.getAdapterPosition()).icon != null) ((SquareImageView) holder.v.findViewById(R.id.image)).setImageDrawable(filteredList.get(holder.getAdapterPosition()).icon);
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
                Intent i = manager.getLaunchIntentForPackage(filteredList.get(holder.getAdapterPosition()).name);
                activity.startActivity(i);
            }
        });

        holder.v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                final BottomSheetDialog dialog = new BottomSheetDialog(v.getContext());
                View sheet = activity.getLayoutInflater().inflate(R.layout.bottom_sheet, null);

                AppDetail app = filteredList.get(holder.getAdapterPosition());
                if (app.fav) ((TextView) sheet.findViewById(R.id.fav_text)).setText("Remove from Favorites");
                if (app.hide) ((TextView) sheet.findViewById(R.id.hide_text)).setText("Set Visible");

                if (app.icon != null) ((ImageView) sheet.findViewById(R.id.icon)).setImageDrawable(app.icon);
                ((TextView) sheet.findViewById(R.id.title)).setText(app.label);

                sheet.findViewById(R.id.sheet_fav).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppDetail app = filteredList.get(holder.getAdapterPosition());
                        app.setFav(activity, !app.fav);
                        listener.onChange();

                        dialog.dismiss();
                    }
                });

                sheet.findViewById(R.id.sheet_hide).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppDetail app = filteredList.get(holder.getAdapterPosition());
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

            Collections.sort(filteredList, new Comparator<AppDetail>() {
                public int compare(AppDetail v1, AppDetail v2) {
                    return v1.label.compareTo(v2.label);
                }
            });

            notifyDataSetChanged();
            return;
        }

        for (AppDetail detail : list) {
            if (detail.label.toLowerCase().contains(text.toLowerCase())) filteredList.add(detail);
        }

        Collections.sort(filteredList, new Comparator<AppDetail>() {
            public int compare(AppDetail v1, AppDetail v2) {
                return v1.label.compareTo(v2.label);
            }
        });

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View v;
        public Thread t;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
