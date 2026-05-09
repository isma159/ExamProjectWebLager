package ScanHub.BE;

import java.time.LocalDateTime;

public class Log {
    private int logsId;
    private int userId;
    private String username;
    private int fileId;
    private int documentId;
    private String action;
    private LocalDateTime timestamp;

    public Log(int logsId, int userId, String username, int fileId, int documentId, String action, LocalDateTime timestamp) {
        this.logsId = logsId;
        this.userId = userId;
        this.username = username;
        this.fileId = fileId;
        this.documentId = documentId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getLogsId() { return logsId; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getFileId() { return fileId; }
    public int getDocumentId() { return documentId; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }
}