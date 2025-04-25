package com.jsrr.android_app93;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView albumRecyclerView;
    private AlbumAdapter albumAdapter;
    private List<Album> albumList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        albumRecyclerView = findViewById(R.id.album_recycler_view);
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize album list with default data
        initializeDefaultAlbums();

        // Set up the adapter
        albumAdapter = new AlbumAdapter(albumList);
        albumRecyclerView.setAdapter(albumAdapter);
    }

    private void initializeDefaultAlbums() {
        // Create default album list
        albumList = new ArrayList<>(Arrays.asList(
                new Album("The Dark Side of the Moon"),
                new Album("Abbey Road"),
                new Album("Thriller"),
                new Album("Rumours"),
                new Album("Back in Black"),
                new Album("Nevermind"),
                new Album("OK Computer"),
                new Album("The Joshua Tree"),
                new Album("Purple Rain"),
                new Album("Random Access Memories")
        ));
    }

    // Album data class
//    public static class Album {
//        private String name;
//
//        public Album(String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//    }

    // RecyclerView Adapter for Albums
    private class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

        private List<Album> albums;

        public AlbumAdapter(List<Album> albums) {
            this.albums = albums;
        }

        @NonNull
        @Override
        public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.album_item, parent, false);
            return new AlbumViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
            Album album = albums.get(position);
            holder.albumNameTextView.setText(album.getName());

            // Set click listener to open the gallery
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, PhotoGalleryActivity.class);
                intent.putExtra("ALBUM_NAME", album.getName());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return albums.size();
        }

        class AlbumViewHolder extends RecyclerView.ViewHolder {
            TextView albumNameTextView;

            public AlbumViewHolder(@NonNull View itemView) {
                super(itemView);
                albumNameTextView = itemView.findViewById(R.id.album_name);
            }
        }
    }
}