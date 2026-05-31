package ScanHub.BLL;

import ScanHub.BE.Log;
import ScanHub.DAL.facade.DAOFacade;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogManager {

    private final DAOFacade daoFacade = DAOFacade.getInstance();

    public LogManager() {}

    public List<Log> getLogs() throws Exception {
        return daoFacade.getLogDAO().getData();
    }

    public Log createLog(Log log) throws Exception {
        return daoFacade.getLogDAO().createData(log);
    }

    public void exportLogs(Path path, List<Log> logs) throws Exception {

        StringBuilder sb = new StringBuilder();
        String delimiter = ";";
        sb.append("id;username;action;entity-type;entity-id;timestamp\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Log log: logs) {
            sb.append(log.getLogId()).append(delimiter)
                    .append(log.getUser().getUsername()).append(delimiter)
                    .append(log.getAction()).append(delimiter)
                    .append(log.getEntityType()).append(delimiter)
                    .append(log.getEntityName()).append(delimiter)
                    .append(log.getTimestamp().format(formatter)).append("\n");
        }

        Files.writeString(path, sb.toString());

    }
}