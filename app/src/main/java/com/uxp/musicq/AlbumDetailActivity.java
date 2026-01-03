package com.uxp.musicq;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;

public class AlbumDetailActivity extends AppCompatActivity {
    private MusicService musicService;
    private boolean serviceBound = false;
    private RecyclerView recyclerView;
    private TextView txtSongCount;
    private ImageView imgAlbumArt; // Moved to class level for easier access
    private long albumId;
    private String albumName, artistName;
    private List<Song> albumSongs;

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
        setContentView(R.layout.activity_album_detail);

        // Order matters!
        getIntentData();    // 1. Get IDs from Intent
        initViews();        // 2. Link XML IDs to Java objects (findViewById)
        loadAlbumSongs();   // 3. Load data and fill the views now that they aren't null
        bindMusicService(); // 4. Connect to service
    }

    private void getIntentData() {
        albumId = getIntent().getLongExtra("album_id", -1);
        albumName = getIntent().getStringExtra("album_name");
        artistName = getIntent().getStringExtra("artist_name");
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // We find all views here, but don't use 'albumSongs' yet because it's null
        imgAlbumArt = findViewById(R.id.imgAlbumArt);
        TextView txtAlbumName = findViewById(R.id.txtAlbumName);
        TextView txtArtist = findViewById(R.id.txtArtist);
        txtSongCount = findViewById(R.id.txtSongCount);
        recyclerView = findViewById(R.id.recyclerView);

        txtAlbumName.setText(albumName);
        txtArtist.setText(artistName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAlbumSongs() {
        MusicLoader loader = new MusicLoader(this);
        albumSongs = loader.loadSongsByAlbum(albumId);

        // Add safety check: Ensure the list was loaded and views are ready
        if (albumSongs != null) {
            txtSongCount.setText(String.format("%d songs", albumSongs.size()));

            if (!albumSongs.isEmpty()) {
                // Now it's safe to use albumSongs because it was just initialized above
                AlbumArtLoader.loadAlbumArt(this, albumSongs.get(0).getPath(), imgAlbumArt);
            }

            SongAdapter songAdapter = new SongAdapter(albumSongs, this::onSongClick);
            recyclerView.setAdapter(songAdapter);
        } else {
            txtSongCount.setText("0 songs");
        }
    }

    private void onSongClick(Song song, int position) {
        if (musicService != null && albumSongs != null) {
            musicService.setSongList(albumSongs);
            musicService.playSong(position);
        }
    }

    private void bindMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }
}