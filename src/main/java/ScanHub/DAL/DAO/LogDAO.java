package ScanHub.DAL.DAO;

import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.Role;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class LogDAO implements IDataAccess<Log> {

    public LogDAO() {}

    @Override
    public List<Log> getData() throws Exception {
        List<Log> logs = new ArrayList<>();

        String sql = """
            SELECT l.logsId, l.userId AS logUserId, l.entityName, l.entityType, l.action, l.log_timestamp, u.userId AS userId, u.username, u.passwordHash, u.role
            FROM Logs l
            JOIN Users u ON l.userId = u.userId
            ORDER BY l.log_timestamp ASC
        """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Could not get logs", e);
        }
        return logs;
    }

    @Override
    public Log getDataFromName(String name) throws Exception {
        return null;
    }

    @Override
    public Log getDataFromId(int id) throws Exception {
        return null;
    }

    @Override
    public void updateData(Log newData) throws Exception {

    }

    @Override
    public void deleteData(Log data) throws Exception {

    }

    @Override
    public Log createData(Log log) throws Exception {
        String sql = """
                INSERT INTO Logs (userId, entityName, entityType, action, log_timestamp)
                OUTPUT INSERTED.logsId, INSERTED.log_timestamp
                VALUES (?, ?, ?, ?, SYSUTCDATETIME())
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, log.getUser().getUserId());
            ps.setString(2, log.getEntityName());
            ps.setString(3, log.getEntityType().toString());
            ps.setString(4, log.getAction().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    LocalDateTime utcTime = rs.getTimestamp("log_timestamp").toLocalDateTime();
                    ZonedDateTime localTime = utcTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault());

                    Log newLog = new Log(rs.getInt("logsId"),
                            log.getUser(),
                            log.getEntityName(),
                            log.getEntityType(),
                            log.getAction());

                    newLog.setTimestamp(localTime);

                    return newLog;
                }
            }

            throw new SQLException("Insert returned no logsId");

        } catch (SQLException e) {
            throw new Exception("Could not create log", e);
        }
    }

    private Log mapRow(ResultSet rs) throws SQLException {

        Log log = new Log(
                rs.getInt("logsId"),
                new User(rs.getInt("userId"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        Role.valueOf(rs.getString("role"))),
                rs.getString("entityName"),
                EntityType.valueOf(rs.getString("entityType")),
                LogAction.valueOf(rs.getString("action")));

        ZonedDateTime timestamp = rs.getTimestamp("log_timestamp", Calendar.getInstance(TimeZone.getTimeZone("UTC")))
                .toInstant().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault());

        log.setTimestamp(timestamp);

        return log;
    }
}
