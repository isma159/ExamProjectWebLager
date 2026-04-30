package ScanHub.GUI.controllers;

import ScanHub.GUI.util.AlertTypes;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AlertController {

    @FXML private Label lblHeader, lblContent;
    @FXML private Button btnCancel, btnConfirm, btnExit;

    private Stage stage;
    private Runnable onConfirm;
    private Runnable onCancel;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setHeaderText(String text) {
        lblHeader.setText(text);
    }

    public void setContentText(String text) {
        lblContent.setText(text);
    }

    public void setAlertType(AlertTypes alertType) {
        btnCancel.getStyleClass().clear();

        switch (alertType) {
            case ERROR -> {
                btnCancel.getStyleClass().add("tertiary-btn");
                btnCancel.setText("Cancel");
                btnConfirm.setText("Ok");
            }
            case WARNING -> {
                btnCancel.getStyleClass().add("tertiary-btn");
                btnCancel.setText("Cancel");
                btnConfirm.setText("Proceed");
            }
            case CONFIRMATION -> {
                btnCancel.getStyleClass().add("tertiary-btn");
                btnCancel.setText("Cancel");
                btnConfirm.setText("Confirm");
            }
            case SAVE -> {
                btnCancel.getStyleClass().add("destructive-btn");
                btnCancel.setText("Discard changes");
                btnConfirm.setText("Save Changes");
            }
        }
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    @FXML
    private void onExitBtnClick() {
        stage.close();
    }

    @FXML
    private void handleConfirm() {
        if (onConfirm != null) onConfirm.run();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        if (onCancel != null) onCancel.run();
        stage.close();
    }
}