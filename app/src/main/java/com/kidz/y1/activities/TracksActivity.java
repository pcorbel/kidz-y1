package com.kidz.y1.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.kidz.y1.models.Album;
import com.kidz.y1.models.Profile;
import com.kidz.y1.models.Track;
import com.kidz.y1.utils.Constants;
import com.kidz.y1.utils.NavigationHelper;
import com.kidz.y1.viewmodels.TracksViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TracksActivity extends CoverFlowActivity {
    private TracksViewModel viewModel;
    private Album album;
    private List<Track> tracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File profileDirectory = NavigationHelper.getProfileDirectory(getIntent());
        File albumDirectory = NavigationHelper.getAlbumDirectory(getIntent());
        
        if (profileDirectory != null && albumDirectory != null) {
            Profile profile = new Profile(profileDirectory);
            album = new Album(albumDirectory, profile);
        }
        
        super.onCreate(savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(TracksViewModel.class);
        
        if (album != null) {
            viewModel.setAlbum(album);
        }
        
        viewModel.getTracks().observe(this, this::onTracksLoaded);
        viewModel.getErrorMessage().observe(this, this::onError);
    }

    @Override
    protected void loadData() {
        if (viewModel == null) {
            items = new ArrayList<>();
            selectedIndex = 0;
            return;
        }
        
        List<Track> currentTracks = viewModel.getTracks().getValue();
        if (currentTracks != null) {
            tracks = currentTracks;
            items = new ArrayList<>();
            for (Track track : tracks) {
                items.add(track.getFile());
            }
        } else {
            items = new ArrayList<>();
        }
    }

    private void onTracksLoaded(List<Track> tracksList) {
        if (tracksList != null) {
            tracks = tracksList;
            items = new ArrayList<>();
            for (Track track : tracks) {
                items.add(track.getFile());
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
        return ImageType.TRACK;
    }

    @Override
    protected String getEmptyMessage() {
        return "No tracks found";
    }

    @Override
    protected String getItemName(File item) {
        String name = item.getName();
        if (name.toLowerCase().endsWith(Constants.MP3_EXTENSION)) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    @Override
    protected String getSelectionExtraKey() {
        return Constants.EXTRA_TRACK_SELECTED_INDEX;
    }

    @Override
    protected void handleCenterClick() {
        if (items == null || items.isEmpty() || tracks.isEmpty() || album == null) {
            return;
        }
        selectedIndex = coverFlowView.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < tracks.size()) {
            Track selectedTrack = tracks.get(selectedIndex);
            int profileIndex = getIntent().getIntExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, -1);
            int albumIndex = getIntent().getIntExtra(Constants.EXTRA_ALBUM_SELECTED_INDEX, -1);
            Intent intent = NavigationHelper.createNowPlayingIntent(
                    this, 
                    selectedTrack.getFile(), 
                    album.getProfile().getDirectory(),
                    album.getDirectory(), 
                    profileIndex, 
                    albumIndex, 
                    selectedIndex
            );
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void handleBackPress() {
        if (album == null) {
            return;
        }
        int albumIndex = getIntent().getIntExtra(Constants.EXTRA_ALBUM_SELECTED_INDEX, -1);
        int profileIndex = getIntent().getIntExtra(Constants.EXTRA_PROFILE_SELECTED_INDEX, -1);
        Intent intent = NavigationHelper.createAlbumsBackIntent(
                this, 
                album.getProfile().getDirectory(), 
                profileIndex
        );
        intent.putExtra(Constants.EXTRA_ALBUM_SELECTED_INDEX, albumIndex);
        startActivity(intent);
        finish();
    }
}
