package ScanHub.GUI.util;

import ScanHub.GUI.controllers.AlertController;
import ScanHub.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertCaller {

    public static AlertBuilder showError(String title, String message) {

        return new AlertBuilder().setType(AlertTypes.ERROR)
                .setHeaderText(title)
                .setContentText(message)
                .setTitle(title);

    }

    public static AlertBuilder showWarning(String title, String message) {

        return new AlertBuilder().setType(AlertTypes.ERROR)
                .setHeaderText(title)
                .setContentText(message)
                .setTitle(title);

    }

}
