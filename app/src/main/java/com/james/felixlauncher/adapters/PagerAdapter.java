package com.james.felixlauncher.adapters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.james.felixlauncher.fragments.AppsFragment;
import com.james.felixlauncher.fragments.ClockFragment;
import com.james.felixlauncher.fragments.FavFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    Activity activity;

    ClockFragment clockFragment;
    AppsFragment appsFragment;
    FavFragment favFragment;

    public PagerAdapter(Activity activity, final FragmentManager manager) {
        super(manager);
        this.activity = activity;

        clockFragment = new ClockFragment();
        appsFragment = new AppsFragment();
        favFragment = new FavFragment();
    }

    @Override
    public Fragment getItem(int position) {
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
                return "Clock";
            case 1:
                return "Apps";
            case 2:
                return "Favorites";
            default:
                return null;
        }
    }

    public void onPageChange(int position) {
        if (position == 1) appsFragment.load();
        else if (position == 2) favFragment.load();
    }

    public void search(String text) {
        appsFragment.search(text);
    }
}
