package ScanHub.BE;

import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;

import java.time.LocalDateTime;

public class Log {
    private int logId;
    private User user;
    private int entityId;
    private EntityType entityType;
    private LogAction action;
    private LocalDateTime timestamp;

    public Log(int logId, User user, int entityId, EntityType entityType, LogAction action, LocalDateTime timestamp) {
        this.logId = logId;
        this.user = user;
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.timestamp = timestamp;
    }

    public Log(User user, int entityId, EntityType entityType, LogAction action, LocalDateTime timestamp) {
        this.user = user;
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getLogId() {
        return logId;
    }
    public User getUser() {
        return user;
    }
    public int getEntityId() {
        return entityId;
    }
    public EntityType getEntityType() {
        return entityType;
    }
    public LogAction getAction() {
        return action;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    public void setAction(LogAction action) {
        this.action = action;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return user.getUsername() + " " + action.toString() + "-" + entityType.toString() + "-" + entityId;
    }
}