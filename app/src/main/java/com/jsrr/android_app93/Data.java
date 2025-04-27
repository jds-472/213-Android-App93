package com.jsrr.android_app93;

import java.util.HashSet;
import java.util.Set;
import java.io.*;
import android.content.Context;
import android.util.Log;

/**
 * The {@code Data} class holds static fields and methods used by the whole app.
 * It provides functionality to set the current Album and Photo, as well as to save and load data with Serialization.
 *
 * <p>Features of the {@code Data} class include:
 * <ul>
 *   <li>Accessing and setting the current fields</li>
 *   <li>Saving and loading data with serialization</li>
 * </ul>
 *
 * @author [Joseph Scarpulla and Roger Ramirez]
 * @version 2.0
 */
public class Data {
    private static final String TAG = "Data";
    private Data(){}

    private static Album currentAlbum = null;
    private static Photo currentPhoto = null;
    private static Set<Album> albums = new HashSet<>();

    public static final String storeFile = "albums.ser";

    /**
     * Returns the current album of the system.
     *
     * @return the current album
     */
    public static Album getCurrentAlbum() {
        return currentAlbum;
    }

    /**
     * Sets the current album of the system.
     *
     * @param currentAlbum the album to set as the current album
     */
    public static void setCurrentAlbum(Album currentAlbum) {
        Data.currentAlbum = currentAlbum;
    }

    /**
     * Returns the current photo of the system.
     *
     * @return the current photo
     */
    public static Photo getCurrentPhoto() {
        return currentPhoto;
    }

    /**
     * Sets the current photo of the system.
     *
     * @param currentPhoto the photo to set as the current photo
     */
    public static void setCurrentPhoto(Photo currentPhoto) {
        Data.currentPhoto = currentPhoto;
    }

    /**
     * Returns the album with the specified name.
     *
     * @param name the name of the album to retrieve
     * @return the album with the specified name, or null if not found
     */
    public static Album getAlbum(String name) {
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                return album;
            }
        }
        return null;
    }

    /**
     * Adds an album to the list of albums.
     *
     * @param album the album to add
     */
    public static void addAlbum(Album album) {
        albums.add(album);
    }

    /**
     * Removes an album from the list of albums.
     *
     * @param album the album to remove
     */
    public static void removeAlbum(Album album) {
        albums.remove(album);
    }

    /**
     * Returns the set of albums in the system.
     *
     * @return the set of albums
     */
    public static Set<Album> getAlbums() {
        return albums;
    }

    /**
     * Saves the data to a file using serialization.
     *
     * @param context the application context
     */
    public static void saveData(Context context) {
        try {
            FileOutputStream fileOut = context.openFileOutput(storeFile, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fileOut);
            oos.writeObject(albums);
            oos.close();
            fileOut.close();
            Log.d(TAG, "Data saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving data: " + e.getMessage(), e);
        }
    }

    /**
     * Loads the data from a file using deserialization.
     *
     * @param context the application context
     */
    public static void loadData(Context context) {
        try {
            FileInputStream fileIn = context.openFileInput(storeFile);
            ObjectInputStream ois = new ObjectInputStream(fileIn);
            Object obj = ois.readObject();
            if (obj instanceof Set<?>) {
                albums = (Set<Album>) obj;
                Log.d(TAG, "Data loaded successfully. Albums: " + albums.size());
            }
            ois.close();
            fileIn.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "No saved data found. Creating new albums collection.");
            albums = new HashSet<>();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading data: " + e.getMessage(), e);
            albums = new HashSet<>();
        }
    }
}