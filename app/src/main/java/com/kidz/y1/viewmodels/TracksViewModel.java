package com.kidz.y1.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidz.y1.di.DependencyProvider;
import com.kidz.y1.models.Album;
import com.kidz.y1.models.Track;
import com.kidz.y1.repositories.MusicRepository;

import java.util.List;

/**
 * ViewModel for TracksActivity.
 * Manages track data for a specific album.
 */
public class TracksViewModel extends ViewModel {
    private final MusicRepository musicRepository;
    private final MutableLiveData<List<Track>> tracks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private Album album;

    public TracksViewModel() {
        this(DependencyProvider.getMusicRepository());
    }

    public TracksViewModel(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    /**
     * Set the album and load tracks.
     */
    public void setAlbum(Album album) {
        this.album = album;
        loadTracks();
    }

    /**
     * Load tracks for the current album.
     */
    public void loadTracks() {
        if (album == null) {
            errorMessage.setValue("No album selected");
            return;
        }

        if (isLoading.getValue() != null && isLoading.getValue()) {
            return; // Already loading
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        musicRepository.getTracks(album, new MusicRepository.RepositoryCallback<List<Track>>() {
            @Override
            public void onSuccess(List<Track> result) {
                tracks.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception error) {
                errorMessage.postValue("Failed to load tracks: " + error.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<List<Track>> getTracks() {
        return tracks;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public Album getAlbum() {
        return album;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
