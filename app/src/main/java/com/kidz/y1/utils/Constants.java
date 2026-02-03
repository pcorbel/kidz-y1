package com.kidz.y1.utils;

/**
 * Application-wide constants.
 * Centralizes all magic strings, numbers, and configuration values.
 */
public class Constants {
    // Intent Extra Keys
    public static final String EXTRA_PROFILE_DIRECTORY = "profile_directory";
    public static final String EXTRA_ALBUM_DIRECTORY = "album_directory";
    public static final String EXTRA_TRACK_FILE = "track_file";
    public static final String EXTRA_PROFILE_SELECTED_INDEX = "profile_selected_index";
    public static final String EXTRA_ALBUM_SELECTED_INDEX = "album_selected_index";
    public static final String EXTRA_TRACK_SELECTED_INDEX = "track_selected_index";

    // Timing Constants
    public static final long VOLUME_UPDATE_INTERVAL_MS = 500;
    public static final long PROGRESS_UPDATE_INTERVAL_MS = 100;
    public static final int SCROLL_ANIMATION_DURATION_MS = 400;
    public static final int SEEK_STEP_SECONDS = 30;

    // Directory and File Constants
    public static final String KIDZ_DIRECTORY_NAME = "Kidz";
    public static final String ID3_PREFIX = "id3:";
    public static final String MP3_EXTENSION = ".mp3";
    public static final String HIDDEN_FILE_PREFIX = "._";

    // Image Extensions
    public static final String[] IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".PNG", ".JPG", ".JPEG"};

    // Battery Level Thresholds
    public static final int BATTERY_CRITICAL_THRESHOLD = 5;
    public static final int BATTERY_LOW_THRESHOLD = 33;
    public static final int BATTERY_MEDIUM_THRESHOLD = 66;

    // Volume Level Thresholds (as fractions of max volume)
    public static final double VOLUME_LOW_THRESHOLD = 1.0 / 3.0;
    public static final double VOLUME_MEDIUM_THRESHOLD = 2.0 / 3.0;

    // Resource Identifier Names
    public static final String IC_BATTERY_CHARGING = "ic_battery_charging";
    public static final String IC_BATTERY_ALERT = "ic_battery_alert";
    public static final String IC_BATTERY_PREFIX = "ic_battery_";
    public static final String IC_VOLUME_MUTE = "ic_volume_mute";
    public static final String IC_VOLUME_LOW = "ic_volume_low";
    public static final String IC_VOLUME_MEDIUM = "ic_volume_medium";
    public static final String IC_VOLUME_HIGH = "ic_volume_high";
    public static final String IC_PLAY = "ic_play";
    public static final String IC_PAUSE = "ic_pause";
    public static final String IC_NO_COVER = "ic_no_cover";

    // Cover Flow Constants
    public static final float COVER_SPACING_RATIO = 0.1f;
    public static final float COVER_SCALE_MIN = 0.75f;
    public static final float COVER_ALPHA_MIN = 0.3f;
    public static final float COVER_ROTATION_MULTIPLIER = 45f;
    public static final int COVER_PRELOAD_RANGE = 2;
    public static final int COVER_VISIBILITY_THRESHOLD = 1;

    // UI Constants
    public static final float CORNER_RADIUS_DP = 16f;
    public static final float TEXT_SPACE_DP = 140f;
    public static final float COVER_MAX_WIDTH_RATIO = 0.9f;

    // Media Player Constants
    public static final String WAKE_LOCK_TAG = "KidzY1:MusicPlayer";
    public static final int PROGRESS_BAR_MAX = 1000;

    private Constants() {
        // Prevent instantiation
    }
}
