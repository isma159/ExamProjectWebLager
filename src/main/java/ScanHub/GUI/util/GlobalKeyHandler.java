package ScanHub.GUI.util;

import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalKeyHandler manages keyboard shortcuts for a JavaFX Scene.
 *
 * Usage:
 *   1. Create an instance and call attach(scene) once the scene is ready.
 *   2. Register scene-wide shortcuts via register(combo, action).
 *   3. Controllers can replace or clear their layer with setLayer() / clearLayer().
 *
 * Two-layer design:
 *   - Global layer  : persistent shortcuts (e.g. F2 dark-mode, Ctrl+Q quit).
 *   - Controller layer : shortcuts owned by the currently active view/controller.
 *     Cleared automatically when a new layer is pushed via setLayer().
 *
 * The handler installs a single KEY_PRESSED event filter on the scene root,
 * so it fires before any node's onKeyPressed handler and works regardless of
 * which node currently holds focus.
 */
public class GlobalKeyHandler {

    // ── Singleton ────────────────────────────────────────────────────────────

    private static GlobalKeyHandler instance;

    /** Returns the application-wide singleton, creating it on first call. */
    public static GlobalKeyHandler getInstance() {
        if (instance == null) {
            instance = new GlobalKeyHandler();
        }
        return instance;
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private final Map<KeyCodeCombination, Runnable> globalLayer     = new HashMap<>();
    private final Map<KeyCodeCombination, Runnable> controllerLayer = new HashMap<>();

    private Scene attachedScene;

    // private constructor — use getInstance()
    private GlobalKeyHandler() {}

    // ── Attachment ────────────────────────────────────────────────────────────

    /**
     * Attaches this handler to the given scene by installing an event filter.
     * Safe to call multiple times — each call replaces the previously attached
     * scene (the old filter is removed first).
     *
     * @param scene the JavaFX scene to listen on
     */
    public void attach(Scene scene) {
        if (scene == null) return;

        // Remove filter from any previously attached scene
        if (attachedScene != null && attachedScene != scene) {
            attachedScene.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
        }

        attachedScene = scene;
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Registers a persistent global shortcut.
     * Global shortcuts are never cleared by setLayer() / clearLayer().
     *
     * @param combo  the key combination (e.g. new KeyCodeCombination(KeyCode.F2))
     * @param action the runnable to execute when the combo is matched
     */
    public void register(KeyCodeCombination combo, Runnable action) {
        globalLayer.put(combo, action);
    }

    /**
     * Removes a previously registered global shortcut.
     *
     * @param combo the combination to remove
     */
    public void unregister(KeyCodeCombination combo) {
        globalLayer.remove(combo);
    }

    // ── Controller layer ──────────────────────────────────────────────────────

    /**
     * Replaces the controller-layer shortcuts with the given map.
     * Typically called by a controller's setModel() / initialize() method.
     *
     * @param shortcuts map of key combinations → actions for the active view
     */
    public void setLayer(Map<KeyCodeCombination, Runnable> shortcuts) {
        controllerLayer.clear();
        if (shortcuts != null) {
            controllerLayer.putAll(shortcuts);
        }
    }

    /**
     * Clears all controller-layer shortcuts, leaving only global ones active.
     * Call this when navigating away from a view if you want a clean state.
     */
    public void clearLayer() {
        controllerLayer.clear();
    }

    // ── Event handling ────────────────────────────────────────────────────────

    /**
     * Internal event filter installed on the scene.
     * Controller-layer shortcuts take precedence over global ones so that
     * a view can override a global binding if needed.
     */
    private void handleKeyEvent(KeyEvent event) {
        // Check controller layer first (higher priority)
        for (Map.Entry<KeyCodeCombination, Runnable> entry : new HashMap<>(controllerLayer).entrySet()) {
            if (entry.getKey().match(event)) {
                entry.getValue().run();
                event.consume();
                return;
            }
        }

        // Fall through to global layer
        for (Map.Entry<KeyCodeCombination, Runnable> entry : new HashMap<>(globalLayer).entrySet()) {
            if (entry.getKey().match(event)) {
                entry.getValue().run();
                event.consume();
                return;
            }
        }
    }
}