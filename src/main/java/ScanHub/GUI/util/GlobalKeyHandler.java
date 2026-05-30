package ScanHub.GUI.util;

import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class GlobalKeyHandler {

    private static GlobalKeyHandler instance;

    public static GlobalKeyHandler getInstance() {
        if (instance == null) {
            instance = new GlobalKeyHandler();
        }
        return instance;
    }

    private final Map<KeyCodeCombination, Runnable> globalLayer = new HashMap<>();
    private final Map<KeyCodeCombination, Runnable> controllerLayer = new HashMap<>();

    private Scene attachedScene;

    private GlobalKeyHandler() {}

    public void attach(Scene scene) {
        if (scene == null) return;

        if (attachedScene != null && attachedScene != scene) {
            attachedScene.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
        }

        attachedScene = scene;
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
    }

    public void setLayer(Map<KeyCodeCombination, Runnable> shortcuts) {
        controllerLayer.clear();
        if (shortcuts != null) {
            controllerLayer.putAll(shortcuts);
        }
    }

    private void handleKeyEvent(KeyEvent event) {

        for (Map.Entry<KeyCodeCombination, Runnable> entry : new HashMap<>(controllerLayer).entrySet()) {
            if (entry.getKey().match(event)) {
                entry.getValue().run();
                event.consume();
                return;
            }
        }

        for (Map.Entry<KeyCodeCombination, Runnable> entry : new HashMap<>(globalLayer).entrySet()) {
            if (entry.getKey().match(event)) {
                entry.getValue().run();
                event.consume();
                return;
            }
        }
    }
}