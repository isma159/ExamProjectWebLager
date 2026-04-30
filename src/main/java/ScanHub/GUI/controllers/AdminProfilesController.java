package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminProfilesController implements Initializable {

    @FXML private VBox profileTableBox;
    private ModelFacade modelFacade;
    private Profile selectedProfile;
    private HBox selectedRow;

    public AdminProfilesController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { loadProfiles(); }

    private void loadProfiles() {
        try {
            // resets
            profileTableBox.getChildren().clear();
            selectedProfile = null;
            selectedRow = null;

            // sets up with all profiles by running a for-loop that makes an interactive HBox of every profile
            List<Profile> profiles = modelFacade.profileModel.getProfiles();
            for (Profile profile : profiles) {
                HBox row = RowMaker.addProfileRow(profile, (clickedProfile, rowHBox) -> {
                    // clear highlight of previously selected row
                    if (selectedRow != null) {
                        selectedRow.getStyleClass().remove("row-selected");
                    }
                    // remove selected row if clicked on again
                    if (selectedProfile == clickedProfile) {
                        selectedProfile = null;
                        selectedRow = null;
                        return; // will reselect on the next line if not returning
                    }
                    // select clicked row as selected profile (with highlight to show)
                    selectedProfile = clickedProfile;
                    selectedRow = rowHBox;
                    rowHBox.getStyleClass().add("row-selected");
                });
                profileTableBox.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load profiles.");
        }
    }

    @FXML
    private void onClickCreateProfile() {
        openProfileForm(null);
    }

    @FXML
    private void onClickUpdateProfile() {
        if (selectedProfile == null) {
            AlertHelper.showWarning("No Selection", "Please select a profile to edit.");
            return;
        }
        openProfileForm(selectedProfile);
    }

    @FXML
    private void onClickDeleteProfile(MouseEvent mouseEvent) {
        if (selectedProfile == null) {
            AlertHelper.showWarning("No Selection", "Please select a profile to delete.");
            return;
        }

        AlertHelper.showConfirmation(
                "Delete Profile",
                "Are you sure you want to delete the profile \"" + selectedProfile.getProfileName() + "\"? This action cannot be undone.",
                () -> {
                    try {
                        modelFacade.profileModel.deleteProfile(selectedProfile);
                        loadProfiles();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertHelper.showError("Delete Failed", "Failed to delete profile. Please try again.");
                    }
                }
        );
    }

    private void openProfileForm(Profile profile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProfileFormView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();

            ProfileFormController controller = loader.getController();
            controller.setModel(stage, modelFacade, profile);

            stage.setTitle(profile == null ? "Create Profile" : "Edit Profile");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh the list after the form closes
            loadProfiles();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the profile form. Please try again.");
        }
    }
}
