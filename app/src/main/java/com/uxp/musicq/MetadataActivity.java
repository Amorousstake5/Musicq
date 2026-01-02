package com.uxp.musicq;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;

public class MetadataActivity extends AppCompatActivity {
    private TextView txtTitle, txtArtist, txtAlbum, txtYear, txtGenre, txtDuration,
            txtBitrate, txtSampleRate, txtFileSize, txtFilePath, txtFormat;
    private ImageView imgAlbumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadMetadata();
    }

    private void initViews() {
        imgAlbumArt = findViewById(R.id.imgAlbumArt);
        txtTitle = findViewById(R.id.txtTitle);
        txtArtist = findViewById(R.id.txtArtist);
        txtAlbum = findViewById(R.id.txtAlbum);
        txtYear = findViewById(R.id.txtYear);
        txtGenre = findViewById(R.id.txtGenre);
        txtDuration = findViewById(R.id.txtDuration);
        txtBitrate = findViewById(R.id.txtBitrate);
        txtSampleRate = findViewById(R.id.txtSampleRate);
        txtFileSize = findViewById(R.id.txtFileSize);
        txtFilePath = findViewById(R.id.txtFilePath);
        txtFormat = findViewById(R.id.txtFormat);
    }

    private void loadMetadata() {
        String filePath = getIntent().getStringExtra("file_path");
        long albumId = getIntent().getLongExtra("album_id", -1);

        if (filePath == null) return;

        AlbumArtLoader.loadAlbumArt(this, filePath, imgAlbumArt);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);

            txtTitle.setText(getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_TITLE, "Unknown"));
            txtArtist.setText(getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ARTIST, "Unknown"));
            txtAlbum.setText(getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ALBUM, "Unknown"));
            txtYear.setText(getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_YEAR, "Unknown"));
            txtGenre.setText(getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_GENRE, "Unknown"));

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (duration != null) {
                long ms = Long.parseLong(duration);
                txtDuration.setText(formatDuration(ms));
            }

            String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            if (bitrate != null) {
                txtBitrate.setText((Integer.parseInt(bitrate) / 1000) + " kbps");
            }

            String sampleRate = getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_SAMPLERATE, "Unknown");
            if (!sampleRate.equals("Unknown")) {
                txtSampleRate.setText((Integer.parseInt(sampleRate) / 1000) + " kHz");
            }

            File file = new File(filePath);
            txtFileSize.setText(formatFileSize(file.length()));
            txtFilePath.setText(filePath);
            txtFormat.setText(filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getMetadata(MediaMetadataRetriever retriever, int key, String defaultValue) {
        String value = retriever.extractMetadata(key);
        return value != null ? value : defaultValue;
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}