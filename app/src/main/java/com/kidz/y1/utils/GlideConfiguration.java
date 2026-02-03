package com.kidz.y1.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class GlideConfiguration extends AppGlideModule {
    private static final int DISK_CACHE_SIZE = 5 * 1024 * 1024 * 1024;
    private static final int MEMORY_CACHE_SIZE = 256 * 1024 * 1024;
    private static final int BITMAP_POOL_SIZE = 64 * 1024 * 1024;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE));
        builder.setBitmapPool(new LruBitmapPool(BITMAP_POOL_SIZE));
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        builder.setDefaultRequestOptions(
                com.bumptech.glide.request.RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.prepend(String.class, Bitmap.class, new Id3ModelLoader.Factory());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
