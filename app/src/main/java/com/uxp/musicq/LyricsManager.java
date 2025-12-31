package com.uxp.musicq;


import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LyricsManager {
    private Context context;
    private static final String LYRICS_DIR = "lyrics";
    private static final String TAG = "LyricsManager";

    public LyricsManager(Context context) {
        this.context = context;
        createLyricsDirectory();
    }

    private void createLyricsDirectory() {
        try {
            File lyricsDir = new File(context.getFilesDir(), LYRICS_DIR);
            if (!lyricsDir.exists()) {
                boolean created = lyricsDir.mkdirs();
                if (created) {
                    Log.d(TAG, "Lyrics directory created");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating lyrics directory", e);
        }
    }

    public boolean saveLyrics(long songId, String lyrics) {
        if (lyrics == null) {
            Log.w(TAG, "Cannot save null lyrics");
            return false;
        }

        File lyricsFile = new File(context.getFilesDir(), LYRICS_DIR + "/" + songId + ".txt");
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(lyricsFile);
            fos.write(lyrics.getBytes());
            Log.d(TAG, "Lyrics saved for song: " + songId);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving lyrics", e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing file stream", e);
                }
            }
        }
    }

    public String loadLyrics(long songId) {
        File lyricsFile = new File(context.getFilesDir(), LYRICS_DIR + "/" + songId + ".txt");

        if (!lyricsFile.exists()) {
            return "";
        }

        StringBuilder lyrics = new StringBuilder();
        FileInputStream fis = null;
        BufferedReader reader = null;

        try {
            fis = new FileInputStream(lyricsFile);
            reader = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = reader.readLine()) != null) {
                lyrics.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading lyrics", e);
            return "";
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing file streams", e);
            }
        }

        return lyrics.toString();
    }

    public boolean deleteLyrics(long songId) {
        try {
            File lyricsFile = new File(context.getFilesDir(), LYRICS_DIR + "/" + songId + ".txt");
            if (lyricsFile.exists()) {
                boolean deleted = lyricsFile.delete();
                if (deleted) {
                    Log.d(TAG, "Lyrics deleted for song: " + songId);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting lyrics", e);
            return false;
        }
    }

    public boolean hasLyrics(long songId) {
        try {
            File lyricsFile = new File(context.getFilesDir(), LYRICS_DIR + "/" + songId + ".txt");
            return lyricsFile.exists() && lyricsFile.length() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking lyrics existence", e);
            return false;
        }
    }
}