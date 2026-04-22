package ScanHub;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application{

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.resizableProperty().setValue(false);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }
}
