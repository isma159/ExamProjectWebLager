package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.GUI.facade.ModelFacade;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminProfilesController {

    @FXML TableView <Profile> tblProfiles;
    private ModelFacade modelFacade;

    public AdminProfilesController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @FXML
    private void onClickCreateProfile() {
        openProfileForm(null);
    }

    @FXML
    private void onClickUpdateProfile() {
        Profile selectedProfile = null; // TODO: add when sat up tblProfiles.getSelectionModel().getSelectedItem();
        if (selectedProfile == null) return;
        openProfileForm(selectedProfile);
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
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}