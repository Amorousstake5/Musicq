package com.uxp.musicq;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity implements MusicService.PlayerUpdateListener {
    private MusicService musicService;
    private boolean serviceBound = false;
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private AlbumAdapter albumAdapter;
    private PlaylistAdapter playlistAdapter;
    private TextView txtSongTitle, txtArtist, txtCurrentTime, txtTotalTime;
    private ImageView imgAlbumArt, btnNext, btnPrev, btnShuffle, btnRepeat;
    private FloatingActionButton btnPlay;
    private SeekBar seekBar;
    private View playerCard;
    private List<Song> allSongs;
    private Handler handler = new Handler();
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private String currentView = "songs";
    private boolean userSeeking = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            musicService.registerListener(PlayerActivity.this);
            loadMusic();
            updateUIFromService();
            startSeekBarUpdate();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        bindMusicService();

        playerCard.setVisibility(View.GONE);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        playerCard = findViewById(R.id.playerCard);
        txtSongTitle = findViewById(R.id.txtSongTitle);
        txtArtist = findViewById(R.id.txtArtist);
        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        txtTotalTime = findViewById(R.id.txtTotalTime);
        imgAlbumArt = findViewById(R.id.imgAlbumArt);
        seekBar = findViewById(R.id.seekBar);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);

        setupControlListeners();
    }

    private void setupControlListeners() {
        btnPlay.setOnClickListener(v -> {
            if (musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.play();
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.playNext();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.playPrevious();
            }
        });

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            if (musicService != null) {
                musicService.setShuffle(isShuffle);
            }
            btnShuffle.setAlpha(isShuffle ? 1.0f : 0.5f);
            Toast.makeText(this, isShuffle ? "Shuffle ON" : "Shuffle OFF", Toast.LENGTH_SHORT).show();
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            if (musicService != null) {
                musicService.setRepeat(isRepeat);
            }
            btnRepeat.setAlpha(isRepeat ? 1.0f : 0.5f);
            Toast.makeText(this, isRepeat ? "Repeat ON" : "Repeat OFF", Toast.LENGTH_SHORT).show();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    txtCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicService != null) {
                    musicService.seekTo(seekBar.getProgress());
                }
                userSeeking = false;
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allSongs = new ArrayList<>();
        songAdapter = new SongAdapter(allSongs, this::onSongClick);
        recyclerView.setAdapter(songAdapter);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_songs) {
                currentView = "songs";
                loadMusic();
                return true;
            } else if (id == R.id.nav_albums) {
                currentView = "albums";
                loadAlbums();
                return true;
            } else if (id == R.id.nav_playlists) {
                currentView = "playlists";
                loadPlaylists();
                return true;
            }
            return false;
        });

        findViewById(R.id.toolbar).setOnClickListener(v -> showMenu());
    }

    private void showMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, findViewById(R.id.toolbar));
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_create_playlist) {
                startActivity(new Intent(this, CreatePlaylistActivity.class));
                return true;
            } else if (id == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.menu_credits) {
                startActivity(new Intent(this, CreditsActivity.class));
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void bindMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void loadMusic() {
        try {
            MusicLoader loader = new MusicLoader(this);
            allSongs = loader.loadSongs();

            if (allSongs.isEmpty()) {
                Toast.makeText(this, "No music found", Toast.LENGTH_LONG).show();
            }

            songAdapter.updateSongs(allSongs);
            recyclerView.setAdapter(songAdapter);

            if (musicService != null) {
                musicService.setSongList(allSongs);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading music", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAlbums() {
        try {
            MusicLoader loader = new MusicLoader(this);
            List<Album> albums = loader.loadAlbums();

            if (albumAdapter == null) {
                albumAdapter = new AlbumAdapter(albums, this::onAlbumClick);
            } else {
                albumAdapter.updateAlbums(albums);
            }

            recyclerView.setAdapter(albumAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading albums", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPlaylists() {
        try {
            PlaylistManager manager = new PlaylistManager(this);
            List<Playlist> playlists = manager.getAllPlaylists();

            if (playlistAdapter == null) {
                playlistAdapter = new PlaylistAdapter(playlists, new PlaylistAdapter.OnPlaylistClickListener() {
                    @Override
                    public void onPlaylistClick(Playlist playlist, int position) {
                        Toast.makeText(PlayerActivity.this, "Playlist: " + playlist.getName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPlaylistLongClick(Playlist playlist, int position) {
                        Toast.makeText(PlayerActivity.this, "Long press", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                playlistAdapter.updatePlaylists(playlists);
            }

            recyclerView.setAdapter(playlistAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading playlists", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSongClick(Song song, int position) {
        if (musicService != null) {
            musicService.playSong(position);
            playerCard.setVisibility(View.VISIBLE);
        }
    }

    private void onAlbumClick(Album album, int position) {
        Intent intent = new Intent(this, AlbumDetailActivity.class);
        intent.putExtra("album_id", album.getId());
        intent.putExtra("album_name", album.getName());
        intent.putExtra("artist_name", album.getArtist());
        startActivity(intent);
    }

    @Override
    public void onSongChanged(Song song) {
        runOnUiThread(() -> {
            if (song != null) {
                txtSongTitle.setText(song.getTitle());
                txtArtist.setText(song.getArtist());
                txtTotalTime.setText(song.getFormattedDuration());
                seekBar.setMax((int) song.getDuration());
                AlbumArtLoader.loadAlbumArt(this, song.getAlbumId(), imgAlbumArt);
                playerCard.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        runOnUiThread(() -> {
            btnPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        });
    }

    private void updateUIFromService() {
        if (musicService != null && musicService.getCurrentSong() != null) {
            onSongChanged(musicService.getCurrentSong());
            onPlaybackStateChanged(musicService.isPlaying());
        }
    }

    private void startSeekBarUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (musicService != null && !userSeeking) {
                    int currentPos = musicService.getCurrentPosition();
                    seekBar.setProgress(currentPos);
                    txtCurrentTime.setText(formatTime(currentPos));
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (serviceBound && musicService != null) {
            musicService.unregisterListener(this);
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicService != null) {
            updateUIFromService();
        }
    }
}