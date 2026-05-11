package ScanHub.GUI.controllers;

import ScanHub.BE.Log;
import ScanHub.GUI.facade.ModelFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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

    private ModelFacade modelFacade;
    private String activeAction = null;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminLogsController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
            List<Log> logs = modelFacade.getLogModel().getFilteredLogs(search, activeAction, dateFrom, dateTo);
            renderLogs(logs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderLogs(List<Log> logs) {
        logsTableBox.getChildren().clear();

        if (logs.isEmpty()) {
            Label empty = new Label("No logs found.");
            empty.getStyleClass().add("lbl");
            empty.setStyle("-fx-padding: 18 0 0 18;");
            logsTableBox.getChildren().add(empty);
            return;
        }

        for (Log log : logs) {
            HBox row = new HBox();
            row.getStyleClass().add("table-row");
            row.setPrefHeight(33);

            if ("FILE_CREATED".equals(log.getAction())) {
                row.setStyle("-fx-background-color: rgba(76, 208, 125, 0.15);");
            } else if ("FILE_DELETED".equals(log.getAction())) {
                row.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15);");
            }

            Label logId     = makeCell(String.valueOf(log.getLogsId()), 80);
            Label username  = makeCell(log.getUsername(), 200);
            Label action    = makeCell(log.getAction(), 260);
            Label fileId    = makeCell(String.valueOf(log.getFileId()), 120);
            Label timestamp = makeCell(log.getTimestamp().format(DISPLAY_FORMAT), 200);

            row.getChildren().addAll(
                    logId,   makeSep(),
                    username, makeSep(),
                    action,   makeSep(),
                    fileId,   makeSep(),
                    timestamp
            );

            logsTableBox.getChildren().add(row);
            logsTableBox.getChildren().add(new Separator());
        }
    }

    private Label makeCell(String text, double width) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("lbl");
        lbl.setPrefWidth(width);
        lbl.setPrefHeight(33);
        lbl.setStyle("-fx-padding: 0 9 0 9; -fx-alignment: CENTER_LEFT;");
        HBox.setHgrow(lbl, Priority.ALWAYS);
        return lbl;
    }

    private Separator makeSep() {
        Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        sep.setPrefHeight(33);
        return sep;
    }

    private LocalDate tryParseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML public void onTbAllLogsClick(ActionEvent e)    { activeAction = null;           applyFilters(); }
    @FXML public void onTbCreateLogsClick(ActionEvent e) { activeAction = "FILE_CREATED"; applyFilters(); }
    @FXML public void onTbDeleteLogsClick(ActionEvent e) { activeAction = "FILE_DELETED"; applyFilters(); }

    @FXML public void onLogIdClick(MouseEvent e) {}
    @FXML public void onUsernameClick(MouseEvent e) {}
    @FXML public void onActionClick(MouseEvent e) {}
    @FXML public void onFileIdClick(MouseEvent e) {}
    @FXML public void onTimestampClick(MouseEvent e) {}
}