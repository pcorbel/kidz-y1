package com.kidz.y1.utils;

import android.util.Log;

/**
 * Simple logging utility for the application.
 * Provides consistent logging interface compatible with API 17+.
 */
public class Logger {
    private static final String TAG = "KidzY1";
    private static final boolean DEBUG = true;

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(TAG, tag + ": " + message);
        }
    }

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void i(String tag, String message) {
        Log.i(TAG, tag + ": " + message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void w(String tag, String message) {
        Log.w(TAG, tag + ": " + message);
    }

    public static void w(String tag, String message, Throwable throwable) {
        Log.w(TAG, tag + ": " + message, throwable);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }

    public static void e(String tag, String message) {
        Log.e(TAG, tag + ": " + message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG, tag + ": " + message, throwable);
    }
}
