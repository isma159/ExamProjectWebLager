package ScanHub.GUI.controllers;

// project imports
import ScanHub.BE.Client;
import ScanHub.BE.Log;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.Role;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IShortcutHandler;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import ScanHub.GUI.util.TableLoader;
import ScanHub.GUI.util.ViewHandler;
import javafx.event.ActionEvent;

// java imports
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class AdminUsersController implements Initializable, IShortcutHandler {

    @FXML private VBox userTableBox;
    @FXML private TextField txtFldUserSearch;
    @FXML private Pagination pgUsers;
    private List<User> currentUsers = new ArrayList<>();
    private boolean userAscending = true;

    private final ModelFacade modelFacade;
    private User selectedUser = null;
    private HBox selectedUserRow = null;
    private Role selectedRole = null;
    private Stage currentStage;

    private final int TOTAL_TABLE_SIZE = 15;

    public AdminUsersController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUsers();
        txtFldUserSearch.textProperty().addListener((observable, oldValue, newValue) -> filterUsers(newValue));

        pgUsers.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> loadUsers()));
    }

    private void loadUsers() {
        try {
            selectedUser = null;
            selectedUserRow = null;

            List<User> users = modelFacade.getUserModel().getUsers();
            TableLoader.loadTable(userTableBox, pgUsers, TOTAL_TABLE_SIZE, users, item -> {
                User user = (User) item;
                return RowMaker.addUserRow(user, this::selectUser);
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load users.");
        }
    }

    private void selectUser(User user, HBox rowHBox) {
        if (selectedUserRow != null) {
            selectedUserRow.getStyleClass().remove("row-selected");
            selectedUser = null;
            selectedUserRow = null;
            return;
        }

        selectedUser = user;
        selectedUserRow = rowHBox;
        rowHBox.getStyleClass().add("row-selected");
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
            AlertHelper.showError("No Selection", "Please select a user to delete.");
            return;
        }

        AlertHelper.showConfirmation("Delete User", "Are you sure you want to delete \"" + selectedUser.getUsername() + "\"? This action cannot be undone.", () -> {
            try {
                modelFacade.getUserModel().deleteUser(selectedUser);
                modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), selectedUser.getUserId(), EntityType.USER, LogAction.DELETE, LocalDateTime.now()));
                loadUsers();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Delete Failed", "Failed to delete user. Please try again.");
            }
        });
    }

    private void openUserForm(User user) { // TODO
        try {
            ViewHandler handler = user == null ? ViewHandler.CREATE_USER : ViewHandler.EDIT_USER;
            handler.reset();
            handler.preLoad();
            UserFormController controller = handler.getController();
            Stage stage = handler.prepareStage();
            controller.setModel(stage, modelFacade, user);

            stage.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                    event.consume();
                }
            });

            stage.showAndWait();

            loadUsers(); // refresh the list after the form closes
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the user form. Please try again.");
        }
    }

    private void filterUsers(String search) { // TODO: needs rework
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

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        return Map.of(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::onClickCreateUser,
                new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), this::onClickUpdateUser,
                new KeyCodeCombination(KeyCode.DELETE), () -> onClickDeleteUser(null)
        );
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
        userAscending = !userAscending;
        // sorting the usernames on the direction
        currentUsers.sort(userAscending ? Comparator.comparing(User::getUsername) : Comparator.comparing(User::getUsername).reversed());
        userTableBox.getChildren().clear();

        for (User user : currentUsers) {
            HBox row = RowMaker.addUserRow(user, (clickedUser, rowHBox) -> {
                if (selectedUser != null) selectedUserRow.getStyleClass().remove("row-selected");
                if (selectedUser == clickedUser) { selectedUser = null; selectedUserRow = null; return;}
                selectedUser = clickedUser;
                selectedUserRow = rowHBox;
                rowHBox.getStyleClass().add("row-selected");
            });
            row.setFocusTraversable(true);
            row.focusedProperty().addListener((observable, oldValue, isFocused) -> {
                if (isFocused) {
                    selectUser(user, row);
                }
            });
            row.setUserData(user);
            userTableBox.getChildren().add(row);
        }
        filterUsers(txtFldUserSearch.getText());
    }
}