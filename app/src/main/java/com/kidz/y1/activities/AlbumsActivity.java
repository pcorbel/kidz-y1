package com.kidz.y1.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.kidz.y1.models.Album;
import com.kidz.y1.models.Profile;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.Logger;
import com.kidz.y1.utils.NavigationHelper;
import com.kidz.y1.viewmodels.AlbumsViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumsActivity extends CoverFlowActivity {
    private AlbumsViewModel viewModel;
    private Profile profile;
    private List<Album> albums = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        File profileDirectory = NavigationHelper.getProfileDirectory(getIntent());
        if (profileDirectory != null && profileDirectory.exists()) {
            profile = new Profile(profileDirectory);
        } else {
            Logger.e("AlbumsActivity", "Profile directory is null or doesn't exist");
        }
        
        viewModel = new ViewModelProvider(this).get(AlbumsViewModel.class);
        
        if (profile != null) {
            viewModel.setProfile(profile);
        } else {
            Logger.e("AlbumsActivity", "Profile is null - cannot load albums");
            if (items == null) {
                items = new ArrayList<>();
            }
            selectedIndex = 0;
            updateDisplay();
            showErrorMessage("Invalid profile directory");
        }
        
        viewModel.getAlbums().observe(this, this::onAlbumsLoaded);
        viewModel.getErrorMessage().observe(this, this::onError);
    }

    @Override
    protected void loadData() {
        if (viewModel == null) {
            items = new ArrayList<>();
            selectedIndex = 0;
            return;
        }
        
        List<Album> currentAlbums = viewModel.getAlbums().getValue();
        if (currentAlbums != null) {
            albums = currentAlbums;
            items = new ArrayList<>();
            for (Album album : albums) {
                items.add(album.getDirectory());
            }
        } else {
            items = new ArrayList<>();
        }
    }

    private void onAlbumsLoaded(List<Album> albumsList) {
        if (albumsList != null) {
            albums = albumsList;
            items = new ArrayList<>();
            for (Album album : albums) {
                items.add(album.getDirectory());
            }
        } else {
            items = new ArrayList<>();
        }
        restoreSelection();
        if (coverFlowView != null && items != null && !items.isEmpty()) {
            coverFlowView.setSelectedIndexWithoutAnimation(selectedIndex);
        }
        updateDisplay();
    }

    private void onError(String error) {
        if (error != null) {
            showErrorMessage(error);
        }
    }

    @Override
    protected ImageType getImageType() {
        return ImageType.ALBUM;
    }

    @Override
    protected String getEmptyMessage() {
        return "No albums found";
    }

    @Override
    protected String getItemName(File item) {
        return item.getName();
    }

    @Override
    protected String getSelectionExtraKey() {
        return Constants.EXTRA_ALBUM_SELECTED_INDEX;
    }

    @Override
    protected void handleCenterClick() {
        if (items == null || items.isEmpty() || albums.isEmpty()) {
            return;
        }
        selectedIndex = coverFlowView.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < albums.size()) {
            Album selectedAlbum = albums.get(selectedIndex);
            int profileIndex = getIntent().getIntExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, -1);
            Intent intent = NavigationHelper.createTracksIntent(
                    this, 
                    profile.getDirectory(), 
                    selectedAlbum.getDirectory(),
                    profileIndex, 
                    selectedIndex
            );
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void handleBackPress() {
        int currentProfileIndex = getIntent().getIntExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, -1);
        Intent intent = NavigationHelper.createProfilesIntent(this, currentProfileIndex);
        startActivity(intent);
        finish();
    }
}
