package ScanHub.GUI.util;

import ScanHub.GUI.controllers.AlertController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class AlertHelper {

    private static final String ALERT_FXML = "/views/AlertView.fxml";

    private static void show(String title, String header, String content, AlertTypes type, Runnable onConfirm, Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(AlertHelper.class.getResource(ALERT_FXML));
            Parent root = loader.load();

            AlertController controller = loader.getController();

            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            controller.setStage(stage);
            controller.setHeaderText(header);
            controller.setContentText(content);
            controller.setAlertType(type);
            controller.setOnConfirm(onConfirm);
            controller.setOnCancel(onCancel);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();

            // fallback (ensures feedback)
            javafx.scene.control.Alert fallback = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);

            fallback.setTitle("Error");
            fallback.setHeaderText("UI Error");
            fallback.setContentText("Failed to load custom alert: " + content);
            fallback.showAndWait();
        }
    }


    public static void showError(String header, String content) {
        show("ERROR", header, content, AlertTypes.ERROR, null, null);
    }

    public static void showWarning(String header, String content) {
        show("WARNING", header, content, AlertTypes.WARNING, null, null);
    }

    public static void showConfirmation(String header, String content, Runnable onConfirm) {
        show("CONFIRMATION", header, content, AlertTypes.CONFIRMATION, onConfirm, null);
    }

    public static void showInformation(String header, String content) {
        show("INFORMATION", header, content, AlertTypes.INFORMATION, null, null);
    }

    public static void showSaveDialog(String content, Runnable onSave, Runnable onDiscard) {
        show("UNSAVED CHANGES", "Unsaved Changes", content, AlertTypes.SAVE, onSave, onDiscard);
    }
}