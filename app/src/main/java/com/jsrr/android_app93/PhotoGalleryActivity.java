package com.jsrr.android_app93;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryActivity extends AppCompatActivity {

    private static final String TAG = "PhotoGalleryActivity";
    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photoList;
    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        // Get album name from intent
        albumName = getIntent().getStringExtra("ALBUM_NAME");

        // Set title to album name
        if (albumName != null && !albumName.isEmpty()) {
            setTitle(albumName);
        }

        // Enable the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize RecyclerView
        photoRecyclerView = findViewById(R.id.photo_recycler_view);
        photoRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        // Initialize photo list with default data
        initializeDefaultPhotos();

        // Set up the adapter
        photoAdapter = new PhotoAdapter(photoList);
        photoRecyclerView.setAdapter(photoAdapter);

        // Log that we've set up the recycler view
        Log.d(TAG, "RecyclerView initialized with " + photoList.size() + " photos");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeDefaultPhotos() {
        photoList = new ArrayList<>();

        // Add default photos with proper drawable paths
        photoList.add(new Photo("Pac-Man", "/drawable/pacmanstock"));
        photoList.add(new Photo("Blinky", "/drawable/blinkystock"));
        photoList.add(new Photo("Pinky", "/drawable/pinkystock"));
        photoList.add(new Photo("Inky", "/drawable/inkystock"));
        photoList.add(new Photo("Clyde", "/drawable/clydestock"));

        // Add some sample tags to demonstrate tag functionality
        Photo pacman = photoList.get(0);
        pacman.addTag(new Tag("Person", "Pac-Man"));
        pacman.addTag(new Tag("Location", "Maze"));

        Photo blinky = photoList.get(1);
        blinky.addTag(new Tag("Person", "Ghost"));
        blinky.addTag(new Tag("Location", "Maze"));

        // Verify the photos were added
        Log.d(TAG, "Added " + photoList.size() + " default photos");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up bitmap resources
        if (photoList != null) {
            for (Photo photo : photoList) {
                photo.recycleImages();
            }
        }
    }

    // RecyclerView Adapter for Photos
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

        private List<Photo> photos;

        public PhotoAdapter(List<Photo> photos) {
            this.photos = photos;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_item, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            Photo photo = photos.get(position);
            holder.photoNameTextView.setText(photo.getCaption());

            // Use the thumbnail and handle potential null
            try {
                holder.photoImageView.setImageBitmap(photo.getThumbnail(holder.itemView.getContext()));
                if (holder.photoImageView.getDrawable() == null) {
                    Log.e(TAG, "Failed to load thumbnail for " + photo.getCaption());
                    // Set a placeholder or default image
                    holder.photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error displaying photo: " + e.getMessage());
                holder.photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Launch PhotoDetailActivity when a photo is clicked
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PhotoGalleryActivity.this, PhotoDetailActivity.class);
                intent.putExtra("PHOTO_LIST", (Serializable) photos);
                intent.putExtra("PHOTO_INDEX", position);
                startActivity(intent);
            });
        }

        @Override
        public void onViewRecycled(@NonNull PhotoViewHolder holder) {
            super.onViewRecycled(holder);
            // Clear the ImageView to help with recycling
            holder.photoImageView.setImageBitmap(null);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView photoImageView;
            TextView photoNameTextView;

            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                photoImageView = itemView.findViewById(R.id.photo_image);
                photoNameTextView = itemView.findViewById(R.id.photo_name);
            }
        }
    }
}