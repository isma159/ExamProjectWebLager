package ScanHub.GUI.controllers;

import ScanHub.BE.Log;
import ScanHub.GUI.facade.ModelFacade;
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
import java.util.ArrayList;
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

    private ModelFacade modelFacade;
    private String activeAction = null;
    private Stage currentStage;

    private final int ROWS_PER_PAGE = 15;
    private List<Log> allFilteredLogs = new ArrayList<>();

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

        // Listeners for text fields
        txtFldSearchLogs.textProperty().addListener((obs, o, n) -> resetAndApply());
        txtFldFilterUser.textProperty().addListener((obs, o, n) -> resetAndApply());
        txtFldFilterDateFrom.textProperty().addListener((obs, o, n) -> resetAndApply());
        txtFldFilterDateTo.textProperty().addListener((obs, o, n) -> resetAndApply());
        txtFldFilterDocument.textProperty().addListener((obs, o, n) -> resetAndApply());

        // Manual Pagination Listener (instead of Page Factory)
        logsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            renderLogsForCurrentPage();
        });

        applyFilters();
    }

    private void resetAndApply() {
        logsPagination.setCurrentPageIndex(0);
        applyFilters();
    }

    private void applyFilters() {
        String search = txtFldSearchLogs.getText().toLowerCase();
        LocalDate dateFrom = tryParseDate(txtFldFilterDateFrom.getText());
        LocalDate dateTo = tryParseDate(txtFldFilterDateTo.getText());

        try {
            List<Log> logs = modelFacade.getLogModel().getLogs();

            // Filter logic
            if (dateFrom != null && dateTo != null) {
                logs = logs.stream().filter(log ->
                        !log.getTimestamp().toLocalDate().isBefore(dateFrom) &&
                                !log.getTimestamp().toLocalDate().isAfter(dateTo)).toList();
            }

            if (activeAction != null) {
                logs = logs.stream().filter(log -> log.getAction().name().equals(activeAction)).toList();
            }

            FilteredList<Log> filteredList = new FilteredList<>(FXCollections.observableArrayList(logs));
            filteredList.setPredicate(log -> {
                if (search.isBlank()) return true;
                return log.getUser().getUsername().toLowerCase().contains(search);
            });

            allFilteredLogs = new ArrayList<>(filteredList);

            // Calculate and set page count
            int pageCount = Math.max(1, (int) Math.ceil((double) allFilteredLogs.size() / ROWS_PER_PAGE));
            logsPagination.setPageCount(pageCount);

            renderLogsForCurrentPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderLogsForCurrentPage() {
        logsTableBox.getChildren().clear();

        if (allFilteredLogs.isEmpty()) {
            Label empty = new Label("No logs found.");
            empty.getStyleClass().add("lbl");
            logsTableBox.getChildren().add(empty);
            return;
        }

        int startIndex = logsPagination.getCurrentPageIndex() * ROWS_PER_PAGE;
        int endIndex = Math.min(startIndex + ROWS_PER_PAGE, allFilteredLogs.size());

        // Extract sublist for the current page
        List<Log> pageItems = allFilteredLogs.subList(startIndex, endIndex);

        for (Log log : pageItems) {
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
            if (file != null) {
                modelFacade.getLogModel().exportLogs(file.toPath(), allFilteredLogs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void onTbAllLogsClick(ActionEvent e)    { activeAction = null;           resetAndApply(); }
    @FXML private void onTbCreateLogsClick(ActionEvent e) { activeAction = "FILE_CREATED"; resetAndApply(); }
    @FXML private void onTbDeleteLogsClick(ActionEvent e) { activeAction = "FILE_DELETED"; resetAndApply(); }

    @FXML private void onUsernameClick(MouseEvent e) {}
    @FXML private void onActionClick(MouseEvent e) {}
    @FXML private void onFileIdClick(MouseEvent e) {}
    @FXML private void onTimestampClick(MouseEvent e) {}
}