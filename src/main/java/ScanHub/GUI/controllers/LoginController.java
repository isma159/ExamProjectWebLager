package ScanHub.GUI.controllers;

import ScanHub.BLL.util.PasswordEncrypter;
import ScanHub.DAL.interfaces.IPasswordEncrypter;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.Main;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtFldUser, txtFldPass;
    @FXML private PasswordField passFldPass;
    @FXML private Label lblSignIn;
    @FXML private ImageView showImg;

    private Stage currentStage;
    private boolean isHidden = true;
    private IPasswordEncrypter encrypter = new PasswordEncrypter();
    private ModelFacade facade;

    public LoginController() {
        try {
            facade = new ModelFacade();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {

        this.currentStage = stage;

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        txtFldUser.textProperty().addListener((obs, oldVal, newVal) -> resetLabel());
        passFldPass.textProperty().addListener((obs, oldVal, newVal) -> resetLabel());

        passFldPass.textProperty().bindBidirectional(txtFldPass.textProperty());

        passFldPass.setPrefWidth(350);
        txtFldPass.setPrefWidth(350);

        passFldPass.setManaged(true);
        passFldPass.setVisible(true);

        txtFldPass.setManaged(false);
        txtFldPass.setVisible(false);

    }

    // Admin            Username: admin     Password: admin123
    // Coordinator      Username: coord     Password: coord123
    @FXML
    private void onSignInBtnClick() throws Exception {

        if (txtFldUser.getText().isEmpty() || txtFldPass.getText().isEmpty()) {

            ObservableList<String> username = txtFldUser.getStyleClass();
            ObservableList<String> password = txtFldPass.getStyleClass();

            if (!username.contains("errorState")) {
                username.add("errorState");
            }
            if (!password.contains("errorState")) {
                password.add("errorState");
            }

            lblSignIn.setText("Please fill out all fields.");
            lblSignIn.getStyleClass().remove("error-label"); // prevents stacking
            lblSignIn.getStyleClass().add("error-label");
            return;
        }

        /*User user = facade.userModel.getUserFromUsername(txtFldUser.getText().strip());

        if (user == null || !encrypter.verifyPassword(txtFldPass.getText(), user.getPassword())) {
            lblSignIn.setText("Incorrect username or password. Please try again.");
            lblSignIn.getStyleClass().remove("error-label"); // prevents stacking
            lblSignIn.getStyleClass().add("error-label");
            return;
        }

        String role = user.getRole();
        // TODO: Try-Catch (user has neither admin or user role)
        if (role.equalsIgnoreCase("Admin")) {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/AdminView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();

            AdminController adminController = fxmlLoader.getController();
            adminController.initializeClass(stage, user.getUsername(), facade);

            stage.resizableProperty().setValue(false);

            stage.setTitle("Admin Dashboard");
            stage.setScene(scene);
            stage.show();

            currentStage.close();
        }
        else if (role.equalsIgnoreCase("Coordinator")) {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/CoordinatorView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();

            CoordinatorController coordinatorController = fxmlLoader.getController();
            coordinatorController.initializeClass(stage, user.getUsername(), facade);

            stage.resizableProperty().setValue(false);

            stage.setTitle("Coordinator Dashboard");
            stage.setScene(scene);
            stage.show();

            currentStage.close();
        }*/
    }

    @FXML
    private void onShowHideBtnClick() {

        isHidden = !isHidden;

        if (isHidden) {

            showImg.setImage(new Image(Main.class.getResource("images/Custom/hide.png").toExternalForm()));

            passFldPass.setManaged(true);
            passFldPass.setVisible(true);

            txtFldPass.setManaged(false);
            txtFldPass.setVisible(false);

        } else {

            showImg.setImage(new Image(Main.class.getResource("images/Custom/hide.png").toExternalForm()));

            passFldPass.setManaged(false);
            passFldPass.setVisible(false);

            txtFldPass.setManaged(true);
            txtFldPass.setVisible(true);

        }
    }

    private void resetLabel() {
        lblSignIn.setText("Sign in to manage events and tickets");
        lblSignIn.getStyleClass().remove("error-label");
    }

    public void onEnterClick(KeyEvent keyEvent) throws Exception {
        if (keyEvent.getCode() == javafx.scene.input.KeyCode.ENTER) {
            onSignInBtnClick();
        }
    }
}
