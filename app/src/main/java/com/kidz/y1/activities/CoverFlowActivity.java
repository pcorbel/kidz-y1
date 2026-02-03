package com.kidz.y1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.kidz.y1.R;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.ImageHelper;
import com.kidz.y1.utils.NavigationHelper;
import com.kidz.y1.views.CoverFlowView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class CoverFlowActivity extends BaseActivity {
    protected CoverFlowView coverFlowView;
    protected int selectedIndex = 0;
    protected List<File> items;
    protected ImageType imageType;

    public enum ImageType {
        PROFILE, ALBUM, TRACK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (items == null) {
            items = new ArrayList<>();
        }
        loadData();
        if (items == null) {
            items = new ArrayList<>();
        }
        restoreSelection();
        updateDisplay();
    }

    @Override
    protected void setupMainContentArea() {
        getLayoutInflater().inflate(R.layout.activity_cover_flow, mainContentLayout);
        coverFlowView = findViewById(R.id.coverFlowView);
        errorMessageText = mainContentLayout.findViewById(R.id.errorMessageText);
        topBarText.setVisibility(View.VISIBLE);
        topBarIcon.setVisibility(View.GONE);
        setupCoverFlowView();
    }

    /**
     * Loads data for the cover flow.
     * Must be implemented by subclasses to provide the list of items to display.
     */
    protected abstract void loadData();

    /**
     * Gets the image type for this cover flow.
     * @return the ImageType (PROFILE, ALBUM, or TRACK)
     */
    protected abstract ImageType getImageType();

    /**
     * Gets the message to display when no items are found.
     * @return the empty message string
     */
    protected abstract String getEmptyMessage();

    /**
     * Gets the display name for an item.
     * @param item the File representing the item
     * @return the display name
     */
    protected abstract String getItemName(File item);

    /**
     * Handles center button/enter key press.
     * Must be implemented by subclasses to define navigation behavior.
     */
    protected abstract void handleCenterClick();

    /**
     * Handles back button press.
     * Must be implemented by subclasses to define back navigation behavior.
     */
    protected abstract void handleBackPress();

    /**
     * Restores the selected index from the intent extras.
     * Used to maintain selection when navigating back to an activity.
     */
    protected void restoreSelection() {
        if (items == null || items.isEmpty()) {
            selectedIndex = 0;
            return;
        }
        int savedIndex = getIntent().getIntExtra(getSelectionExtraKey(), -1);
        if (savedIndex >= 0 && savedIndex < items.size()) {
            selectedIndex = savedIndex;
        } else {
            selectedIndex = 0;
        }
    }

    protected abstract String getSelectionExtraKey();

    private void setupCoverFlowView() {
        this.imageType = getImageType();
        int coverSize = calculateCoverSize();
        coverFlowView.setCoverSize(coverSize);
        coverFlowView.setOnCoverSelectedListener(index -> {
            selectedIndex = index;
            updateBottomBarText();
            updateTopBarText();
        });
    }

    /**
     * Updates the display based on current items.
     * Shows error message if no items, otherwise displays the cover flow.
     */
    protected void updateDisplay() {
        if (items == null || items.isEmpty()) {
            showErrorMessage(getEmptyMessage());
            if (coverFlowView != null) {
                coverFlowView.setVisibility(View.GONE);
            }
        } else {
            hideErrorMessage();
            if (coverFlowView != null) {
                coverFlowView.setVisibility(View.VISIBLE);
            }
            updateBottomBarText();
            updateTopBarText();
            loadCovers();
        }
    }

    private void loadCovers() {
        if (items == null || items.isEmpty() || coverFlowView == null) {
            return;
        }
        int coverSize = calculateCoverSize();
        ImageHelper.ImageType helperImageType = ImageHelper.ImageType.valueOf(imageType.name());
        coverFlowView.setCoversFromSources(items, helperImageType, coverSize);
        coverFlowView.setSelectedIndexWithoutAnimation(selectedIndex);
    }

    private void updateBottomBarText() {
        if (items != null && !items.isEmpty() && selectedIndex >= 0 && selectedIndex < items.size()) {
            bottomBarText.setText(getItemName(items.get(selectedIndex)));
        }
    }

    private void updateTopBarText() {
        if (items != null && !items.isEmpty()) {
            int current = selectedIndex + 1;
            int total = items.size();
            topBarText.setText(current + "/" + total);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (items == null || items.isEmpty()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                handleBackPress();
                return true;
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            handleCenterClick();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            coverFlowView.scrollToPrevious();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            coverFlowView.scrollToNext();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBackPress();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        handleBackPress();
    }
}
