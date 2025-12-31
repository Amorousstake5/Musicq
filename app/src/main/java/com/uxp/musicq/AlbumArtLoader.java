package com.uxp.musicq;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AlbumArtLoader {
    private static final String TAG = "AlbumArtLoader";

    public static void loadAlbumArt(Context context, long albumId, ImageView imageView) {
        if (context == null || imageView == null) {
            Log.e(TAG, "Context or ImageView is null");
            return;
        }

        try {
            Uri albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId);

            InputStream inputStream = context.getContentResolver().openInputStream(albumArtUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.default_album_art);
                }
                inputStream.close();
            } else {
                imageView.setImageResource(R.drawable.default_album_art);
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Album art not found for album ID: " + albumId);
            imageView.setImageResource(R.drawable.default_album_art);
        } catch (Exception e) {
            Log.e(TAG, "Error loading album art", e);
            imageView.setImageResource(R.drawable.default_album_art);
        }
    }

    public static Bitmap getAlbumArt(Context context, long albumId) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return null;
        }

        try {
            Uri albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId);

            InputStream inputStream = context.getContentResolver().openInputStream(albumArtUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Album art not found for album ID: " + albumId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting album art bitmap", e);
        }
        return null;
    }
}