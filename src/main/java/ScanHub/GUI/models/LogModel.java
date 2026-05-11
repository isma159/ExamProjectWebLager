package ScanHub.GUI.models;

import ScanHub.BE.Log;
import ScanHub.BLL.LogManager;

import java.time.LocalDate;
import java.util.List;

public class LogModel {

    private final LogManager logManager;

    public LogModel() throws Exception {
        this.logManager = new LogManager();
    }

    public List<Log> getFilteredLogs(String search, String action, LocalDate from, LocalDate to) throws Exception {
        return logManager.getFilteredLogs(search, action, from, to);
    }

    public void createLog(int userId, int fileId, int documentId, String action) throws Exception {
        logManager.createLog(userId, fileId, documentId, action);
    }
}