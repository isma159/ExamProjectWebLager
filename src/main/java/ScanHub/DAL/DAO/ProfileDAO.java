package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.*;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO implements IDataAccess<Profile> {

    DBConnector dbConnector = new DBConnector();

    public ProfileDAO() throws IOException {}

    @Override
    public Profile createData(Profile newProfile) throws Exception {
        String sql = "INSERT INTO Profiles (clientId, profileName, splitBehavior, status, exportLabel) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dbConnector.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                int clientId = resolveClientId(connection, newProfile);

                ps.setInt(1, clientId);
                ps.setString(2, newProfile.getProfileName());
                ps.setString(3, newProfile.getSplitBehavior().toString());
                ps.setString(4, newProfile.getStatus().toString());
                ps.setString(5, newProfile.getExportLabel());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        newProfile.setProfileId(rs.getInt(1));
                    } else {
                        throw new SQLException("No generated profileId returned");
                    }
                }

                newProfile.setClientId(clientId);
                newProfile.setClient(loadClient(connection, clientId));
                connection.commit();
                return newProfile;
            }
            catch (SQLException e) {
                connection.rollback();
                throw new Exception("Failed to create profile in database", e);
            }
            finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new Exception("Could not create profile", e);
        }
    }

    @Override
    public List<Profile> getData() throws Exception {

        List<Profile> profiles = new ArrayList<>();

        String selectProfileSQL = """
                SELECT p.profileId, p.clientId, c.clientName, p.profileName,
                       p.splitBehavior, p.status, p.exportLabel
                FROM Profiles p
                LEFT JOIN Clients c ON p.clientId = c.clientId
                WHERE p.deleted_at IS NULL
                ORDER BY c.clientName, p.profileName
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(selectProfileSQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                profiles.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Could not get profiles", e);
        }

        return profiles;
    }

    @Override
    public Profile getDataFromName(String name) throws Exception {
        String sql = """
                SELECT p.profileId, p.clientId, c.clientName, p.profileName,
                       p.splitBehavior, p.status, p.exportLabel
                FROM Profiles p
                LEFT JOIN Clients c ON p.clientId = c.clientId
                WHERE p.profileName = ? AND p.deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new Exception("Could not fetch profile from name " + name, e);
        }
    }

    @Override
    public void updateData(Profile newData) throws Exception {
        String sql = "UPDATE Profiles SET clientId = ?, profileName = ?, splitBehavior = ?, status = ?, exportLabel = ? WHERE profileId = ?";

        try (Connection connection = dbConnector.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int clientId = resolveClientId(connection, newData);

                ps.setInt(1, clientId);
                ps.setString(2, newData.getProfileName());
                ps.setString(3, newData.getSplitBehavior().toString());
                ps.setString(4, newData.getStatus().toString());
                ps.setString(5, newData.getExportLabel());
                ps.setInt(6, newData.getProfileId());
                ps.executeUpdate();

                newData.setClientId(clientId);
                newData.setClient(loadClient(connection, clientId));
                connection.commit();
            }
            catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new Exception("Could not update profile", e);
        }
    }

    @Override
    public void deleteData(Profile data) throws Exception {
        String sql = "UPDATE Profiles SET deleted_at = SYSDATETIME() WHERE profileId = ?";
        String deleteJunctionSQL = "DELETE FROM UserProfiles WHERE profileId = ?";

        try (Connection connection = dbConnector.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql);
                 PreparedStatement ps2 = connection.prepareStatement(deleteJunctionSQL)) {

                ps.setInt(1, data.getProfileId());
                ps.executeUpdate();

                ps2.setInt(1, data.getProfileId());
                ps2.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new Exception("Could not delete profile", e);
        }
    }

    private int resolveClientId(Connection connection, Profile profile) throws SQLException {
        if (profile.getClientId() > 0) {
            return profile.getClientId();
        }

        if (profile.getClient() != null && profile.getClient().getClientId() > 0) {
            return profile.getClient().getClientId();
        }

        throw new SQLException("Profile requires a selected clientId");
    }

    private Client loadClient(Connection connection, int clientId) throws SQLException {
        String sql = "SELECT clientId, clientName FROM Clients WHERE clientId = ? AND deleted_at IS NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Client(rs.getInt("clientId"), rs.getString("clientName"));
                }
            }
        }
        return null;
    }

    private Profile mapRow(ResultSet rs) throws SQLException {
        Profile profile = new Profile(
                rs.getInt("profileId"),
                rs.getInt("clientId"),
                rs.getString("profileName"),
                SplitBehavior.valueOf(rs.getString("splitBehavior")),
                ProfileStatus.valueOf(rs.getString("status")),
                rs.getString("exportLabel")
        );

        int clientId = rs.getInt("clientId");
        String clientName = rs.getString("clientName");
        if (!rs.wasNull() && clientName != null) {
            profile.setClient(new Client(clientId, clientName));
        }

        return profile;
    }
}
