package com.jsrr.android_app93;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@code Photo} class represents a photo that contains a collection of tags.
 * It provides functionality to manage tags, retrieve photo details, and compare photos with each other.
 *
 * <p>This class implements {@link Serializable} to allow serialization of album objects.
 *
 * <p>Features of the {@code Photo} class include:
 * <ul>
 *   <li>Adding and removing tags</li>
 *   <li>Retrieving the total number of tags</li>
 *   <li>Managing the fields and setting the Date</li>
 *   <li>Creating thumbnails of specified size</li>
 * </ul>
 *
 * @author [Joseph Scarpulla and Roger Ramirez]
 * @version 2.0
 */
public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAG = "Photo";
    private static final int THUMBNAIL_SIZE = 200;
    private String caption;
    private String pathName;
    private transient Bitmap fullImage;
    private transient Bitmap thumbnailImage;
    private Set<Tag> tags = new HashSet<>();

    /**
     * Constructs a {@code Photo} with the specified caption and path name.
     *
     * @param caption  the caption of the photo
     * @param pathName the path name of the photo
     */
    public Photo(String caption, String pathName) {
        this.caption = caption;
        this.pathName = pathName;
    }

    /**
     * Retrieves the caption of the photo.
     *
     * @return the caption of the photo
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Retrieves the path name of the photo.
     *
     * @return the path name of the photo
     */
    public String getPathName() {
        return pathName;
    }

    /**
     * Loads and retrieves the full-sized image of the photo.
     *
     * @param context the Android context
     * @return the bitmap of the photo, or null if it cannot be loaded
     */
    public Bitmap getFullImage(Context context) {
        if (fullImage == null || fullImage.isRecycled()) {
            try {
                if (pathName.startsWith("/drawable/")) {
                    // Handle drawable resources
                    String resourceName = pathName.substring("/drawable/".length());
                    int resourceId = context.getResources().getIdentifier(
                            resourceName, "drawable", context.getPackageName());
                    if (resourceId != 0) {
                        fullImage = BitmapFactory.decodeResource(context.getResources(), resourceId);
                    } else {
                        Log.e(TAG, "Resource not found: " + resourceName);
                    }
                } else {
                    // Try to load from path as URI
                    Uri uri = Uri.parse(pathName);
                    fullImage = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                }
            } catch (FileNotFoundException | SecurityException | IllegalArgumentException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                // If we're using default resources, we might be using resource IDs
                try {
                    // Try to parse as resource ID
                    int resourceId = Integer.parseInt(pathName);
                    fullImage = BitmapFactory.decodeResource(context.getResources(), resourceId);
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, "Path is neither a valid URI nor a resource ID: " + nfe.getMessage());
                    return null;
                }
            }
        }
        return fullImage;
    }

    /**
     * Gets a thumbnail version of the image with specified dimensions.
     *
     * @param context the Android context needed to access resources
     * @return A Bitmap of the thumbnail, or null if it cannot be created
     */
    public Bitmap getThumbnail(Context context) {
        if (thumbnailImage == null || thumbnailImage.isRecycled()) {
            Bitmap original = getFullImage(context);
            if (original != null) {
                thumbnailImage = Bitmap.createScaledBitmap(original, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true);
            }
        }
        else {System.out.println("Thumbnail already exists");}
        return thumbnailImage;
    }

    /**
     * Retrieves the set of tags associated with the photo.
     *
     * @return the set of tags associated with the photo
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Retrieves the set of tags associated with the photo as a string.
     *
     * @return the tags associated with the photo as a string
     */
    public Set<String> getTagsAsString() {
        Set<String> tagStrings = new HashSet<>();
        for (Tag tag : tags) {
            tagStrings.add(tag.toString());
        }
        return tagStrings;
    }

    /**
     * Retrieves the date of the photo.
     *
     * @return the date of the photo
     */

    /**
     * Sets the caption of the photo.
     *
     * @param caption the caption to set for the photo
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Sets the path name of the photo.
     *
     * @param pathName the path name to set for the photo
     * @param context the Android context
     */
    public void setPathName(String pathName, Context context) {
        this.pathName = pathName;
        this.fullImage = null; // Force reload
        this.thumbnailImage = null; // Force thumbnail recreation
    }

    /**
     * Adds a tag to this photo's set of tags.
     *
     * @param tag the tag to be added to the set
     */
    public void addTag(Tag tag) {
        tags.add(tag);
    }

    /**
     * Removes a tag from this photo's set of tags.
     *
     * @param tag the tag to be removed from the set
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    /**
     * Clean up bitmap resources when no longer needed
     */
    public void recycleImages() {
        if (fullImage != null && !fullImage.isRecycled()) {
            fullImage.recycle();
            fullImage = null;
        }
        if (thumbnailImage != null && !thumbnailImage.isRecycled()) {
            thumbnailImage.recycle();
            thumbnailImage = null;
        }
    }

    /**
     * Converts a photo to a string representation.
     *
     * @return a string representation of the photo
     */
    @Override
    public String toString() {
        return caption + " | " + pathName + " | " + tags;
    }

    /**
     * Checks if two photos are equal based on their path names.
     *
     * @param obj the object to compare with
     * @return true if the photos are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Photo photo = (Photo) obj;
        return pathName.equals(photo.pathName);
    }

    /**
     * Returns the hash code value for this photo.
     * @return the hash code value for this photo
     */
    @Override
    public int hashCode() {
        return pathName.hashCode();
    }
}