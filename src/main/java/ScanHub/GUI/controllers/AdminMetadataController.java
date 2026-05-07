package ScanHub.GUI.controllers;

import ScanHub.BE.BoxMetadata;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminMetadataController implements Initializable {

    @FXML private VBox metadataTableBox;
    @FXML private TextField txtFldSearchMetadata;
    @FXML private Pagination metadataPagination;

    private ModelFacade modelFacade;
    private BoxMetadata selected = null;

    public AdminMetadataController() {}

    public AdminMetadataController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void setModel(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
        load();
    }

    private void load() {
        try {
            metadataTableBox.getChildren().clear();
            selected = null;

            List<BoxMetadata> list = modelFacade.getMetadataModel().getAllMetadata();

            for (BoxMetadata m : list) {
                Label row = new Label("Box #" + m.getBoxId()
                        + "  |  " + m.getProfileName()
                        + "  |  " + m.getBoxName()
                        + "  |  Docs: " + m.getDocumentCount()
                        + "  |  Files: " + m.getFileCount());
                row.getStyleClass().addAll("lbl", "box-card", "user-row");
                row.setMaxWidth(Double.MAX_VALUE);
                row.setPrefHeight(45);
                row.setOnMouseClicked(e -> {
                    selected = m;
                    row.getStyleClass().add("row-selected");
                });
                metadataTableBox.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Load Error", "Failed to load metadata.");
        }
    }

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
                    load();
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
                load();
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Delete Failed", "Failed to delete.");
            }
        });
    }

    @FXML private void onTbAllMetadataClick()   { load(); }
    @FXML private void onTbWithMetadataClick()  {}
    @FXML private void onTbNoMetadataClick()    {}
    @FXML private void onDocumentIdClick()      {}
    @FXML private void onTitleClick()           {}
    @FXML private void onDocumentTypeClick()    {}
    @FXML private void onAuthorClick()          {}
    @FXML private void onReferenceNumberClick() {}
    @FXML private void onDocumentDateClick()    {}
}
