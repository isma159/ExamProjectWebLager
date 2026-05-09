package ScanHub.DAL.DAO;

import ScanHub.BE.Log;
import ScanHub.DAL.DB.DBConnector;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    DBConnector dbConnector = new DBConnector();

    public LogDAO() throws IOException {}

    public List<Log> getFilteredLogs(String search, String action, LocalDate from, LocalDate to) throws Exception {
        List<Log> logs = new ArrayList<>();

        String sql = """
            SELECT l.logsId, l.userId, u.username, l.fileId, l.documentId, l.action, l.log_timestamp
            FROM Logs l
            JOIN Users u ON l.userId = u.userId
            WHERE l.deleted_at IS NULL
            AND (? IS NULL OR LOWER(u.username) LIKE ?)
            AND (? IS NULL OR l.action = ?)
            AND (? IS NULL OR CAST(l.log_timestamp AS DATE) >= ?)
            AND (? IS NULL OR CAST(l.log_timestamp AS DATE) <= ?)
            ORDER BY l.log_timestamp DESC
        """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            setLikeParam(ps, 1, 2, search);
            setExactParam(ps, 3, 4, action);
            setDateParam(ps, 5, 6, from);
            setDateParam(ps, 7, 8, to);

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

    public void createLog(int userId, int fileId, int documentId, String action) throws Exception {
        String sql = "INSERT INTO Logs (userId, fileId, documentId, action, log_timestamp) VALUES (?, ?, ?, ?, GETDATE())";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, fileId);
            ps.setInt(3, documentId);
            ps.setString(4, action);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Could not create log", e);
        }
    }

    private Log mapRow(ResultSet rs) throws SQLException {
        return new Log(
                rs.getInt("logsId"),
                rs.getInt("userId"),
                rs.getString("username"),
                rs.getInt("fileId"),
                rs.getInt("documentId"),
                rs.getString("action"),
                rs.getTimestamp("log_timestamp").toLocalDateTime()
        );
    }

    private void setLikeParam(PreparedStatement ps, int nullIdx, int valIdx, String val) throws SQLException {
        if (val == null || val.isBlank()) {
            ps.setNull(nullIdx, Types.NVARCHAR);
            ps.setNull(valIdx, Types.NVARCHAR);
        } else {
            ps.setString(nullIdx, val);
            ps.setString(valIdx, "%" + val.toLowerCase() + "%");
        }
    }

    private void setExactParam(PreparedStatement ps, int nullIdx, int valIdx, String val) throws SQLException {
        if (val == null || val.isBlank()) {
            ps.setNull(nullIdx, Types.NVARCHAR);
            ps.setNull(valIdx, Types.NVARCHAR);
        } else {
            ps.setString(nullIdx, val);
            ps.setString(valIdx, val);
        }
    }

    private void setDateParam(PreparedStatement ps, int nullIdx, int valIdx, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(nullIdx, Types.DATE);
            ps.setNull(valIdx, Types.DATE);
        } else {
            ps.setDate(nullIdx, Date.valueOf(date));
            ps.setDate(valIdx, Date.valueOf(date));
        }
    }
}