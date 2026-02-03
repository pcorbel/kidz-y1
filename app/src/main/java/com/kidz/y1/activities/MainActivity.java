package com.kidz.y1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.kidz.y1.R;
import com.kidz.y1.models.Album;
import com.kidz.y1.models.Profile;
import com.kidz.y1.models.Track;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.GlideOptionsCache;
import com.kidz.y1.utils.ImageHelper;
import com.kidz.y1.utils.Logger;
import com.kidz.y1.utils.MusicFileScanner;
import com.kidz.y1.utils.NavigationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    private TextView initTitleText;
    private TextView progressText;
    private ProgressBar progressBar;
    private Handler mainHandler;
    
    private Set<String> allImagePaths = new HashSet<>(); // Use Set to avoid duplicates
    private int totalCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initTitleText = findViewById(R.id.initTitleText);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.initProgressBar);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // "Initializing Kidz" is always visible (already shown in layout)
        // Hide progress elements initially
        progressText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        
        // Start collecting and warming cache
        startCacheWarming();
    }

    private void startCacheWarming() {
        new Thread(() -> {
            try {
                // Show "Listing files" message and empty progress bar during collection phase
                mainHandler.post(() -> {
                    progressText.setVisibility(View.VISIBLE);
                    progressText.setText("Listing files…");
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                });
                
                // Step 1: Collect all image paths first (complete listing)
                collectAllImagePaths();
                
                if (allImagePaths.isEmpty()) {
                    // No images to process, go directly to ProfilesActivity
                    navigateToProfilesActivity();
                    return;
                }
                
                // Convert to list for sequential processing
                List<String> imagePathsList = new ArrayList<>(allImagePaths);
                totalCount = imagePathsList.size();
                
                // Switch to showing progress counter and progress bar
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(totalCount);
                    updateProgress(0);
                });
                
                // Step 2: Warm cache one by one sequentially
                warmImageCacheSequentially(imagePathsList);
                
                // Navigate to ProfilesActivity after all are processed
                navigateToProfilesActivity();
            } catch (Exception e) {
                Logger.e("MainActivity", "Error during cache warming", e);
                // Even if there's an error, navigate to ProfilesActivity
                navigateToProfilesActivity();
            }
        }).start();
    }

    private void collectAllImagePaths() {
        // Use MusicFileScanner directly to avoid callback complexity
        try {
            List<File> profileDirs = MusicFileScanner.scanMusicDirectories();
            List<Profile> profiles = new ArrayList<>();
            for (File dir : profileDirs) {
                profiles.add(new Profile(dir));
            }
            
            // Get image paths for profiles
            for (Profile profile : profiles) {
                String imagePath = ImageHelper.findImagePath(profile.getDirectory(), ImageHelper.ImageType.PROFILE);
                if (imagePath != null) {
                    allImagePaths.add(imagePath);
                }
                
                // Get albums for this profile
                List<File> albumDirs = MusicFileScanner.scanProfileDirectories(profile.getDirectory());
                for (File albumDir : albumDirs) {
                    Album album = new Album(albumDir, profile);
                    String albumImagePath = ImageHelper.findImagePath(album.getDirectory(), ImageHelper.ImageType.ALBUM);
                    if (albumImagePath != null) {
                        allImagePaths.add(albumImagePath);
                    }
                    
                    // Get tracks for this album
                    List<File> trackFiles = MusicFileScanner.scanTracks(album.getDirectory());
                    for (File trackFile : trackFiles) {
                        Track track = new Track(trackFile, album);
                        String trackImagePath = ImageHelper.findImagePath(track.getFile(), ImageHelper.ImageType.TRACK);
                        if (trackImagePath == null && track.getAlbum() != null) {
                            trackImagePath = ImageHelper.findImagePath(track.getAlbum().getDirectory(), ImageHelper.ImageType.ALBUM);
                        }
                        if (trackImagePath != null) {
                            allImagePaths.add(trackImagePath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.e("MainActivity", "Error collecting image paths", e);
        }
    }

    private void warmImageCacheSequentially(List<String> imagePaths) {
        int coverSize = calculateCoverSize();
        
        RequestOptions baseOptions = GlideOptionsCache.getBaseOptions(
                this, null, coverSize, coverSize);
        
        // Calculate corner radius same as CoverFlowView
        int cornerRadius = (int) (android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP,
                Constants.CORNER_RADIUS_DP,
                getResources().getDisplayMetrics()));
        
        // Apply transform
        RequestOptions options = baseOptions
                .transform(new RoundedCorners(cornerRadius));
        
        int currentIndex = 0;
        
        // Process images one by one sequentially
        for (String imagePath : imagePaths) {
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    // Verify file exists for non-ID3 images
                    boolean needToPreload = true;
                    if (!imagePath.startsWith(Constants.ID3_PREFIX)) {
                        File imageFile = new File(imagePath);
                        if (!imageFile.exists()) {
                            needToPreload = false;
                        }
                    }
                    
                    if (needToPreload) {
                        // Use submit() to get a Future and wait for it to complete
                        // This ensures sequential processing, one image at a time
                        Future<?> future = null;
                        
                        if (imagePath.startsWith(Constants.ID3_PREFIX)) {
                            // ID3 embedded image - submit and wait
                            future = Glide.with(this)
                                    .load(imagePath)
                                    .apply(options)
                                    .submit(coverSize, coverSize);
                        } else {
                            // File-based image - submit and wait
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                future = Glide.with(this)
                                        .load(imageFile)
                                        .apply(options)
                                        .submit(coverSize, coverSize);
                            }
                        }
                        
                        // Wait for this image to complete before moving to the next
                        if (future != null) {
                            try {
                                future.get(); // Block until this image is loaded/cached
                            } catch (ExecutionException e) {
                                // Image failed to load - continue with next
                                Logger.w("MainActivity", "Error loading image: " + imagePath, e.getCause());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                Logger.w("MainActivity", "Interrupted while loading image: " + imagePath, e);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.w("MainActivity", "Error processing image: " + imagePath, e);
            }
            
            // Update progress after each image is processed
            currentIndex++;
            final int current = currentIndex;
            mainHandler.post(() -> updateProgress(current));
        }
    }

    private int calculateCoverSize() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int textSpace = (int) (Constants.TEXT_SPACE_DP * getResources().getDisplayMetrics().density);
        int availableHeight = screenHeight - textSpace;
        int maxWidth = (int) (screenWidth * Constants.COVER_MAX_WIDTH_RATIO);
        return Math.min(maxWidth, availableHeight);
    }

    private void updateProgress(int current) {
        if (progressText != null) {
            progressText.setText(current + "/" + totalCount);
        }
        if (progressBar != null) {
            progressBar.setProgress(current);
        }
    }

    private void navigateToProfilesActivity() {
        mainHandler.post(() -> {
            Intent intent = NavigationHelper.createProfilesIntent(this, 0);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
    }
}
