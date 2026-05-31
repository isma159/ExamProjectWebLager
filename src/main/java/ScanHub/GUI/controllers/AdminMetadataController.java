package ScanHub.GUI.controllers;

import ScanHub.BE.BoxMetadata;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.interfaces.IShortcutHandler;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import ScanHub.GUI.util.TableLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class AdminMetadataController implements Initializable, IShortcutHandler {

    @FXML private VBox metadataTableBox;
    @FXML private TextField txtFldSearchMetadata;
    @FXML private Pagination metadataPagination;

    private final ModelFacade modelFacade;
    private BoxMetadata selected = null;
    private HBox selectedRow = null;
    private Stage currentStage;

    private final int TOTAL_TABLE_SIZE = 15;

    public AdminMetadataController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        applyFilter();
        txtFldSearchMetadata.textProperty().addListener(((obs, oldVal, newVal) -> applyFilter()));
    }

    private void loadMetadata(List<BoxMetadata> metadata) {
        try {
            // resets
            selected = null;
            selectedRow = null;

            TableLoader.loadTable(metadataTableBox, metadataPagination, TOTAL_TABLE_SIZE, metadata, item -> {
                BoxMetadata boxMetadata = (BoxMetadata) item;
                return RowMaker.addMetadataRow(boxMetadata);
            });

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load metadata.");
        }
    }

    private void applyFilter() {

        String search = txtFldSearchMetadata.getText();
        List<BoxMetadata> metadata = modelFacade.getMetadataModel().getAllMetadata();

        metadata = metadata.stream().filter(boxMetadata -> search.isBlank() || String.valueOf(boxMetadata.getBoxId()).contains(search) || boxMetadata.getBoxName().contains(search)).toList();

        loadMetadata(metadata);
    }

    @Override
    public Map<KeyCodeCombination, Runnable> getShortcuts() {
        Map<KeyCodeCombination, Runnable> shortcuts = new HashMap<>();
        shortcuts.put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> {
            txtFldSearchMetadata.requestFocus();
            txtFldSearchMetadata.selectAll();
        });
        return shortcuts;
    }
}
