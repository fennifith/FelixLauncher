package com.james.felixlauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class AboutActivity extends AppCompatActivity {

    ArrayList<AppDetail> hidden;
    PackageManager manager;
    RecyclerView rv;
    AppDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        rv = (RecyclerView) findViewById(R.id.rv);
        manager = getPackageManager();
        hidden = new ArrayList<>();

        adapter = new AppDetailAdapter(this, manager, hidden);
        adapter.setListener(new AppDetailAdapter.Listener() {
            @Override
            public void onChange() {
                load();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        load();
    }

    public void load() {
        hidden.clear();

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        for (ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail(this, ri.loadLabel(manager).toString(), ri.activityInfo.packageName);
            if (app.hide) hidden.add(app);
        }

        adapter.setList(hidden);
    }
}
