package com.james.felixlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

public class LauncherActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager viewPager;
    PagerAdapter adapter;
    ImageView clock, apps, fav;
    int primary, accent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clock = (ImageView) findViewById(R.id.clock);
        apps = (ImageView) findViewById(R.id.apps);
        fav = (ImageView) findViewById(R.id.fav);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TypedValue tp = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimaryInverse, tp, true);
        primary = tp.data;

        accent = ContextCompat.getColor(this, R.color.colorAccent);

        viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new PagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch(position) {
                    case 0:
                        clock.setColorFilter(accent);
                        apps.setColorFilter(primary);
                        fav.setColorFilter(primary);
                        break;
                    case 1:
                        clock.setColorFilter(primary);
                        apps.setColorFilter(accent);
                        fav.setColorFilter(primary);
                        break;
                    case 2:
                        clock.setColorFilter(primary);
                        apps.setColorFilter(primary);
                        fav.setColorFilter(accent);
                        break;
                }

                adapter.onPageChange(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });

        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (viewPager.getCurrentItem() != 1) viewPager.setCurrentItem(1);
                adapter.search(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.search(null);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) startActivity(new Intent(LauncherActivity.this, AboutActivity.class));
        return super.onOptionsItemSelected(item);
    }
}
