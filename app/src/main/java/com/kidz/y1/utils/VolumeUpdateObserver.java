package com.kidz.y1.utils;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.kidz.y1.utils.Logger;
import com.kidz.y1.utils.ResourceHelper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Lifecycle-aware observer for volume updates.
 * Automatically starts/stops volume icon updates based on activity lifecycle.
 * Compatible with API 17+.
 */
public class VolumeUpdateObserver implements LifecycleObserver {
    private final AudioManager audioManager;
    private final ImageView volumeIcon;
    private final Handler handler;
    private Runnable updateRunnable;

    public VolumeUpdateObserver(AudioManager audioManager, ImageView volumeIcon) {
        this.audioManager = audioManager;
        this.volumeIcon = volumeIcon;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void startUpdates() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        updateVolumeIcon();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateVolumeIcon();
                handler.postDelayed(this, Constants.VOLUME_UPDATE_INTERVAL_MS);
            }
        };
        handler.postDelayed(updateRunnable, Constants.VOLUME_UPDATE_INTERVAL_MS);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void stopUpdates() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
            updateRunnable = null;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanup() {
        stopUpdates();
    }

    private void updateVolumeIcon() {
        if (volumeIcon == null || audioManager == null) {
            return;
        }
        try {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (maxVolume == 0) {
                return;
            }
            String iconName = currentVolume == 0 ? Constants.IC_VOLUME_MUTE
                    : currentVolume < maxVolume * Constants.VOLUME_LOW_THRESHOLD ? Constants.IC_VOLUME_LOW
                            : currentVolume < maxVolume * Constants.VOLUME_MEDIUM_THRESHOLD ? Constants.IC_VOLUME_MEDIUM
                                    : Constants.IC_VOLUME_HIGH;
            
            int resId = ResourceHelper.getDrawableResourceId(volumeIcon.getContext(), iconName);
            if (resId != 0) {
                volumeIcon.setImageResource(resId);
            }
        } catch (Exception e) {
            Logger.w("VolumeUpdateObserver", "Error updating volume icon", e);
        }
    }
}
