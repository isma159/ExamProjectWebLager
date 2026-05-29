package ScanHub.GUI.controllers;

// project imports
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

    private final List<User> currentUsers = new ArrayList<>();
    private boolean userAscending = true;

    private final ModelFacade modelFacade;
    private User selectedUser = null;
    private HBox selectedUserRow = null;
    private Role selectedRole = null;
    private final Stage currentStage;

    private final int TOTAL_TABLE_SIZE = 15;

    public AdminUsersController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterUsers();
        txtFldUserSearch.textProperty().addListener((observable, oldValue, newValue) -> filterUsers());

        pgUsers.currentPageIndexProperty().addListener(((observable, oldValue, newValue) -> filterUsers()));
    }

    private void loadUsers(List<User> users) {
        try {
            selectedUser = null;
            selectedUserRow = null;

            TableLoader.loadTable(userTableBox, pgUsers, TOTAL_TABLE_SIZE, users, item -> {
                User user = (User) item;
                return RowMaker.addUserRow(user, this::selectUser, this::openUserForm, this::deleteUser);
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load users.");
        }
    }

    private void selectUser(User user, HBox rowHBox) {
        if (selectedUserRow != null) {
            selectedUserRow.getStyleClass().remove("row-selected");
        }

        // clicking the already-selected row deselects it
        if (selectedUserRow == rowHBox) {
            selectedUser = null;
            selectedUserRow = null;
            return;
        }

        selectedUser = user;
        selectedUserRow = rowHBox;
        rowHBox.getStyleClass().add("row-selected");
    }

    @FXML
    private void onClickCreateUser() { openUserForm(null); }

    private void openUserForm(User user) {
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

            filterUsers(); // refresh the list after the form closes
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open the user form. Please try again.");
        }
    }

    private void deleteUser(User user) {
        AlertHelper.showConfirmation("Delete User", "Are you sure you want to delete \"" + user.getUsername() + "\"? This action cannot be undone.", () -> {
            try {
                modelFacade.getUserModel().deleteUser(user);
                modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), user.getUserId(), EntityType.USER, LogAction.DELETE, LocalDateTime.now()));
                filterUsers();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Delete Failed", "Failed to delete user. Please try again.");
            }
        });
    }

    private void filterUsers() {

        String search = txtFldUserSearch.getText();

        List<User> users = modelFacade.getUserModel().getUsers();

        users = users.stream().filter(u -> {

            boolean roleMatch = selectedRole == null || selectedRole == u.getRole();
            boolean searchMatch = search.isBlank() || u.getUsername().contains(search);

            return roleMatch && searchMatch;

        }).toList();

        loadUsers(users);

    }

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        return Map.of(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::onClickCreateUser,
                new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), () -> {
                    if (selectedUser == null) { AlertHelper.showWarning("No Selection", "Please select a user to edit."); return; }
                    openUserForm(selectedUser);
                },
                new KeyCodeCombination(KeyCode.DELETE), () -> {
                    if (selectedUser == null) { AlertHelper.showWarning("No Selection", "Please select a user to delete."); return; }
                    deleteUser(selectedUser);
                }
        );
    }

    @FXML
    private void onTbAllUsersClick(ActionEvent actionEvent) {
        selectedRole = null;
        filterUsers();
    }

    @FXML
    private void onTbAdminsClick(ActionEvent actionEvent) {
        selectedRole = Role.ADMIN;
        filterUsers();
    }

    @FXML
    private void onTbUsersClick(ActionEvent actionEvent) {
        selectedRole = Role.USER;
        filterUsers();
    }

    @FXML
    private void onUsernameClick(Event event) {
        // toggle ascending and descending order
        userAscending = !userAscending;
        // sorting the usernames on the direction
        currentUsers.sort(userAscending ? Comparator.comparing(User::getUsername) : Comparator.comparing(User::getUsername).reversed());
        filterUsers();
    }
}