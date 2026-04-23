package ScanHub.GUI.controllers;


import ScanHub.BE.Profile;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ProfileFormController {

    private ModelFacade modelFacade;
    private Profile editingProfile = null; // null means create mode, non-null means edit mode

    /**
     * Receives the shared model and optionally a profile to edit.
     * Called before the window is shown, so it can access FXML fields here.
     * @param modelFacade the shared model instance from AdminController
     * @param profile the Profile to edit, or null if creating a new one
     */
    public void setModel(ModelFacade modelFacade, Profile profile) {
        this.modelFacade = modelFacade;
        this.editingProfile = profile;

        if (editingProfile != null) {
            populateFields(editingProfile);
        }
    }

    /**
     * Pre-fills input fields when editing an existing Profile.
     */
    private void populateFields(Profile profile) {

    }

    @FXML
    private void onClickSave(ActionEvent actionEvent) {
        if (editingProfile != null) {
            updateProfile();
        } else {
            createProfile();
        }
    }

    private void createProfile() {

    }

    private void updateProfile() {

    }
}
