package com.kidz.y1.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidz.y1.di.DependencyProvider;
import com.kidz.y1.models.Album;
import com.kidz.y1.models.Track;
import com.kidz.y1.repositories.ImageRepository;
import com.kidz.y1.repositories.MusicRepository;

import java.util.List;

/**
 * ViewModel for NowPlayingActivity.
 * Manages playback state and track information.
 */
public class NowPlayingViewModel extends ViewModel {
    private final MusicRepository musicRepository;
    private final ImageRepository imageRepository;
    private final MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private final MutableLiveData<List<Track>> allTracks = new MutableLiveData<>();
    private final MutableLiveData<String> imagePath = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private Album album;
    private int trackIndex = 0;

    public NowPlayingViewModel() {
        this(DependencyProvider.getMusicRepository(), DependencyProvider.getImageRepository());
    }

    public NowPlayingViewModel(MusicRepository musicRepository, ImageRepository imageRepository) {
        this.musicRepository = musicRepository;
        this.imageRepository = imageRepository;
    }

    /**
     * Set the current track and album.
     */
    public void setTrack(Track track, Album album, int trackIndex) {
        this.album = album;
        this.trackIndex = trackIndex;
        currentTrack.setValue(track);
        loadImagePath(track);
        loadAllTracks();
    }

    /**
     * Load all tracks for the current album.
     */
    private void loadAllTracks() {
        if (album == null) {
            return;
        }

        isLoading.setValue(true);
        musicRepository.getTracks(album, new MusicRepository.RepositoryCallback<List<Track>>() {
            @Override
            public void onSuccess(List<Track> result) {
                allTracks.postValue(result);
                isLoading.postValue(false);
                
                Track current = currentTrack.getValue();
                if (current != null && result != null) {
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).getFile().equals(current.getFile())) {
                            trackIndex = i;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(Exception error) {
                errorMessage.postValue("Failed to load tracks: " + error.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Load image path for a track.
     */
    private void loadImagePath(Track track) {
        imageRepository.getTrackImagePath(track, new ImageRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                imagePath.postValue(result);
            }

            @Override
            public void onError(Exception error) {
                imagePath.postValue(null);
            }
        });
    }

    public LiveData<Track> getCurrentTrack() {
        return currentTrack;
    }

    public LiveData<List<Track>> getAllTracks() {
        return allTracks;
    }

    public LiveData<String> getImagePath() {
        return imagePath;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public Album getAlbum() {
        return album;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
