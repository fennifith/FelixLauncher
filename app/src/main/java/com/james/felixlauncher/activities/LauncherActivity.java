package com.james.felixlauncher.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.james.felixlauncher.Felix;
import com.james.felixlauncher.R;
import com.james.felixlauncher.adapters.PagerAdapter;
import com.james.felixlauncher.utils.ImageUtils;

public class LauncherActivity extends AppCompatActivity implements SensorEventListener {

    private ViewPager viewPager;
    private PagerAdapter adapter;
    private ImageView clockImage, appsImage, favImage;
    private TextView clockText, appsText, favText;
    private View coordinator;
    private int primary;

    private SensorManager sensorManager;
    private Sensor sensor;

    private ValueAnimator animator;
    private int color;
    private int oldColor;

    private Felix felix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        felix = (Felix) getApplicationContext();

        coordinator = findViewById(R.id.coordinator);
        clockImage = (ImageView) findViewById(R.id.clockImage);
        appsImage = (ImageView) findViewById(R.id.appsImage);
        favImage = (ImageView) findViewById(R.id.favImage);
        clockText = (TextView) findViewById(R.id.clockText);
        appsText = (TextView) findViewById(R.id.appsText);
        favText = (TextView) findViewById(R.id.favText);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        primary = ContextCompat.getColor(this, android.R.color.secondary_text_dark);

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
                int accent = felix.getAccentColor();

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

        findViewById(R.id.clock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });

        findViewById(R.id.apps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });

        findViewById(R.id.fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
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
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(LauncherActivity.this, SettingsActivity.class));
                break;
            case R.id.action_hidden:
                startActivity(new Intent(LauncherActivity.this, HiddenActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (coordinator != null) {
            color = Color.argb(0, 0, 0, 0);
            coordinator.setBackgroundColor(color);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ImageUtils.addAlpha(color, 70));
                getWindow().setNavigationBarColor(ImageUtils.addAlpha(color, 70));
            }
        }

        oldColor = color;

        if (viewPager != null && adapter != null) {
            if (viewPager.getCurrentItem() != 1) viewPager.setCurrentItem(1);
            else adapter.onPageChange(1);
        }

        if (sensorManager != null && sensor != null)
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (animator != null) animator.cancel();
        int light = (int) sensorEvent.values[0] / 100;

        animator = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, Color.argb(Color.alpha(color) + Math.abs(light - 100), Color.red(color) + light, Color.green(color) + light, Color.blue(color) + light));
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                oldColor = (int) valueAnimator.getAnimatedValue();
                if (coordinator != null) {
                    coordinator.setBackgroundColor(oldColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ImageUtils.addAlpha(oldColor, 70));
                        getWindow().setNavigationBarColor(ImageUtils.addAlpha(oldColor, 70));
                    }
                }
            }
        });
        animator.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
