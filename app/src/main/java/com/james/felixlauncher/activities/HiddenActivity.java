package com.james.felixlauncher.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.james.felixlauncher.Felix;
import com.james.felixlauncher.R;
import com.james.felixlauncher.adapters.AppDetailAdapter;

public class HiddenActivity extends AppCompatActivity implements Felix.AppsChangedListener {

    private AppDetailAdapter adapter;
    private ProgressBar progress;
    private Felix felix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recycler);

        felix = (Felix) getApplicationContext();
        felix.addListener(this);

        progress = (ProgressBar) findViewById(R.id.progressBar);
        if (felix.isLoading()) progress.setVisibility(View.VISIBLE);

        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppDetailAdapter(this, getPackageManager(), felix.getHidden());
        adapter.setListener(new AppDetailAdapter.Listener() {
            @Override
            public void onChange() {
                if (felix != null) felix.onAppsChanged();
            }
        });

        recycler.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        felix.removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onAppsChanged() {
        if (felix != null && adapter != null && progress != null) {
            adapter.setList(felix.getHidden());
            progress.setVisibility(View.GONE);
        }
    }
}
