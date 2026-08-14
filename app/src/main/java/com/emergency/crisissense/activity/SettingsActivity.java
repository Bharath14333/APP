package com.emergency.crisissense.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.emergency.crisissense.R;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat switchNotifications, switchLocation, switchDarkMode;
    private Button btnHelp, btnAbout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        switchNotifications = findViewById(R.id.switch_notifications);
        switchLocation = findViewById(R.id.switch_location);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        btnHelp = findViewById(R.id.btn_help_support);
        btnAbout = findViewById(R.id.btn_about_app);

        sharedPreferences = getSharedPreferences("CrisisSensePrefs", MODE_PRIVATE);

        loadSettings();

        // Save switch settings
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications", isChecked).apply();
            Toast.makeText(this, isChecked ? "Push notifications enabled" : "Push notifications disabled", Toast.LENGTH_SHORT).show();
        });

        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("location", isChecked).apply();
            Toast.makeText(this, isChecked ? "GPS tracking enabled" : "GPS tracking disabled", Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Toast.makeText(this, isChecked ? "Dark theme activated" : "Light theme activated", Toast.LENGTH_SHORT).show();
        });

        btnHelp.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, HelpSupportActivity.class));
        });

        btnAbout.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
        });
    }

    private void loadSettings() {
        boolean notifVal = sharedPreferences.getBoolean("notifications", true);
        boolean locVal = sharedPreferences.getBoolean("location", true);
        boolean darkVal = sharedPreferences.getBoolean("dark_mode", false);

        switchNotifications.setChecked(notifVal);
        switchLocation.setChecked(locVal);
        switchDarkMode.setChecked(darkVal);
    }
}
