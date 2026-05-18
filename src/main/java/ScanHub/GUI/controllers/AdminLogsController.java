package ScanHub.GUI.controllers;

import ScanHub.BE.enums.EntityType;
import ScanHub.BE.Log;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.enums.Role;
import ScanHub.GUI.facade.ModelFacade;
import ScanHub.GUI.util.AlertHelper;
import ScanHub.GUI.util.RowMaker;
import ScanHub.GUI.util.ViewHandler;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminLogsController implements Initializable {

    @FXML private TextField txtFldSearchLogs;
    @FXML private DatePicker dtPickerFrom, dtPickerTo;
    @FXML private VBox logsTableBox;
    @FXML private Pagination logsPagination;
    @FXML private ComboBox<LogAction> cbFilter;

    private ModelFacade modelFacade;
    private LogAction selectedAction = null;
    private Stage currentStage;

    private List<Log> currentLogs = new ArrayList<>();

    private final int ROWS_PER_PAGE = 15;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AdminLogsController(ModelFacade modelFacade, Stage currentStage) {
        this.modelFacade = modelFacade;
        this.currentStage = currentStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            modelFacade.getLogModel().refreshModel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cbFilter.getItems().addAll(LogAction.values());

        txtFldSearchLogs.textProperty().addListener((obs, o, n) -> applyFilters());
        dtPickerFrom.valueProperty().addListener((obs, o, n) -> applyFilters());
        dtPickerTo.valueProperty().addListener((obs, o, n) -> applyFilters());

        cbFilter.valueProperty().addListener(((obs, o, n) -> applyFilters()));

        cbFilter.getSelectionModel().select(LogAction.ALL);

        // Manual Pagination Listener (instead of Page Factory)
        logsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            applyFilters();
        });

        applyFilters();
    }

    private void resetAndApply() {
        logsPagination.setCurrentPageIndex(0);
        applyFilters();
    }

    private void applyFilters() {
        String search = txtFldSearchLogs.getText();
        LocalDate dateFrom = dtPickerFrom.getValue();
        LocalDate dateTo = dtPickerTo.getValue();

        try {
            List<Log> logs = modelFacade.getLogModel().getLogs();

            // Filter logic
            if (dateFrom != null && dateTo != null) {
                logs = logs.stream().filter(log ->
                        !log.getTimestamp().toLocalDate().isBefore(dateFrom) &&
                                !log.getTimestamp().toLocalDate().isAfter(dateTo)).toList();
            }

            List<Log> firstFiltering = logs.stream().filter(log -> {

                if (cbFilter.getSelectionModel().getSelectedItem() == LogAction.ALL) {return true;}

                return log.getAction() == cbFilter.getSelectionModel().getSelectedItem();

            }).toList();

            List<Log> filteredLogs = firstFiltering.stream().filter(log -> {

                if (search.isBlank()) return true;

                if (log.getUser().getUsername().contains(search)) {
                    return true;
                } else if (String.valueOf(log.getLogId()).contains(search)) {
                    return true;
                } else {
                    return false;
                }
            }).toList();

            renderLogsForCurrentPage(filteredLogs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderLogsForCurrentPage(List<Log> logs) {
        logsTableBox.getChildren().clear();
        currentLogs.clear();
        currentLogs.addAll(logs);

        if (logs.isEmpty()) {
            Label empty = new Label("No logs found.");
            empty.getStyleClass().add("lbl");
            logsTableBox.getChildren().add(empty);
            return;
        }

        // Calculate and set page count
        int pageCount = Math.ceilDiv(logs.size(), ROWS_PER_PAGE);
        logsPagination.setPageCount(pageCount);

        int startIndex = logsPagination.getCurrentPageIndex() * ROWS_PER_PAGE;
        int endIndex = Math.min(startIndex + ROWS_PER_PAGE, logs.size());

        // Extract sublist for the current page
        List<Log> pageItems = logs.subList(startIndex, endIndex);

        for (Log log : pageItems) {
            HBox row = RowMaker.addLogRow(log);
            logsTableBox.getChildren().add(row);
        }
    }

    @FXML private void onExportBtnClick() {
        try {
            if (!currentLogs.isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Export");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                fileChooser.setInitialFileName("activityLog.csv");

                File file = fileChooser.showSaveDialog(currentStage);

                modelFacade.getLogModel().exportLogs(file.toPath(), currentLogs);
            }
            else {
                AlertHelper.showError("No Logs", "There are no logs to export.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            // TODO Alert View?
        }
    }
}