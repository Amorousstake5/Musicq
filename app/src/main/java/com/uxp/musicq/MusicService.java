package com.uxp.musicq;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
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
    private static final String CHANNEL_ID = "harmoniq_playback";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PLAY = "com.example.harmoniq.PLAY";
    public static final String ACTION_PAUSE = "com.example.harmoniq.PAUSE";
    public static final String ACTION_NEXT = "com.example.harmoniq.NEXT";
    public static final String ACTION_PREV = "com.example.harmoniq.PREV";
    public static final String ACTION_STOP = "com.example.harmoniq.STOP";

    private List<PlayerUpdateListener> listeners = new ArrayList<>();

    public interface PlayerUpdateListener {
        void onSongChanged(Song song);
        void onPlaybackStateChanged(boolean isPlaying);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

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
                Log.e(TAG, "MediaPlayer error: " + what);
                return false;
            });

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                showNotification();
                notifyPlaybackStateChanged(true);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY:
                    play();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREV:
                    playPrevious();
                    break;
                case ACTION_STOP:
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void registerListener(PlayerUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(PlayerUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifySongChanged(Song song) {
        for (PlayerUpdateListener listener : listeners) {
            listener.onSongChanged(song);
        }
    }

    private void notifyPlaybackStateChanged(boolean isPlaying) {
        for (PlayerUpdateListener listener : listeners) {
            listener.onPlaybackStateChanged(isPlaying);
        }
    }

    public void setSongList(List<Song> songs) {
        if (songs != null) {
            this.songList = new ArrayList<>(songs);
        }
    }

    public void playSong(int position) {
        if (songList == null || songList.isEmpty() || position < 0 || position >= songList.size()) {
            return;
        }

        currentPosition = position;
        Song song = songList.get(position);

        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getPath());
                mediaPlayer.prepareAsync();
                notifySongChanged(song);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error playing song", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
        }
    }

    public void play() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                showNotification();
                notifyPlaybackStateChanged(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing", e);
        }
    }

    public void pause() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                showNotification();
                notifyPlaybackStateChanged(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error pausing", e);
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
            Log.e(TAG, "Error playing next", e);
        }
    }

    public void playPrevious() {
        if (songList == null || songList.isEmpty()) return;

        try {
            currentPosition = (currentPosition - 1 + songList.size()) % songList.size();
            playSong(currentPosition);
        } catch (Exception e) {
            Log.e(TAG, "Error playing previous", e);
        }
    }

    private void showNotification() {
        Song currentSong = getCurrentSong();
        if (currentSong == null) return;

        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, MusicService.class).setAction(isPlaying() ? ACTION_PAUSE : ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(
                this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, MusicService.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(
                this, 1, nextIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, MusicService.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(
                this, 2, prevIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap albumArt = AlbumArtLoader.getAlbumArt(this, currentSong.getAlbumId());

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtist())
                .setLargeIcon(albumArt)
                .setContentIntent(pendingIntent)
                .setOngoing(isPlaying())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
                .addAction(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying() ? "Pause" : "Play", playPendingIntent)
                .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    public void seekTo(int position) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error seeking", e);
        }
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        } catch (Exception e) {
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
        listeners.clear();
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