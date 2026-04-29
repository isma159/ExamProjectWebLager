package ScanHub.GUI.controllers;

import ScanHub.BE.Profile;
import ScanHub.BE.Role;
import ScanHub.BE.User;
import ScanHub.BLL.interfaces.IPasswordEncrypter;
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.RowMaker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupRole;
    @FXML private Label formTitle, passwordHint, usernameError, passwordError, confirmError;
    @FXML private RadioButton radioADMIN, radioUSER;
    @FXML private VBox vboxRole, vboxProfiles;
    @FXML private TextField usernameField;
    @FXML private Button saveButton;
    @FXML private PasswordField passwordField, confirmPasswordField;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private User editingUser = null; // null means create mode, non-null means edit mode
    private IPasswordEncrypter encrypter = new PasswordEncrypter();
    private List<Profile> selectedProfiles;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioADMIN.setUserData(Role.ADMIN);
        radioUSER.setUserData(Role.USER);
        selectedProfiles = new ArrayList<>();
    }

    /**
     * Receives the shared model and optionally a user to edit.
     * Called before the window is shown, so it can access FXML fields here.
     * @param modelFacade the shared model instance from AdminController
     * @param user the user to edit, or null if creating a new one
     */
    public void setModel(Stage currentStage, ModelFacade modelFacade, User user) {
        this.currentStage = currentStage;
        this.modelFacade = modelFacade;
        this.editingUser = user;

        if (editingUser != null) {
            formTitle.setText("Edit user");
            passwordHint.setText("Enter a password only to change the current one.");
            saveButton.setText("Save changes");
            populateFields(editingUser);
        }

        loadProfiles();
    }

    private void loadProfiles() {

        List<Profile> profiles = modelFacade.profileModel.getProfiles();

        for (Profile profile: profiles) {

            vboxProfiles.getChildren().add(RowMaker.addProfileRowToForm(profile, editingUser, (selectedProfile, isChecked) -> {
                if (isChecked) {
                    selectedProfiles.add(profile);
                }
                else {
                    selectedProfiles.remove(profile);
                }

                System.out.println(selectedProfiles);
            }));
        }
    }

    /**
     * Pre-fills input fields when editing an existing user.
     */
    private void populateFields(User user) { // TODO populate profiles

        usernameField.setText(user.getUsername());

        if (user.getRole() == Role.ADMIN) {
            toggleGroupRole.selectToggle(radioADMIN);
        }
        else if (user.getRole() == Role.USER) {
            toggleGroupRole.selectToggle(radioUSER);
        }


    }

    @FXML
    private void onClickSave(ActionEvent actionEvent) {
        if (editingUser != null) {
            updateUser();
            // TODO show success
        } else {
            createUser();
            // TODO show success
        }
    }

    private void createUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String passwordConfirm = confirmPasswordField.getText();
        Toggle selectedToggle = toggleGroupRole.getSelectedToggle();

        clearError(); // prevents stacking of error borders

        // TODO compare usernames so no identical
        // TODO ? minimum username/password length ?
        if (username.isBlank() || password.isBlank() || passwordConfirm.isBlank() || selectedToggle == null) {

            if (username.isBlank())
                usernameField.getStyleClass().add("error-border");
            if (password.isBlank())
                passwordField.getStyleClass().add("error-border");
            if (passwordConfirm.isBlank())
                confirmPasswordField.getStyleClass().add("error-border");
            if (selectedToggle == null)
                vboxRole.getStyleClass().add("error-border");

            // TODO add AlertView
            return;
        }

        if (!password.equals(passwordConfirm) || !passwordConfirm.equals(password)) {
            passwordField.getStyleClass().add("error-border");
            confirmPasswordField.getStyleClass().add("error-border");

            // TODO add AlertView
            return;
        }

        String hashedPassword = encrypter.hashedPassword(password);
        Role role = (Role) selectedToggle.getUserData();

        try {
            User newUser = new User(username, hashedPassword, role, selectedProfiles);
            modelFacade.userModel.createUser(newUser);
            currentStage.close();
        } catch (Exception e) {
            // TODO add the AlertView
            throw new RuntimeException(e);
        }
    }

    // TODO find out if a user should be be able to be updated to admin and vice versa
    private void updateUser() {
        String newUsername = usernameField.getText();
        String newPassword = passwordField.getText();
        String newPasswordConfirm = confirmPasswordField.getText();
        Toggle selectedToggle = toggleGroupRole.getSelectedToggle();

        clearError(); // prevents that stacking baby

        if (newUsername.isBlank() || selectedToggle == null) {
            if (newUsername.isBlank())
                usernameField.getStyleClass().add("error-border");
            if (selectedToggle == null)
                vboxRole.getStyleClass().add("error-border");

            // TODO add AlertView
            return;
        }

        if (!newPassword.equals(newPasswordConfirm) || !newPasswordConfirm.equals(newPassword)) {
            passwordField.getStyleClass().add("error-border");
            confirmPasswordField.getStyleClass().add("error-border");

            // TODO add AlertView
            return;
        }

        String newHashedPassword = encrypter.hashedPassword(newPassword);
        Role newRole = (Role) selectedToggle.getUserData();

        editingUser.setUsername(newUsername);
        editingUser.setPasswordHash(newHashedPassword);
        editingUser.setRole(newRole);
        editingUser.setProfiles(selectedProfiles);

        try {
            modelFacade.userModel.updateUser(editingUser);
            currentStage.close();
        } catch (Exception e) {
            // TODO add AlertView
            throw new RuntimeException(e);
        }
    }

    private void clearError() {
        usernameField.getStyleClass().remove("error-border");
        passwordField.getStyleClass().remove("error-border");
        confirmPasswordField.getStyleClass().remove("error-border");
        vboxRole.getStyleClass().remove("error-border");
    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }
}
