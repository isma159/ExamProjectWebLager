package ScanHub.GUI.controllers;

import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IShortcutHandler;
import ScanHub.GUI.interfaces.IViewController;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ShortcutsController implements Initializable, IShortcutHandler {
    private ModelFacade modelFacade;
    private Stage currentStage;

    public ShortcutsController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        return Map.of();
    }
}