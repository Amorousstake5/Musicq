package com.uxp.musicq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "harmoniq.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "PlaylistManager";

    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String TABLE_PLAYLIST_SONGS = "playlist_songs";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_CREATED_DATE = "created_date";
    private static final String COL_PLAYLIST_ID = "playlist_id";
    private static final String COL_SONG_ID = "song_id";

    public PlaylistManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String createPlaylistsTable = "CREATE TABLE " + TABLE_PLAYLISTS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_CREATED_DATE + " INTEGER)";

            String createPlaylistSongsTable = "CREATE TABLE " + TABLE_PLAYLIST_SONGS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PLAYLIST_ID + " INTEGER, " +
                    COL_SONG_ID + " INTEGER)";

            db.execSQL(createPlaylistsTable);
            db.execSQL(createPlaylistSongsTable);
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
            onCreate(db);
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
        }
    }

    public long createPlaylist(String name) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_NAME, name);
            values.put(COL_CREATED_DATE, System.currentTimeMillis());

            long id = db.insert(TABLE_PLAYLISTS, null, values);
            Log.d(TAG, "Playlist created with ID: " + id);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error creating playlist", e);
            return -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean addSongToPlaylist(long playlistId, long songId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_PLAYLIST_ID, playlistId);
            values.put(COL_SONG_ID, songId);

            long result = db.insert(TABLE_PLAYLIST_SONGS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding song to playlist", e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean removeSongFromPlaylist(long playlistId, long songId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rows = db.delete(TABLE_PLAYLIST_SONGS,
                    COL_PLAYLIST_ID + "=? AND " + COL_SONG_ID + "=?",
                    new String[]{String.valueOf(playlistId), String.valueOf(songId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error removing song from playlist", e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean deletePlaylist(long playlistId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.delete(TABLE_PLAYLISTS, COL_ID + "=?",
                    new String[]{String.valueOf(playlistId)});
            db.delete(TABLE_PLAYLIST_SONGS, COL_PLAYLIST_ID + "=?",
                    new String[]{String.valueOf(playlistId)});
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting playlist", e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_PLAYLISTS, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(COL_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(COL_NAME);

                do {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);

                    Playlist playlist = new Playlist(id, name);
                    playlists.add(playlist);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting playlists", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return playlists;
    }

    public List<Long> getPlaylistSongIds(long playlistId) {
        List<Long> songIds = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_PLAYLIST_SONGS,
                    new String[]{COL_SONG_ID},
                    COL_PLAYLIST_ID + "=?",
                    new String[]{String.valueOf(playlistId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int songIdIndex = cursor.getColumnIndexOrThrow(COL_SONG_ID);
                do {
                    songIds.add(cursor.getLong(songIdIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting playlist songs", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return songIds;
    }
}