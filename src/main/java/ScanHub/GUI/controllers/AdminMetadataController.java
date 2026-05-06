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
                Label row = new Label("Doc #" + m.getDocumentId() + "  |  " + m.getTitle() + "  |  " + m.getAuthor());
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

        TextField title  = new TextField(selected.getTitle() != null ? selected.getTitle() : "");
        TextField type   = new TextField(selected.getDocumentType() != null ? selected.getDocumentType() : "");
        TextField author = new TextField(selected.getAuthor() != null ? selected.getAuthor() : "");
        TextField ref    = new TextField(selected.getReferenceNumber() != null ? selected.getReferenceNumber() : "");

        grid.addRow(0, new Label("Title:"),  title);
        grid.addRow(1, new Label("Type:"),   type);
        grid.addRow(2, new Label("Author:"), author);
        grid.addRow(3, new Label("Ref No:"), ref);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                selected.setTitle(title.getText());
                selected.setDocumentType(type.getText());
                selected.setAuthor(author.getText());
                selected.setReferenceNumber(ref.getText());
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
        AlertHelper.showConfirmation("Delete", "Delete metadata for Doc #" + selected.getDocumentId() + "?", () -> {
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