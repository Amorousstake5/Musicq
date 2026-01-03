package com.uxp.musicq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreatePlaylistActivity extends AppCompatActivity {
    private TextInputEditText edtPlaylistName;
    private Button btnCreatePlaylist;
    private RecyclerView recyclerView;
    private SongSelectionAdapter songAdapter;
    private List<Song> allSongs;
    private List<Song> selectedSongs;
    private XspfPlaylistManager playlistManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        initViews();
        loadSongs();
        setupListeners();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        edtPlaylistName = findViewById(R.id.edtPlaylistName);
        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
        recyclerView = findViewById(R.id.recyclerView);

        playlistManager = new XspfPlaylistManager(this);
        selectedSongs = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadSongs() {
        MusicLoader loader = new MusicLoader(this);
        allSongs = loader.loadSongs();

        songAdapter = new SongSelectionAdapter(allSongs, (song, isSelected) -> {
            if (isSelected) {
                selectedSongs.add(song);
            } else {
                selectedSongs.remove(song);
            }
            updateButtonText();
        });

        recyclerView.setAdapter(songAdapter);
    }

    private void setupListeners() {
        btnCreatePlaylist.setOnClickListener(v -> createPlaylist());
    }

    private void createPlaylist() {
        String name = Objects.requireNonNull(edtPlaylistName.getText()).toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter playlist name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSongs.isEmpty()) {
            Toast.makeText(this, "Please select at least one song", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = playlistManager.createPlaylist(name, selectedSongs);

        if (success) {
            Toast.makeText(this, "Playlist created successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to create playlist", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateButtonText() {
        btnCreatePlaylist.setText("Create Playlist (" + selectedSongs.size() + " songs)");
    }
}
