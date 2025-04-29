package com.jsrr.android_app93;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView resultsRecyclerView;
    private SearchResultsAdapter searchResultsAdapter;
    private List<Photo> searchResults;
    private Spinner tagTypeSpinner;
    private String selectedTagType = "Location"; // Default selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set up the action bar with back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Search Photos by Tag");
        }

        // Initialize UI components
        searchEditText = findViewById(R.id.search_edit_text);
        resultsRecyclerView = findViewById(R.id.search_results_recycler_view);
        tagTypeSpinner = findViewById(R.id.tag_type_spinner);

        // Set up tag type spinner
        setupTagTypeSpinner();

        // Set up RecyclerView
        resultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        searchResults = new ArrayList<>();
        searchResultsAdapter = new SearchResultsAdapter(searchResults);
        resultsRecyclerView.setAdapter(searchResultsAdapter);

        // Set up search text listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchPhotos(s.toString());
            }
        });
    }

    private void setupTagTypeSpinner() {
        // Create an ArrayAdapter using a simple spinner layout and the tag types
        ArrayAdapter<String> tagTypesAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item,
                new String[]{"Location", "Person"});

        // Specify the layout to use when the list of choices appears
        tagTypesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Apply the adapter to the spinner
        tagTypeSpinner.setAdapter(tagTypesAdapter);

        // Set up listener for when an item is selected
        tagTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTagType = parent.getItemAtPosition(position).toString();
                // Re-run the search with current filter
                searchPhotos(searchEditText.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void searchPhotos(String query) {
        if (query.isEmpty()) {
            // If the query is empty, show all tags of the selected type
            query = "";
        }

        // Convert query to lowercase for case-insensitive search
        String lowercaseQuery = query.toLowerCase().trim();

        // Clear previous results
        searchResults.clear();

        // Get all albums
        Set<Album> albums = Data.getAlbums();

        // Search through all albums for photos with matching tags
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                Set<Tag> tags = photo.getTags();

                // Check if any tag contains the search query and matches the selected tag type
                for (Tag tag : tags) {
                    if (matchesTagTypeAndQuery(tag, lowercaseQuery)) {
                        // Add photo to search results if it's not already there
                        if (!searchResults.contains(photo)) {
                            searchResults.add(photo);
                        }
                        break;  // Break once we've found a match
                    }
                }
            }
        }

        // Update the adapter
        searchResultsAdapter.notifyDataSetChanged();

        // Show or hide the "No results found" message
        TextView emptyResultsText = findViewById(R.id.empty_results_text);
        if (searchResults.isEmpty()) {
            emptyResultsText.setVisibility(View.VISIBLE);
            resultsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyResultsText.setVisibility(View.GONE);
            resultsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean matchesTagTypeAndQuery(Tag tag, String query) {
        // Match based on both tag type and query
        return tag.getName().equals(selectedTagType) &&
                (query.isEmpty() || tag.getValue().toLowerCase().contains(query));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // RecyclerView Adapter for Search Results
    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder> {

        private List<Photo> photos;

        public SearchResultsAdapter(List<Photo> photos) {
            this.photos = photos;
        }

        @NonNull
        @Override
        public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_item, parent, false);
            return new SearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
            Photo photo = photos.get(position);

            // Display the photo caption
            if (holder.photoNameTextView != null) {
                holder.photoNameTextView.setText(photo.getCaption());
            }

            // Display the photo thumbnail
            if (holder.photoImageView != null) {
                try {
                    holder.photoImageView.setImageBitmap(photo.getThumbnail(holder.itemView.getContext()));
                    if (holder.photoImageView.getDrawable() == null) {
                        Log.e("SearchActivity", "Failed to load thumbnail for " + photo.getCaption());
                        // Set a placeholder or default image
                        holder.photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } catch (Exception e) {
                    Log.e("SearchActivity", "Error displaying photo: " + e.getMessage());
                    holder.photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            // Set click listener to open the photo detail view
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(SearchActivity.this, PhotoDetailActivity.class);
                intent.putExtra("PHOTO_LIST", (Serializable) photos);
                intent.putExtra("POSITION", position);  // Also pass the position
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class SearchResultViewHolder extends RecyclerView.ViewHolder {
            TextView photoNameTextView;
            ImageView photoImageView;

            public SearchResultViewHolder(@NonNull View itemView) {
                super(itemView);
                photoNameTextView = itemView.findViewById(R.id.photo_name);
                photoImageView = itemView.findViewById(R.id.photo_image);  // Make sure this ID exists in your photo_item.xml
            }
        }
    }
    //i was wrong
}