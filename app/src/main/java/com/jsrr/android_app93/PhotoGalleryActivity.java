package com.jsrr.android_app93;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PhotoGalleryActivity extends AppCompatActivity {

    private static final String TAG = "PhotoGalleryActivity";
    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photoList;
    private String albumName;
    private Album currentAlbum;
    private Button movePhotoButton; // New button
    private int selectedPhotoPosition = -1; // To track selected photo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        // Get album name from intent
        albumName = getIntent().getStringExtra("ALBUM_NAME");

        // Get the current album from Data
        currentAlbum = Data.getCurrentAlbum();

        // If no album is set in Data, try to find it by name
        if (currentAlbum == null && albumName != null) {
            currentAlbum = Data.getAlbum(albumName);
        }

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

        // Check if the album has photos, if not initialize with defaults
        if (currentAlbum != null && currentAlbum.getPhotos().size() > 0) {
            photoList = new ArrayList<>(currentAlbum.getPhotos());
        } else {
            initializeDefaultPhotos();
        }

        // Set up the adapter
        photoAdapter = new PhotoAdapter(photoList);
        photoRecyclerView.setAdapter(photoAdapter);

        // Initialize and set up move photo button
        movePhotoButton = findViewById(R.id.move_photo_button);
        movePhotoButton.setOnClickListener(v -> {
            if (selectedPhotoPosition == -1) {
                Toast.makeText(this, "Please select a photo first", Toast.LENGTH_SHORT).show();
                return;
            }
            showMovePhotoDialog();
        });

        // Log that we've set up the recycler view
        Log.d(TAG, "RecyclerView initialized with " + photoList.size() + " photos");
    }

    /**
     * Shows a dialog to select the destination album for moving a photo
     */
    private void showMovePhotoDialog() {
        // Get the list of all albums
        Set<Album> allAlbums = Data.getAlbums();

        // Create a list of albums excluding the current one
        List<Album> availableAlbums = new ArrayList<>();
        for (Album album : allAlbums) {
            // Only add albums that aren't the current album
            if (!album.equals(currentAlbum)) {
                availableAlbums.add(album);
            }
        }

        // Check if there are other albums to move to
        if (availableAlbums.isEmpty()) {
            Toast.makeText(this, "No other albums available to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an array of album names for display
        final String[] albumNames = new String[availableAlbums.size()];
        for (int i = 0; i < availableAlbums.size(); i++) {
            albumNames[i] = availableAlbums.get(i).getName();
        }

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move Photo to Album")
                .setSingleChoiceItems(albumNames, -1, null)
                .setPositiveButton("Move", (dialog, which) -> {
                    // Get the selected position from the ListView
                    ListView listView = ((AlertDialog) dialog).getListView();
                    int selectedAlbumPosition = listView.getCheckedItemPosition();

                    // Check if an album was selected
                    if (selectedAlbumPosition != AdapterView.INVALID_POSITION) {
                        // Get the selected photo and destination album
                        Photo photoToMove = photoList.get(selectedPhotoPosition);
                        Album destinationAlbum = availableAlbums.get(selectedAlbumPosition);

                        // Move the photo between albums
                        movePhotoToAlbum(photoToMove, currentAlbum, destinationAlbum);

                        // Remove the photo from the current display list and notify adapter
                        photoList.remove(selectedPhotoPosition);
                        photoAdapter.notifyItemRemoved(selectedPhotoPosition);

                        // Reset selection
                        selectedPhotoPosition = -1;

                        // Show confirmation
                        Toast.makeText(this, "Photo moved to " + destinationAlbum.getName(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No album selected", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Moves a photo from one album to another
     * @param photo The photo to move
     * @param sourceAlbum The source album to remove from
     * @param destinationAlbum The destination album to add to
     */
    private void movePhotoToAlbum(Photo photo, Album sourceAlbum, Album destinationAlbum) {
        // Remove from source album
        sourceAlbum.removePhoto(photo);

        // Add to destination album
        destinationAlbum.addPhoto(photo);

        // Save the changes
        Data.saveData(this);
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
        photoList.add(new Photo("Pac-Man", String.valueOf(R.drawable.pacmanstock)));
        photoList.add(new Photo("Blinky", String.valueOf(R.drawable.blinkystock)));
        photoList.add(new Photo("Pinky", String.valueOf(R.drawable.pinkystock)));
        photoList.add(new Photo("Inky", String.valueOf(R.drawable.inkystock)));
        photoList.add(new Photo("Clyde", String.valueOf(R.drawable.clydestock)));

        // Add some sample tags to demonstrate tag functionality
        Photo pacman = photoList.get(0);
        pacman.addTag(new Tag("Person", "Pac-Man"));
        pacman.addTag(new Tag("Location", "Maze"));

        Photo blinky = photoList.get(1);
        blinky.addTag(new Tag("Person", "Ghost"));
        blinky.addTag(new Tag("Location", "Maze"));

        // If we have a valid album, add the photos to it
        if (currentAlbum != null) {
            for (Photo photo : photoList) {
                currentAlbum.addPhoto(photo);
            }
        }

        // Verify the photos were added
        Log.d(TAG, "Added " + photoList.size() + " default photos");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save data when activity is paused
        Data.saveData(this);
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

            // Set background color based on selection state
            if (position == selectedPhotoPosition) {
                holder.itemView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                holder.itemView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            // Handle long click for selection for move functionality
            holder.itemView.setOnLongClickListener(v -> {
                selectedPhotoPosition = position;
                photoAdapter.notifyDataSetChanged(); // Update all items to refresh the selected state
                Toast.makeText(PhotoGalleryActivity.this,
                        "Selected " + photo.getCaption() + " for move",
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            // Launch PhotoDetailActivity when a photo is clicked normally
            holder.itemView.setOnClickListener(v -> {
                // Set the current photo in Data before opening details
                Data.setCurrentPhoto(photo);

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