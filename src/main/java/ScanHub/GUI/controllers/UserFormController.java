package ScanHub.GUI.controllers;

import ScanHub.BE.Role;
import ScanHub.BE.User;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserFormController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Button saveButton, cancelButton;

    private ModelFacade modelFacade;
    private User editingUser = null; // null means create mode, non-null means edit mode

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(Role.values());
    }

    /**
     * Receives the shared model and optionally a user to edit.
     * Called before the window is shown, so it can access FXML fields here.
     * @param modelFacade the shared model instance from AdminController
     * @param user the user to edit, or null if creating a new one
     */
    public void setModel(ModelFacade modelFacade, User user) {
        this.modelFacade = modelFacade;
        this.editingUser = user;

        if (editingUser != null) {
            populateFields(editingUser);
        }
    }

    /**
     * Pre-fills input fields when editing an existing user.
     */
    private void populateFields(User user) {

    }

    @FXML
    private void onClickSave(ActionEvent actionEvent) {
        if (editingUser != null) {
            updateUser();
        } else {
            createUser();
        }
    }

    private void createUser() {

    }

    private void updateUser() {

    }

    public void handleCancel(ActionEvent actionEvent) {
    }

    public void handleSave(ActionEvent actionEvent) {
    }
}