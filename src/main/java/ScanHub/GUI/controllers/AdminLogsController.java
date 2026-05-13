package ScanHub.GUI.controllers;

import ScanHub.BE.EntityType;
import ScanHub.BE.Log;
import ScanHub.BE.LogAction;
import ScanHub.BE.Role;
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
    @FXML private ToggleGroup logsFilter;
    @FXML private VBox logsTableBox;
    @FXML private Pagination logsPagination;

    private ModelFacade modelFacade;
    private LogAction selectedAction = null;
    private Stage currentStage;

    private List<Log> currentLogs = new ArrayList<>();

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
        dtPickerFrom.valueProperty().addListener((obs, o, n) -> applyFilters());
        dtPickerTo.valueProperty().addListener((obs, o, n) -> applyFilters());

        applyFilters();
    }

    private void applyFilters() {
        String search = txtFldSearchLogs.getText();
        LocalDate dateFrom = dtPickerFrom.getValue();
        LocalDate dateTo = dtPickerTo.getValue();

        try {
            List<Log> logs = modelFacade.getLogModel().getLogs();
            if (dateFrom != null && dateTo != null) {
                logs = logs.stream()
                        .filter(
                                log -> !log.getTimestamp().toLocalDate().isBefore(dateFrom) &&
                                        !log.getTimestamp().toLocalDate().isAfter(dateTo)
                        ).toList();
            }

            if (selectedAction != null) {

                logs = logs.stream().filter(log -> log.getAction() == selectedAction).toList();

            }

            FilteredList<Log> filteredLogs = new FilteredList<>(FXCollections.observableArrayList(logs));
            filteredLogs.setPredicate(log -> {

                if (search.isBlank()) return true;

                if (log.getUser().getUsername().contains(search)) {
                    return true;
                }
                else if (String.valueOf(log.getLogId()).contains(search)) {
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
        currentLogs.clear();
        currentLogs.addAll(logs);

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

    @FXML private void onTbAllLogsClick(ActionEvent e)    { selectedAction = null;           applyFilters(); }
    @FXML private void onTbCreateLogsClick(ActionEvent e) { selectedAction = LogAction.CREATE; applyFilters(); }
    @FXML private void onTbDeleteLogsClick(ActionEvent e) { selectedAction = LogAction.DELETE; applyFilters(); }
    @FXML private void onTbScanLogsClick(ActionEvent e) { selectedAction = LogAction.SCAN; applyFilters(); }
    @FXML private void onTbExportLogsClick(ActionEvent e) { selectedAction = LogAction.EXPORT; applyFilters(); }
    @FXML private void onTbLoginLogsClick(ActionEvent e) { selectedAction = LogAction.LOGIN; applyFilters(); }


    @FXML private void onUsernameClick(MouseEvent e) {}
    @FXML private void onActionClick(MouseEvent e) {}
    @FXML private void onFileIdClick(MouseEvent e) {}
    @FXML private void onTimestampClick(MouseEvent e) {}
}