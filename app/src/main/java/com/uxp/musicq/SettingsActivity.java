package com.uxp.musicq;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private MusicService musicService;
    private boolean serviceBound = false;
    private SeekBar seekBarVolume, seekBarSpeed, seekBarPitch, seekBarCrossfade;
    private TextView txtVolume, txtSpeed, txtPitch, txtCrossfade;
    private Switch switchEqualizer, switchBassBoost, switchVirtualizer, switchBatterySaver;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("harmoniq_settings", MODE_PRIVATE);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        seekBarVolume = findViewById(R.id.seekBarVolume);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);
        seekBarPitch = findViewById(R.id.seekBarPitch);
        seekBarCrossfade = findViewById(R.id.seekBarCrossfade);

        txtVolume = findViewById(R.id.txtVolume);
        txtSpeed = findViewById(R.id.txtSpeed);
        txtPitch = findViewById(R.id.txtPitch);
        txtCrossfade = findViewById(R.id.txtCrossfade);

        switchEqualizer = findViewById(R.id.switchEqualizer);
        switchBassBoost = findViewById(R.id.switchBassBoost);
        switchVirtualizer = findViewById(R.id.switchVirtualizer);
        switchBatterySaver = findViewById(R.id.switchBatterySaver);
    }

    private void loadSettings() {
        int volume = prefs.getInt("volume", 100);
        float speed = prefs.getFloat("speed", 1.0f);
        float pitch = prefs.getFloat("pitch", 1.0f);
        int crossfade = prefs.getInt("crossfade", 5);

        seekBarVolume.setProgress(volume);
        seekBarSpeed.setProgress((int)((speed - 0.5f) * 100));
        seekBarPitch.setProgress((int)((pitch - 0.5f) * 100));
        seekBarCrossfade.setProgress(crossfade);

        updateLabels();

        switchEqualizer.setChecked(prefs.getBoolean("equalizer", false));
        switchBassBoost.setChecked(prefs.getBoolean("bass_boost", false));
        switchVirtualizer.setChecked(prefs.getBoolean("virtualizer", false));
        switchBatterySaver.setChecked(prefs.getBoolean("battery_saver", false));
    }

    private void setupListeners() {
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtVolume.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveInt("volume", seekBar.getProgress());
                Toast.makeText(SettingsActivity.this, "Restart playback to apply", Toast.LENGTH_SHORT).show();
            }
        });

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = 0.5f + (progress / 100.0f);
                txtSpeed.setText(String.format("%.2fx", speed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float speed = 0.5f + (seekBar.getProgress() / 100.0f);
                saveFloat("speed", speed);
                Toast.makeText(SettingsActivity.this, "Restart playback to apply", Toast.LENGTH_SHORT).show();
            }
        });

        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float pitch = 0.5f + (progress / 100.0f);
                txtPitch.setText(String.format("%.2fx", pitch));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float pitch = 0.5f + (seekBar.getProgress() / 100.0f);
                saveFloat("pitch", pitch);
                Toast.makeText(SettingsActivity.this, "Restart playback to apply", Toast.LENGTH_SHORT).show();
            }
        });

        seekBarCrossfade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtCrossfade.setText(progress + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveInt("crossfade", seekBar.getProgress());
            }
        });

        switchEqualizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBoolean("equalizer", isChecked);
            Toast.makeText(this, "Restart playback to apply", Toast.LENGTH_SHORT).show();
        });

        switchBassBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBoolean("bass_boost", isChecked);
            Toast.makeText(this, "Restart playback to apply", Toast.LENGTH_SHORT).show();
        });

        switchVirtualizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBoolean("virtualizer", isChecked);
            Toast.makeText(this, "Restart playback to apply", Toast.LENGTH_SHORT).show();
        });

        switchBatterySaver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBoolean("battery_saver", isChecked);
            if (musicService != null) {
                musicService.setBatterySaverMode(isChecked);
            }
            Toast.makeText(this, isChecked ? "Battery saver enabled" : "Battery saver disabled",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLabels() {
        txtVolume.setText(seekBarVolume.getProgress() + "%");
        float speed = 0.5f + (seekBarSpeed.getProgress() / 100.0f);
        txtSpeed.setText(String.format("%.2fx", speed));
        float pitch = 0.5f + (seekBarPitch.getProgress() / 100.0f);
        txtPitch.setText(String.format("%.2fx", pitch));
        txtCrossfade.setText(seekBarCrossfade.getProgress() + "s");
    }

    private void saveInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    private void saveFloat(String key, float value) {
        prefs.edit().putFloat(key, value).apply();
    }

    private void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }
}