package com.kidz.y1.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import com.kidz.y1.utils.Logger;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.IOException;

/**
 * Glide DataFetcher for extracting album art from ID3 tags in MP3 files.
 * 
 * Threading:
 * - Glide automatically calls loadData() on a background thread
 * - MediaMetadataRetriever operations are blocking but safe here as Glide handles threading
 * - Callbacks (onDataReady, onLoadFailed) are thread-safe
 */
public class Id3DataFetcher implements DataFetcher<Bitmap> {
    private final String mp3Path;

    public Id3DataFetcher(String mp3Path) {
        this.mp3Path = mp3Path;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super Bitmap> callback) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(mp3Path);
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null && albumArt.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                if (bitmap != null) {
                    callback.onDataReady(bitmap);
                    return;
                }
            }
            callback.onLoadFailed(new IOException("No ID3 album art found in: " + mp3Path));
        } catch (IllegalArgumentException e) {
            Logger.w("Id3DataFetcher", "Invalid MP3 file: " + mp3Path, e);
            callback.onLoadFailed(new IOException("Invalid MP3 file: " + mp3Path, e));
        } catch (RuntimeException e) {
            Logger.w("Id3DataFetcher", "Error reading MP3 metadata: " + mp3Path, e);
            callback.onLoadFailed(new IOException("Failed to read MP3 metadata: " + mp3Path, e));
        } catch (Exception e) {
            Logger.e("Id3DataFetcher", "Failed to extract ID3 album art from: " + mp3Path, e);
            callback.onLoadFailed(new IOException("Failed to extract ID3 album art from: " + mp3Path, e));
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Logger.w("Id3DataFetcher", "Error releasing MediaMetadataRetriever", e);
            }
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
