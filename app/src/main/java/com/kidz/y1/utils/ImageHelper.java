package com.kidz.y1.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import com.kidz.y1.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for finding image files associated with music directories and tracks.
 * 
 * Supports:
 * - Profile images (in parent directory)
 * - Album images (in album directory or from track names)
 * - Track images (from track file or ID3 tags)
 * 
 * Threading:
 * - All methods perform file I/O and should be called from background threads
 * - MediaMetadataRetriever operations are blocking and must run off main thread
 * - This class is typically called from ImageRepository which uses ExecutorService
 * 
 * Compatible with API 17+.
 */
public class ImageHelper {
    private static File cachedParentFolder;
    private static final Object parentFolderLock = new Object();

    /**
     * Finds the image path for a given directory or file.
     * 
     * @param directory the directory or file to find an image for
     * @param type the type of image to find (PROFILE, ALBUM, or TRACK)
     * @return the absolute path to the image file, or null if not found
     */
    public static String findImagePath(File directory, ImageType type) {
        if (directory == null) {
            return null;
        }
        
        String folderName = directory.getName();
        File parentFolder = directory.getParentFile();
        
        if (parentFolder == null) {
            synchronized (parentFolderLock) {
                if (cachedParentFolder == null) {
                    cachedParentFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                }
                parentFolder = cachedParentFolder;
            }
        }

        for (String ext : Constants.IMAGE_EXTENSIONS) {
            File imageFile = new File(parentFolder, folderName + ext);
            if (imageFile.exists() && imageFile.isFile()) {
                return imageFile.getAbsolutePath();
            }
        }

        if (type == ImageType.ALBUM) {
            List<File> tracks = MusicFileScanner.scanTracks(directory);
            if (tracks != null && !tracks.isEmpty()) {
                for (File track : tracks) {
                    String trackName = track.getName();
                    if (trackName.toLowerCase().endsWith(Constants.MP3_EXTENSION)) {
                        trackName = trackName.substring(0, trackName.length() - 4);
                    }
                    for (String ext : Constants.IMAGE_EXTENSIONS) {
                        File imageFile = new File(directory, trackName + ext);
                        if (imageFile.exists() && imageFile.isFile()) {
                            return imageFile.getAbsolutePath();
                        }
                    }
                }
                String id3Path = extractId3ArtPath(tracks.get(0));
                if (id3Path != null) {
                    return id3Path;
                }
            }
        }

        if (type == ImageType.TRACK && directory.isFile()) {
            String id3Path = extractId3ArtPath(directory);
            if (id3Path != null) {
                return id3Path;
            }
        }

        return null;
    }

    /**
     * Extract ID3 album art path from MP3 file.
     * Optimized to check file existence before creating MediaMetadataRetriever.
     * 
     * Threading: This method uses MediaMetadataRetriever which is a blocking operation.
     * Must be called from a background thread. Typically called from ImageRepository.
     * 
     * @param mp3File the MP3 file to extract ID3 art from
     * @return the ID3 art path string, or null if not found
     */
    private static String extractId3ArtPath(File mp3File) {
        if (mp3File == null || !mp3File.exists() || !mp3File.isFile()) {
            return null;
        }
        
        String fileName = mp3File.getName().toLowerCase();
        if (!fileName.endsWith(Constants.MP3_EXTENSION)) {
            return null;
        }
        
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mp3File.getAbsolutePath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null && albumArt.length > 0) {
                StringBuilder sb = new StringBuilder(Constants.ID3_PREFIX.length() + mp3File.getAbsolutePath().length());
                sb.append(Constants.ID3_PREFIX);
                sb.append(mp3File.getAbsolutePath());
                return sb.toString();
            }
        } catch (IllegalArgumentException e) {
            Logger.w("ImageHelper", "Invalid MP3 file: " + mp3File.getAbsolutePath(), e);
        } catch (RuntimeException e) {
            Logger.w("ImageHelper", "Error reading MP3 metadata: " + mp3File.getAbsolutePath(), e);
        } catch (Exception e) {
            Logger.w("ImageHelper", "Unexpected error extracting ID3 art: " + mp3File.getAbsolutePath(), e);
        } finally {
            if (retriever != null) {
                try {
                    retriever.release();
                } catch (IOException e) {
                    Logger.w("ImageHelper", "Error releasing MediaMetadataRetriever", e);
                }
            }
        }
        return null;
    }

    public enum ImageType {
        PROFILE, ALBUM, TRACK
    }
}
