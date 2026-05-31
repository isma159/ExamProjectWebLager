package ScanHub.BE;

import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Log {
    private int logId;
    private User user;
    private String entityName;
    private EntityType entityType;
    private LogAction action;
    private ZonedDateTime timestamp;

    public Log(int logId, User user, String entityName, EntityType entityType, LogAction action) {
        this.logId = logId;
        this.user = user;
        this.entityName = entityName;
        this.entityType = entityType;
        this.action = action;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public Log(User user, String entityName, EntityType entityType, LogAction action) {
        this.user = user;
        this.entityName = entityName;
        this.entityType = entityType;
        this.action = action;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public int getLogId() {
        return logId;
    }
    public User getUser() {
        return user;
    }
    public String getEntityName() {
        return entityName;
    }
    public EntityType getEntityType() {
        return entityType;
    }
    public LogAction getAction() {
        return action;
    }
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    public void setAction(LogAction action) {
        this.action = action;
    }
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return user.getUsername() + " " + action.toString() + "-" + entityType.toString() + "-" + entityName;
    }
}