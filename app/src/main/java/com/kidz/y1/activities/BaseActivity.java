package com.kidz.y1.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.request.RequestOptions;
import com.kidz.y1.R;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.GlideOptionsCache;
import com.kidz.y1.utils.Logger;
import com.kidz.y1.utils.ResourceHelper;
import com.kidz.y1.utils.VolumeUpdateObserver;

/**
 * Base activity providing common UI elements and functionality.
 * All activities in the app extend this class to get consistent UI elements
 * like top bar, bottom bar, battery indicator, and volume indicator.
 * 
 * Features:
 * - Lifecycle-aware volume updates
 * - Battery level monitoring
 * - Common UI setup
 * - Error message display
 * 
 * Compatible with API 17+.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected LinearLayout topBar;
    protected LinearLayout bottomBar;
    protected ImageView batteryIcon;
    protected ImageView volumeIcon;
    protected ImageView topBarIcon;
    protected ImageView playPauseIcon;
    protected TextView topBarText;
    protected TextView bottomBarText;
    protected TextView currentTimeText;
    protected TextView totalTimeText;
    protected ProgressBar progressBar;
    protected BroadcastReceiver batteryReceiver;
    protected AudioManager audioManager;
    protected VolumeUpdateObserver volumeUpdateObserver;
    protected LinearLayout contentLayout;
    protected FrameLayout mainContentLayout;
    protected TextView errorMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setupBaseUI();
        registerBatteryReceiver();
        setupVolumeObserver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
        if (volumeUpdateObserver != null) {
            getLifecycle().removeObserver(volumeUpdateObserver);
            volumeUpdateObserver = null;
        }
    }

    private void setupVolumeObserver() {
        if (volumeIcon != null && audioManager != null) {
            volumeUpdateObserver = new VolumeUpdateObserver(audioManager, volumeIcon);
            getLifecycle().addObserver(volumeUpdateObserver);
        }
    }

    /**
     * Sets up the base UI components.
     * Broken down into smaller focused methods for better maintainability.
     */
    protected void setupBaseUI() {
        setContentView(R.layout.activity_base);
        initializeViews();
        setupMainContentArea();
        setupTopBar();
        setupBottomBar();
        setupFocus();
        updateBatteryLevel();
    }

    /**
     * Initialize all view references from the layout.
     */
    private void initializeViews() {
        contentLayout = findViewById(R.id.contentLayout);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        volumeIcon = findViewById(R.id.volumeIcon);
        topBarIcon = findViewById(R.id.topBarIcon);
        playPauseIcon = findViewById(R.id.playPauseIcon);
        topBarText = findViewById(R.id.topBarText);
        bottomBarText = findViewById(R.id.bottomBarText);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        progressBar = findViewById(R.id.progressBar);
        batteryIcon = findViewById(R.id.batteryIcon);
        mainContentLayout = findViewById(R.id.mainContentArea);
    }

    /**
     * Set up focus handling for the content layout.
     */
    private void setupFocus() {
        if (contentLayout != null) {
            contentLayout.setFocusable(true);
            contentLayout.requestFocus();
        }
    }

    protected void setupTopBar() {
    }

    protected void setupBottomBar() {
    }

    /**
     * Sets up the main content area for the activity.
     * Must be implemented by subclasses to provide activity-specific content.
     */
    protected abstract void setupMainContentArea();

    /**
     * Registers a broadcast receiver to monitor battery level changes.
     */
    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBatteryLevel();
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    /**
     * Updates the battery level icon based on current battery status.
     * Checks both battery level and charging status.
     */
    /**
     * Updates the battery level icon based on current battery status.
     * Uses a registered receiver to get battery status (compatible with API 17+).
     * Note: BatteryManager.getIntProperty() requires API 21+, so we use the broadcast receiver pattern.
     */
    protected void updateBatteryLevel() {
        if (batteryIcon == null) {
            return;
        }
        
        try {
            Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryStatus == null) {
                return;
            }
            
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (scale == 0 || level < 0) {
                return;
            }
            
            int batteryPct = (level * 100) / scale;
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            
            int resId = getBatteryIconResource(batteryPct, isCharging);
            if (resId != 0) {
                batteryIcon.setImageResource(resId);
            }
        } catch (Exception e) {
            Logger.w("BaseActivity", "Error updating battery level", e);
        }
    }

    /**
     * Gets the battery icon resource ID based on battery level and charging status.
     * Uses cached resource lookups for better performance.
     * 
     * @param level battery level (0-100)
     * @param charging whether the device is charging
     * @return the drawable resource ID, or 0 if not found
     */
    private int getBatteryIconResource(int level, boolean charging) {
        String iconName;
        if (charging) {
            iconName = Constants.IC_BATTERY_CHARGING;
        } else if (level <= Constants.BATTERY_CRITICAL_THRESHOLD) {
            iconName = Constants.IC_BATTERY_ALERT;
        } else {
            int batteryLevel = Math.min(9, Math.max(0, (level + 5) / 10));
            iconName = Constants.IC_BATTERY_PREFIX + batteryLevel;
        }
        
        return ResourceHelper.getDrawableResourceId(this, iconName);
    }

    @Deprecated
    protected void startVolumeUpdates() {
    }

    @Deprecated
    protected void stopVolumeUpdates() {
    }

    @Deprecated
    protected void updateVolumeIcon() {
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    protected int calculateCoverSize() {
        Resources res = getResources();
        int screenWidth = res.getDisplayMetrics().widthPixels;
        int screenHeight = res.getDisplayMetrics().heightPixels;
        int textSpace = (int) (Constants.TEXT_SPACE_DP * res.getDisplayMetrics().density);
        int availableHeight = screenHeight - textSpace;
        int maxWidth = (int) (screenWidth * Constants.COVER_MAX_WIDTH_RATIO);
        return Math.min(maxWidth, availableHeight);
    }

    /**
     * Gets a drawable resource ID by name.
     * Uses cached resource lookups for better performance.
     * 
     * @param iconName the name of the drawable resource
     * @return the resource ID, or 0 if not found
     */
    protected int getThemeDrawableResource(String iconName) {
        return ResourceHelper.getDrawableResourceId(this, iconName);
    }

    /**
     * Displays a cover image using Glide.
     * Relies on Glide's built-in cache for image data.
     * 
     * @param imageView the ImageView to display the image in
     * @param imagePath the file path to the image, or null to show fallback
     * @param fallbackIconName the resource name of the fallback icon if imagePath is null
     */
    protected void showCover(ImageView imageView, String imagePath, String fallbackIconName) {
        if (imagePath != null && !imagePath.isEmpty()) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            int width = params != null ? params.width : 0;
            int height = params != null ? params.height : 0;
            
            RequestOptions options = GlideOptionsCache.getCoverOptions(
                    this, fallbackIconName, width, height);

            com.bumptech.glide.Glide.with(this)
                    .load(new java.io.File(imagePath))
                    .apply(options)
                    .into(imageView);
        } else {
            int resId = getThemeDrawableResource(fallbackIconName);
            Drawable drawable = resId != 0 ? ContextCompat.getDrawable(this, resId) : null;
            if (drawable != null) {
                imageView.setImageDrawable(drawable);
            }
        }
    }

    protected void setupCoverViewSize(ImageView coverView) {
        int coverSize = calculateCoverSize();
        ViewGroup.LayoutParams params = coverView.getLayoutParams();
        if (params != null) {
            params.width = coverSize;
            params.height = coverSize;
            coverView.setLayoutParams(params);
        }
    }

    /**
     * Displays an error message to the user.
     * Hides the top and bottom bars when showing error.
     * 
     * @param message the error message to display
     */
    protected void showErrorMessage(String message) {
        if (errorMessageText != null) {
            errorMessageText.setText(message);
            errorMessageText.setVisibility(View.VISIBLE);
        }
        if (topBar != null) {
            topBar.setVisibility(View.GONE);
        }
        if (bottomBar != null) {
            bottomBar.setVisibility(View.GONE);
        }
    }

    /**
     * Hides the error message and restores the normal UI.
     */
    protected void hideErrorMessage() {
        if (errorMessageText != null) {
            errorMessageText.setVisibility(View.GONE);
        }
        if (topBar != null) {
            topBar.setVisibility(View.VISIBLE);
        }
        if (bottomBar != null) {
            bottomBar.setVisibility(View.VISIBLE);
        }
    }
}
