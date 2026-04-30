package ScanHub.GUI.controllers;

import ScanHub.BE.Role;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AdminUsersController implements Initializable {

    @FXML private VBox userTableBox;
    @FXML private TextField txtFldUserSearch;
    private List<User> currentUsers = new ArrayList<>();
    private boolean ascending = true;

    private ModelFacade modelFacade;
    private User selectedUser = null;
    private HBox selectedRow = null;
    private Role selectedRole = null;

    public AdminUsersController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUsers();
        txtFldUserSearch.textProperty().addListener((observable, oldValue, newValue) -> filterUsers(newValue));
    }

    private void loadUsers() {
        try {
            // resets
            userTableBox.getChildren().clear();
            selectedUser = null;
            selectedRow = null;

            // sets up with all users by running a for-loop that makes an interactive HBox of every user
            List<User> users = modelFacade.userModel.getUsers();
            currentUsers = new ArrayList<>(users);
            for (User user : currentUsers) {
                HBox row = RowMaker.addUserRow(user, (clickedUser, rowHBox) -> {
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
                row.setUserData(user);
                userTableBox.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load users.");
        }
    }

    @FXML
    private void onClickCreateUser() {
        openUserForm(null);
    }

    @FXML
    private void onClickUpdateUser() {
        if (selectedUser == null) {
            AlertHelper.showWarning("No Selection", "Please select a user to edit.");
            return;
        }
        openUserForm(selectedUser);
    }

    @FXML
    private void onClickDeleteUser(MouseEvent mouseEvent) {
        if (selectedUser == null) {
            AlertHelper.showWarning("No Selection", "Please select a user to delete.");
            return;
        }

        AlertHelper.showConfirmation(
                "Delete User",
                "Are you sure you want to delete \"" + selectedUser.getUsername() + "\"? This action cannot be undone.",
                () -> {
                    try {
                        modelFacade.userModel.deleteUser(selectedUser);
                        loadUsers();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertHelper.showError("Delete Failed", "Failed to delete user. Please try again.");
                    }
                }
        );
    }

    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserFormView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();

            stage.setTitle(user == null ? "Create User" : "Edit User");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            UserFormController controller = loader.getController();
            controller.setModel(stage, modelFacade, user);

            stage.showAndWait();

            // refresh the list after the form closes
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the user form. Please try again.");
        }
    }

    private void filterUsers(String search) {
        // loops through every row in user table
        for (var node : userTableBox.getChildren()) {
            HBox row = (HBox) node;
            User user = (User) row.getUserData();

            // checks input matching for username and role
            boolean matching = search.isBlank()
            || user.getUsername().toLowerCase().contains(search.toLowerCase());
            // for the togglebuttons filtering
            boolean matchingRole = selectedRole ==  null || user.getRole() == selectedRole;

            // show or hide if matching or not
            row.setVisible(matching && matchingRole);
            row.setManaged(matching && matchingRole);
        }
    }

    @FXML
    private void onTbAllUsersClick(ActionEvent actionEvent) {
        selectedRole = null;
        filterUsers(txtFldUserSearch.getText());
    }

    @FXML
    private void onTbAdminsClick(ActionEvent actionEvent) {
        selectedRole = Role.ADMIN;
        filterUsers(txtFldUserSearch.getText());
    }

    @FXML
    private void onTbUsersClick(ActionEvent actionEvent) {
        selectedRole = Role.USER;
        filterUsers(txtFldUserSearch.getText());
    }

    @FXML
    private void onUsernameClick(Event event) {
        // toggle ascending and descending order
        ascending = !ascending;
        // sorting the usernames on the direction
        currentUsers.sort(ascending ? Comparator.comparing(User::getUsername) : Comparator.comparing(User::getUsername).reversed());
        userTableBox.getChildren().clear();

        for (User user : currentUsers) {
            HBox row = RowMaker.addUserRow(user, (clickedUser, rowHBox) -> {
                if (selectedUser != null) selectedRow.getStyleClass().remove("row-selected");
                if (selectedUser == clickedUser) { selectedUser = null; selectedRow = null; return;}
                selectedUser = clickedUser;
                selectedRow = rowHBox;
                rowHBox.getStyleClass().add("row-selected");
            });
            row.setUserData(user);
            userTableBox.getChildren().add(row);
        }
    }
}
