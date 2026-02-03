package com.kidz.y1.utils;

import android.content.Context;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for Glide RequestOptions to avoid repeated creation.
 * Relies on Glide's built-in image cache for actual image data.
 * Compatible with API 17+.
 */
public class GlideOptionsCache {
    private static final ConcurrentHashMap<String, RequestOptions> optionsCache = new ConcurrentHashMap<>();

    /**
     * Get or create RequestOptions for cover images.
     * Options are cached and reused to reduce object allocations.
     */
    public static RequestOptions getCoverOptions(Context context, String fallbackIconName,
            int width, int height) {
        // Use default fallback icon if none specified
        String iconName = fallbackIconName;
        if (iconName == null || iconName.isEmpty()) {
            iconName = Constants.IC_NO_COVER;
        }

        String cacheKey = buildCacheKey(iconName, width, height);

        RequestOptions cached = optionsCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop();

        if (iconName != null && !iconName.isEmpty()) {
            int resId = getResourceId(context, iconName);
            if (resId != 0) {
                options = options.placeholder(resId).error(resId);
            }
        }

        if (width > 0 && height > 0) {
            options = options.override(width, height);
        }

        optionsCache.put(cacheKey, options);
        return options;
    }

    /**
     * Get base RequestOptions (without transforms) for preloaded covers.
     * No placeholders to avoid flickering, but includes error fallback for
     * corrupted images.
     * Transforms should be applied per-request as they can't be cached.
     */
    public static RequestOptions getBaseOptions(Context context, String fallbackIconName,
            int width, int height) {
        // Use default fallback icon if none specified
        String iconName = fallbackIconName;
        if (iconName == null || iconName.isEmpty()) {
            iconName = Constants.IC_NO_COVER;
        }

        // Cache key includes fallback icon name for error handling
        String cacheKey = "base_" + iconName + "_" + width + "x" + height;

        RequestOptions cached = optionsCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Simplified options for preloaded covers - no placeholders to avoid flickering
        // But include error fallback for corrupted or missing images
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop();

        // Add error fallback (but not placeholder to avoid flickering)
        int resId = getResourceId(context, iconName);
        if (resId != 0) {
            options = options.error(resId);
        }

        if (width > 0 && height > 0) {
            options = options.override(width, height);
        }

        optionsCache.put(cacheKey, options);
        return options;
    }

    /**
     * Clear the options cache (useful for memory management).
     */
    public static void clearCache() {
        optionsCache.clear();
    }

    private static String buildCacheKey(String fallbackIconName, int width, int height) {
        StringBuilder sb = new StringBuilder();
        if (fallbackIconName != null) {
            sb.append(fallbackIconName);
        }
        sb.append("_").append(width).append("x").append(height);
        return sb.toString();
    }

    private static int getResourceId(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }
}
