package ScanHub.GUI.controllers;

import ScanHub.BE.Log;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminLogsController implements Initializable {

    @FXML private TextField txtFldSearchLogs;
    @FXML private TextField txtFldFilterUser;
    @FXML private TextField txtFldFilterDateFrom;
    @FXML private TextField txtFldFilterDateTo;
    @FXML private TextField txtFldFilterDocument;
    @FXML private ToggleGroup logsFilter;
    @FXML private VBox logsTableBox;
    @FXML private Pagination logsPagination;

    private final ModelFacade modelFacade;
    private final Stage currentStage;
    private String activeAction = null;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminLogsController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            modelFacade.getLogModel().refreshModel();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        txtFldSearchLogs.textProperty().addListener((obs, o, n) -> applyFilters());
        txtFldFilterUser.textProperty().addListener((obs, o, n) -> applyFilters());
        txtFldFilterDateFrom.textProperty().addListener((obs, o, n) -> applyFilters());
        txtFldFilterDateTo.textProperty().addListener((obs, o, n) -> applyFilters());
        txtFldFilterDocument.textProperty().addListener((obs, o, n) -> applyFilters());

        applyFilters();
    }

    private void applyFilters() {
        String search = txtFldSearchLogs.getText();
        LocalDate dateFrom = tryParseDate(txtFldFilterDateFrom.getText());
        LocalDate dateTo = tryParseDate(txtFldFilterDateTo.getText());

        try {
            List<Log> logs = modelFacade.getLogModel().getLogs();
            if (dateFrom != null && dateTo != null) {
                logs = logs.stream().filter(log -> !log.getTimestamp().toLocalDate().isBefore(dateFrom) &&
                        !log.getTimestamp().toLocalDate().isAfter(dateTo)).toList();
            }

            FilteredList<Log> filteredLogs = new FilteredList<>(FXCollections.observableArrayList(logs));
            filteredLogs.setPredicate(log -> {

                if (search.isBlank()) return true;

                if (log.getUser().getUsername().contains(search)) {
                    return true;
                }
                else {
                    return false;
                }

            });
            renderLogs(filteredLogs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderLogs(List<Log> logs) {
        logsTableBox.getChildren().clear();

        if (logs.isEmpty()) {
            Label empty = new Label("No logs found.");
            empty.getStyleClass().add("lbl");
            logsTableBox.getChildren().add(empty);
            return;
        }

        for (Log log : logs) {
            HBox row = RowMaker.addLogRow(log);
            logsTableBox.getChildren().add(row);
        }
    }

    private LocalDate tryParseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML private void onExportBtnClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Export");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("activityLog.csv");

            File file = fileChooser.showSaveDialog(currentStage);

            modelFacade.getLogModel().exportLogs(file.toPath(), modelFacade.getLogModel().getLogs());
        }
        catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Export Error", "Could not export to CSV");
        }
    }

    @FXML private void onTbAllLogsClick(ActionEvent e)    { activeAction = null;           applyFilters(); }
    @FXML private void onTbCreateLogsClick(ActionEvent e) { activeAction = "FILE_CREATED"; applyFilters(); }
    @FXML private void onTbDeleteLogsClick(ActionEvent e) { activeAction = "FILE_DELETED"; applyFilters(); }

    @FXML private void onUsernameClick(MouseEvent e) {}
    @FXML private void onActionClick(MouseEvent e) {}
    @FXML private void onFileIdClick(MouseEvent e) {}
    @FXML private void onTimestampClick(MouseEvent e) {}
}