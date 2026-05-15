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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProfileFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupProfileStatus;
    @FXML private Label formTitle, profileIdLabel, nameError, exportPreviewLabel, usersError;
    @FXML private Label lblHueValue, lblBrightnessValue, lblContrastValue, lblSaturationValue;
    @FXML private RadioButton radioACTIVE, radioINACTIVE;
    @FXML private VBox vboxStatus;
    @FXML private TextField profileNameField;
    @FXML private SearchableComboBox<Client> searchableComboBoxClient;
    @FXML private Slider sliderHue, sliderBrightness, sliderContrast, sliderSaturation;
    @FXML private Button saveButton;
    @FXML private ImageView imgPreview;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private Profile editingProfile = null;
    private List<User> selectedUsers;

    private double hue;
    private double brightness;
    private double contrast;
    private double saturation;

    public void setModel(Stage currentStage, ModelFacade modelFacade, Profile profile) {
        this.currentStage = currentStage;
        this.modelFacade = modelFacade;
        this.editingProfile = profile;

        ThemeManager.apply(currentStage.getScene());

        if (editingProfile != null) {
            formTitle.setText("Edit Profile");
            saveButton.setText("Save Changes");
            populateFields(editingProfile);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        radioACTIVE.setUserData(ProfileStatus.ACTIVE);
        radioINACTIVE.setUserData(ProfileStatus.INACTIVE);

        selectedUsers = new ArrayList<>();

        profileNameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            exportPreviewLabel.setText(buildExportLabel(newValue) + "1");
        }));

        // Wire sliders to their value labels
        sliderHue.valueProperty().addListener((obs, oldVal, newVal) -> {
            hue = newVal.intValue();
            lblHueValue.setText(String.valueOf(hue));
            imgPreview.setEffect(new ColorAdjust(hue / 100, saturation / 100, contrast / 100, brightness / 100));
        });

        sliderBrightness.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightness = newVal.intValue();
            lblBrightnessValue.setText(String.valueOf(brightness));
            imgPreview.setEffect(new ColorAdjust(hue / 100, saturation / 100, contrast / 100, brightness / 100));
        });

        sliderContrast.valueProperty().addListener((obs, oldVal, newVal) -> {
            contrast = newVal.intValue();
            lblContrastValue.setText(String.valueOf(contrast));
            imgPreview.setEffect(new ColorAdjust(hue / 100, saturation / 100, contrast / 100, brightness / 100));
        });

        sliderSaturation.valueProperty().addListener((obs, oldVal, newVal) -> {
            saturation = newVal.intValue();
            lblSaturationValue.setText(String.valueOf(saturation));
            imgPreview.setEffect(new ColorAdjust(hue / 100, saturation / 100, contrast / 100, brightness / 100));
        });
    }

    private void populateFields(Profile profile) {
        profileNameField.setText(profile.getProfileName());

        if (profile.getStatus() == ProfileStatus.ACTIVE) { toggleGroupProfileStatus.selectToggle(radioACTIVE); }
        else toggleGroupProfileStatus.selectToggle(radioINACTIVE);

        // Populate slider values from existing profile
        sliderHue.setValue(profile.getFileSettings().getHue());
        sliderBrightness.setValue(profile.getFileSettings().getBrightness());
        sliderContrast.setValue(profile.getFileSettings().getContrast());
        sliderSaturation.setValue(profile.getFileSettings().getSaturation());

        lblHueValue.setText(String.valueOf(profile.getFileSettings().getHue()));
        lblBrightnessValue.setText(String.valueOf(profile.getFileSettings().getBrightness()));
        lblContrastValue.setText(String.valueOf(profile.getFileSettings().getContrast()));
        lblContrastValue.setText(String.valueOf(profile.getFileSettings().getSaturation()));
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
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();

        clearError();

        if (profileName.isBlank() || selectedClient == null || selectedStatusToggle == null) {
            if (profileName.isBlank()) profileNameField.getStyleClass().add("error-border");
            if (selectedClient == null) searchableComboBoxClient.getStyleClass().add("error-border");
            if (selectedStatusToggle == null) vboxStatus.getStyleClass().add("error-border");
            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields.");
            return;
        }

        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        double hue = sliderHue.getValue();
        double brightness = sliderBrightness.getValue();
        double contrast = sliderContrast.getValue();
        double saturation = sliderContrast.getValue();

        try {

            Profile newProfile = new Profile(selectedClient, profileName, SplitBehavior.BARCODE, status, buildExportLabel(profileName), new FileSettings(hue, brightness, contrast, saturation));
            Profile createdProfile = modelFacade.getProfileModel().createProfile(newProfile);
            modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), createdProfile.getProfileId(), EntityType.PROFILE, LogAction.CREATE, LocalDateTime.now()));
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Create Failed", "Failed to create profile. Please try again.");
        }
    }

    private void updateProfile() {
        String newProfileName = profileNameField.getText();
        Client selectedClient = searchableComboBoxClient.getValue();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();
        String newExportLabel = buildExportLabel(newProfileName);

        clearError();

        if (newProfileName.isBlank() || selectedClient == null || selectedStatusToggle == null) {
            if (newProfileName.isBlank()) profileNameField.getStyleClass().add("error-border");
            if (selectedClient == null) searchableComboBoxClient.getStyleClass().add("error-border");
            if (selectedStatusToggle == null) vboxStatus.getStyleClass().add("error-border");
            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields.");
            return;
        }

        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        double hue = sliderHue.getValue();
        double brightness = sliderBrightness.getValue();
        double contrast = sliderContrast.getValue();
        double saturation = sliderContrast.getValue();

        editingProfile.setProfileName(newProfileName);
        editingProfile.setClient(selectedClient);
        editingProfile.setSplitBehavior(SplitBehavior.BARCODE);
        editingProfile.setStatus(status);
        editingProfile.setExportLabel(newExportLabel);
        editingProfile.setFileSettings(new FileSettings(hue, brightness, contrast, saturation));

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
        searchableComboBoxClient.getStyleClass().remove("error-border");
        vboxStatus.getStyleClass().remove("error-border");
    }

    private String buildExportLabel(String profileName) {
        return profileName.replace(" ", "") + "_";
    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }
}