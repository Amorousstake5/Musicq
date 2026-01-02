package com.uxp.musicq;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class MemoryManager {
    private static final String TAG = "MemoryManager";
    private static long lastCleanup = 0;
    private static final long CLEANUP_INTERVAL = 60000; // 1 minute

    public static void checkMemory(Context context) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanup > CLEANUP_INTERVAL) {
            cleanupMemory();
            lastCleanup = currentTime;
            logMemoryUsage(context);
        }
    }

    private static void cleanupMemory() {
        System.gc();
        AlbumArtLoader.clearCache();
    }

    private static void logMemoryUsage(Context context) {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memInfo);

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;

        Log.d(TAG, "Memory usage: " + usedMemory + " MB");
    }

    public static void onLowMemory() {
        Log.w(TAG, "Low memory - clearing cache");
        AlbumArtLoader.clearCache();
        System.gc();
    }
}