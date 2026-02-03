package com.kidz.y1.utils;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kidz.y1.utils.Logger;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Lifecycle-aware observer for media playback progress updates.
 * Automatically starts/stops progress updates based on activity lifecycle.
 * Compatible with API 17+.
 */
public class ProgressUpdateObserver implements LifecycleObserver {
    private final MediaPlayer mediaPlayer;
    private final ProgressBar progressBar;
    private final TextView currentTimeText;
    private final TextView totalTimeText;
    private final Handler handler;
    private Runnable updateRunnable;
    private boolean isUpdating = false;

    public ProgressUpdateObserver(MediaPlayer mediaPlayer, ProgressBar progressBar,
                                  TextView currentTimeText, TextView totalTimeText) {
        this.mediaPlayer = mediaPlayer;
        this.progressBar = progressBar;
        this.currentTimeText = currentTimeText;
        this.totalTimeText = totalTimeText;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void startUpdates() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (updateRunnable == null) {
                    return; // Updates were stopped
                }
                updateProgress();
                handler.postDelayed(this, Constants.PROGRESS_UPDATE_INTERVAL_MS);
            }
        };
        handler.post(updateRunnable);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void stopUpdates() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
            updateRunnable = null;
        }
        isUpdating = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanup() {
        stopUpdates();
    }

    private void updateProgress() {
        // Prevent re-entrant calls
        if (isUpdating) {
            return;
        }
        
        if (mediaPlayer == null || progressBar == null || currentTimeText == null || totalTimeText == null) {
            return;
        }
        
        isUpdating = true;
        try {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            if (duration > 0) {
                progressBar.setProgress((int) ((currentPosition * (long) 1000) / duration));
                currentTimeText.setText(formatTime(currentPosition, duration));
                totalTimeText.setText(formatTime(duration, duration));
            }
        } catch (IllegalStateException e) {
            Logger.w("ProgressUpdateObserver", "MediaPlayer in invalid state, stopping updates", e);
            stopUpdates();
        } catch (Exception e) {
            Logger.w("ProgressUpdateObserver", "Error updating progress", e);
            stopUpdates();
        } finally {
            isUpdating = false;
        }
    }

    private String formatTime(int milliseconds, int totalDuration) {
        int totalSeconds = milliseconds / 1000;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        boolean showHours = (totalDuration / 1000) >= 3600;

        if (showHours) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
