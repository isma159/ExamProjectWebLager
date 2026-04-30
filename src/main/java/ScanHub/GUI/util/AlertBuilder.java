package ScanHub.GUI.util;

import ScanHub.GUI.controllers.AlertController;
import ScanHub.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AlertBuilder {

    private String title;
    private String headerText;
    private String contentText;
    private AlertTypes type;
    private Runnable onConfirm;
    private Runnable onCancel;

    public AlertBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public AlertBuilder setHeaderText(String headerText) {
        this.headerText = headerText;
        return this;
    }

    public AlertBuilder setContentText(String contentText) {
        this.contentText = contentText;
        return this;
    }

    public AlertBuilder setType(AlertTypes type) {
        this.type = type;
        return this;
    }

    public AlertBuilder setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
        return this;
    }

    public AlertBuilder setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;

    }

    public void show() {

        try {

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/AlertView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage(StageStyle.UNDECORATED);

            AlertController controller = loader.getController();
            controller.setHeaderText(headerText);
            controller.setContentText(contentText);
            controller.setAlertType(type);
            controller.setStage(stage);

            controller.setOnConfirm(() -> {
                if (onConfirm != null) onConfirm.run();
                stage.close();
            });
            controller.setOnCancel(() -> {
                if (onCancel != null) onCancel.run();
                stage.close();
            });

            stage.setResizable(false);
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
