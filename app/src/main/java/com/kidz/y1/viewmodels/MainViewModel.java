package com.kidz.y1.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.kidz.y1.di.DependencyProvider;
import com.kidz.y1.models.Profile;
import com.kidz.y1.repositories.MusicRepository;
import com.kidz.y1.utils.Constants;

import java.util.List;

/**
 * ViewModel for MainActivity.
 * Manages profile data and state.
 * Uses SavedStateHandle to survive process death (API 17+ compatible).
 */
public class MainViewModel extends ViewModel {
    private final MusicRepository musicRepository;
    private final SavedStateHandle savedStateHandle;
    private final MutableLiveData<List<Profile>> profiles = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isKidzDirectoryExists = new MutableLiveData<>();

    public MainViewModel() {
        this(DependencyProvider.getMusicRepository(), null);
    }

    public MainViewModel(MusicRepository musicRepository, SavedStateHandle savedStateHandle) {
        this.musicRepository = musicRepository;
        this.savedStateHandle = savedStateHandle;
        
        checkKidzDirectory();
    }

    public void checkKidzDirectory() {
        isKidzDirectoryExists.setValue(musicRepository.isKidzDirectoryExists());
    }

    /**
     * Load profiles from the repository.
     */
    public void loadProfiles() {
        if (isLoading.getValue() != null && isLoading.getValue()) {
            return; // Already loading
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        musicRepository.getProfiles(new MusicRepository.RepositoryCallback<List<Profile>>() {
            @Override
            public void onSuccess(List<Profile> result) {
                profiles.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception error) {
                errorMessage.postValue("Failed to load profiles: " + error.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<List<Profile>> getProfiles() {
        return profiles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsKidzDirectoryExists() {
        return isKidzDirectoryExists;
    }

    /**
     * Get empty message based on current state.
     */
    public String getEmptyMessage() {
        Boolean exists = isKidzDirectoryExists.getValue();
        if (exists == null || !exists) {
            return "No Kidz directory found";
        }
        return "No profiles found";
    }

    /**
     * Save selected profile index to survive process death.
     */
    public void saveSelectedIndex(int index) {
        if (savedStateHandle != null) {
            savedStateHandle.set(Constants.EXTRA_PROFILE_SELECTED_INDEX, index);
        }
    }

    /**
     * Get saved selected profile index.
     */
    public int getSavedSelectedIndex() {
        if (savedStateHandle != null) {
            return savedStateHandle.get(Constants.EXTRA_PROFILE_SELECTED_INDEX);
        }
        return -1;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
