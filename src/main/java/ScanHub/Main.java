package ScanHub;

import ScanHub.GUI.controllers.LoginController;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application{

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Font primeicons = Font.loadFont(getClass().getResourceAsStream("/fonts/primeicons.ttf"), 12);
            Font montserratsemibold = Font.loadFont(getClass().getResourceAsStream("/fonts/Montserrat-SemiBold.ttf"), 12);

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/LoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            LoginController loginController = fxmlLoader.getController();
            loginController.setModel(new ModelFacade(), stage);

            stage.setResizable(false);
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Startup Error", "Failed to start the application. Please try again.");
        }
    }
}
