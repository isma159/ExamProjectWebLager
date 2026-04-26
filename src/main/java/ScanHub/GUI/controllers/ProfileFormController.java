package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.Role;
import ScanHub.BE.SplitBehavior;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupSplitBehavior;
    @FXML private Label formTitle, profileIdLabel, nameError, exportPreviewLabel, usersError;
    @FXML private RadioButton radioBARCODE, radioMANUAL, radioNONE;
    @FXML private VBox userCheckboxList;
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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioBARCODE.setUserData(SplitBehavior.BARCODE);
        radioMANUAL.setUserData(SplitBehavior.MANUAL);
        radioNONE.setUserData(SplitBehavior.NONE);

        if (editingProfile != null) {
            formTitle.setText("Edit profile");
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
        Toggle selectedToggle = toggleGroupSplitBehavior.getSelectedToggle();



        SplitBehavior splitBehavior = (SplitBehavior) selectedToggle.getUserData();

        try {
            Profile newProfile = new Profile(0, profileName, splitBehavior);
            modelFacade.profileModel.createProfile(newProfile);
            currentStage.close();
        } catch (Exception e) {
            // TODO add the AlertView
            throw new RuntimeException(e);
        }
    }

    private void updateProfile() {

    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }
}
