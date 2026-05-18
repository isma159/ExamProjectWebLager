package ScanHub.GUI.controllers;

import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.GUI.util.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
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
import java.util.ResourceBundle;
import java.util.function.DoubleConsumer;

public class ProfileFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupProfileStatus;
    @FXML private Label formTitle, exportPreviewLabel;
    @FXML private Label lblHueValue, lblBrightnessValue, lblContrastValue, lblSaturationValue, lblRotationValue;
    @FXML private RadioButton radioACTIVE, radioINACTIVE;
    @FXML private VBox vboxStatus;
    @FXML private TextField profileNameField;
    @FXML private SearchableComboBox<Client> searchableComboBoxClient;
    @FXML private Slider sliderHue, sliderBrightness, sliderContrast, sliderSaturation, sliderRotation;
    @FXML private Button saveButton;
    @FXML private ImageView imgPreview;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private Profile editingProfile = null;

    private double hue;
    private double brightness;
    private double contrast;
    private double saturation;
    private int rotation;

    public void setModel(Stage currentStage, ModelFacade modelFacade, Profile profile) {
        this.currentStage = currentStage;
        this.modelFacade = modelFacade;
        this.editingProfile = profile;

        ThemeManager.apply(currentStage.getScene());

        searchableComboBoxClient.setItems(modelFacade.getClientModel().getClients());

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

        profileNameField.textProperty().addListener(((observable, oldValue, newValue) -> {

            String clientName = (searchableComboBoxClient.getValue() != null) ? searchableComboBoxClient.getValue().getClientName() : "";

            exportPreviewLabel.setText(buildExportLabel(newValue, clientName) + "1");
        }));

        // Wire sliders to their value labels
        bindSlider(sliderHue, lblHueValue, val -> hue = val);
        bindSlider(sliderBrightness, lblBrightnessValue, val -> brightness = val);
        bindSlider(sliderContrast, lblContrastValue, val -> contrast = val);
        bindSlider(sliderSaturation, lblSaturationValue, val -> saturation = val);
        bindSlider(sliderRotation, lblRotationValue, val -> rotation = (int) val);
    }

    // event handlers

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
            modelFacade.getClientModel().refreshClients();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Could not save changes. Please try again.");
        }
    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }

    // helper methods

    private void createProfile() {
        String profileName = profileNameField.getText();
        Client selectedClient = searchableComboBoxClient.getValue();
        Toggle selectedStatusToggle = toggleGroupProfileStatus.getSelectedToggle();

        if (!validateFields(profileName, selectedClient, selectedStatusToggle)) {return;}

        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        try {

            Profile newProfile = new Profile(selectedClient, profileName, status, buildExportLabel(profileName, selectedClient.getClientName()), buildFileSettings());
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

        if (!validateFields(newProfileName, selectedClient, selectedStatusToggle)) {return;}

        String newExportLabel = buildExportLabel(newProfileName, selectedClient.getClientName());
        ProfileStatus status = (ProfileStatus) selectedStatusToggle.getUserData();

        editingProfile.setProfileName(newProfileName);
        editingProfile.setClient(selectedClient);
        editingProfile.setStatus(status);
        editingProfile.setExportLabel(newExportLabel);
        editingProfile.setFileSettings(buildFileSettings());

        try {
            modelFacade.getProfileModel().updateProfile(editingProfile);
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Update Failed", "Failed to update profile. Please try again.");
        }
    }

    private void populateFields(Profile profile) {
        profileNameField.setText(profile.getProfileName());

        if (profile.getStatus() == ProfileStatus.ACTIVE) { toggleGroupProfileStatus.selectToggle(radioACTIVE); }
        else toggleGroupProfileStatus.selectToggle(radioINACTIVE);

        searchableComboBoxClient.getSelectionModel().select(profile.getClient());

        // Populate slider values from existing profile
        sliderHue.setValue(profile.getFileSettings().getHue());
        sliderBrightness.setValue(profile.getFileSettings().getBrightness());
        sliderContrast.setValue(profile.getFileSettings().getContrast());
        sliderSaturation.setValue(profile.getFileSettings().getSaturation());
        sliderRotation.setValue(profile.getFileSettings().getRotation());
    }

    private void bindSlider(Slider slider, Label label, DoubleConsumer setter) {
        slider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            setter.accept(newValue.doubleValue());
            label.setText(String.valueOf(newValue.intValue()));
            updatePreview();
        }));
    }

    private void updatePreview() {
        imgPreview.setEffect(new ColorAdjust(hue / 100, saturation / 100, brightness / 100, contrast / 100));
        imgPreview.setRotate(rotation);
    }

    private boolean validateFields(String profile, Client client, Toggle statusToggle) {
        clearError();

        if (profile.isBlank()) profileNameField.getStyleClass().add("error-border");
        if (client == null) searchableComboBoxClient.getStyleClass().add("error-border");
        if (statusToggle == null) vboxStatus.getStyleClass().add("error-border");

        if (profile.isBlank() || client == null || statusToggle == null) {
            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields.");
            return false;
        }

        return true;
    }

    private void clearError() {
        profileNameField.getStyleClass().remove("error-border");
        searchableComboBoxClient.getStyleClass().remove("error-border");
        vboxStatus.getStyleClass().remove("error-border");
    }

    private FileSettings buildFileSettings() {
        return new FileSettings(
                rotation,
                hue,
                brightness,
                contrast,
                saturation
        );
    }

    private String buildExportLabel(String profileName, String clientName) {
        if (clientName != null) {
            return clientName.replace(" ", "") + "_" + profileName.replace(" ", "") + "_";
        }
        else {
            return profileName.replace(" ", "") + "_";
        }
    }
}