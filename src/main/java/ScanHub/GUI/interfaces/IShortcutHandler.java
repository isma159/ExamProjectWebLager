package ScanHub.GUI.interfaces;

import javafx.scene.input.KeyCodeCombination;

import java.util.Map;

public interface IShortcutHandler {

    Map<KeyCodeCombination, Runnable> getShortcuts();

}
