package ScanHub.GUI.models;

import ScanHub.BE.Log;
import ScanHub.BLL.LogManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.util.List;

public class LogModel {

    private final ObservableList<Log> logObservableList;
    private final LogManager logManager;

    public LogModel() throws Exception {
        logManager = new LogManager();
        logObservableList = FXCollections.observableArrayList();
        logObservableList.setAll(logManager.getLogs().reversed());
    }

    public void refreshModel() throws Exception { logObservableList.setAll(logManager.getLogs().reversed()); }

    public List<Log> getLogs() { return logObservableList; }

    public Log createLog(Log log) throws Exception {
        Log newLog = logManager.createLog(log);
        logObservableList.add(log);
        return newLog;
    }

    public void exportLogs(Path path, List<Log> logs) throws Exception { logManager.exportLogs(path, logs); }
}
