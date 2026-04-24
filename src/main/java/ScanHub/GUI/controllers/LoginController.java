package ScanHub.GUI.controllers;

import ScanHub.GUI.util.TextFieldListeners;
import ScanHub.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtFldUser, txtFldPass;
    @FXML private PasswordField passFldPass;
    @FXML private Button signInBtn;
    @FXML private HBox hboxUser, hboxPass1, hboxPass2;

    private Stage currentStage;

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        List<HBox> containers = new ArrayList<>();
        containers.add(hboxUser);
        containers.add(hboxPass1);
        containers.add(hboxPass2);

        signInBtn.focusedProperty().addListener((observable, oldValue, newValue) -> {

            System.out.println(newValue );

        });

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

    }

    private String userMock = "admin";
    private String passMock = "admin123";

    @FXML
    private void onSignInBtnClick() {
        if (!txtFldUser.getText().isBlank() && !passFldPass.getText().isBlank()) {
            if (txtFldUser.getText().equals(userMock) && passFldPass.getText().equals(passMock)) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/AdminView.fxml"));
                    Scene scene = new Scene(fxmlLoader.load());
                    Stage stage = new Stage();

                    stage.setMinWidth(1200);
                    stage.setMinHeight(600);

                    stage.setTitle("Admin Panel");
                    stage.setScene(scene);
                    stage.show();

                    currentStage.close();
                }
                catch (Exception e) {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Failed to open Admin Panel");
                    alert.setContentText("Please try again!");
                    alert.showAndWait();

                }
            }
            else {
                onLoginError();
            }
        }
        else {
            onLoginError();
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
