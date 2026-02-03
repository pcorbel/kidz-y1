package com.kidz.y1.utils;

import android.content.Context;
import android.content.Intent;

import com.kidz.y1.activities.AlbumsActivity;
import com.kidz.y1.activities.ProfilesActivity;
import com.kidz.y1.activities.NowPlayingActivity;
import com.kidz.y1.activities.TracksActivity;
import com.kidz.y1.utils.Logger;

import java.io.File;

/**
 * Helper class for navigation between activities.
 * 
 * Provides type-safe methods for creating navigation intents and extracting
 * data from intents. Uses Constants for intent extra keys to ensure consistency.
 * 
 * All methods are static utility methods.
 * Compatible with API 17+.
 */
public class NavigationHelper {
    /**
     * Creates an intent to navigate to AlbumsActivity.
     * 
     * @param context the context
     * @param profileDirectory the selected profile directory
     * @param profileSelectedIndex the index of the selected profile
     * @return the configured Intent
     */
    public static Intent createAlbumsIntent(Context context, File profileDirectory, int profileSelectedIndex) {
        Intent intent = new Intent(context, AlbumsActivity.class);
        intent.putExtra(Constants.EXTRA_PROFILE_DIRECTORY, profileDirectory.getAbsolutePath());
        intent.putExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, profileSelectedIndex);
        return intent;
    }

    /**
     * Creates an intent to navigate to TracksActivity.
     * 
     * @param context the context
     * @param profileDirectory the profile directory
     * @param albumDirectory the selected album directory
     * @param profileSelectedIndex the index of the selected profile
     * @param albumSelectedIndex the index of the selected album
     * @return the configured Intent
     */
    public static Intent createTracksIntent(Context context, File profileDirectory, File albumDirectory,
            int profileSelectedIndex, int albumSelectedIndex) {
        Intent intent = new Intent(context, TracksActivity.class);
        intent.putExtra(Constants.EXTRA_PROFILE_DIRECTORY, profileDirectory.getAbsolutePath());
        intent.putExtra(Constants.EXTRA_ALBUM_DIRECTORY, albumDirectory.getAbsolutePath());
        intent.putExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, profileSelectedIndex);
        intent.putExtra(Constants.EXTRA_ALBUM_SELECTED_INDEX, albumSelectedIndex);
        return intent;
    }

    /**
     * Creates an intent to navigate to NowPlayingActivity.
     * 
     * @param context the context
     * @param trackFile the track file to play
     * @param profileDirectory the profile directory
     * @param albumDirectory the album directory
     * @param profileSelectedIndex the index of the selected profile
     * @param albumSelectedIndex the index of the selected album
     * @param trackSelectedIndex the index of the selected track
     * @return the configured Intent
     */
    public static Intent createNowPlayingIntent(Context context, File trackFile, File profileDirectory,
            File albumDirectory, int profileSelectedIndex,
            int albumSelectedIndex, int trackSelectedIndex) {
        Intent intent = new Intent(context, NowPlayingActivity.class);
        intent.putExtra(Constants.EXTRA_TRACK_FILE, trackFile.getAbsolutePath());
        intent.putExtra(Constants.EXTRA_PROFILE_DIRECTORY, profileDirectory.getAbsolutePath());
        intent.putExtra(Constants.EXTRA_ALBUM_DIRECTORY, albumDirectory.getAbsolutePath());
        intent.putExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, profileSelectedIndex);
        intent.putExtra(Constants.EXTRA_ALBUM_SELECTED_INDEX, albumSelectedIndex);
        intent.putExtra(Constants.EXTRA_TRACK_SELECTED_INDEX, trackSelectedIndex);
        return intent;
    }

    /**
     * Creates an intent to navigate to ProfilesActivity.
     * 
     * @param context the context
     * @param profileSelectedIndex the index of the selected profile (for restoration)
     * @return the configured Intent
     */
    public static Intent createProfilesIntent(Context context, int profileSelectedIndex) {
        Intent intent = new Intent(context, ProfilesActivity.class);
        intent.putExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, profileSelectedIndex);
        return intent;
    }

    /**
     * Creates an intent to navigate back to AlbumsActivity.
     * This is a convenience method that calls createAlbumsIntent.
     * 
     * @param context the context
     * @param profileDirectory the profile directory
     * @param profileSelectedIndex the selected profile index
     * @return the configured Intent
     */
    public static Intent createAlbumsBackIntent(Context context, File profileDirectory, int profileSelectedIndex) {
        return createAlbumsIntent(context, profileDirectory, profileSelectedIndex);
    }

    /**
     * Creates an intent to navigate back to TracksActivity.
     * This is a convenience method that calls createTracksIntent.
     * 
     * @param context the context
     * @param profileDirectory the profile directory
     * @param albumDirectory the album directory
     * @param profileSelectedIndex the selected profile index
     * @param albumSelectedIndex the selected album index
     * @return the configured Intent
     */
    public static Intent createTracksBackIntent(Context context, File profileDirectory, File albumDirectory,
            int profileSelectedIndex, int albumSelectedIndex) {
        return createTracksIntent(context, profileDirectory, albumDirectory, profileSelectedIndex, albumSelectedIndex);
    }

    /**
     * Extracts the profile directory from an intent.
     * 
     * @param intent the intent containing the profile directory path
     * @return the File representing the profile directory, or null if not found or invalid
     */
    public static File getProfileDirectory(Intent intent) {
        String path = intent.getStringExtra(Constants.EXTRA_PROFILE_DIRECTORY);
        if (path == null) {
            return null;
        }
        
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return file;
        }
        
        return null;
    }

    /**
     * Extracts the album directory from an intent.
     * 
     * @param intent the intent containing the album directory path
     * @return the File representing the album directory, or null if not found or invalid
     */
    public static File getAlbumDirectory(Intent intent) {
        String path = intent.getStringExtra(Constants.EXTRA_ALBUM_DIRECTORY);
        if (path == null) {
            return null;
        }
        
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return file;
        }
        
        return null;
    }

    /**
     * Extracts the track file from an intent.
     * 
     * @param intent the intent containing the track file path
     * @return the File representing the track file, or null if not found or invalid
     */
    public static File getTrackFile(Intent intent) {
        String path = intent.getStringExtra(Constants.EXTRA_TRACK_FILE);
        if (path == null) {
            return null;
        }
        
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            return file;
        }
        
        return null;
    }

    /**
     * Extracts a selected index from an intent.
     * 
     * @param intent the intent containing the index
     * @param extraKey the key for the intent extra
     * @param defaultValue the default value if not found
     * @return the selected index, or defaultValue if not found
     */
    public static int getSelectedIndex(Intent intent, String extraKey, int defaultValue) {
        return intent.getIntExtra(extraKey, defaultValue);
    }
}
