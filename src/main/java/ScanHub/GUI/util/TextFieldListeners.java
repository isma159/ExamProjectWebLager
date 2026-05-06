package ScanHub.GUI.util;

// java imports
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import java.util.List;

public class TextFieldListeners {

    public static void addFocusListener(TextField textField, HBox container) {
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { container.getStyleClass().add("focused"); }
            else { container.getStyleClass().remove("focused"); }
        });
    }

    public static void addErrorListener(TextField textField, List<HBox> containers) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            for (HBox container: containers) {
                if (container.getStyleClass().contains("error-border")) {
                    container.getStyleClass().remove("error-border");
                }
            }
        });
    }
}