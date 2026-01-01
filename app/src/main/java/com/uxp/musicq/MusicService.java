package com.uxp.musicq;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
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
    private MediaSession mediaSession;
    private SharedPreferences prefs;
    private PowerManager.WakeLock wakeLock;
    private boolean batterySaverMode = false;

    private Equalizer equalizer;
    private BassBoost bassBoost;
    private Virtualizer virtualizer;

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
        prefs = getSharedPreferences("harmoniq_settings", MODE_PRIVATE);
        createNotificationChannel();
        initMediaSession();
        initWakeLock();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
                applyAudioSettings();
                mp.start();
                updateMediaSession();
                showNotification();
                notifyPlaybackStateChanged(true);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing", e);
        }
    }

    private void initWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HarmoniQ::MusicPlayback");
        wakeLock.setReferenceCounted(false);
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

    private void initMediaSession() {
        mediaSession = new MediaSession(this, "HarmoniQSession");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                playPrevious();
            }

            @Override
            public void onStop() {
                stopForeground(true);
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                seekTo((int) pos);
            }
        });

        mediaSession.setActive(true);
    }

    private void updateMediaSession() {
        Song song = getCurrentSong();
        if (song == null) return;

        MediaMetadata.Builder builder = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.getArtist())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, song.getAlbum())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, song.getDuration());

        Bitmap albumArt = AlbumArtLoader.getAlbumArt(this, song.getAlbumId());
        if (albumArt != null) {
            builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt);
        }

        mediaSession.setMetadata(builder.build());

        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackState.ACTION_SEEK_TO)
                .setState(isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                        getCurrentPosition(), 1.0f);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void applyAudioSettings() {
        try {
            int volume = prefs.getInt("volume", 100);
            float vol = volume / 100.0f;
            mediaPlayer.setVolume(vol, vol);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                float speed = prefs.getFloat("speed", 1.0f);
                float pitch = prefs.getFloat("pitch", 1.0f);

                if (!batterySaverMode) {
                    mediaPlayer.setPlaybackParams(
                            mediaPlayer.getPlaybackParams().setSpeed(speed).setPitch(pitch)
                    );
                }
            }

            int audioSessionId = mediaPlayer.getAudioSessionId();

            if (prefs.getBoolean("equalizer", false) && !batterySaverMode) {
                if (equalizer == null) {
                    equalizer = new Equalizer(0, audioSessionId);
                }
                equalizer.setEnabled(true);
            } else if (equalizer != null) {
                equalizer.setEnabled(false);
            }

            if (prefs.getBoolean("bass_boost", false) && !batterySaverMode) {
                if (bassBoost == null) {
                    bassBoost = new BassBoost(0, audioSessionId);
                }
                bassBoost.setStrength((short) 500);
                bassBoost.setEnabled(true);
            } else if (bassBoost != null) {
                bassBoost.setEnabled(false);
            }

            if (prefs.getBoolean("virtualizer", false) && !batterySaverMode) {
                if (virtualizer == null) {
                    virtualizer = new Virtualizer(0, audioSessionId);
                }
                virtualizer.setStrength((short) 500);
                virtualizer.setEnabled(true);
            } else if (virtualizer != null) {
                virtualizer.setEnabled(false);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error applying settings", e);
        }
    }

    public void setBatterySaverMode(boolean enabled) {
        this.batterySaverMode = enabled;
        if (enabled) {
            if (equalizer != null) equalizer.setEnabled(false);
            if (bassBoost != null) bassBoost.setEnabled(false);
            if (virtualizer != null) virtualizer.setEnabled(false);
        } else {
            applyAudioSettings();
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

                if (!batterySaverMode && !wakeLock.isHeld()) {
                    wakeLock.acquire();
                }
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
                updateMediaSession();
                showNotification();
                notifyPlaybackStateChanged(true);

                if (!batterySaverMode && !wakeLock.isHeld()) {
                    wakeLock.acquire();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing", e);
        }
    }

    public void pause() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                updateMediaSession();
                showNotification();
                notifyPlaybackStateChanged(false);

                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
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

        Intent notificationIntent = new Intent(this, FullPlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                .setSubText(currentSong.getAlbum())
                .setLargeIcon(albumArt)
                .setContentIntent(pendingIntent)
                .setOngoing(isPlaying())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
                .addAction(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying() ? "Pause" : "Play", playPendingIntent)
                .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                        .setShowActionsInCompactView(0, 1, 2))
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    public void seekTo(int position) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
                updateMediaSession();
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

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (equalizer != null) equalizer.release();
        if (bassBoost != null) bassBoost.release();
        if (virtualizer != null) virtualizer.release();

        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }

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