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

    private final int TOTAL_TABLE_SIZE = 15;

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
            metadataPagination.setPageCount(Math.ceilDiv(metadata.size(), TOTAL_TABLE_SIZE));

            int startIndex = metadataPagination.getCurrentPageIndex() * TOTAL_TABLE_SIZE;
            int endIndex = Math.min(startIndex + TOTAL_TABLE_SIZE, metadata.size());

            currentMetadata = new ArrayList<>(metadata.subList(startIndex, endIndex));
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
