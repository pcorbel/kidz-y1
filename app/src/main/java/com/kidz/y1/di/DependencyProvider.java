package com.kidz.y1.di;

import com.kidz.y1.repositories.ImageRepository;
import com.kidz.y1.repositories.MusicRepository;

/**
 * Simple Dependency Injection provider.
 * Provides singleton instances of repositories and other dependencies.
 * 
 * This is a manual DI approach. For more complex apps, consider using Hilt or Dagger.
 */
public class DependencyProvider {
    private static MusicRepository musicRepository;
    private static ImageRepository imageRepository;

    /**
     * Get or create MusicRepository instance.
     */
    public static MusicRepository getMusicRepository() {
        if (musicRepository == null) {
            musicRepository = MusicRepository.getInstance();
        }
        return musicRepository;
    }

    /**
     * Get or create ImageRepository instance.
     */
    public static ImageRepository getImageRepository() {
        if (imageRepository == null) {
            imageRepository = ImageRepository.getInstance();
        }
        return imageRepository;
    }

    /**
     * Set MusicRepository (useful for testing).
     */
    public static void setMusicRepository(MusicRepository repository) {
        musicRepository = repository;
    }

    /**
     * Set ImageRepository (useful for testing).
     */
    public static void setImageRepository(ImageRepository repository) {
        imageRepository = repository;
    }

    /**
     * Clear all dependencies (useful for testing).
     */
    public static void clear() {
        musicRepository = null;
        imageRepository = null;
    }
}
