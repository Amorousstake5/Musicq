package com.uxp.musicq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class LyricsActivity extends AppCompatActivity {
    private EditText edtLyrics;
    private Button btnSave, btnDelete;
    private TextView txtSongTitle;
    private LyricsManager lyricsManager;
    private long songId;
    private String songTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        initViews();
        getIntentData();
        loadLyrics();
        setupListeners();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        txtSongTitle = findViewById(R.id.txtLyricsSongTitle);
        edtLyrics = findViewById(R.id.edtLyrics);
        btnSave = findViewById(R.id.btnSaveLyrics);
        btnDelete = findViewById(R.id.btnDeleteLyrics);

        lyricsManager = new LyricsManager(this);
    }

    private void getIntentData() {
        songId = getIntent().getLongExtra("song_id", -1);
        songTitle = getIntent().getStringExtra("song_title");

        if (songTitle != null) {
            txtSongTitle.setText(songTitle);
        }
    }

    private void loadLyrics() {
        if (songId != -1) {
            String lyrics = lyricsManager.loadLyrics(songId);
            edtLyrics.setText(lyrics);

            if (lyrics.isEmpty()) {
                edtLyrics.setHint("Enter lyrics here...");
            }
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveLyrics());
        btnDelete.setOnClickListener(v -> deleteLyrics());
    }

    private void saveLyrics() {
        String lyrics = edtLyrics.getText().toString().trim();

        if (lyrics.isEmpty()) {
            Toast.makeText(this, "Please enter lyrics", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = lyricsManager.saveLyrics(songId, lyrics);
        if (success) {
            Toast.makeText(this, "Lyrics saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save lyrics", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteLyrics() {
        boolean success = lyricsManager.deleteLyrics(songId);
        if (success) {
            edtLyrics.setText("");
            Toast.makeText(this, "Lyrics deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No lyrics to delete", Toast.LENGTH_SHORT).show();
        }
    }
}