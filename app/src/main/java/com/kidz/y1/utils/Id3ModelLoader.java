package com.kidz.y1.utils;

import android.graphics.Bitmap;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import com.kidz.y1.utils.Constants;

public class Id3ModelLoader implements ModelLoader<String, Bitmap> {

    @Override
    public LoadData<Bitmap> buildLoadData(String model, int width, int height, Options options) {
        if (!model.startsWith(Constants.ID3_PREFIX)) {
            return null;
        }
        String mp3Path = model.substring(Constants.ID3_PREFIX.length());
        return new LoadData<>(new ObjectKey(model), new Id3DataFetcher(mp3Path));
    }

    @Override
    public boolean handles(String model) {
        return model != null && model.startsWith(Constants.ID3_PREFIX);
    }

    public static class Factory implements ModelLoaderFactory<String, Bitmap> {
        @Override
        public ModelLoader<String, Bitmap> build(MultiModelLoaderFactory multiFactory) {
            return new Id3ModelLoader();
        }

        @Override
        public void teardown() {
        }
    }
}
