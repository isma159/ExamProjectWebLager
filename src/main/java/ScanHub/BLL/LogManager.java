package ScanHub.BLL;

import ScanHub.BE.Log;
import ScanHub.DAL.DAO.LogDAO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogManager {
    private final LogDAO logDAO;

    public LogManager() throws Exception {
        logDAO = new LogDAO();
    }

    public List<Log> getLogs() throws Exception {
        return logDAO.getLogs();
    }

    public Log createLog(Log log) throws Exception {
        return logDAO.createLog(log);
    }

    public void exportLogs(Path path, List<Log> logs) throws Exception {

        StringBuilder sb = new StringBuilder();
        String delimiter = ";";
        sb.append("id;username;action;entity-type;entity-id;timestamp\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");

        for (Log log: logs) {
            sb.append(log.getLogId()).append(delimiter)
                    .append(log.getUser().getUsername()).append(delimiter)
                    .append(log.getAction()).append(delimiter)
                    .append(log.getEntityType()).append(delimiter)
                    .append(log.getEntityId()).append(delimiter)
                    .append(log.getTimestamp().format(formatter)).append("\n");
        }

        Files.writeString(path, sb.toString());

    }
}