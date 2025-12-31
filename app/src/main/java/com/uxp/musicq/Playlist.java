package com.uxp.musicq;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private long id;
    private String name;
    private List<Song> songs;
    private long createdDate;

    public Playlist(long id, String name) {
        this.id = id;
        this.name = name != null ? name : "Untitled Playlist";
        this.songs = new ArrayList<>();
        this.createdDate = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return new ArrayList<>(songs);
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setName(String name) {
        this.name = name != null ? name : "Untitled Playlist";
    }

    public void addSong(Song song) {
        if (song != null && !songs.contains(song)) {
            songs.add(song);
        }
    }

    public void removeSong(Song song) {
        if (song != null) {
            songs.remove(song);
        }
    }

    public int getSongCount() {
        return songs.size();
    }

    public long getTotalDuration() {
        long total = 0;
        for (Song song : songs) {
            total += song.getDuration();
        }
        return total;
    }

    public String getFormattedTotalDuration() {
        long totalSeconds = getTotalDuration() / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%d hr %d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Playlist playlist = (Playlist) obj;
        return id == playlist.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}