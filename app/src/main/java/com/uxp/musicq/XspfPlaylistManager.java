package com.uxp.musicq;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XspfPlaylistManager {
    private static final String TAG = "XspfPlaylistManager";
    private File playlistDir;

    public XspfPlaylistManager(Context context) {
        // Use external storage with proper path
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        playlistDir = new File(musicDir, "HarmoniQ/Playlists");

        if (!playlistDir.exists()) {
            if (playlistDir.mkdirs()) {
                Log.d(TAG, "Playlist directory created: " + playlistDir.getAbsolutePath());
            }
        }
    }

    public boolean createPlaylist(String name, List<Song> songs) {
        try {
            File playlistFile = new File(playlistDir, sanitizeFilename(name) + ".xspf");

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element playlist = doc.createElement("playlist");
            playlist.setAttribute("version", "1");
            playlist.setAttribute("xmlns", "http://xspf.org/ns/0/");
            doc.appendChild(playlist);

            Element title = doc.createElement("title");
            title.appendChild(doc.createTextNode(name));
            playlist.appendChild(title);

            Element creator = doc.createElement("creator");
            creator.appendChild(doc.createTextNode("HarmoniQ Music Player"));
            playlist.appendChild(creator);

            Element trackList = doc.createElement("trackList");
            playlist.appendChild(trackList);

            for (Song song : songs) {
                Element track = doc.createElement("track");

                Element location = doc.createElement("location");
                location.appendChild(doc.createTextNode("file://" + song.getPath()));
                track.appendChild(location);

                Element trackTitle = doc.createElement("title");
                trackTitle.appendChild(doc.createTextNode(song.getTitle()));
                track.appendChild(trackTitle);

                Element artist = doc.createElement("creator");
                artist.appendChild(doc.createTextNode(song.getArtist()));
                track.appendChild(artist);

                Element album = doc.createElement("album");
                album.appendChild(doc.createTextNode(song.getAlbum()));
                track.appendChild(album);

                Element duration = doc.createElement("duration");
                duration.appendChild(doc.createTextNode(String.valueOf(song.getDuration())));
                track.appendChild(duration);

                trackList.appendChild(track);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(playlistFile);
            transformer.transform(source, result);

            Log.d(TAG, "Playlist created: " + playlistFile.getAbsolutePath());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error creating playlist", e);
            return false;
        }
    }

    public List<XspfPlaylist> getAllPlaylists() {
        List<XspfPlaylist> playlists = new ArrayList<>();

        if (!playlistDir.exists()) {
            return playlists;
        }

        File[] files = playlistDir.listFiles((dir, name) -> name.endsWith(".xspf"));

        if (files != null) {
            for (File file : files) {
                try {
                    XspfPlaylist playlist = loadPlaylist(file);
                    if (playlist != null) {
                        playlists.add(playlist);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading playlist: " + file.getName(), e);
                }
            }
        }

        return playlists;
    }

    public XspfPlaylist loadPlaylist(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            String title = getElementValue(doc, "title");
            List<Song> songs = new ArrayList<>();

            NodeList trackNodes = doc.getElementsByTagName("track");
            for (int i = 0; i < trackNodes.getLength(); i++) {
                Node trackNode = trackNodes.item(i);
                if (trackNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element trackElement = (Element) trackNode;

                    String location = getElementValue(trackElement, "location");
                    if (location != null && location.startsWith("file://")) {
                        location = location.substring(7);
                    }

                    String trackTitle = getElementValue(trackElement, "title");
                    String artist = getElementValue(trackElement, "creator");
                    String album = getElementValue(trackElement, "album");
                    String durationStr = getElementValue(trackElement, "duration");

                    long duration = 0;
                    try {
                        duration = Long.parseLong(durationStr);
                    } catch (Exception e) {
                        duration = 0;
                    }

                    if (location != null && new File(location).exists()) {
                        Song song = new Song(i, trackTitle, artist, album, 0, location, duration);
                        songs.add(song);
                    }
                }
            }

            return new XspfPlaylist(file.getName().replace(".xspf", ""), songs, file);

        } catch (Exception e) {
            Log.e(TAG, "Error loading playlist file", e);
            return null;
        }
    }

    public boolean deletePlaylist(String name) {
        try {
            File file = new File(playlistDir, sanitizeFilename(name) + ".xspf");
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Log.d(TAG, "Playlist deleted: " + name);
                }
                return deleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting playlist", e);
        }
        return false;
    }

    public boolean addSongsToPlaylist(String playlistName, List<Song> newSongs) {
        try {
            File file = new File(playlistDir, sanitizeFilename(playlistName) + ".xspf");
            if (!file.exists()) {
                return false;
            }

            XspfPlaylist playlist = loadPlaylist(file);
            if (playlist != null) {
                List<Song> allSongs = new ArrayList<>(playlist.getSongs());
                allSongs.addAll(newSongs);
                return createPlaylist(playlistName, allSongs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding songs to playlist", e);
        }
        return false;
    }

    public boolean removeSongFromPlaylist(String playlistName, String songPath) {
        try {
            File file = new File(playlistDir, sanitizeFilename(playlistName) + ".xspf");
            if (!file.exists()) {
                return false;
            }

            XspfPlaylist playlist = loadPlaylist(file);
            if (playlist != null) {
                List<Song> songs = new ArrayList<>(playlist.getSongs());
                songs.removeIf(song -> song.getPath().equals(songPath));
                return createPlaylist(playlistName, songs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing song from playlist", e);
        }
        return false;
    }

    private String getElementValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return node.getTextContent();
            }
        }
        return null;
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public static class XspfPlaylist {
        private String name;
        private List<Song> songs;
        private File file;

        public XspfPlaylist(String name, List<Song> songs, File file) {
            this.name = name;
            this.songs = songs;
            this.file = file;
        }

        public String getName() { return name; }
        public List<Song> getSongs() { return songs; }
        public File getFile() { return file; }
        public int getSongCount() { return songs.size(); }

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
    }
}