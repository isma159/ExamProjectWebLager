package ScanHub.GUI.controllers;

// project imports
import ScanHub.BE.Client;
import ScanHub.BE.Log;
import ScanHub.BE.Profile;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.enums.Role;
import ScanHub.BE.User;
import ScanHub.BE.interfaces.CheckTreeNode;
import ScanHub.GUI.util.ThemeManager;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;

// java imports
import ScanHub.GUI.util.TreeViewInitializer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckTreeView;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UserFormController implements Initializable {

    @FXML private ToggleGroup toggleGroupRole;
    @FXML private Label formTitle, passwordHint;
    @FXML private RadioButton radioADMIN, radioUSER;
    @FXML private VBox vboxRole;
    @FXML private CheckTreeView<CheckTreeNode> clientTreeView;
    @FXML private TextField usernameField, txtFldClientSearch;
    @FXML private Button saveButton;
    @FXML private PasswordField passwordField, confirmPasswordField;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private User editingUser = null; // null means create mode, non-null means edit mode


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioADMIN.setUserData(Role.ADMIN);
        radioUSER.setUserData(Role.USER);
        TreeViewInitializer.initUserFormTreeView(clientTreeView);
        clientTreeView.setFixedCellSize(40);
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
            saveButton.setText("Save Changes");
            populateFields(editingUser);
        }

        ThemeManager.apply(currentStage.getScene());
        applyFilters();
        txtFldClientSearch.textProperty().addListener(((obs, oldVal, newVal) -> applyFilters()));
    }

    private void loadClientsAndProfiles(List<Client> clients) {
        clientTreeView.setRoot(null);

        CheckBoxTreeItem<CheckTreeNode> root = new CheckBoxTreeItem<>();
        clientTreeView.setRoot(root);
        root.setExpanded(true);

        // used to check if the profile we're looping through has a matching id with one of the assigned profiles in the user being edited. set has O(1) time complexity :)
        Set<Integer> assignedProfileIds = (editingUser != null) ? editingUser.getProfiles().stream().map(
                Profile::getProfileId).collect(Collectors.toSet()) : Collections.emptySet();

        for (Client client: clients) {
            CheckBoxTreeItem<CheckTreeNode> clientItem = new CheckBoxTreeItem<>(client);
            for (Profile profile: client.getProfiles()) {

                boolean isAssigned = assignedProfileIds.contains(profile.getProfileId());
                if (profile.getStatus() == ProfileStatus.INACTIVE && !isAssigned) {
                    continue;
                }

                CheckBoxTreeItem<CheckTreeNode> profileItem = new CheckBoxTreeItem<>(profile);

                clientItem.getChildren().add(profileItem);

                profileItem.setSelected(isAssigned);
            }
            root.getChildren().add(clientItem);
        }
    }

    /**
     * Pre-fills input fields when editing an existing user.
     */
    private void populateFields(User user) {
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
        } else {
            createUser();
        }

        try {
            modelFacade.getUserModel().refreshUsers();
            modelFacade.getProfileModel().refreshProfiles();
        }
        catch (Exception e) {
            AlertHelper.showError("Refreshing users", "Could not refresh list of users. Please try saving again.");
        }
    }

    /**
     * TODO compare usernames so no identical
     * TODO minimum username/password length ?
     */
    private void createUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String passwordConfirm = confirmPasswordField.getText();
        Toggle selectedToggle = toggleGroupRole.getSelectedToggle();

        clearError(); // prevents stacking of error borders

        if (username.isBlank() || password.isBlank() || passwordConfirm.isBlank() || selectedToggle == null) {
            if (username.isBlank())
                usernameField.getStyleClass().add("error-border");
            if (password.isBlank())
                passwordField.getStyleClass().add("error-border");
            if (passwordConfirm.isBlank())
                confirmPasswordField.getStyleClass().add("error-border");
            if (selectedToggle == null)
                vboxRole.getStyleClass().add("error-border");

            AlertHelper.showWarning("Missing Fields", "Please fill in all required fields and select a role.");
            return;
        }

        if (!password.equals(passwordConfirm)) {
            passwordField.getStyleClass().add("error-border");
            confirmPasswordField.getStyleClass().add("error-border");
            AlertHelper.showWarning("Password Mismatch", "Passwords do not match.");
            return;
        }

        String hashedPassword = modelFacade.getEncrypter().hashedPassword(password);
        Role role = (Role) selectedToggle.getUserData();

        try {
            User newUser = new User(username, hashedPassword, role);
            newUser.setProfiles(retrieveSelectedProfiles());
            modelFacade.getUserModel().createUser(newUser);
            modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), newUser.getUserId(), EntityType.USER, LogAction.CREATE, LocalDateTime.now()));
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Create Failed", "Failed to create user. Please try again.");
        }
    }

    /**
     * TODO find out if a user should be be able to be updated to admin and vice versa
     */
    private void updateUser() {
        String newUsername = usernameField.getText();
        String newPassword = passwordField.getText();
        String newPasswordConfirm = confirmPasswordField.getText();
        Toggle selectedToggle = toggleGroupRole.getSelectedToggle();

        clearError(); // prevents stacking of error borders

        if (newUsername.isBlank() || selectedToggle == null) {
            if (newUsername.isBlank())
                usernameField.getStyleClass().add("error-border");
            if (selectedToggle == null)
                vboxRole.getStyleClass().add("error-border");

            AlertHelper.showWarning("Missing Fields", "Please fill in username and select a role.");
            return;
        }

        // only validate passwords if the user has entered something
        if (!newPassword.isBlank() || !newPasswordConfirm.isBlank()) {
            // validate passwords
            if (!newPassword.equals(newPasswordConfirm)) {
                passwordField.getStyleClass().add("error-border");
                confirmPasswordField.getStyleClass().add("error-border");
                AlertHelper.showWarning("Password Mismatch", "Passwords do not match. Please try again.");
                return;
            }
        }

        Role newRole = (Role) selectedToggle.getUserData();

        editingUser.setUsername(newUsername);
        editingUser.setRole(newRole);
        editingUser.setProfiles(retrieveSelectedProfiles());

        // only update password if user filled in a new one
        if (!newPassword.isBlank()) {
            editingUser.setPasswordHash(modelFacade.getEncrypter().hashedPassword(newPassword));
        }

        try {
            modelFacade.getUserModel().updateUser(editingUser);
            modelFacade.getLogModel().createLog(new Log(modelFacade.getSessionModel().getCurrentUser(), editingUser.getUserId(), EntityType.USER, LogAction.UPDATE, LocalDateTime.now()));
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Update Failed", "Failed to update user. Please try again.");
        }
    }

    private void applyFilters() {

        List<Client> clients = modelFacade.getClientModel().getClients();
        String search = txtFldClientSearch.getText().toLowerCase();


        List<Client> filtered = clients.stream().filter(client -> {

            if (client.getClientName().toLowerCase().contains(search)) {return true;}

            return client.getProfiles().stream().anyMatch(p -> p.getProfileName().toLowerCase().contains(search));

        }).toList();

        loadClientsAndProfiles(filtered);

    }

    // TODO find out if clients should be saved in UserClients in database. If not then the method below in unnecessary.
    private List<Client> retrieveSelectedClients() {
        List<Client> clients = new ArrayList<>();
        for (TreeItem<CheckTreeNode> item: clientTreeView.getRoot().getChildren()) {
            CheckBoxTreeItem<CheckTreeNode> clientItem = (CheckBoxTreeItem<CheckTreeNode>) item;
            if (clientItem.isSelected() || clientItem.isIndeterminate()) {
                clients.add((Client) clientItem.getValue());
            }
        }
        return clients;
    }

    private List<Profile> retrieveSelectedProfiles() {
        List<Profile> profiles = new ArrayList<>();
        for (TreeItem<CheckTreeNode> item: clientTreeView.getRoot().getChildren()) {
            CheckBoxTreeItem<CheckTreeNode> clientItem = (CheckBoxTreeItem<CheckTreeNode>) item;
            for (TreeItem<CheckTreeNode> item2: item.getChildren()) {
                CheckBoxTreeItem<CheckTreeNode> profileItem = (CheckBoxTreeItem<CheckTreeNode>) item2;
                if (profileItem.isSelected()) {
                    profiles.add((Profile) profileItem.getValue());
                }
            }
        }
        return profiles;
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
