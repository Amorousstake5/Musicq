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
    private SongAdapter songAdapter;
    private ImageView imgAlbumArt;
    private TextView txtAlbumName, txtArtist, txtSongCount;
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

        getIntentData();
        initViews();
        loadAlbumSongs();
        bindMusicService();
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

        imgAlbumArt = findViewById(R.id.imgAlbumArt);
        txtAlbumName = findViewById(R.id.txtAlbumName);
        txtArtist = findViewById(R.id.txtArtist);
        txtSongCount = findViewById(R.id.txtSongCount);
        recyclerView = findViewById(R.id.recyclerView);

        txtAlbumName.setText(albumName);
        txtArtist.setText(artistName);
        AlbumArtLoader.loadAlbumArt(this, albumId, imgAlbumArt);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAlbumSongs() {
        MusicLoader loader = new MusicLoader(this);
        albumSongs = loader.loadSongsByAlbum(albumId);

        txtSongCount.setText(albumSongs.size() + " songs");

        songAdapter = new SongAdapter(albumSongs, this::onSongClick);
        recyclerView.setAdapter(songAdapter);
    }

    private void onSongClick(Song song, int position) {
        if (musicService != null) {
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