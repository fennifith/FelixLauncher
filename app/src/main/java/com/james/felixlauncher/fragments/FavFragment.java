package com.james.felixlauncher.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.james.felixlauncher.Felix;
import com.james.felixlauncher.R;
import com.james.felixlauncher.adapters.AppDataAdapter;
import com.james.felixlauncher.data.AppData;

import java.util.List;

public class FavFragment extends CustomFragment implements Felix.AppsChangedListener {

    AppDataAdapter adapter;
    ProgressBar progress;

    private Felix felix;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);

        felix = (Felix) getContext().getApplicationContext();
        felix.addListener(this);

        progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        if (felix.isLoading()) progress.setVisibility(View.VISIBLE);

        RecyclerView recycler = (RecyclerView) rootView.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new AppDataAdapter(getActivity(), getContext().getPackageManager(), felix.getFavorites());
        adapter.setListener(new AppDataAdapter.Listener() {
            @Override
            public void onChange() {
                if (felix != null) felix.onAppsChanged();
            }
        });
        adapter.setGrid(true);

        recycler.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onDestroy() {
        felix.removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onAppsChanged() {
        onSelect();
    }

    @Override
    public void onSelect() {
        if (felix != null && adapter != null && progress != null) {
            List<AppData> apps = felix.getFavorites();
            if (apps.size() > 0) progress.setVisibility(View.GONE);
            adapter.setList(apps);
        }
    }
}
