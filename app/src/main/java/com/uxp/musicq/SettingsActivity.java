package com.uxp.musicq;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private SeekBar seekBarVolume, seekBarSpeed, seekBarPitch, seekBarCrossfade;
    private TextView txtVolume, txtSpeed, txtPitch, txtCrossfade;
    private Switch switchEqualizer, switchBassBoost, switchVirtualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("harmoniq_settings", MODE_PRIVATE);
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

        switchEqualizer.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveBoolean("equalizer", isChecked));

        switchBassBoost.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveBoolean("bass_boost", isChecked));

        switchVirtualizer.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveBoolean("virtualizer", isChecked));
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
}
