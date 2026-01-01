package com.uxp.musicq;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FullPlayerActivity extends AppCompatActivity implements MusicService.PlayerUpdateListener {
    private MusicService musicService;
    private boolean serviceBound = false;
    private TextView txtSongTitle, txtArtist, txtAlbum, txtCurrentTime, txtTotalTime;
    private ImageView imgAlbumArt, btnNext, btnPrev, btnShuffle, btnRepeat, btnMetadata, btnHide;
    private FloatingActionButton btnPlay;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private boolean userSeeking = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            musicService.registerListener(FullPlayerActivity.this);
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

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_full_player);

        initViews();
        bindMusicService();
    }

    private void initViews() {
        imgAlbumArt = findViewById(R.id.imgAlbumArt);
        txtSongTitle = findViewById(R.id.txtSongTitle);
        txtArtist = findViewById(R.id.txtArtist);
        txtAlbum = findViewById(R.id.txtAlbum);
        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        txtTotalTime = findViewById(R.id.txtTotalTime);
        seekBar = findViewById(R.id.seekBar);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnMetadata = findViewById(R.id.btnMetadata);
        btnHide = findViewById(R.id.btnHide);

        setupListeners();
    }

    private void setupListeners() {
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
            if (musicService != null) musicService.playNext();
        });

        btnPrev.setOnClickListener(v -> {
            if (musicService != null) musicService.playPrevious();
        });

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            if (musicService != null) musicService.setShuffle(isShuffle);
            btnShuffle.setAlpha(isShuffle ? 1.0f : 0.5f);
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            if (musicService != null) musicService.setRepeat(isRepeat);
            btnRepeat.setAlpha(isRepeat ? 1.0f : 0.5f);
        });

        btnMetadata.setOnClickListener(v -> showMetadata());

        btnHide.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
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

    private void bindMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void showMetadata() {
        if (musicService != null && musicService.getCurrentSong() != null) {
            Song song = musicService.getCurrentSong();
            Intent intent = new Intent(this, MetadataActivity.class);
            intent.putExtra("file_path", song.getPath());
            intent.putExtra("album_id", song.getAlbumId());
            startActivity(intent);
        }
    }

    @Override
    public void onSongChanged(Song song) {
        runOnUiThread(() -> {
            if (song != null) {
                txtSongTitle.setText(song.getTitle());
                txtArtist.setText(song.getArtist());
                txtAlbum.setText(song.getAlbum());
                txtTotalTime.setText(song.getFormattedDuration());
                seekBar.setMax((int) song.getDuration());
                AlbumArtLoader.loadAlbumArt(this, song.getAlbumId(), imgAlbumArt);
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
}