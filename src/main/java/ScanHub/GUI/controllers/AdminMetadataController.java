package ScanHub.GUI.controllers;

import ScanHub.BE.BoxMetadata;
import ScanHub.BE.Profile;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminMetadataController implements Initializable {

    @FXML private VBox metadataTableBox;
    @FXML private TextField txtFldSearchMetadata;
    @FXML private Pagination metadataPagination;

    private ModelFacade modelFacade;
    private BoxMetadata selected = null;
    private HBox selectedRow = null;
    private List<BoxMetadata> currentMetadata;
    private Stage currentStage;

    public AdminMetadataController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMetadata();
    }

    private void loadMetadata() {
        try {
            // resets
            metadataTableBox.getChildren().clear();
            selected = null;
            selectedRow = null;

            // sets up with all profiles by running a for-loop that makes an interactive HBox of every profile
            List<BoxMetadata> metadata = modelFacade.getMetadataModel().getAllMetadata();
            currentMetadata = new ArrayList<>(metadata);
            for (BoxMetadata boxMetadata : currentMetadata) {
                HBox row = RowMaker.addMetadataRow(boxMetadata, (clickedMetadata, rowHBox) -> {
                    if (selectedRow != null) {
                        selectedRow.getStyleClass().remove("row-selected");
                    }
                    if (selected == clickedMetadata) {
                        selected = null;
                        selectedRow = null;
                        return;
                    }
                    selected = clickedMetadata;
                    selectedRow = rowHBox;
                    rowHBox.getStyleClass().add("row-selected");
                });
                row.setUserData(boxMetadata);
                metadataTableBox.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load metadata.");
        }
    }

    // TODO: find out if editing metadata manually is necessary?? it should update when you scan no?
    @FXML
    private void onClickEditMetadata() {
        if (selected == null) { AlertHelper.showWarning("No Selection", "Select a row first."); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Metadata");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField profileName = new TextField(selected.getProfileName() != null ? selected.getProfileName() : "");
        TextField boxName = new TextField(selected.getBoxName() != null ? selected.getBoxName() : "");
        TextField documentCount = new TextField(String.valueOf(selected.getDocumentCount()));
        TextField fileCount = new TextField(String.valueOf(selected.getFileCount()));
        TextField boxCreatedAt = new TextField(selected.getBoxCreatedAt() != null ? selected.getBoxCreatedAt().toString() : "");

        grid.addRow(0, new Label("Profile:"), profileName);
        grid.addRow(1, new Label("Box:"), boxName);
        grid.addRow(2, new Label("Documents:"), documentCount);
        grid.addRow(3, new Label("Files:"), fileCount);
        grid.addRow(4, new Label("Created at:"), boxCreatedAt);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    selected.setProfileName(profileName.getText());
                    selected.setBoxName(boxName.getText());
                    selected.setDocumentCount(Integer.parseInt(documentCount.getText()));
                    selected.setFileCount(Integer.parseInt(fileCount.getText()));
                    selected.setBoxCreatedAt(LocalDateTime.parse(boxCreatedAt.getText()));
                    modelFacade.getMetadataModel().updateMetadata(selected);
                    loadMetadata();
                } catch (NumberFormatException | DateTimeParseException e) {
                    AlertHelper.showWarning("Invalid Values", "Counts must be numbers and created at must use ISO date-time format.");
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertHelper.showError("Update Failed", "Failed to update.");
                }
            }
        });
    }

    @FXML
    private void onClickDeleteMetadata() {
        if (selected == null) { AlertHelper.showWarning("No Selection", "Select a row first."); return; }
        AlertHelper.showConfirmation("Delete", "Delete metadata for Box #" + selected.getBoxId() + "?", () -> {
            try {
                modelFacade.getMetadataModel().deleteMetadata(selected);
                loadMetadata();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Delete Failed", "Failed to delete.");
            }
        });
    }

    // TODO: needs to be removed properly
    @FXML private void onTbAllMetadataClick()   { loadMetadata(); }
    @FXML private void onTbWithMetadataClick()  {}
    @FXML private void onTbNoMetadataClick()    {}
    @FXML private void onDocumentIdClick()      {}
    @FXML private void onTitleClick()           {}
    @FXML private void onDocumentTypeClick()    {}
    @FXML private void onAuthorClick()          {}
    @FXML private void onReferenceNumberClick() {}
    @FXML private void onDocumentDateClick()    {}
}
