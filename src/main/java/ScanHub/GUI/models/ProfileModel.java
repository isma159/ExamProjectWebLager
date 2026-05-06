package ScanHub.GUI.models;

import ScanHub.BE.Profile;
import ScanHub.BLL.ProfileManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProfileModel {
    private ObservableList<Profile> profileObservableList;
    private ProfileManager profileManager = new ProfileManager();

    public ProfileModel() throws Exception {
        profileObservableList = FXCollections.observableArrayList();
        profileObservableList.setAll(profileManager.getProfiles());
    }

    public void createProfile(Profile newProfile) throws Exception {
        Profile createdProfile = profileManager.createProfile(newProfile);
        profileObservableList.add(createdProfile);
    }

    public ObservableList<Profile> getProfiles() {
        return profileObservableList;
    }

    public void refreshProfiles() throws Exception {
        profileObservableList.setAll(profileManager.getProfiles());
    }

    public void updateProfile(Profile updatedProfile) throws Exception {
        profileManager.updateProfile(updatedProfile);
        // Update the local observable list so the table refreshes automatically
        int index = profileObservableList.indexOf(updatedProfile);
        if (index >= 0) {
            profileObservableList.set(index, updatedProfile);
        }
    }

    public void deleteProfile(Profile selectedProfile) throws Exception {
        profileManager.deleteProfile(selectedProfile);
        profileObservableList.remove(selectedProfile);
    }
}
