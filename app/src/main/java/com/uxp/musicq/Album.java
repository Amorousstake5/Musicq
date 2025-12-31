package com.uxp.musicq;

public class Album {
    private long id;
    private String name;
    private String artist;
    private int songCount;

    public Album(long id, String name, String artist, int songCount) {
        this.id = id;
        this.name = name != null ? name : "Unknown Album";
        this.artist = artist != null ? artist : "Unknown Artist";
        this.songCount = songCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public int getSongCount() {
        return songCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Album album = (Album) obj;
        return id == album.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}