package com.jsrr.android_app93;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PhotoGalleryActivity extends AppCompatActivity {

    private static final String TAG = "PhotoGalleryActivity";
    private static final int REQUEST_IMAGE_GALLERY = 101;

    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photoList;
    private String albumName;
    private Album currentAlbum;
    private FloatingActionButton addPhotoFab;

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

        // Initialize Add Photo FAB
        addPhotoFab = findViewById(R.id.add_photo_fab);
        addPhotoFab.setOnClickListener(v -> openGallery());

        // Check if the album has photos, if not initialize with defaults
        if (currentAlbum != null && currentAlbum.getPhotos().size() > 0) {
            photoList = new ArrayList<>(currentAlbum.getPhotos());
        } else {
            initializeDefaultPhotos();
        }

        // Set up the adapter
        photoAdapter = new PhotoAdapter(photoList);
        photoRecyclerView.setAdapter(photoAdapter);

        // Log that we've set up the recycler view
        Log.d(TAG, "RecyclerView initialized with " + photoList.size() + " photos");
    }

    /**
     * Open the gallery to select an image
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                showAddPhotoDialog(selectedImageUri);
            }
        }
    }

    /**
     * Show dialog to enter caption for the new photo
     */
    private void showAddPhotoDialog(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_photo, null);
        EditText captionEditText = dialogView.findViewById(R.id.caption_edit_text);
        ImageView previewImageView = dialogView.findViewById(R.id.preview_image_view);

        // Load a preview of the selected image
        try {
            Bitmap previewBitmap = getBitmapFromUri(imageUri);
            if (previewBitmap != null) {
                previewImageView.setImageBitmap(previewBitmap);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading preview image: " + e.getMessage());
            previewImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        builder.setView(dialogView)
                .setTitle("Add New Photo")
                .setPositiveButton("Add", (dialog, which) -> {
                    String caption = captionEditText.getText().toString();
                    if (caption.isEmpty()) {
                        caption = "Photo " + (photoList.size() + 1);
                    }
                    savePhotoToAlbum(imageUri, caption);
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Get bitmap from Uri
     */
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * Save the selected photo to the album and device gallery
     */
// Modified savePhotoToAlbum method for PhotoGalleryActivity.java
// This version stores a reference to the original photo URI instead of making a copy

    /**
     * Save a reference to the selected photo to the album without duplicating it
     */
    private void savePhotoToAlbum(Uri imageUri, String caption) {
        try {
            Log.d(TAG, "Creating reference to photo URI: " + imageUri);

            // Take persistent permission for this URI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    // Request persistent read permission for the URI
                    getContentResolver().takePersistableUriPermission(
                            imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    Log.d(TAG, "Taken persistable URI permission for: " + imageUri);
                } catch (SecurityException se) {
                    Log.e(TAG, "Failed to take persistable URI permission: " + se.getMessage());
                    Toast.makeText(this, "Unable to access this photo permanently", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // Create a new Photo object with the original URI
            // Convert URI to string for storage
            String uriString = imageUri.toString();
            Photo newPhoto = new Photo(caption, uriString);
            Log.d(TAG, "Created new Photo with URI: " + uriString);

            // Add to current album
            if (currentAlbum != null) {
                currentAlbum.addPhoto(newPhoto);
                Log.d(TAG, "Added photo to album: " + currentAlbum.getName());
            } else {
                Log.e(TAG, "Current album is null, cannot add photo");
            }

            // Add to the display list
            photoList.add(newPhoto);
            photoAdapter.notifyItemInserted(photoList.size() - 1);

            // Save data
            Data.saveData(this);

            Toast.makeText(this, "Photo added successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error saving photo reference: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to add photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save the image to the MediaStore (public gallery) for Android 9 and below
     */
    private void saveToMediaStore(File imageFile, String caption) {
        try {
            // Insert the new image into the MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, caption);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
            values.put(MediaStore.Images.Media.DESCRIPTION, "Photo from " + getString(R.string.app_name));
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());

            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Log.d(TAG, "Image saved to MediaStore using legacy method");
        } catch (Exception e) {
            Log.e(TAG, "Error saving to media store: " + e.getMessage());
        }
    }

    /**
     * Save the image to the MediaStore (public gallery) for Android 10 and above using Scoped Storage
     */
    private void saveToMediaStoreQ(File imageFile, String caption) {
        try {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFile.getName());
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.TITLE, caption);
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Photo from " + getString(R.string.app_name));
            contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

            // For Android 10+, use the new RELATIVE_PATH field
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name)
                );
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            if (uri != null) {
                try (FileInputStream is = new FileInputStream(imageFile);
                     OutputStream os = resolver.openOutputStream(uri)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                // For Android 10+, clear the IS_PENDING flag
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear();
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(uri, contentValues, null, null);
                }

                Log.d(TAG, "Image saved to MediaStore using Scoped Storage");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving to MediaStore (Q): " + e.getMessage());
        }
    }

    /**
     * Shows a dialog to select the destination album for moving a photo
     */
    private void showMovePhotoDialog(int photoPosition) {
        // Get the list of all albums directly from Data to ensure freshness
        Set<Album> allAlbums = Data.getAlbums();
        Log.d(TAG, "Moving photo. Total albums: " + allAlbums.size());

        // Create a list of albums excluding the current one
        List<Album> availableAlbums = new ArrayList<>();
        for (Album album : allAlbums) {
            // Only add albums that aren't the current album
            if (!album.getName().equals(currentAlbum.getName())) {
                availableAlbums.add(album);
                Log.d(TAG, "Available destination album: " + album.getName() +
                        " with " + album.getPhotos().size() + " photos");
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
                        Photo photoToMove = photoList.get(photoPosition);
                        Album destinationAlbum = availableAlbums.get(selectedAlbumPosition);

                        Log.d(TAG, "Selected photo: " + photoToMove.getCaption());
                        Log.d(TAG, "Destination album: " + destinationAlbum.getName());

                        // Ensure we have the latest reference to the destination album
                        Album freshDestinationAlbum = Data.getAlbum(destinationAlbum.getName());
                        if (freshDestinationAlbum != null) {
                            destinationAlbum = freshDestinationAlbum;
                            Log.d(TAG, "Using fresh destination album reference");
                        }

                        // Check if the destination album already contains the photo
                        boolean isDuplicate = false;
                        for (Photo existingPhoto : destinationAlbum.getPhotos()) {
                            if (existingPhoto.equals(photoToMove)) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (isDuplicate) {
                            // Alert the user that the photo already exists in the destination album
                            Toast.makeText(PhotoGalleryActivity.this, "Photo already exists in " +
                                            destinationAlbum.getName() + ". Move cancelled.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Move the photo between albums
                        movePhotoToAlbum(photoToMove, currentAlbum, destinationAlbum);

                        // Remove the photo from the current display list and notify adapter
                        photoList.remove(photoPosition);
                        photoAdapter.notifyItemRemoved(photoPosition);

                        // Reset expanded state
                        photoAdapter.expandedPosition = -1;

                        // Show confirmation
                        Toast.makeText(PhotoGalleryActivity.this, "Photo moved to " + destinationAlbum.getName(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PhotoGalleryActivity.this, "No album selected", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Shows a confirmation dialog for deleting a photo
     */
    private void showDeletePhotoDialog(int photoPosition) {
        Photo photoToDelete = photoList.get(photoPosition);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete the photo \"" + photoToDelete.getCaption() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the photo from the album and update the display
                    deletePhoto(photoPosition);
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Deletes a photo from the current album
     * @param photoPosition The position of the photo to delete
     */
    private void deletePhoto(int photoPosition) {
        if (photoPosition < 0 || photoPosition >= photoList.size()) {
            Log.e(TAG, "Invalid photo position: " + photoPosition);
            return;
        }

        Photo photoToDelete = photoList.get(photoPosition);

        // Remove photo from current album
        if (currentAlbum != null) {
            currentAlbum.removePhoto(photoToDelete);
            Log.d(TAG, "Removed photo " + photoToDelete.getCaption() + " from album " + currentAlbum.getName());
        }

        // Remove photo from the list and update adapter
        photoList.remove(photoPosition);
        photoAdapter.notifyItemRemoved(photoPosition);

        // Reset expanded position if necessary
        if (photoAdapter.expandedPosition == photoPosition) {
            photoAdapter.expandedPosition = -1;
        } else if (photoAdapter.expandedPosition > photoPosition) {
            // Adjust expanded position if it was after the deleted item
            photoAdapter.expandedPosition--;
        }

        // Save changes
        Data.saveData(this);

        // Show confirmation
        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
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

        // Log the operation for debugging
        Log.d(TAG, "Moving photo: " + photo.getCaption());
        Log.d(TAG, "From album: " + sourceAlbum.getName() + " (now has " +
                sourceAlbum.getPhotos().size() + " photos)");
        Log.d(TAG, "To album: " + destinationAlbum.getName() + " (now has " +
                destinationAlbum.getPhotos().size() + " photos)");

        // Verify the move operation
        boolean sourceContains = sourceAlbum.getPhotos().contains(photo);
        boolean destContains = destinationAlbum.getPhotos().contains(photo);
        Log.d(TAG, "Source still contains photo: " + sourceContains);
        Log.d(TAG, "Destination contains photo: " + destContains);

        // Make sure changes are reflected in Data's albums collection
        Set<Album> allAlbums = Data.getAlbums();
        for (Album album : allAlbums) {
            if (album.getName().equals(sourceAlbum.getName())) {
                // Make sure the source album in Data's collection is updated
                if (album != sourceAlbum) {
                    album.removePhoto(photo);
                    Log.d(TAG, "Updated source album in Data's collection");
                }
            } else if (album.getName().equals(destinationAlbum.getName())) {
                // Make sure the destination album in Data's collection is updated
                if (album != destinationAlbum) {
                    album.addPhoto(photo);
                    Log.d(TAG, "Updated destination album in Data's collection");
                }
            }
        }

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
        private int expandedPosition = -1; // Track which item has expanded menu

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

            // Handle expanded state (show/hide action buttons)
            boolean isExpanded = position == expandedPosition;
            holder.actionButtonsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Set click listener to show/hide the action buttons
            holder.itemView.setOnClickListener(v -> {
                // Set the current photo in Data
                Data.setCurrentPhoto(photo);

                // Update expandedPosition
                int oldExpandedPosition = expandedPosition;
                expandedPosition = isExpanded ? -1 : position;

                // Notify item changes to refresh the view
                if (oldExpandedPosition >= 0) {
                    notifyItemChanged(oldExpandedPosition);
                }
                notifyItemChanged(position);
            });

            // Set up action buttons
            holder.displayButton.setOnClickListener(v -> {
                // Launch PhotoDetailActivity
                Intent intent = new Intent(PhotoGalleryActivity.this, PhotoDetailActivity.class);
                intent.putExtra("PHOTO_LIST", (Serializable) photos);
                intent.putExtra("PHOTO_INDEX", position);
                startActivity(intent);
            });

            holder.moveButton.setOnClickListener(v -> {
                // Show the move dialog
                showMovePhotoDialog(position);
            });

            holder.deleteButton.setOnClickListener(v -> {
                // Show the delete confirmation dialog
                showDeletePhotoDialog(position);
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
            LinearLayout actionButtonsLayout;
            Button displayButton;
            Button moveButton;
            Button deleteButton;

            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                photoImageView = itemView.findViewById(R.id.photo_image);
                photoNameTextView = itemView.findViewById(R.id.photo_name);
                actionButtonsLayout = itemView.findViewById(R.id.action_buttons_layout);
                displayButton = itemView.findViewById(R.id.display_button);
                moveButton = itemView.findViewById(R.id.move_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }
}