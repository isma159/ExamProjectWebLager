package ScanHub.DAL.DAO;

import ScanHub.BE.User;
import ScanHub.DAL.DB.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SessionDAO {

    public SessionDAO() {}

    public boolean tryInsertSession(User user, String boxName) throws SQLException {

        String insertSQL = "INSERT INTO Sessions (userId, boxName) VALUES (?, ?)";

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(insertSQL)) {

            ps.setInt(1, user.getUserId());
            ps.setString(2, boxName);

            ps.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            if (e.getErrorCode() == 2627) {return false;}
            throw new SQLException("Could not create session", e);
        }
    }

    public void refreshSession(String boxName) throws SQLException {

        String updateSQL = "UPDATE Sessions SET lockedAt = GETUTCDATE() WHERE boxName = ?";

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(updateSQL)) {

            ps.setString(1, boxName);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new SQLException("Could not refresh session", e);
        }
    }

    public void deleteSession(String boxName) throws SQLException {

        String deleteSQL = "DELETE FROM Sessions WHERE boxName = ?";

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSQL)) {

            ps.setString(1, boxName);

            ps.executeUpdate();

        }
        catch (SQLException e) {
            throw new SQLException("Could not delete session", e);
        }
    }

    public void cleanupStaleSessions() throws SQLException {

        String cleanupSQL = "DELETE FROM Sessions WHERE lockedAt < DATEADD(MINUTE, -15, GETUTCDATE())";

        try (Connection connection = DBConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(cleanupSQL)) {

            ps.executeUpdate();

        }
        catch (SQLException e) {
            throw new SQLException("Could not clean up stale sessions", e);
        }
    }
}
