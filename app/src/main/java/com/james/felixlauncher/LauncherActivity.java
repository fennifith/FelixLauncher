package com.james.felixlauncher;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.animation.AnimatorListenerCompat;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
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
    View clock, apps, fav;
    ImageView clockImage, appsImage, favImage;
    TextView clockText, appsText, favText;
    int primary, accent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clock = findViewById(R.id.clock);
        apps = findViewById(R.id.apps);
        fav = findViewById(R.id.fav);

        clockImage = (ImageView) findViewById(R.id.clockImage);
        appsImage = (ImageView) findViewById(R.id.appsImage);
        favImage = (ImageView) findViewById(R.id.favImage);

        clockText = (TextView) findViewById(R.id.clockText);
        appsText = (TextView) findViewById(R.id.appsText);
        favText = (TextView) findViewById(R.id.favText);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        primary = ContextCompat.getColor(this, android.R.color.secondary_text_dark);

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
                if (position == 0) {
                    clockImage.setColorFilter(accent);
                    clockText.setTextColor(accent);
                    animateText(14, clockText);
                } else {
                    clockImage.setColorFilter(primary);
                    clockText.setTextColor(primary);
                    animateText(0, clockText);
                }

                if (position == 1) {
                    appsImage.setColorFilter(accent);
                    appsText.setTextColor(accent);
                    animateText(14, appsText);
                } else {
                    appsImage.setColorFilter(primary);
                    appsText.setTextColor(primary);
                    animateText(0, appsText);
                }

                if (position == 2) {
                    favImage.setColorFilter(accent);
                    favText.setTextColor(accent);
                    animateText(14, favText);
                } else {
                    favImage.setColorFilter(primary);
                    favText.setTextColor(primary);
                    animateText(0, favText);
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

    private void animateText(final int end, final TextView textView) {
        ValueAnimator animator = ValueAnimator.ofFloat(textView.getTextSize() / getResources().getDisplayMetrics().scaledDensity, end);
        animator.setDuration(150);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) animation.getAnimatedValue());
            }
        });

        animator.start();
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
