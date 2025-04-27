package com.jsrr.android_app93;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhotoDetailActivity extends AppCompatActivity {
    private static final String TAG = "PhotoDetailActivity";

    private ImageView photoImageView;
    private TextView captionTextView;
    private RecyclerView tagRecyclerView;
    private TagAdapter tagAdapter;
    private Button addTagButton;
    private Button removeTagButton;
    private Button prevPhotoButton;
    private Button nextPhotoButton;

    private List<Photo> photoList;
    private int currentPhotoIndex;
    private Photo currentPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        photoImageView = findViewById(R.id.detail_photo_image);
        captionTextView = findViewById(R.id.detail_photo_caption);
        tagRecyclerView = findViewById(R.id.tag_recycler_view);
        addTagButton = findViewById(R.id.add_tag_button);
        removeTagButton = findViewById(R.id.remove_tag_button);
        prevPhotoButton = findViewById(R.id.prev_photo_button);
        nextPhotoButton = findViewById(R.id.next_photo_button);

        // Set up the tag RecyclerView
        tagRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get data from intent
        photoList = (ArrayList<Photo>) getIntent().getSerializableExtra("PHOTO_LIST");
        currentPhotoIndex = getIntent().getIntExtra("PHOTO_INDEX", 0);

        // Try to get the current photo from Data if intent doesn't have valid data
        if (photoList == null || photoList.isEmpty()) {
            currentPhoto = Data.getCurrentPhoto();
            if (currentPhoto == null) {
                Log.e(TAG, "No photos provided to PhotoDetailActivity");
                Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // If we have a valid album, get its photos
            Album currentAlbum = Data.getCurrentAlbum();
            if (currentAlbum != null) {
                photoList = new ArrayList<>(currentAlbum.getPhotos());
                currentPhotoIndex = photoList.indexOf(currentPhoto);
                if (currentPhotoIndex < 0) currentPhotoIndex = 0;
            } else {
                // Create a single-photo list if we have no album
                photoList = new ArrayList<>();
                photoList.add(currentPhoto);
                currentPhotoIndex = 0;
            }
        }

        // Display the current photo
        displayCurrentPhoto();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void displayCurrentPhoto() {
        Photo currentPhoto = photoList.get(currentPhotoIndex);

        // Update the current photo in Data
        Data.setCurrentPhoto(currentPhoto);

        // Set photo caption
        captionTextView.setText(currentPhoto.getCaption());

        // Load and display the full image
        photoImageView.setImageBitmap(currentPhoto.getFullImage(this));

        // Update the title to show position in album
        setTitle(String.format("Photo %d of %d", currentPhotoIndex + 1, photoList.size()));

        // Set up the tags adapter
        updateTagsList();

        // Update navigation button states
        updateNavigationButtons();
    }

    private void updateTagsList() {
        Photo currentPhoto = photoList.get(currentPhotoIndex);
        List<Tag> tagList = new ArrayList<>(currentPhoto.getTags());
        tagAdapter = new TagAdapter(tagList);
        tagRecyclerView.setAdapter(tagAdapter);
    }

    private void updateNavigationButtons() {
        prevPhotoButton.setEnabled(currentPhotoIndex > 0);
        nextPhotoButton.setEnabled(currentPhotoIndex < photoList.size() - 1);
    }

    private void setupButtonListeners() {
        addTagButton.setOnClickListener(v -> showAddTagDialog());

        removeTagButton.setOnClickListener(v -> showRemoveTagDialog());

        prevPhotoButton.setOnClickListener(v -> {
            if (currentPhotoIndex > 0) {
                currentPhotoIndex--;
                displayCurrentPhoto();
            }
        });

        nextPhotoButton.setOnClickListener(v -> {
            if (currentPhotoIndex < photoList.size() - 1) {
                currentPhotoIndex++;
                displayCurrentPhoto();
            }
        });
    }

    private void showAddTagDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);

        // Get references to dialog components
        Spinner tagTypeSpinner = dialogView.findViewById(R.id.tag_type_spinner);
        EditText tagValueEditText = dialogView.findViewById(R.id.tag_value_edit_text);

        // Set up the spinner with tag types
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Tag.tagTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagTypeSpinner.setAdapter(adapter);

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Tag")
                .setView(dialogView)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    // Get the selected tag type and entered value
                    String tagType = tagTypeSpinner.getSelectedItem().toString();
                    String tagValue = tagValueEditText.getText().toString().trim();

                    // Validate input
                    if (tagValue.isEmpty()) {
                        Toast.makeText(this, "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create and add the new tag
                    Tag newTag = new Tag(tagType, tagValue);
                    Photo currentPhoto = photoList.get(currentPhotoIndex);
                    currentPhoto.addTag(newTag);

                    // Make sure the same photo in the album is updated
                    Album currentAlbum = Data.getCurrentAlbum();
                    if (currentAlbum != null) {
                        // Find the photo in the album and update it if necessary
                        for (Photo albumPhoto : currentAlbum.getPhotos()) {
                            if (albumPhoto.equals(currentPhoto)) {
                                albumPhoto.addTag(newTag);
                                break;
                            }
                        }
                    }

                    // Update the display and save data
                    updateTagsList();
                    Data.saveData(this);
                    Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void showRemoveTagDialog() {
        Photo currentPhoto = photoList.get(currentPhotoIndex);
        List<Tag> tagList = new ArrayList<>(currentPhoto.getTags());

        // Check if there are any tags to remove
        if (tagList.isEmpty()) {
            Toast.makeText(this, "No tags to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an array of tag strings for display
        final String[] tagStrings = new String[tagList.size()];
        for (int i = 0; i < tagList.size(); i++) {
            tagStrings[i] = tagList.get(i).toString();
        }

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Tag to Remove")
                .setSingleChoiceItems(tagStrings, -1, null)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Get the selected position from the ListView
                    ListView listView = ((AlertDialog) dialog).getListView();
                    int selectedPosition = listView.getCheckedItemPosition();

                    // Check if a tag was selected
                    if (selectedPosition != AdapterView.INVALID_POSITION) {
                        // Remove the selected tag
                        Tag tagToRemove = tagList.get(selectedPosition);
                        currentPhoto.removeTag(tagToRemove);

                        // Make sure the same photo in the album is updated
                        Album currentAlbum = Data.getCurrentAlbum();
                        if (currentAlbum != null) {
                            // Find the photo in the album and update it if necessary
                            for (Photo albumPhoto : currentAlbum.getPhotos()) {
                                if (albumPhoto.equals(currentPhoto)) {
                                    albumPhoto.removeTag(tagToRemove);
                                    break;
                                }
                            }
                        }

                        // Update the display and save data
                        updateTagsList();
                        Data.saveData(this);
                        Toast.makeText(this, "Tag removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No tag selected", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save data when activity is paused
        Data.saveData(this);
    }

    // Adapter for the Tags RecyclerView
    private class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
        private List<Tag> tags;

        public TagAdapter(List<Tag> tags) {
            this.tags = tags;
        }

        @Override
        public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new TagViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TagViewHolder holder, int position) {
            Tag tag = tags.get(position);
            holder.tagTextView.setText(tag.toString());
        }

        @Override
        public int getItemCount() {
            return tags.size();
        }

        class TagViewHolder extends RecyclerView.ViewHolder {
            TextView tagTextView;

            public TagViewHolder(View itemView) {
                super(itemView);
                tagTextView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}