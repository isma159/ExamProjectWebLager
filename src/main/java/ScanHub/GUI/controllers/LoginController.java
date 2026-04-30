package ScanHub.GUI.controllers;

import ScanHub.BE.User;
import ScanHub.BLL.interfaces.IPasswordEncrypter;
import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.TextFieldListeners;
import ScanHub.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtFldUser, txtFldPass;
    @FXML private PasswordField passFldPass;
    @FXML private Button signInBtn;
    @FXML private HBox hboxUser, hboxPass1, hboxPass2;

    private ModelFacade modelFacade;
    private IPasswordEncrypter encrypter = new PasswordEncrypter();
    private Stage currentStage;

    public void setModel (ModelFacade modelFacade, Stage stage) {
        this.modelFacade = modelFacade;
        this.currentStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        List<HBox> containers = new ArrayList<>();
        containers.add(hboxUser);
        containers.add(hboxPass1);
        containers.add(hboxPass2);

        passFldPass.textProperty().bindBidirectional(txtFldPass.textProperty());

        hboxPass1.setVisible(true);
        hboxPass1.setManaged(true);
        hboxPass2.setVisible(false);
        hboxPass2.setManaged(false);

        hboxPass1.setPrefWidth(300);
        hboxPass2.setPrefWidth(300);

        TextFieldListeners.addFocusListener(txtFldUser, hboxUser);
        TextFieldListeners.addFocusListener(passFldPass, hboxPass1);
        TextFieldListeners.addFocusListener(txtFldPass, hboxPass2);

        TextFieldListeners.addErrorListener(txtFldUser, containers);
        TextFieldListeners.addErrorListener(passFldPass, containers);
        TextFieldListeners.addErrorListener(txtFldPass, containers);

        EventHandler<ActionEvent> signIn = event -> {
            try {
                onSignInBtnClick();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
        txtFldUser.setOnAction(signIn);
        txtFldPass.setOnAction(signIn);
        passFldPass.setOnAction(signIn);
    }

    // Admin            Username: admin     Password: admin123
    // User             Username: user      Password: user123
    @FXML
    private void onSignInBtnClick() throws Exception {
        if (txtFldUser.getText().isBlank() || passFldPass.getText().isBlank()) {
            onLoginError();
            AlertHelper.showWarning("Missing Fields", "Please enter your username and password.");
            return;
        }

        User user = modelFacade.userModel.getUserFromUsername(txtFldUser.getText().strip());

        if (user == null || !encrypter.verifyPassword(passFldPass.getText(), user.getPasswordHash())) {
            onLoginError();
            AlertHelper.showError("Login Failed", "Incorrect username or password.");
            return;
        }

        try {
            String view = user.isAdmin() ? "/views/AdminView.fxml" : "/views/UserView.fxml";
            String title = user.isAdmin() ? "Admin Panel" : "User Panel";

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(view));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = new Stage();
            stage.setMinWidth(1200);
            stage.setMinHeight(600);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);

            if (user.isAdmin()) {
                AdminController adminController = fxmlLoader.getController();
                adminController.setModel(modelFacade, stage);
            }
            else {
                UserController userController = fxmlLoader.getController();
                userController.setModel(modelFacade, stage);
            }

            stage.show();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Login Error", "Failed to open panel. Please try again.");
        }
    }

    private void onLoginError() {
        if(!hboxUser.getStyleClass().contains("error-border")) {
            hboxUser.getStyleClass().add("error-border");
        }
        if(!hboxPass1.getStyleClass().contains("error-border")) {
            hboxPass1.getStyleClass().add("error-border");
        }
        if(!hboxPass2.getStyleClass().contains("error-border")) {
            hboxPass2.getStyleClass().add("error-border");
        }
    }

    @FXML
    private void onShowBtnClick() {
        hboxPass1.setVisible(false);
        hboxPass1.setManaged(false);
        hboxPass2.setVisible(true);
        hboxPass2.setManaged(true);
    }

    @FXML
    private void onHideBtnClick() {
        hboxPass1.setVisible(true);
        hboxPass1.setManaged(true);
        hboxPass2.setVisible(false);
        hboxPass2.setManaged(false);
    }
}
