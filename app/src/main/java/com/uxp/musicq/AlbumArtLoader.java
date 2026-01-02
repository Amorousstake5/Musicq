package com.uxp.musicq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import java.io.File;
import java.lang.ref.WeakReference;

public class AlbumArtLoader {
    private static final String TAG = "AlbumArtLoader";
    private static LruCache<String, Bitmap> memoryCache;
    private static final int MAX_IMAGE_SIZE = 512;

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void loadAlbumArt(Context context, String filePath, ImageView imageView) {
        if (context == null || imageView == null || filePath == null) {
            setDefaultArt(imageView);
            return;
        }

        imageView.setTag(filePath);

        Bitmap cached = memoryCache.get(filePath);
        if (cached != null && !cached.isRecycled()) {
            imageView.setImageBitmap(cached);
            return;
        }

        new Thread(() -> {
            Bitmap bitmap = extractAlbumArt(filePath);
            WeakReference<ImageView> viewRef = new WeakReference<>(imageView);

            imageView.post(() -> {
                ImageView view = viewRef.get();
                if (view != null && filePath.equals(view.getTag())) {
                    if (bitmap != null && !bitmap.isRecycled()) {
                        view.setImageBitmap(bitmap);
                    } else {
                        setDefaultArt(view);
                    }
                }
            });
        }).start();
    }

    private static Bitmap extractAlbumArt(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return null;

            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    if (imageData != null && imageData.length > 0) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

                        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;

                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
                        if (bitmap != null) {
                            memoryCache.put(filePath, bitmap);
                            return bitmap;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting album art: " + e.getMessage());
        }
        return null;
    }

    public static Bitmap getAlbumArt(Context context, String filePath) {
        if (filePath == null) return null;

        Bitmap cached = memoryCache.get(filePath);
        if (cached != null && !cached.isRecycled()) {
            return cached;
        }

        return extractAlbumArt(filePath);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static void setDefaultArt(ImageView imageView) {
        imageView.setImageResource(R.drawable.default_album_art);
    }

    public static void clearCache() {
        if (memoryCache != null) {
            memoryCache.evictAll();
        }
    }
}