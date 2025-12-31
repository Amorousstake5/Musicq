package com.uxp.musicq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SongSelectionAdapter extends RecyclerView.Adapter<SongSelectionAdapter.ViewHolder> {
    private List<Song> songs;
    private OnSongSelectListener listener;

    public interface OnSongSelectListener {
        void onSongSelect(Song song, boolean isSelected);
    }

    public SongSelectionAdapter(List<Song> songs, OnSongSelectListener listener) {
        this.songs = songs != null ? songs : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(songs.get(position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtArtist;
        ImageView imgAlbumArt;
        CheckBox checkbox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtSongTitle);
            txtArtist = itemView.findViewById(R.id.txtSongArtist);
            imgAlbumArt = itemView.findViewById(R.id.imgSongAlbumArt);
            checkbox = itemView.findViewById(R.id.checkbox);
        }

        void bind(Song song) {
            txtTitle.setText(song.getTitle());
            txtArtist.setText(song.getArtist());
            AlbumArtLoader.loadAlbumArt(itemView.getContext(), song.getAlbumId(), imgAlbumArt);

            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(false);

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSongSelect(song, isChecked);
                }
            });

            itemView.setOnClickListener(v -> checkbox.toggle());
        }
    }
}