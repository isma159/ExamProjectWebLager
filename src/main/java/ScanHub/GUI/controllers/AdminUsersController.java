package ScanHub.GUI.controllers;

import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.UserTableRow;
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

public class AdminUsersController implements Initializable {

    @FXML private VBox userTableBox;

    private ModelFacade modelFacade;
    private User selectedUser = null;
    private HBox selectedRow = null;

    public AdminUsersController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUsers();
    }

    private void loadUsers() {
        try {
            // resets
            userTableBox.getChildren().clear();
            selectedUser = null;
            selectedRow = null;

            // sets up with all users by running a for-loop that makes an interactive HBox of every user
            List<User> users = modelFacade.userModel.getUsers();
            for (User user : users) {
                HBox row = UserTableRow.addRow(user, (clickedUser, rowHBox) -> {
                    // clear highlight of previously selected row
                    if (selectedRow != null) {
                        selectedRow.getStyleClass().remove("row-selected");
                    }
                    // remove selected row if clicked on again
                    if (selectedUser == clickedUser) {
                        selectedUser = null;
                        selectedRow = null;
                        return; // will reselect on the next line if not returning
                    }
                    // select clicked row as selected user (with highlight to show)
                    selectedUser = clickedUser;
                    selectedRow = rowHBox;
                    rowHBox.getStyleClass().add("row-selected");
                });
                userTableBox.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClickCreateUser() {
        openUserForm(null);
    }

    @FXML
    private void onClickUpdateUser() {
        if (selectedUser == null) return;
        openUserForm(selectedUser);
    }

    @FXML
    private void onClickDeleteUser(MouseEvent mouseEvent) {
        if (selectedUser == null) return;
        try {
            modelFacade.userModel.deleteUser(selectedUser);
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
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