package com.kidz.y1.utils;

import android.content.Context;
import android.media.AudioManager;

public class VolumeManager {
    private final AudioManager audioManager;
    private final VolumeChangeListener listener;

    public interface VolumeChangeListener {
        void onVolumeChanged();
    }

    public VolumeManager(Context context, VolumeChangeListener listener) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.listener = listener;
    }

    public void adjustVolume(int direction) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = Math.max(0, Math.min(currentVolume + direction, maxVolume));

        if (newVolume != currentVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
            if (listener != null) {
                listener.onVolumeChanged();
            }
        }
    }
}
