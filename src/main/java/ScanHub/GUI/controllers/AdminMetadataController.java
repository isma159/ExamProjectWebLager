package ScanHub.GUI.controllers;

import ScanHub.BE.DocumentMetadata;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminMetadataController implements Initializable {

    @FXML private VBox metadataTableBox;
    @FXML private TextField txtFldSearchMetadata;
    @FXML private Pagination metadataPagination;

    private ModelFacade modelFacade;
    private DocumentMetadata selected = null;

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

            List<DocumentMetadata> list = modelFacade.getMetadataModel().getAllMetadata();

            for (DocumentMetadata m : list) {
                Label row = new Label("Box #" + m.getBoxId() + "  |  " + m.getBoxName() + "  |  " + m.getProfileName());
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

        TextField profileName   = new TextField(selected.getProfileName() != null ? selected.getProfileName() : "");
        TextField boxName       = new TextField(selected.getBoxName() != null ? selected.getBoxName() : "");
        TextField documentCount = new TextField(String.valueOf(selected.getDocumentCount()));
        TextField fileCount     = new TextField(String.valueOf(selected.getFileCount()));

        grid.addRow(0, new Label("Profile Name:"),   profileName);
        grid.addRow(1, new Label("Box Name:"),       boxName);
        grid.addRow(2, new Label("Document Count:"), documentCount);
        grid.addRow(3, new Label("File Count:"),     fileCount);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                selected.setProfileName(profileName.getText());
                selected.setBoxName(boxName.getText());
                try {
                    selected.setDocumentCount(Integer.parseInt(documentCount.getText()));
                    selected.setFileCount(Integer.parseInt(fileCount.getText()));
                } catch (NumberFormatException ex) {
                    AlertHelper.showError("Invalid Input", "Document count and file count must be numbers.");
                    return;
                }
                try {
                    modelFacade.getMetadataModel().updateMetadata(selected);
                    load();
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