package com.kidz.y1.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import android.util.TypedValue;
import com.kidz.y1.R;
import com.kidz.y1.utils.ImageHelper;
import com.kidz.y1.utils.MusicFileScanner;
import com.kidz.y1.utils.NavigationHelper;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.GlideOptionsCache;
import com.kidz.y1.utils.Logger;
import com.kidz.y1.utils.ProgressUpdateObserver;
import com.kidz.y1.utils.VolumeManager;

import java.io.File;
import java.util.List;

public class NowPlayingActivity extends BaseActivity {
    private ImageView coverView;
    private TextView titleText;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private File trackFile;
    private File albumDirectory;
    private int trackIndex = 0;
    private List<File> trackFiles;

    private ProgressUpdateObserver progressUpdateObserver;

    private VolumeManager volumeManager;
    private PowerManager.WakeLock wakeLock;
    private int cornerRadius;
    private int coverSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.WAKE_LOCK_TAG);

        volumeManager = new VolumeManager(this, this::updateVolumeIcon);
        startPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressUpdateObserver != null) {
            getLifecycle().removeObserver(progressUpdateObserver);
            progressUpdateObserver = null;
        }
        releaseMediaPlayer();
        releaseWakeLock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        contentLayout.setFocusable(true);
        contentLayout.requestFocus();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            contentLayout.requestFocus();
        }
    }

    @Override
    protected void setupMainContentArea() {
        getLayoutInflater().inflate(R.layout.activity_now_playing, mainContentLayout);

        float cornerRadiusDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                Constants.CORNER_RADIUS_DP,
                getResources().getDisplayMetrics());
        cornerRadius = Math.max(1, (int) cornerRadiusDp);

        trackFile = NavigationHelper.getTrackFile(getIntent());
        albumDirectory = NavigationHelper.getAlbumDirectory(getIntent());
        trackIndex = NavigationHelper.getSelectedIndex(getIntent(), Constants.EXTRA_TRACK_SELECTED_INDEX, 0);

        coverView = findViewById(R.id.coverView);
        titleText = findViewById(R.id.titleText);
        if (mainContentLayout != null) {
            errorMessageText = mainContentLayout.findViewById(R.id.errorMessageText);
        }

        topBarIcon.setVisibility(View.GONE);
        topBarText.setVisibility(View.GONE);

        coverSize = calculateCoverSize();
        ViewGroup.LayoutParams params = coverView.getLayoutParams();
        params.width = coverSize;
        params.height = coverSize;
        coverView.setLayoutParams(params);
        coverView.setBackgroundColor(0xFF000000);
        coverView.setImageDrawable(null);

        loadTrackInfoAsync();
    }

    @Override
    protected void setupTopBar() {
        super.setupTopBar();
        playPauseIcon.setVisibility(View.VISIBLE);
        updatePlayPauseIcon();
    }

    @Override
    protected void setupBottomBar() {
        super.setupBottomBar();
        bottomBarText.setVisibility(View.GONE);
        currentTimeText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        totalTimeText.setVisibility(View.VISIBLE);
    }

    private void loadTrackInfoAsync() {
        trackFiles = MusicFileScanner.scanTracks(albumDirectory);
        if (trackIndex < 0 || trackIndex >= trackFiles.size()) {
            trackIndex = trackFiles.indexOf(trackFile);
            if (trackIndex < 0) {
                trackIndex = 0;
            }
        }

        String imagePath = ImageHelper.findImagePath(trackFile, ImageHelper.ImageType.TRACK);
        if (imagePath == null) {
            imagePath = ImageHelper.findImagePath(albumDirectory, ImageHelper.ImageType.ALBUM);
        }

        loadCoverImage(imagePath);
        updateTrackCounter();
    }

    private void loadCoverImage(String imagePath) {
        RequestOptions baseOptions = GlideOptionsCache.getBaseOptions(
                this, null, coverSize, coverSize);
        
        RequestOptions options = baseOptions
                .transform(new RoundedCorners(cornerRadius));

        if (imagePath != null && !imagePath.startsWith(Constants.ID3_PREFIX)) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Glide.with(this)
                        .load(imageFile)
                        .apply(options)
                        .into(coverView);
            } else {
                // Load null to trigger error placeholder (ic_no_cover)
                Glide.with(this)
                        .load((Object) null)
                        .apply(options)
                        .into(coverView);
            }
        } else if (imagePath != null && imagePath.startsWith(Constants.ID3_PREFIX)) {
            Glide.with(this)
                    .load(imagePath)
                    .apply(options)
                    .into(coverView);
        } else {
            // Load null to trigger error placeholder (ic_no_cover)
            Glide.with(this)
                    .load((Object) null)
                    .apply(options)
                    .into(coverView);
        }
    }

    private void updateTrackCounter() {
    }

    /**
     * Starts media playback.
     * Broken down into smaller methods for better maintainability.
     */
    private void startPlayback() {
        contentLayout.requestFocus();

        if (!validateTrackFile()) {
            return;
        }

        try {
            initializeMediaPlayer();
            configureMediaPlayer();
            startMediaPlayback();
        } catch (Exception e) {
            handlePlaybackError(e);
        }
    }

    /**
     * Validates that the track file exists and is accessible.
     * @return true if valid, false otherwise
     */
    private boolean validateTrackFile() {
        if (trackFile == null || !trackFile.exists()) {
            showError("Music file not found");
            return false;
        }
        return true;
    }

    /**
     * Initializes the MediaPlayer with the track file.
     * @throws Exception if MediaPlayer initialization fails
     */
    private void initializeMediaPlayer() throws Exception {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(trackFile.getAbsolutePath());
        mediaPlayer.prepare();
    }

    /**
     * Configures MediaPlayer callbacks and observers.
     */
    private void configureMediaPlayer() {
        mediaPlayer.setOnCompletionListener(mp -> onPlaybackComplete());
        setupProgressObserver();
    }

    /**
     * Starts media playback and updates UI state.
     */
    private void startMediaPlayback() {
        mediaPlayer.start();
        isPlaying = true;
        acquireWakeLock();
        updatePlayPauseIcon();
    }

    /**
     * Handles playback completion.
     */
    private void onPlaybackComplete() {
        isPlaying = false;
        releaseWakeLock();
        if (progressUpdateObserver != null) {
            getLifecycle().removeObserver(progressUpdateObserver);
            progressUpdateObserver = null;
        }
        updateProgressOnComplete();
        updatePlayPauseIcon();
    }

    /**
     * Updates progress UI when playback completes.
     */
    private void updateProgressOnComplete() {
        if (mediaPlayer != null && progressBar != null && currentTimeText != null) {
            int duration = mediaPlayer.getDuration();
            if (duration > 0) {
                progressBar.setProgress(1000);
                currentTimeText.setText(formatTime(duration, duration));
            }
        }
    }

    /**
     * Handles playback errors.
     * @param e the exception that occurred
     */
    private void handlePlaybackError(Exception e) {
        android.util.Log.e("NowPlayingActivity", "Playback error: " + e.getMessage(), e);
        releaseMediaPlayer();
        showError("Music file unsupported");
    }

    private void showError(String errorMessage) {
        showErrorMessage(errorMessage);
        if (coverView != null) {
            coverView.setVisibility(View.GONE);
        }
        if (titleText != null) {
            titleText.setVisibility(View.GONE);
        }
        if (playPauseIcon != null) {
            playPauseIcon.setVisibility(View.GONE);
        }
    }

    private void setupProgressObserver() {
        if (mediaPlayer != null && progressBar != null && currentTimeText != null && totalTimeText != null) {
            if (progressUpdateObserver != null) {
                getLifecycle().removeObserver(progressUpdateObserver);
            }
            progressUpdateObserver = new ProgressUpdateObserver(mediaPlayer, progressBar, currentTimeText, totalTimeText);
            getLifecycle().addObserver(progressUpdateObserver);
        }
    }

    @Deprecated
    private void startProgressUpdates() {
        setupProgressObserver();
    }

    @Deprecated
    private void stopProgressUpdates() {
        if (progressUpdateObserver != null) {
            getLifecycle().removeObserver(progressUpdateObserver);
            progressUpdateObserver = null;
        }
    }

    @Deprecated
    private void updateProgress() {
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

    private void updatePlayPauseIcon() {
        String iconName = isPlaying ? Constants.IC_PAUSE : Constants.IC_PLAY;
        int resId = getThemeDrawableResource(iconName);
        if (resId != 0) {
            playPauseIcon.setImageResource(resId);
            playPauseIcon.setVisibility(View.VISIBLE);
        }
    }

    private void handlePlayPause() {
        if (mediaPlayer == null)
            return;
        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            releaseWakeLock();
        } else {
            mediaPlayer.start();
            isPlaying = true;
            acquireWakeLock();
            setupProgressObserver();
        }
        updatePlayPauseIcon();
    }

    private void releaseMediaPlayer() {
        if (progressUpdateObserver != null) {
            getLifecycle().removeObserver(progressUpdateObserver);
            progressUpdateObserver = null;
        }
        releaseWakeLock();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                Logger.w("NowPlayingActivity", "MediaPlayer in invalid state during stop", e);
            } catch (Exception e) {
                Logger.w("NowPlayingActivity", "Error stopping MediaPlayer", e);
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onBackPressed() {
        releaseMediaPlayer();

        File profileDir = NavigationHelper.getProfileDirectory(getIntent());
        File albumDir = NavigationHelper.getAlbumDirectory(getIntent());
        int profileIndex = NavigationHelper.getSelectedIndex(getIntent(), Constants.EXTRA_PROFILE_SELECTED_INDEX, -1);
        int albumIndex = NavigationHelper.getSelectedIndex(getIntent(), Constants.EXTRA_ALBUM_SELECTED_INDEX, -1);

        Intent intent = NavigationHelper.createTracksBackIntent(this, profileDir, albumDir, profileIndex, albumIndex);
        intent.putExtra(Constants.EXTRA_TRACK_SELECTED_INDEX, trackIndex);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (errorMessageText != null && errorMessageText.getVisibility() == View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed();
                return true;
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (mediaPlayer != null) {
                handlePlayPause();
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            volumeManager.adjustVolume(-1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            volumeManager.adjustVolume(1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            if (mediaPlayer != null) {
                seekForward(Constants.SEEK_STEP_SECONDS);
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            if (mediaPlayer != null) {
                seekBackward(Constants.SEEK_STEP_SECONDS);
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void seekForward(int seconds) {
        if (mediaPlayer == null)
            return;
        int currentPosition = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();
        int seekToPosition = Math.min(currentPosition + (seconds * 1000), duration);
        mediaPlayer.seekTo(seekToPosition);
        updateProgress();
    }

    private void seekBackward(int seconds) {
        if (mediaPlayer == null)
            return;
        int currentPosition = mediaPlayer.getCurrentPosition();
        int seekToPosition = Math.max(currentPosition - (seconds * 1000), 0);
        mediaPlayer.seekTo(seekToPosition);
        updateProgress();
    }
}
