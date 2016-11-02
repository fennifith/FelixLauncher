package com.james.felixlauncher.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.james.felixlauncher.R;


public class SettingsActivity extends AppCompatActivity {

    private final static String DARK_TEXT = "darkText", WALLPAPER = "wallpaper";
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_settings);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ((SwitchCompat) findViewById(R.id.darktext)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(DARK_TEXT, isChecked).apply();
            }
        });

        ((SwitchCompat) findViewById(R.id.wallpaper)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(WALLPAPER, isChecked).apply();
            }
        });

        findViewById(R.id.setwallpaper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 100);
            }
        });
    }

    public static boolean isTextDark(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DARK_TEXT, false);
    }

    public static int getPrimaryTextColor(Context context) {
        return ContextCompat.getColor(context, isTextDark(context) ? R.color.textColorPrimaryDark : R.color.textColorPrimary);
    }

    public static int getSecondaryTextColor(Context context) {
        return ContextCompat.getColor(context, isTextDark(context) ? R.color.textColorSecondaryDark : R.color.textColorSecondary);
    }

    public static boolean isWallpaper(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(WALLPAPER, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(data.getData(), "image/*");
            intent.putExtra("mimeType", "image/*");
            startActivity(Intent.createChooser(intent, "Set as:"));
        }
    }
}
