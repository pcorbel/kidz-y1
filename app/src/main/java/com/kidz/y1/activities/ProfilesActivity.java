package com.kidz.y1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.kidz.y1.models.Profile;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.NavigationHelper;
import com.kidz.y1.viewmodels.MainViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfilesActivity extends CoverFlowActivity {
    private MainViewModel viewModel;
    private List<Profile> profiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        viewModel.getProfiles().observe(this, this::onProfilesLoaded);
        viewModel.getErrorMessage().observe(this, this::onError);
        viewModel.getIsKidzDirectoryExists().observe(this, exists -> {
            if (exists != null && exists) {
                viewModel.loadProfiles();
            }
        });
        
        viewModel.checkKidzDirectory();
        Boolean exists = viewModel.getIsKidzDirectoryExists().getValue();
        if (exists != null && exists) {
            viewModel.loadProfiles();
        } else {
            if (items == null) {
                items = new ArrayList<>();
            }
            selectedIndex = 0;
            updateDisplay();
        }
    }

    @Override
    protected void loadData() {
        if (viewModel == null) {
            items = new ArrayList<>();
            selectedIndex = 0;
            return;
        }
        
        List<Profile> currentProfiles = viewModel.getProfiles().getValue();
        if (currentProfiles != null) {
            profiles = currentProfiles;
            items = new ArrayList<>();
            for (Profile profile : profiles) {
                items.add(profile.getDirectory());
            }
        } else {
            items = new ArrayList<>();
        }
    }

    private void onProfilesLoaded(List<Profile> profilesList) {
        if (profilesList != null) {
            profiles = profilesList;
            items = new ArrayList<>();
            for (Profile profile : profiles) {
                items.add(profile.getDirectory());
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
        return ImageType.PROFILE;
    }

    @Override
    protected String getEmptyMessage() {
        if (viewModel != null) {
            return viewModel.getEmptyMessage();
        }
        return "No profiles found";
    }

    @Override
    protected String getItemName(File item) {
        return item.getName();
    }

    @Override
    protected String getSelectionExtraKey() {
        return Constants.EXTRA_PROFILE_SELECTED_INDEX;
    }

    @Override
    protected void handleCenterClick() {
        if (items == null || items.isEmpty() || profiles.isEmpty()) {
            return;
        }
        selectedIndex = coverFlowView.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < profiles.size()) {
            Profile selectedProfile = profiles.get(selectedIndex);
            Intent intent = NavigationHelper.createAlbumsIntent(
                    this, 
                    selectedProfile.getDirectory(), 
                    selectedIndex
            );
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void handleBackPress() {
    }
}
