package ScanHub.BLL;

import ScanHub.BE.Log;
import ScanHub.DAL.DAO.LogDAO;

import java.time.LocalDate;
import java.util.List;

public class LogManager {
    private final LogDAO logDAO;

    public LogManager() throws Exception {
        logDAO = new LogDAO();
    }

    public List<Log> getFilteredLogs(String search, String action, LocalDate from, LocalDate to) throws Exception {
        return logDAO.getFilteredLogs(search, action, from, to);
    }

    public void createLog(int userId, int fieldId, int documentId, String action) throws Exception {
        logDAO.createLog(userId, fieldId, documentId, action);
    }
}