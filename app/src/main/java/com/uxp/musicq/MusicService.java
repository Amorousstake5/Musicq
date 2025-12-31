package com.uxp.musicq;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private List<Song> songList;
    private int currentPosition = 0;
    private final IBinder binder = new MusicBinder();
    private boolean shuffle = false;
    private boolean repeat = false;
    private Random random = new Random();
    private static final String TAG = "MusicService";

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mediaPlayer = new MediaPlayer();
            songList = new ArrayList<>();

            mediaPlayer.setOnCompletionListener(mp -> {
                if (repeat) {
                    play();
                } else {
                    playNext();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", extra: " + extra);
                return false;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MediaPlayer", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setSongList(List<Song> songs) {
        if (songs != null) {
            this.songList = new ArrayList<>(songs);
        }
    }

    public void playSong(int position) {
        if (songList == null || songList.isEmpty() || position < 0 || position >= songList.size()) {
            Log.e(TAG, "Invalid song position or empty song list");
            return;
        }

        currentPosition = position;
        Song song = songList.get(position);

        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error playing song: " + song.getTitle(), e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer in invalid state", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error playing song", e);
        }
    }

    public void play() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Cannot play - MediaPlayer in invalid state", e);
        }
    }

    public void pause() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Cannot pause - MediaPlayer in invalid state", e);
        }
    }

    public void playNext() {
        if (songList == null || songList.isEmpty()) return;

        try {
            if (shuffle) {
                currentPosition = random.nextInt(songList.size());
            } else {
                currentPosition = (currentPosition + 1) % songList.size();
            }
            playSong(currentPosition);
        } catch (Exception e) {
            Log.e(TAG, "Error playing next song", e);
        }
    }

    public void playPrevious() {
        if (songList == null || songList.isEmpty()) return;

        try {
            currentPosition = (currentPosition - 1 + songList.size()) % songList.size();
            playSong(currentPosition);
        } catch (Exception e) {
            Log.e(TAG, "Error playing previous song", e);
        }
    }

    public void seekTo(int position) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Cannot seek - MediaPlayer in invalid state", e);
        }
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error checking play state", e);
            return false;
        }
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error getting current position", e);
            return 0;
        }
    }

    public Song getCurrentSong() {
        if (songList != null && currentPosition >= 0 && currentPosition < songList.size()) {
            return songList.get(currentPosition);
        }
        return null;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error destroying service", e);
        }
    }
}