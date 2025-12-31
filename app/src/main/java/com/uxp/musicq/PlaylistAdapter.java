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

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private List<Playlist> playlists;
    private OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist, int position);
        void onPlaylistLongClick(Playlist playlist, int position);
    }

    public PlaylistAdapter(List<Playlist> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists != null ? playlists : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.bind(playlist, position);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void updatePlaylists(List<Playlist> newPlaylists) {
        this.playlists = newPlaylists != null ? newPlaylists : new ArrayList<>();
        notifyDataSetChanged();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPlaylistName, txtSongCount, txtDuration;
        private ImageView imgPlaylist;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlaylistName = itemView.findViewById(R.id.txtPlaylistName);
            txtSongCount = itemView.findViewById(R.id.txtPlaylistSongCount);
            txtDuration = itemView.findViewById(R.id.txtPlaylistDuration);
            imgPlaylist = itemView.findViewById(R.id.imgPlaylist);
        }

        public void bind(Playlist playlist, int position) {
            txtPlaylistName.setText(playlist.getName());
            txtSongCount.setText(playlist.getSongCount() + " songs");
            txtDuration.setText(playlist.getFormattedTotalDuration());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaylistClick(playlist, position);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onPlaylistLongClick(playlist, position);
                }
                return true;
            });
        }
    }
}