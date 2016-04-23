package com.james.felixlauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class HiddenActivity extends AppCompatActivity {

    AppDetailAdapter adapter;
    ArrayList<AppDetail> list;
    ProgressBar progress;
    PackageManager manager;
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recycler);

        manager = getPackageManager();
        list = new ArrayList<>();

        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setVisibility(View.VISIBLE);

        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppDetailAdapter(this, manager, list);
        adapter.setListener(new AppDetailAdapter.Listener() {
            @Override
            public void onChange() {
                load();
            }
        });

        recycler.setAdapter(adapter);

        load();
    }

    public void load() {
        if (t != null && t.isAlive()) t.interrupt();
        list.clear();

        t = new Thread() {
            @Override
            public void run() {
                List<ResolveInfo> availableActivities = manager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
                for (ResolveInfo ri : availableActivities) {
                    AppDetail app = new AppDetail(HiddenActivity.this, ri.loadLabel(manager).toString(), ri.activityInfo.packageName);
                    if (app.hide) list.add(app);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getList().size() != list.size()) adapter.setList(list);
                        progress.setVisibility(View.GONE);
                    }
                });
            }
        };
        t.start();
    }
}
