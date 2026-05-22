package ScanHub.GUI.util;

// java imports
import javafx.scene.Scene;
import java.util.Objects;

public class ThemeHandler {

    private static final String DARK = Objects.requireNonNull(ThemeHandler.class.getResource("/css/darkStyle.css")).toExternalForm();
    public static boolean darkMode;

    public static void apply(Scene scene) {
        scene.getStylesheets().clear();
        if (darkMode) {
            scene.getStylesheets().add(DARK);
        }
    }

    public static void toggle(Scene scene, boolean state) {
        darkMode = state;

        if (state) { scene.getStylesheets().add(DARK); }

        else { scene.getStylesheets().remove(DARK); }
    }
}
