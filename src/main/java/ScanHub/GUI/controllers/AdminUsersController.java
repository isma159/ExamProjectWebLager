package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminUsersController {

    ModelFacade modelFacade;

    public AdminUsersController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    // CREATE & EDIT PROFILE
    @FXML
    private void onClickCreateUser() {
        openUserForm(null);
    }

    @FXML
    private void onClickUpdateUser() {
        User selectedUser = null; //= tblUser.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        openUserForm(selectedUser);
    }

    /**
     * Opens the ProfileFormView. If profile is null, opens in create mode.
     * If profile is provided, opens in edit mode with fields pre-filled.
     */
    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserFormView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();

            if (user == null) {
                stage.setTitle("Create User");
            } else {
                stage.setTitle("Edit User");
            }

            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
