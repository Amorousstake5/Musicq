package com.uxp.musicq;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private long albumId;
    private String path;
    private long duration;
    private String lyrics;

    public Song(long id, String title, String artist, String album,
                long albumId, String path, long duration) {
        this.id = id;
        this.title = title != null ? title : "Unknown Title";
        this.artist = artist != null ? artist : "Unknown Artist";
        this.album = album != null ? album : "Unknown Album";
        this.albumId = albumId;
        this.path = path;
        this.duration = duration;
        this.lyrics = "";
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics != null ? lyrics : "";
    }

    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Song song = (Song) obj;
        return id == song.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}