package com.jsrr.android_app93;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private Button createAlbumButton;
    private Button deleteAlbumButton;
    private Button renameAlbumButton;
    private Button searchButton; // New search button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        albumRecyclerView = findViewById(R.id.album_recycler_view);
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize buttons
        createAlbumButton = findViewById(R.id.create_album_button);
        deleteAlbumButton = findViewById(R.id.delete_album_button);
        renameAlbumButton = findViewById(R.id.rename_album_button);
        searchButton = findViewById(R.id.search_button); // Initialize search button

        // Set up button click listeners
        createAlbumButton.setOnClickListener(v -> showCreateAlbumDialog());
        deleteAlbumButton.setOnClickListener(v -> showDeleteAlbumDialog());
        renameAlbumButton.setOnClickListener(v -> showRenameAlbumDialog());
        searchButton.setOnClickListener(v -> openSearchActivity()); // Add click listener for search button

        // Load data or initialize
        Data.loadData(this);
        // Convert Set to List for the adapter
        refreshAlbumList();
    }

    // Method to open the search activity
    private void openSearchActivity() {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    private void refreshAlbumList() {
        albumList = new ArrayList<>(Data.getAlbums());
        // Set up the adapter
        albumAdapter = new AlbumAdapter(albumList);
        albumRecyclerView.setAdapter(albumAdapter);
    }

    private void showCreateAlbumDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_album, null);
        EditText albumNameEditText = dialogView.findViewById(R.id.album_name_edit_text);

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create New Album")
                .setView(dialogView)
                .setPositiveButton("Create", (dialogInterface, i) -> {
                    // Get the entered album name
                    String albumName = albumNameEditText.getText().toString().trim();

                    // Validate input
                    if (albumName.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if an album with this name already exists
                    if (Data.getAlbum(albumName) != null) {
                        Toast.makeText(this, "An album with this name already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create and add the new album
                    Album newAlbum = new Album(albumName);
                    Data.addAlbum(newAlbum);

                    // Update the display and save data
                    refreshAlbumList();
                    Data.saveData(this);
                    Toast.makeText(this, "Album created", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void showDeleteAlbumDialog() {
        // Check if there are any albums to delete
        if (albumList.isEmpty()) {
            Toast.makeText(this, "No albums to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an array of album names for display
        final String[] albumNames = new String[albumList.size()];
        for (int i = 0; i < albumList.size(); i++) {
            albumNames[i] = albumList.get(i).getName();
        }

        // Create and show the album selection dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Album to Delete")
                .setSingleChoiceItems(albumNames, -1, null)
                .setPositiveButton("Next", (dialog, which) -> {
                    // Get the selected position from the ListView
                    ListView listView = ((AlertDialog) dialog).getListView();
                    int selectedPosition = listView.getCheckedItemPosition();

                    // Check if an album was selected
                    if (selectedPosition != AdapterView.INVALID_POSITION) {
                        // Show confirmation dialog for the selected album
                        showDeleteConfirmationDialog(albumList.get(selectedPosition));
                    } else {
                        Toast.makeText(this, "No album selected", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showDeleteConfirmationDialog(Album albumToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete the album '" + albumToDelete.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Remove the album
                    Data.removeAlbum(albumToDelete);

                    // Update the display and save data
                    refreshAlbumList();
                    Data.saveData(this);
                    Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    private void showRenameAlbumDialog() {
        // Check if there are any albums to rename
        if (albumList.isEmpty()) {
            Toast.makeText(this, "No albums to rename", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an array of album names for display
        final String[] albumNames = new String[albumList.size()];
        for (int i = 0; i < albumList.size(); i++) {
            albumNames[i] = albumList.get(i).getName();
        }

        // Create and show the album selection dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Album to Rename")
                .setSingleChoiceItems(albumNames, -1, null)
                .setPositiveButton("Next", (dialog, which) -> {
                    // Get the selected position from the ListView
                    ListView listView = ((AlertDialog) dialog).getListView();
                    int selectedPosition = listView.getCheckedItemPosition();

                    // Check if an album was selected
                    if (selectedPosition != AdapterView.INVALID_POSITION) {
                        // Show rename dialog for the selected album
                        showRenameInputDialog(albumList.get(selectedPosition));
                    } else {
                        Toast.makeText(this, "No album selected", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showRenameInputDialog(Album albumToRename) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rename_album, null);
        EditText newNameEditText = dialogView.findViewById(R.id.new_album_name_edit_text);

        // Pre-fill with current name
        newNameEditText.setText(albumToRename.getName());
        newNameEditText.setSelection(newNameEditText.getText().length());

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(dialogView)
                .setPositiveButton("Rename", (dialogInterface, i) -> {
                    // Get the entered new name
                    String newName = newNameEditText.getText().toString().trim();

                    // Validate input
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if the name is unchanged
                    if (newName.equals(albumToRename.getName())) {
                        return;
                    }

                    // Check if an album with this name already exists
                    if (Data.getAlbum(newName) != null) {
                        Toast.makeText(this, "An album with this name already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Rename the album
                    albumToRename.setName(newName);

                    // Update the display and save data
                    refreshAlbumList();
                    Data.saveData(this);
                    Toast.makeText(this, "Album renamed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh album list when returning to MainActivity
        refreshAlbumList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save data when app is paused
        Data.saveData(this);
    }

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
                // Set the current album in Data before opening the gallery
                Data.setCurrentAlbum(album);

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