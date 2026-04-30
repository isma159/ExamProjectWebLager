package ScanHub.GUI.controllers;

import ScanHub.BE.*;
import ScanHub.BLL.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertCaller;
import ScanHub.GUI.util.AlertTypes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupSplitBehavior, toggleGroupProfileStatus;
    @FXML private Label formTitle, profileIdLabel, nameError, exportPreviewLabel, usersError;
    @FXML private RadioButton radioBARCODE, radioMANUAL, radioNONE, radioACTIVE, radioINACTIVE;
    @FXML private VBox userCheckboxList, vboxSplitBehavior, vboxStatus;
    @FXML private TextField profileNameField;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private Profile editingProfile = null; // null means create mode, non-null means edit mode

    /**
     * Receives the shared model and optionally a profile to edit.
     * Called before the window is shown, so it can access FXML fields here.
     * @param modelFacade the shared model instance from AdminController
     * @param profile the Profile to edit, or null if creating a new one
     */
    public void setModel(Stage currentStage, ModelFacade modelFacade, Profile profile) {
        this.currentStage = currentStage;
        this.modelFacade = modelFacade;
        this.editingProfile = profile;

        if (editingProfile != null) {
            populateFields(editingProfile);
        }

        ThemeManager.apply(currentStage.getScene());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioBARCODE.setUserData(SplitBehavior.BARCODE);
        radioMANUAL.setUserData(SplitBehavior.MANUAL);
        radioNONE.setUserData(SplitBehavior.NONE);

        radioACTIVE.setUserData(ProfileStatus.Active);
        radioINACTIVE.setUserData(ProfileStatus.Inactive);

        if (editingProfile != null) {
            formTitle.setText("Edit profile");
        }

        profileNameField.textProperty().addListener(((observable, oldValue, newValue) -> {

            String result = newValue.replace(" ", "");

            exportPreviewLabel.setText(result + "_1");

        }));

    }

    /**
     * Pre-fills input fields when editing an existing Profile.
     */
    private void populateFields(Profile profile) {

        profileNameField.setText(profile.getProfileName());

        if (profile.getSplitBehavior() == SplitBehavior.BARCODE) { radioBARCODE.fire(); }
        else if (profile.getSplitBehavior() == SplitBehavior.MANUAL) { radioMANUAL.fire(); }
        else radioNONE.fire();

        if (profile.getStatus() == ProfileStatus.Active) { radioACTIVE.fire(); }
        else radioINACTIVE.fire();

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

        String profileName = profileNameField.getText();
        Toggle selectedSplitBehaviorToggle = toggleGroupSplitBehavior.getSelectedToggle();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();

        if (profileName.isBlank() || selectedSplitBehaviorToggle == null || selectedStatusToggle == null) {

            if (profileName.isBlank()) {
                profileNameField.getStyleClass().add("error-border");
            }
            if (selectedSplitBehaviorToggle == null) {
                vboxSplitBehavior.getStyleClass().add("error-border");
            }
            if (selectedStatusToggle == null) {
                vboxStatus.getStyleClass().add("error-border");
            }
            return;
        }

        SplitBehavior splitBehavior = (SplitBehavior) selectedSplitBehaviorToggle.getUserData();
        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        try {
            Profile newProfile = new Profile(profileName, splitBehavior, status, exportPreviewLabel.getText().replace("1", ""));
            modelFacade.profileModel.createProfile(newProfile);
            currentStage.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateProfile() {
        String newProfileName = profileNameField.getText();
        Toggle selectedSplitToggle = toggleGroupSplitBehavior.getSelectedToggle();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();
        SplitBehavior newSplitBehavior = (SplitBehavior) selectedSplitToggle.getUserData();
        ProfileStatus newStatus = (ProfileStatus) selectedStatusToggle.getUserData();
        String newExportLabel = exportPreviewLabel.getText().replace("1", "");

        clearError();

        if (newProfileName.isBlank() || selectedSplitToggle == null || selectedStatusToggle == null) {

            if (newProfileName.isBlank()) {
                profileNameField.getStyleClass().add("error-border");
            }
            if (selectedSplitToggle == null) {
                vboxSplitBehavior.getStyleClass().add("error-border");
            }
            if (selectedStatusToggle == null) {
                vboxStatus.getStyleClass().add("error-border");
            }
            return;
        }

        SplitBehavior splitBehavior = (SplitBehavior) selectedSplitToggle.getUserData();
        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        editingProfile.setProfileName(newProfileName);
        editingProfile.setSplitBehavior(newSplitBehavior);
        editingProfile.setStatus(newStatus);
        editingProfile.setExportLabel(newExportLabel);

        try {

            modelFacade.profileModel.updateProfile(editingProfile);
            currentStage.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void clearError() {
        profileNameField.getStyleClass().remove("error-border");
        vboxSplitBehavior.getStyleClass().remove("error-border");
        vboxStatus.getStyleClass().remove("error-border");
    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }
}
