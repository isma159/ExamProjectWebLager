package ScanHub;

import ScanHub.GUI.controllers.LoginController;
import ScanHub.GUI.facade.ModelFacade;
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
    public void start(Stage stage) throws Exception {
        Font loaded = Font.loadFont(getClass().getResourceAsStream("/fonts/primeicons.ttf"), 14);

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        LoginController loginController = fxmlLoader.getController();
        loginController.setStage(stage);
        loginController.setModelFacade(new ModelFacade());
        stage.setResizable(false);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }
}
