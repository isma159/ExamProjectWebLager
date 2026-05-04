package ScanHub.GUI.controllers;

import ScanHub.GUI.facade.ModelFacade;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminMetadataController implements Initializable {

    @FXML private VBox metadataTableBox;
    @FXML private TextField txtFldSearchMetadata;
    @FXML private Pagination metadataPagination;

    private ModelFacade modelFacade;

    public AdminMetadataController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void onClickEditMetadata() {}

    @FXML
    private void onClickDeleteMetadata() {}

    @FXML
    private void onTbAllMetadataClick() {}

    @FXML
    private void onTbWithMetadataClick() {}

    @FXML
    private void onTbNoMetadataClick() {}

    @FXML
    private void onDocumentIdClick() {}

    @FXML
    private void onTitleClick() {}

    @FXML
    private void onDocumentTypeClick() {}

    @FXML
    private void onAuthorClick() {}

    @FXML
    private void onReferenceNumberClick() {}

    @FXML
    private void onDocumentDateClick() {}
}