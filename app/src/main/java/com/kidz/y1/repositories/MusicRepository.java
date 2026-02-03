package com.kidz.y1.repositories;

import android.os.Environment;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.kidz.y1.models.Album;
import com.kidz.y1.models.Profile;
import com.kidz.y1.models.Track;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.Logger;
import com.kidz.y1.utils.MusicFileScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for music file operations.
 * Handles scanning and caching of music directories, albums, and tracks.
 * 
 * Threading:
 * - All file I/O operations run on background threads via ExecutorService
 * - Callbacks are invoked from background threads
 * - ViewModels should use postValue() to update LiveData from callbacks
 */
public class MusicRepository {
    private static MusicRepository instance;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, List<File>> scanCache;

    private MusicRepository() {
        executorService = Executors.newFixedThreadPool(2);
        scanCache = new ConcurrentHashMap<>();
    }

    public static synchronized MusicRepository getInstance() {
        if (instance == null) {
            instance = new MusicRepository();
        }
        return instance;
    }

    /**
     * Check if the Kidz directory exists.
     * This is a lightweight check that can be called from any thread.
     * 
     * @return true if the Kidz directory exists, false otherwise
     */
    public boolean isKidzDirectoryExists() {
        return MusicFileScanner.isKidzDirectoryExists();
    }

    /**
     * Get all profiles (music directories).
     * Runs on background thread via ExecutorService.
     * Callback is invoked from background thread - use postValue() in ViewModels.
     * 
     * @param callback invoked on background thread with results
     */
    @MainThread
    public void getProfiles(RepositoryCallback<List<Profile>> callback) {
        executorService.execute(() -> {
            try {
                List<File> directories = MusicFileScanner.scanMusicDirectories();
                List<Profile> profiles = new ArrayList<>();
                for (File dir : directories) {
                    profiles.add(new Profile(dir));
                }
                callback.onSuccess(profiles);
            } catch (SecurityException e) {
                Logger.e("MusicRepository", "Permission denied accessing music directories", e);
                callback.onError(e);
            } catch (Exception e) {
                Logger.e("MusicRepository", "Error loading profiles", e);
                callback.onError(e);
            }
        });
    }

    /**
     * Get all albums for a profile.
     * Runs on background thread via ExecutorService.
     * Uses caching to avoid repeated file system scans.
     * Callback is invoked from background thread - use postValue() in ViewModels.
     * 
     * @param profile the profile to get albums for
     * @param callback invoked on background thread with results
     */
    @MainThread
    public void getAlbums(Profile profile, RepositoryCallback<List<Album>> callback) {
        executorService.execute(() -> {
            try {
                File profileDir = profile.getDirectory();
                String cacheKey = "albums_" + profileDir.getAbsolutePath();
                
                List<File> directories = scanCache.get(cacheKey);
                
                if (directories == null) {
                    directories = MusicFileScanner.scanProfileDirectories(profileDir);
                    scanCache.put(cacheKey, directories);
                }
                
                List<Album> albums = new ArrayList<>(directories.size());
                for (File dir : directories) {
                    albums.add(new Album(dir, profile));
                }
                callback.onSuccess(albums);
            } catch (SecurityException e) {
                Logger.e("MusicRepository", "Permission denied accessing album directories", e);
                callback.onError(e);
            } catch (Exception e) {
                Logger.e("MusicRepository", "Error loading albums for profile: " + profile.getName(), e);
                callback.onError(e);
            }
        });
    }

    /**
     * Get all tracks for an album.
     * Runs on background thread via ExecutorService.
     * Uses caching to avoid repeated file system scans.
     * Callback is invoked from background thread - use postValue() in ViewModels.
     * 
     * @param album the album to get tracks for
     * @param callback invoked on background thread with results
     */
    @MainThread
    public void getTracks(Album album, RepositoryCallback<List<Track>> callback) {
        executorService.execute(() -> {
            try {
                File albumDir = album.getDirectory();
                String cacheKey = "tracks_" + albumDir.getAbsolutePath();
                
                List<File> trackFiles = scanCache.get(cacheKey);
                
                if (trackFiles == null) {
                    trackFiles = MusicFileScanner.scanTracks(albumDir);
                    scanCache.put(cacheKey, trackFiles);
                }
                
                List<Track> tracks = new ArrayList<>(trackFiles.size());
                for (File trackFile : trackFiles) {
                    tracks.add(new Track(trackFile, album));
                }
                callback.onSuccess(tracks);
            } catch (SecurityException e) {
                Logger.e("MusicRepository", "Permission denied accessing track files", e);
                callback.onError(e);
            } catch (Exception e) {
                Logger.e("MusicRepository", "Error loading tracks for album: " + album.getName(), e);
                callback.onError(e);
            }
        });
    }

    /**
     * Invalidate cache for a specific directory.
     * Use this when you know the directory contents have changed.
     */
    public void invalidateCache(File directory) {
        String path = directory.getAbsolutePath();
        Iterator<Map.Entry<String, List<File>>> iterator = scanCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<File>> entry = iterator.next();
            if (entry.getKey().contains(path)) {
                iterator.remove();
            }
        }
    }

    /**
     * Clear all caches.
     */
    public void clearCache() {
        scanCache.clear();
    }

    /**
     * Shutdown the executor service.
     * Should be called when the repository is no longer needed.
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Callback interface for repository operations.
     * 
     * Threading: Callbacks are invoked from background threads.
     * ViewModels should use postValue() to update LiveData from these callbacks.
     */
    public interface RepositoryCallback<T> {
        /**
         * Called when the operation succeeds.
         * Invoked from background thread.
         */
        @WorkerThread
        void onSuccess(T result);
        
        /**
         * Called when the operation fails.
         * Invoked from background thread.
         */
        @WorkerThread
        void onError(Exception error);
    }
}
