package ScanHub.DAL.DAO;

import ScanHub.BE.*;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;
import ScanHub.DAL.interfaces.ILogDataAccess;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LogDAO implements ILogDataAccess {

    DBConnector dbConnector = new DBConnector();

    public LogDAO() throws IOException {}

    @Override
    public List<Log> getLogs() throws Exception {
        List<Log> logs = new ArrayList<>();

        String sql = """
            SELECT l.logsId, l.userId AS logUserId, l.entityId, l.entityType, l.action, l.log_timestamp, u.userId AS userId, u.username, u.passwordHash, u.role
            FROM Logs l
            JOIN Users u ON l.userId = u.userId
            WHERE l.deleted_at IS NULL
            ORDER BY l.log_timestamp ASC
        """;

        try (Connection connection = dbConnector.getConnection();
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
    public Log createLog(Log log) throws Exception {
        String sql = "INSERT INTO Logs (userId, entityId, entityType, action, log_timestamp) VALUES (?, ?, ?, ?, GETDATE())";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, log.getUser().getUserId());
            ps.setInt(2, log.getEntityId());
            ps.setString(3, log.getEntityType().toString());
            ps.setString(4, log.getAction().toString());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            Log newLog = null;

            if (rs.next()) {
                newLog = new Log(rs.getInt(1),
                        log.getUser(), log.getEntityId(),
                        log.getEntityType(),
                        log.getAction(),
                        log.getTimestamp());
            }

            return newLog;

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