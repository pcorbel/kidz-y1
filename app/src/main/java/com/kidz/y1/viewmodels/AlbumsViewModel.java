package com.kidz.y1.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kidz.y1.di.DependencyProvider;
import com.kidz.y1.models.Album;
import com.kidz.y1.models.Profile;
import com.kidz.y1.repositories.MusicRepository;

import java.util.List;

/**
 * ViewModel for AlbumsActivity.
 * Manages album data for a specific profile.
 */
public class AlbumsViewModel extends ViewModel {
    private final MusicRepository musicRepository;
    private final MutableLiveData<List<Album>> albums = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private Profile profile;

    public AlbumsViewModel() {
        this(DependencyProvider.getMusicRepository());
    }

    public AlbumsViewModel(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    /**
     * Set the profile and load albums.
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
        loadAlbums();
    }

    /**
     * Load albums for the current profile.
     */
    public void loadAlbums() {
        if (profile == null) {
            errorMessage.setValue("No profile selected");
            return;
        }

        if (isLoading.getValue() != null && isLoading.getValue()) {
            return; // Already loading
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        musicRepository.getAlbums(profile, new MusicRepository.RepositoryCallback<List<Album>>() {
            @Override
            public void onSuccess(List<Album> result) {
                albums.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception error) {
                errorMessage.postValue("Failed to load albums: " + error.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<List<Album>> getAlbums() {
        return albums;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
