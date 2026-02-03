package com.kidz.y1.utils;

import android.content.Context;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class for efficient resource identifier lookups.
 * Caches resource IDs to avoid repeated getIdentifier() calls.
 * Compatible with API 17+.
 */
public class ResourceHelper {
    private static final ConcurrentHashMap<String, Integer> resourceCache = new ConcurrentHashMap<>();
    
    /**
     * Gets a drawable resource ID by name, with caching.
     * 
     * @param context the context
     * @param resourceName the name of the drawable resource
     * @return the resource ID, or 0 if not found
     */
    public static int getDrawableResourceId(Context context, String resourceName) {
        if (context == null || resourceName == null || resourceName.isEmpty()) {
            return 0;
        }
        
        String cacheKey = context.getPackageName() + ":" + resourceName;
        Integer cached = resourceCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        if (resId != 0) {
            resourceCache.put(cacheKey, resId);
        }
        return resId;
    }
    
    /**
     * Clears the resource cache.
     * Useful for memory management or when resources change.
     */
    public static void clearCache() {
        resourceCache.clear();
    }
}
