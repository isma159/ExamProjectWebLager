package ScanHub.GUI.controllers;

import ScanHub.BE.*;
import ScanHub.BLL.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProfileFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupSplitBehavior, toggleGroupProfileStatus;
    @FXML private Label formTitle, profileIdLabel, nameError, exportPreviewLabel, usersError;
    @FXML private RadioButton radioBARCODE, radioMANUAL, radioNONE, radioACTIVE, radioINACTIVE;
    @FXML private VBox userCheckboxList, vboxSplitBehavior, vboxStatus, vboxUsers;
    @FXML private TextField profileNameField;
    @FXML private Button saveButton;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private Profile editingProfile = null; // null means create mode, non-null means edit mode
    private List<User> selectedUsers;

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
            formTitle.setText("Edit Profile");
            saveButton.setText("Save Changes");
            populateFields(editingProfile);
        }

        ThemeManager.apply(currentStage.getScene());
        loadUsers();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioBARCODE.setUserData(SplitBehavior.BARCODE);
        radioMANUAL.setUserData(SplitBehavior.MANUAL);
        radioNONE.setUserData(SplitBehavior.NONE);

        radioACTIVE.setUserData(ProfileStatus.Active);
        radioINACTIVE.setUserData(ProfileStatus.Inactive);

        selectedUsers = new ArrayList<>();

        if (editingProfile != null) {
            formTitle.setText("Edit profile");
        }

        profileNameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            String result = newValue.replace(" ", "");
            exportPreviewLabel.setText(result + "_1");
        }));
    }

    private void loadUsers() {

        List<User> users = modelFacade.userModel.getUsers();

        for (User user: users) {
            vboxUsers.getChildren().add(RowMaker.addUserRowToForm(user, editingProfile, (selectedUser, isChecked) -> {
                if (isChecked) {
                    selectedUsers.add(user);
                }
                else {
                    selectedUsers.remove(user);
                }

                System.out.println(selectedUser);
            }));
        }
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

        try {
            modelFacade.userModel.refreshModel();
            modelFacade.profileModel.refreshModel();
        }
        catch (Exception e) {
            // TODO AlertView?
        }
    }

    private void createProfile() {
        String profileName = profileNameField.getText();
        Toggle selectedSplitBehaviorToggle = toggleGroupSplitBehavior.getSelectedToggle();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();

        clearError();

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
            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields.");
            return;
        }

        SplitBehavior splitBehavior = (SplitBehavior) selectedSplitBehaviorToggle.getUserData();
        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        try {
            Profile newProfile = new Profile(profileName, splitBehavior, status, exportPreviewLabel.getText().replace("1", ""));
            newProfile.setUsers(selectedUsers);
            modelFacade.getProfileModel().createProfile(newProfile);
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Create Failed", "Failed to create profile. Please try again.");
        }
    }

    private void updateProfile() {
        String newProfileName = profileNameField.getText();
        Toggle selectedSplitToggle = toggleGroupSplitBehavior.getSelectedToggle();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();
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
            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields.");
            return;
        }

        SplitBehavior splitBehavior = (SplitBehavior) selectedSplitToggle.getUserData();
        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        editingProfile.setProfileName(newProfileName);
        editingProfile.setSplitBehavior(splitBehavior);
        editingProfile.setStatus(status);
        editingProfile.setExportLabel(newExportLabel);
        editingProfile.setUsers(selectedUsers);

        try {
            modelFacade.getProfileModel().updateProfile(editingProfile);
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Update Failed", "Failed to update profile. Please try again.");
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
