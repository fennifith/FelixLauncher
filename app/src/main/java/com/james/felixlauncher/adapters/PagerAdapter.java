package com.james.felixlauncher.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.james.felixlauncher.R;
import com.james.felixlauncher.fragments.AppsFragment;
import com.james.felixlauncher.fragments.ClockFragment;
import com.james.felixlauncher.fragments.CustomFragment;
import com.james.felixlauncher.fragments.FavFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private Context context;

    private ClockFragment clockFragment;
    private AppsFragment appsFragment;
    private FavFragment favFragment;

    public PagerAdapter(Context context, final FragmentManager manager) {
        super(manager);
        this.context = context;

        clockFragment = new ClockFragment();
        appsFragment = new AppsFragment();
        favFragment = new FavFragment();
    }

    @Override
    public CustomFragment getItem(int position) {
        switch (position) {
            case 0:
                return clockFragment;
            case 1:
                return appsFragment;
            case 2:
                return favFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.clock);
            case 1:
                return context.getString(R.string.apps);
            case 2:
                return context.getString(R.string.favorites);
            default:
                return null;
        }
    }

    public void onPageChange(int position) {
        getItem(position).onSelect();
    }

    public void search(String text) {
        appsFragment.search(text);
    }
}
