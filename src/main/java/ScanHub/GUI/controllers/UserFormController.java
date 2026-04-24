package ScanHub.GUI.controllers;

import ScanHub.BE.Role;
import ScanHub.BE.User;
import ScanHub.BLL.interfaces.IPasswordEncrypter;
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {

    @FXML private Label formTitle, userIdLabel, usernameError, passwordHint, passwordError, confirmError;
    @FXML private RadioButton radioADMIN, radioUSER;
    @FXML private VBox profileCheckboxList;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;

    private Stage currentStage;
    private ModelFacade modelFacade;
    private User editingUser = null; // null means create mode, non-null means edit mode
    private IPasswordEncrypter encrypter = new PasswordEncrypter();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
            populateFields(editingUser);
        }
    }

    /**
     * Pre-fills input fields when editing an existing user.
     */
    private void populateFields(User user) { // TODO populate profiles

        usernameField.setText(user.getUsername());

        if (user.getRole() == Role.ADMIN) { radioADMIN.fire(); }
        else { radioUSER.fire(); }

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

        String username = usernameField.getText();
        String password = encrypter.hashedPassword(passwordField.getText());
        String passwordConfirm = encrypter.hashedPassword(confirmPasswordField.getText());


    }

    private void updateUser() {

    }

    @FXML
    private void onClickCancel(ActionEvent actionEvent) {
        currentStage.close();
    }
}
