package com.uxp.musicq;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MusicLoader {
    private Context context;
    private static final String TAG = "MusicLoader";

    public MusicLoader(Context context) {
        this.context = context;
    }

    public List<Song> loadSongs() {
        List<Song> songs = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, selection, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                do {
                    try {
                        long id = cursor.getLong(idColumn);
                        String title = cursor.getString(titleColumn);
                        String artist = cursor.getString(artistColumn);
                        String album = cursor.getString(albumColumn);
                        long albumId = cursor.getLong(albumIdColumn);
                        String path = cursor.getString(pathColumn);
                        long duration = cursor.getLong(durationColumn);

                        Song song = new Song(id, title, artist, album, albumId, path, duration);
                        songs.add(song);
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading individual song", e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading songs", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Loaded " + songs.size() + " songs");
        return songs;
    }

    public List<Song> loadSongsByAlbum(long albumId) {
        List<Song> songs = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.ALBUM_ID + " = ? AND " +
                MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] selectionArgs = {String.valueOf(albumId)};
        String sortOrder = MediaStore.Audio.Media.TRACK + " ASC";

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                do {
                    try {
                        long id = cursor.getLong(idColumn);
                        String title = cursor.getString(titleColumn);
                        String artist = cursor.getString(artistColumn);
                        String album = cursor.getString(albumColumn);
                        long albId = cursor.getLong(albumIdColumn);
                        String path = cursor.getString(pathColumn);
                        long duration = cursor.getLong(durationColumn);

                        Song song = new Song(id, title, artist, album, albId, path, duration);
                        songs.add(song);
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading song from album", e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading album songs", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return songs;
    }

    public List<Album> loadAlbums() {
        List<Album> albums = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        };

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, null, null,
                    MediaStore.Audio.Albums.ALBUM + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
                int songsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

                do {
                    try {
                        long id = cursor.getLong(idColumn);
                        String album = cursor.getString(albumColumn);
                        String artist = cursor.getString(artistColumn);
                        int songCount = cursor.getInt(songsColumn);

                        Album albumObj = new Album(id, album, artist, songCount);
                        albums.add(albumObj);
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading individual album", e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading albums", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Loaded " + albums.size() + " albums");
        return albums;
    }
}