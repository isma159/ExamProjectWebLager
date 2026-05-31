package ScanHub.GUI.controllers;

import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.interfaces.CheckTreeNode;
import ScanHub.GUI.util.ThemeHandler;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.DoubleConsumer;
import java.util.stream.Collectors;

public class ProfileFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupProfileStatus;
    @FXML private Label formTitle;
    @FXML private Spinner<Integer> spnHue, spnBrightness, spnContrast, spnSaturation, spnRotation;
    @FXML private RadioButton radioACTIVE, radioINACTIVE;
    @FXML private VBox vboxStatus;
    @FXML private TextField profileNameField, txtFldExportPreview, txtFldUserSearch;
    @FXML private SearchableComboBox<Client> searchableComboBoxClient;
    @FXML private Slider sliderHue, sliderBrightness, sliderContrast, sliderSaturation, sliderRotation;
    @FXML private Button saveButton;
    @FXML private StackPane previewPaneBefore, previewPaneAfter;
    @FXML private ImageView imgPreviewBefore, imgPreviewAfter;
    @FXML private CheckTreeView<CheckTreeNode> userTreeView;

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

        ThemeHandler.apply(currentStage.getScene());

        applyUserFilters();
        txtFldUserSearch.textProperty().addListener((obs, oldVal, newVal) -> applyUserFilters());

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

        spnHue.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spnBrightness.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spnContrast.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spnSaturation.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0, 1));
        spnRotation.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-360, 360, 0, 1));

        profileNameField.textProperty().addListener(((observable, oldValue, newValue) -> {

            String clientName = (searchableComboBoxClient.getValue() != null) ? searchableComboBoxClient.getValue().getClientName() : "";

            txtFldExportPreview.setText(buildExportLabel(newValue, clientName) + "1");
        }));

        // Wire sliders to their value labels
        bindSlider(sliderHue, spnHue, val -> hue = val);
        bindSlider(sliderBrightness, spnBrightness, val -> brightness = val);
        bindSlider(sliderContrast, spnContrast, val -> contrast = val);
        bindSlider(sliderSaturation, spnSaturation, val -> saturation = val);
        bindSlider(sliderRotation, spnRotation, val -> rotation = (int) val);
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
        editingProfile.setFileAdjustmentSettings(buildFileSettings());

        try {
            modelFacade.getProfileModel().updateProfile(editingProfile);
            modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), editingProfile.getProfileId(), EntityType.PROFILE, LogAction.UPDATE, LocalDateTime.now()));
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
        sliderHue.setValue(profile.getFileAdjustmentSettings().getHue());
        sliderBrightness.setValue(profile.getFileAdjustmentSettings().getBrightness());
        sliderContrast.setValue(profile.getFileAdjustmentSettings().getContrast());
        sliderSaturation.setValue(profile.getFileAdjustmentSettings().getSaturation());
        sliderRotation.setValue(profile.getFileAdjustmentSettings().getRotation());
    }

    private void bindSlider(Slider slider, Spinner<Integer> spinner, DoubleConsumer setter) {
        slider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            setter.accept(newValue.doubleValue());
            spinner.getValueFactory().setValue(newValue.intValue());
            updatePreview();
        }));
        spinner.getEditor().textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                slider.setValue(Integer.parseInt(newValue));
            }
            catch (NumberFormatException e) {
                // wrong input format, ignoring :)
            }
        }));
    }

    private void loadUsers(List<User> users) {
        userTreeView.setRoot(null);

        CheckBoxTreeItem<CheckTreeNode> root = new CheckBoxTreeItem<>();
        userTreeView.setRoot(root);
        root.setExpanded(true);

        Set<Integer> assignedUserIds = (editingProfile != null)
                ? editingProfile.getUsers().stream().map(User::getUserId).collect(Collectors.toSet())
                : Collections.emptySet();

        for (User user : users) {
            if (user.isAdmin()) continue;

            CheckBoxTreeItem<CheckTreeNode> userItem = new CheckBoxTreeItem<>(user);
            userItem.setSelected(assignedUserIds.contains(user.getUserId()));
            root.getChildren().add(userItem);
        }
    }

    private void applyUserFilters() {
        String search = txtFldUserSearch.getText().toLowerCase();

        List<User> filtered = modelFacade.getUserModel().getUsers().stream()
                .filter(u -> !u.isAdmin())
                .filter(u -> u.getUsername().toLowerCase().contains(search))
                .toList();

        loadUsers(filtered);
    }

    private void updatePreview() {
        Image image = imgPreviewAfter.getImage();
        if (image == null || image.isError()) return;

        // fall back to the fixed FXML size (240) when the pane reports 0, which happens when sliders fire during populateFields() before the stage is shown.
        // Otherwise scale becomes Infinity/NaN and the image fills the entire window
        double paneWidth = previewPaneAfter.getWidth();
        double paneHeight = previewPaneAfter.getHeight();
        double maxWidth = (paneWidth > 0 ? paneWidth : 270) - 30;
        double maxHeight = (paneHeight > 0 ? paneHeight : 270) - 30;

        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();

        double radians = Math.toRadians(rotation);
        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));

        double rotatedBoundingWidth = originalWidth * cos + originalHeight * sin;
        double rotatedBoundingHeight = originalWidth * sin + originalHeight * cos;

        double scale = Math.min(maxWidth / rotatedBoundingWidth, maxHeight / rotatedBoundingHeight);

        imgPreviewAfter.setFitWidth(originalWidth * scale);
        imgPreviewAfter.setFitHeight(originalHeight * scale);
        imgPreviewAfter.setRotate(rotation);
        imgPreviewAfter.setEffect(new ColorAdjust(hue / 100, saturation / 100, brightness / 100, contrast / 100));
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

    private FileAdjustmentSettings buildFileSettings() {
        return new FileAdjustmentSettings(rotation, hue, brightness, contrast, saturation, 0);
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