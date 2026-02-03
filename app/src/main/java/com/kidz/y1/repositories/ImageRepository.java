package com.kidz.y1.repositories;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.kidz.y1.models.Album;
import com.kidz.y1.models.Profile;
import com.kidz.y1.models.Track;
import com.kidz.y1.utils.ImageHelper;
import com.kidz.y1.utils.Logger;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for image path operations.
 * Handles finding and caching image paths for profiles, albums, and tracks.
 * 
 * Threading:
 * - All file I/O and MediaMetadataRetriever operations run on background threads
 * - Callbacks are invoked from background threads
 * - ViewModels should use postValue() to update LiveData from callbacks
 */
public class ImageRepository {
    private static ImageRepository instance;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, String> imagePathCache;

    private ImageRepository() {
        executorService = Executors.newFixedThreadPool(2);
        imagePathCache = new ConcurrentHashMap<>();
    }

    public static synchronized ImageRepository getInstance() {
        if (instance == null) {
            instance = new ImageRepository();
        }
        return instance;
    }

    /**
     * Get image path for a profile.
     * Runs on background thread via ExecutorService.
     * Callback is invoked from background thread - use postValue() in ViewModels.
     * 
     * @param profile the profile to get image path for
     * @param callback invoked on background thread with result
     */
    @MainThread
    public void getProfileImagePath(Profile profile, RepositoryCallback<String> callback) {
        executorService.execute(() -> {
            try {
                String cacheKey = "profile_" + profile.getDirectory().getAbsolutePath();
                String imagePath = imagePathCache.get(cacheKey);
                
                if (imagePath == null) {
                    imagePath = ImageHelper.findImagePath(profile.getDirectory(), ImageHelper.ImageType.PROFILE);
                    if (imagePath != null) {
                        imagePathCache.put(cacheKey, imagePath);
                    }
                }
                
                callback.onSuccess(imagePath);
            } catch (SecurityException e) {
                Logger.w("ImageRepository", "Permission denied accessing image file", e);
                callback.onError(e);
            } catch (Exception e) {
                Logger.w("ImageRepository", "Error finding image path for profile: " + profile.getName(), e);
                callback.onError(e);
            }
        });
    }

    /**
     * Get image path for an album.
     * Runs on background thread via ExecutorService.
     * May use MediaMetadataRetriever which is a blocking operation.
     * Callback is invoked from background thread - use postValue() in ViewModels.
     * 
     * @param album the album to get image path for
     * @param callback invoked on background thread with result
     */
    @MainThread
    public void getAlbumImagePath(Album album, RepositoryCallback<String> callback) {
        executorService.execute(() -> {
            try {
                String cacheKey = "album_" + album.getDirectory().getAbsolutePath();
                String imagePath = imagePathCache.get(cacheKey);
                
                if (imagePath == null) {
                    imagePath = ImageHelper.findImagePath(album.getDirectory(), ImageHelper.ImageType.ALBUM);
                    if (imagePath != null) {
                        imagePathCache.put(cacheKey, imagePath);
                    }
                }
                
                callback.onSuccess(imagePath);
            } catch (SecurityException e) {
                Logger.w("ImageRepository", "Permission denied accessing image file", e);
                callback.onError(e);
            } catch (Exception e) {
                Logger.w("ImageRepository", "Error finding image path for album: " + album.getName(), e);
                callback.onError(e);
            }
        });
    }

    /**
     * Get image path for a track.
     * Runs on background thread via ExecutorService.
     * May use MediaMetadataRetriever which is a blocking operation.
     * Callback is invoked from background thread - use postValue() in ViewModels.
     * 
     * @param track the track to get image path for
     * @param callback invoked on background thread with result
     */
    @MainThread
    public void getTrackImagePath(Track track, RepositoryCallback<String> callback) {
        executorService.execute(() -> {
            try {
                String cacheKey = "track_" + track.getFile().getAbsolutePath();
                String imagePath = imagePathCache.get(cacheKey);
                
                if (imagePath == null) {
                    imagePath = ImageHelper.findImagePath(track.getFile(), ImageHelper.ImageType.TRACK);
                    if (imagePath == null && track.getAlbum() != null) {
                        imagePath = ImageHelper.findImagePath(track.getAlbum().getDirectory(), ImageHelper.ImageType.ALBUM);
                    }
                    if (imagePath != null) {
                        imagePathCache.put(cacheKey, imagePath);
                    }
                }
                
                callback.onSuccess(imagePath);
            } catch (SecurityException e) {
                Logger.w("ImageRepository", "Permission denied accessing image file", e);
                callback.onError(e);
            } catch (Exception e) {
                Logger.w("ImageRepository", "Error finding image path for track: " + track.getName(), e);
                callback.onError(e);
            }
        });
    }

    /**
     * Invalidate cache for a specific item.
     */
    public void invalidateCache(File file, String type) {
        String cacheKey = type + "_" + file.getAbsolutePath();
        imagePathCache.remove(cacheKey);
    }

    /**
     * Clear all caches.
     */
    public void clearCache() {
        imagePathCache.clear();
    }

    /**
     * Shutdown the executor service.
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
