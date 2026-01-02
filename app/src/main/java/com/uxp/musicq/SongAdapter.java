package com.uxp.musicq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs != null ? songs : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song, position);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs != null ? newSongs : new ArrayList<>();
        notifyDataSetChanged();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTitle, txtArtist, txtDuration;
        private ImageView imgAlbumArt;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtSongTitle);
            txtArtist = itemView.findViewById(R.id.txtSongArtist);
            txtDuration = itemView.findViewById(R.id.txtSongDuration);
            imgAlbumArt = itemView.findViewById(R.id.imgSongAlbumArt);
        }

        public void bind(Song song, int position) {
            txtTitle.setText(song.getTitle());
            txtArtist.setText(song.getArtist());
            txtDuration.setText(song.getFormattedDuration());
            AlbumArtLoader.loadAlbumArt(itemView.getContext(), song.getPath(), imgAlbumArt);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongClick(song, position);
                }
            });
        }
    }
}