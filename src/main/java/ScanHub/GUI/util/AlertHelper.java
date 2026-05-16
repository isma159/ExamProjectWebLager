package ScanHub.GUI.util;

// project imports
import ScanHub.GUI.controllers.AlertController;

// java imports
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class AlertHelper {

    private static final String ALERT_FXML = "/views/AlertView.fxml";

    private static void show(String title, String header, String content, AlertTypes type, Runnable onConfirm, Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(AlertHelper.class.getResource(ALERT_FXML));
            Parent root = loader.load();
            AlertController controller = loader.getController();

            // rounded corners
            Rectangle clip = new Rectangle();
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            clip.widthProperty().bind(root.layoutBoundsProperty().map(bounds -> bounds.getWidth()));
            clip.heightProperty().bind(root.layoutBoundsProperty().map(bounds -> bounds.getHeight()));
            root.setClip(clip);

            Stage stage = new Stage(StageStyle.TRANSPARENT);
            stage.setTitle(title);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                    event.consume();
                }
            });

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

            fallback.setOnShown(event -> {
                fallback.getDialogPane().getScene().setOnKeyPressed(keyEvent -> {
                    if (keyEvent.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        fallback.close();
                        keyEvent.consume();
                    }
                });
            });


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