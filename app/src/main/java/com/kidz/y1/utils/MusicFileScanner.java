package com.kidz.y1.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for scanning music files and directories.
 * 
 * Provides methods to:
 * - Check if Kidz directory exists
 * - Scan for profile directories
 * - Scan for album directories within a profile
 * - Scan for track files within an album
 * 
 * Threading:
 * - All methods perform file I/O and should be called from background threads
 * - This class is typically called from MusicRepository which uses ExecutorService
 * - Results are sorted alphabetically (case-insensitive)
 * 
 * Compatible with API 17+.
 */
public class MusicFileScanner {
    /**
     * Checks if the Kidz music directory exists on external storage.
     * 
     * @return true if the directory exists, false otherwise
     */
    public static boolean isKidzDirectoryExists() {
        File musicFolder = new File(Environment.getExternalStorageDirectory(), Constants.KIDZ_DIRECTORY_NAME);
        return musicFolder.exists() && musicFolder.isDirectory();
    }

    /**
     * Scans for profile directories in the Kidz folder.
     * 
     * @return a sorted list of profile directories, or empty list if none found
     */
    public static List<File> scanMusicDirectories() {
        List<File> directories = new ArrayList<>();
        File musicFolder = new File(Environment.getExternalStorageDirectory(), Constants.KIDZ_DIRECTORY_NAME);
        if (!musicFolder.exists() || !musicFolder.isDirectory()) {
            return directories;
        }
        
        File[] files = musicFolder.listFiles();
        if (files == null) {
            return directories;
        }

        for (File file : files) {
            if (file != null && file.isDirectory() && !file.getName().startsWith(Constants.HIDDEN_FILE_PREFIX)) {
                directories.add(file);
            }
        }

        Collections.sort(directories, new java.util.Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1 == null || f2 == null) {
                    return 0;
                }
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
        return directories;
    }

    /**
     * Scans for album directories within a profile directory.
     * 
     * @param profileDir the profile directory to scan
     * @return a sorted list of album directories, or empty list if none found
     */
    public static List<File> scanProfileDirectories(File profileDir) {
        List<File> directories = new ArrayList<>();
        if (profileDir == null || !profileDir.exists() || !profileDir.isDirectory()) {
            return directories;
        }
        
        File[] files = profileDir.listFiles();
        if (files == null) {
            return directories;
        }

        for (File file : files) {
            if (file != null && file.isDirectory() && !file.getName().startsWith(Constants.HIDDEN_FILE_PREFIX)) {
                directories.add(file);
            }
        }

        Collections.sort(directories, new java.util.Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1 == null || f2 == null) {
                    return 0;
                }
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
        return directories;
    }

    /**
     * Scans for MP3 track files within an album directory.
     * 
     * @param albumDir the album directory to scan
     * @return a sorted list of MP3 track files, or empty list if none found
     */
    public static List<File> scanTracks(File albumDir) {
        List<File> tracks = new ArrayList<>();
        if (albumDir == null || !albumDir.exists() || !albumDir.isDirectory()) {
            return tracks;
        }
        
        File[] files = albumDir.listFiles();
        if (files == null) {
            return tracks;
        }

        for (File file : files) {
            if (file != null && file.isFile() && !file.getName().startsWith(Constants.HIDDEN_FILE_PREFIX)) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(Constants.MP3_EXTENSION)) {
                    tracks.add(file);
                }
            }
        }

        Collections.sort(tracks, new java.util.Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1 == null || f2 == null) {
                    return 0;
                }
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
        return tracks;
    }
}
