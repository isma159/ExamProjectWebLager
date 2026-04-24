package ScanHub.BLL;

import javafx.scene.Scene;

import java.util.Objects;

public class ThemeManager { // TODO Placeholder / Hook it up

    private static final String LIGHT = Objects.requireNonNull(ThemeManager.class.getResource("/styles/style.css")).toExternalForm();
    private static final String DARK = Objects.requireNonNull(ThemeManager.class.getResource("/styles/dark.css")).toExternalForm();

    private static boolean isDarkMode = false;

    public static void apply(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(LIGHT);
    }

    public static void toggle(Scene scene) {
        if (isDarkMode) {
            scene.getStylesheets().remove(DARK);
        } else {
            scene.getStylesheets().add(DARK);
        }
        isDarkMode = !isDarkMode;
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

}
