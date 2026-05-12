package ScanHub.GUI.controllers;

import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.enums.SplitBehavior;
import ScanHub.GUI.util.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProfileFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupSplitBehavior, toggleGroupProfileStatus;
    @FXML private Label formTitle, profileIdLabel, nameError, exportPreviewLabel, usersError;
    @FXML private Label lblBrightnessValue, lblContrastValue;
    @FXML private RadioButton radioBARCODE, radioNONE, radioACTIVE, radioINACTIVE;
    @FXML private VBox userCheckboxList, vboxSplitBehavior, vboxStatus, vboxUsers;
    @FXML private TextField profileNameField;
    @FXML private SearchableComboBox<Client> searchableComboBoxClient;
    @FXML private Slider sliderBrightness, sliderContrast;
    @FXML private Button saveButton;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private Profile editingProfile = null;
    private List<User> selectedUsers;

    public void setModel(Stage currentStage, ModelFacade modelFacade, Profile profile) {
        this.currentStage = currentStage;
        this.modelFacade = modelFacade;
        this.editingProfile = profile;

        ThemeManager.apply(currentStage.getScene());
        loadClients();

        if (editingProfile != null) {
            formTitle.setText("Edit Profile");
            saveButton.setText("Save Changes");
            populateFields(editingProfile);
        }

        loadUsers();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioBARCODE.setUserData(SplitBehavior.BARCODE);
        radioNONE.setUserData(SplitBehavior.NONE);

        radioACTIVE.setUserData(ProfileStatus.ACTIVE);
        radioINACTIVE.setUserData(ProfileStatus.INACTIVE);

        selectedUsers = new ArrayList<>();

        profileNameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            exportPreviewLabel.setText(buildExportLabel(newValue) + "1");
        }));

        // Wire sliders to their value labels
        sliderBrightness.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblBrightnessValue.setText(String.valueOf(newVal.intValue()));
        });

        sliderContrast.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblContrastValue.setText(String.valueOf(newVal.intValue()));
        });
    }

    private void loadClients() {
        try {
            searchableComboBoxClient.setItems(modelFacade.getClientModel().getClients());

            if (editingProfile != null) {
                for (Client client : searchableComboBoxClient.getItems()) {
                    if (client.getClientId() == editingProfile.getClientId()) {
                        searchableComboBoxClient.getSelectionModel().select(client);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            AlertHelper.showError("Error", "Could not load clients");
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            selectedUsers.clear();
            vboxUsers.getChildren().clear();
            List<User> users = modelFacade.getUserModel().getUsers();

            for (User user : users) {
                vboxUsers.getChildren().add(RowMaker.addUserRowToForm(user, editingProfile, (selectedUser, isChecked) -> {
                    if (isChecked && !selectedUsers.contains(selectedUser)) {
                        selectedUsers.add(selectedUser);
                    } else if (!isChecked) {
                        selectedUsers.remove(selectedUser);
                    }
                }));
            }
        } catch (Exception e) {
            AlertHelper.showError("Error", "Could not load users");
            e.printStackTrace();
        }
    }

    private void populateFields(Profile profile) {
        profileNameField.setText(profile.getProfileName());

        if (profile.getSplitBehavior() == SplitBehavior.BARCODE) { toggleGroupSplitBehavior.selectToggle(radioBARCODE); }
        else toggleGroupSplitBehavior.selectToggle(radioNONE);

        if (profile.getStatus() == ProfileStatus.ACTIVE) { toggleGroupProfileStatus.selectToggle(radioACTIVE); }
        else toggleGroupProfileStatus.selectToggle(radioINACTIVE);

        // Populate slider values from existing profile
        sliderBrightness.setValue(profile.getBrightness());
        sliderContrast.setValue(profile.getContrast());
        lblBrightnessValue.setText(String.valueOf(profile.getBrightness()));
        lblContrastValue.setText(String.valueOf(profile.getContrast()));
    }

    @FXML
    private void onClickSave(ActionEvent actionEvent) {
        if (editingProfile != null) {
            updateProfile();
        } else {
            createProfile();
        }

        try {
            modelFacade.getUserModel().refreshUsers();
            modelFacade.getProfileModel().refreshProfiles();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Could not save changes. Please try again.");
        }
    }

    private void createProfile() {
        String profileName = profileNameField.getText();
        Client selectedClient = searchableComboBoxClient.getValue();
        Toggle selectedSplitBehaviorToggle = toggleGroupSplitBehavior.getSelectedToggle();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();

        clearError();

        if (profileName.isBlank() || selectedClient == null || selectedSplitBehaviorToggle == null || selectedStatusToggle == null) {
            if (profileName.isBlank()) profileNameField.getStyleClass().add("error-border");
            if (selectedClient == null) searchableComboBoxClient.getStyleClass().add("error-border");
            if (selectedSplitBehaviorToggle == null) vboxSplitBehavior.getStyleClass().add("error-border");
            if (selectedStatusToggle == null) vboxStatus.getStyleClass().add("error-border");
            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields.");
            return;
        }

        SplitBehavior splitBehavior = (SplitBehavior) selectedSplitBehaviorToggle.getUserData();
        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();
        int brightness = (int) sliderBrightness.getValue();
        int contrast = (int) sliderContrast.getValue();

        try {
            Profile newProfile = new Profile(selectedClient.getClientId(), profileName, splitBehavior, status, buildExportLabel(profileName), brightness, contrast);
            newProfile.setClient(selectedClient);
            Profile createdProfile = modelFacade.getProfileModel().createProfile(newProfile);
            modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), createdProfile.getProfileId(), EntityType.PROFILE, LogAction.CREATE, LocalDateTime.now()));
            syncUserAssignments(createdProfile);
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Create Failed", "Failed to create profile. Please try again.");
        }
    }

    private void updateProfile() {
        String newProfileName = profileNameField.getText();
        Client selectedClient = searchableComboBoxClient.getValue();
        Toggle selectedSplitToggle = toggleGroupSplitBehavior.getSelectedToggle();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();
        String newExportLabel = buildExportLabel(newProfileName);

        clearError();

        if (newProfileName.isBlank() || selectedClient == null || selectedSplitToggle == null || selectedStatusToggle == null) {
            if (newProfileName.isBlank()) profileNameField.getStyleClass().add("error-border");
            if (selectedClient == null) searchableComboBoxClient.getStyleClass().add("error-border");
            if (selectedSplitToggle == null) vboxSplitBehavior.getStyleClass().add("error-border");
            if (selectedStatusToggle == null) vboxStatus.getStyleClass().add("error-border");
            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields.");
            return;
        }

        SplitBehavior splitBehavior = (SplitBehavior) selectedSplitToggle.getUserData();
        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        editingProfile.setProfileName(newProfileName);
        editingProfile.setClientId(selectedClient.getClientId());
        editingProfile.setClient(selectedClient);
        editingProfile.setSplitBehavior(splitBehavior);
        editingProfile.setStatus(status);
        editingProfile.setExportLabel(newExportLabel);
        editingProfile.setBrightness((int) sliderBrightness.getValue());
        editingProfile.setContrast((int) sliderContrast.getValue());

        try {
            modelFacade.getProfileModel().updateProfile(editingProfile);
            syncUserAssignments(editingProfile);
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Update Failed", "Failed to update profile. Please try again.");
        }
    }

    private void clearError() {
        profileNameField.getStyleClass().remove("error-border");
        searchableComboBoxClient.getStyleClass().remove("error-border");
        vboxSplitBehavior.getStyleClass().remove("error-border");
        vboxStatus.getStyleClass().remove("error-border");
    }

    private String buildExportLabel(String profileName) {
        return profileName.replace(" ", "") + "_";
    }

    private void syncUserAssignments(Profile profile) throws Exception {
        for (User user : new ArrayList<>(modelFacade.getUserModel().getUsers())) {
            boolean shouldHaveProfile = selectedUsers.contains(user);
            boolean changed = user.getProfiles().removeIf(p -> p.getProfileId() == profile.getProfileId());

            if (shouldHaveProfile) {
                user.getProfiles().add(profile);
                changed = true;
            }

            if (changed) {
                modelFacade.getUserModel().updateUser(user);
            }
        }
    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }
}