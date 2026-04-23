package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML TableView<User> tblUser;
    @FXML TableView<Profile> tblProfile;

    private ModelFacade modelFacade;

    public void setModel(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
        // tblUser.setItems(modelFacade.getObservableUsers());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    // CREATE & EDIT PROFILE
    @FXML
    private void onClickCreateUser(ActionEvent actionEvent) throws IOException {
        openUserForm(null);
    }

    @FXML
    private void onClickUpdateUser(ActionEvent actionEvent) throws IOException {
        User selectedUser = tblUser.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        openUserForm(selectedUser);
    }

    /**
     * Opens the UserFormView. If user is null, opens in create mode.
     * If user is provided, opens in edit mode with fields pre-filled.
     */
    private void openUserForm(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserFormView.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();

        UserFormController controller = loader.getController();
        controller.setModel(modelFacade, user); // user is null for create, non-null for edit

        if (user == null) {
            stage.setTitle("Create User");
        } else {
            stage.setTitle("Edit User");
        }
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // CREATE & UPDATE PROFILE
    @FXML
    private void onClickCreateProfile(ActionEvent actionEvent) throws IOException {
        openProfileForm(null);
    }

    @FXML
    private void onClickUpdateProfile(ActionEvent actionEvent) throws IOException {
        Profile selectedProfile = tblProfile.getSelectionModel().getSelectedItem();
        if (selectedProfile == null) return;
        openProfileForm(selectedProfile);
    }

    /**
     * Opens the ProfileFormView. If profile is null, opens in create mode.
     * If profile is provided, opens in edit mode with fields pre-filled.
     */
    private void openProfileForm(Profile profile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProfileFormView.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();

        ProfileFormController controller = loader.getController();
        controller.setModel(modelFacade, profile); // profile is null for create, non-null for edit

        if (profile == null) {
            stage.setTitle("Create Profile");
        } else {
            stage.setTitle("Edit Profile");
        }
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}