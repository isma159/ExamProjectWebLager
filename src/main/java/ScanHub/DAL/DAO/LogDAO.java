package ScanHub.DAL.DAO;

import ScanHub.BE.*;
import ScanHub.BE.enums.EntityType;
import ScanHub.BE.enums.LogAction;
import ScanHub.BE.enums.Role;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO implements IDataAccess<Log> {

    public LogDAO() {}

    @Override
    public List<Log> getData() throws Exception {
        List<Log> logs = new ArrayList<>();

        String sql = """
            SELECT l.logsId, l.userId AS logUserId, l.entityId, l.entityType, l.action, l.log_timestamp, u.userId AS userId, u.username, u.passwordHash, u.role
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
                INSERT INTO Logs (userId, entityId, entityType, action, log_timestamp)
                OUTPUT INSERTED.logsId, INSERTED.log_timestamp
                VALUES (?, ?, ?, ?, SYSUTCDATETIME())
                """;

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, log.getUser().getUserId());
            ps.setInt(2, log.getEntityId());
            ps.setString(3, log.getEntityType().toString());
            ps.setString(4, log.getAction().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Log(rs.getInt("logsId"),
                            log.getUser(),
                            log.getEntityId(),
                            log.getEntityType(),
                            log.getAction(),
                            rs.getTimestamp("log_timestamp").toLocalDateTime());
                }
            }

            throw new SQLException("Insert returned no logsId");

        } catch (SQLException e) {
            throw new Exception("Could not create log", e);
        }
    }

    private Log mapRow(ResultSet rs) throws SQLException {
        return new Log(
                rs.getInt("logsId"),
                new User(rs.getInt("userId"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        Role.valueOf(rs.getString("role"))),
                rs.getInt("entityId"),
                EntityType.valueOf(rs.getString("entityType")),
                LogAction.valueOf(rs.getString("action")),
                rs.getTimestamp("log_timestamp").toLocalDateTime()
        );
    }
}
