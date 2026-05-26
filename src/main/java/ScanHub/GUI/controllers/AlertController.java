package ScanHub.GUI.controllers;

// project imports
import ScanHub.GUI.util.AlertTypes;

// java imports
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AlertController {

    @FXML private Label lblHeader, lblContent;
    @FXML private Button btnCancel, btnConfirm;

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
            case SPLIT -> {
                btnBefore.setVisible(true);
                btnBefore.setManaged(true);
                btnCancel.getStyleClass().add("secondary-btn");
                btnCancel.setText("Cancel");
                btnBefore.getStyleClass().add("secondary-btn");
                btnBefore.setText("Before selected page");
                btnConfirm.setText("After selected page");
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
    @FXML
    private void handleBefore() {
        if (onBefore != null) onBefore.run();
        stage.close();
    }

    @FXML private Button btnBefore;
    private Runnable onBefore;

    public void setOnBefore(Runnable onBefore) { this.onBefore = onBefore; }
}