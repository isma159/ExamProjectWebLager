package ScanHub.GUI.util;

import ScanHub.GUI.controllers.AlertController;
import ScanHub.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertCaller {

    public static AlertBuilder alert() {
        return new AlertBuilder();
    }

}
