package ScanHub;

import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.ViewHandler;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application{

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/primeicons.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Montserrat-SemiBold.ttf"), 12);

            ViewHandler.LOGIN.reset();
            ViewHandler.LOGIN.show(new ModelFacade(), stage);

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Startup Error", "Failed to start the application. Please try again.");
        }
    }
}
