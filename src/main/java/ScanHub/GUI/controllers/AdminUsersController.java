package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminUsersController implements Initializable {

    @FXML TableView<User> tblUsers;

    private ModelFacade modelFacade;

    public AdminUsersController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void onClickCreateUser() {
        openUserForm(null);
    }

    @FXML
    private void onClickUpdateUser() {
        User selectedUser = null; // TODO: add when sat up tblUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        openUserForm(selectedUser);
    }

    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserFormView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();

            UserFormController controller = loader.getController();
            controller.setModel(stage, modelFacade, user);

            stage.setTitle(user == null ? "Create User" : "Edit User");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}