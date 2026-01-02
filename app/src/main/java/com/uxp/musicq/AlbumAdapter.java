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

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private List<Album> albums;
    private OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album, int position);
    }

    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums != null ? albums : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.bind(album, position);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums = newAlbums != null ? newAlbums : new ArrayList<>();
        notifyDataSetChanged();
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private TextView txtAlbumName, txtArtist, txtSongCount;
        private ImageView imgAlbumArt;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAlbumName = itemView.findViewById(R.id.txtAlbumName);
            txtArtist = itemView.findViewById(R.id.txtAlbumArtist);
            txtSongCount = itemView.findViewById(R.id.txtSongCount);
            imgAlbumArt = itemView.findViewById(R.id.imgAlbumArt);
        }

        public void bind(Album album, int position) {
            txtAlbumName.setText(album.getName());
            txtArtist.setText(album.getArtist());
            txtSongCount.setText(album.getSongCount() + " songs");

            // Load album art from first song in album
            if (album.getFirstSongPath() != null) {
                AlbumArtLoader.loadAlbumArt(itemView.getContext(), album.getFirstSongPath(), imgAlbumArt);
            } else {
                imgAlbumArt.setImageResource(R.drawable.default_album_art);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlbumClick(album, position);
                }
            });
        }
    }
}