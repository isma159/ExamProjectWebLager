package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.*;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.BE.enums.SplitBehavior;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO implements IDataAccess<Profile> {

    DBConnector dbConnector = new DBConnector();

    public ProfileDAO() throws IOException {}

    @Override
    public Profile createData(Profile newProfile) throws Exception {
        String sql = "INSERT INTO Profiles (clientId, profileName, splitBehavior, status, exportLabel, fileSettingsId) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dbConnector.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, newProfile.getClient().getClientId());
                ps.setString(2, newProfile.getProfileName());
                ps.setString(3, newProfile.getSplitBehavior().toString());
                ps.setString(4, newProfile.getStatus().toString());
                ps.setString(5, newProfile.getExportLabel());
                ps.setInt(6, getOrCreateFileSettings(connection, newProfile.getFileSettings()));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        newProfile.setProfileId(rs.getInt(1));
                    } else {
                        throw new SQLException("No generated profileId returned");
                    }
                }

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
                SELECT p.profileId, p.clientId, p.profileName,
                       p.splitBehavior, p.status, p.exportLabel, fs.hue, fs.brightness,
                       fs.contrast, fs.saturation, c.clientName
                FROM Profiles p
                LEFT JOIN Clients c ON p.clientId = c.clientId
                JOIN FileSettings fs ON p.fileSettingsId = fs.fileSettingsId
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
                SELECT p.profileId, p.clientId, p.profileName,
                       p.splitBehavior, p.status, p.exportLabel, fs.hue, fs.brightness,
                       fs.contrast, fs.saturation, c.clientName
                FROM Profiles p
                LEFT JOIN Clients c ON p.clientId = c.clientId
                JOIN FileSettings fs ON p.fileSettingsId = fs.fileSettingsId
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
        String sql = "UPDATE Profiles SET clientId = ?, profileName = ?, splitBehavior = ?, status = ?, exportLabel = ?, fileSettingsId = ? WHERE profileId = ?";

        try (Connection connection = dbConnector.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setInt(1, newData.getClient().getClientId());
                ps.setString(2, newData.getProfileName());
                ps.setString(3, newData.getSplitBehavior().toString());
                ps.setString(4, newData.getStatus().toString());
                ps.setString(5, newData.getExportLabel());
                ps.setInt(6, getOrCreateFileSettings(connection, newData.getFileSettings()));
                ps.setInt(8, newData.getProfileId());
                ps.executeUpdate();

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

    private Profile mapRow(ResultSet rs) throws SQLException {
        Profile profile = new Profile(
                rs.getInt("profileId"),
                new Client(rs.getInt("clientId"),
                        rs.getString("clientName")),
                rs.getString("profileName"),
                SplitBehavior.valueOf(rs.getString("splitBehavior")),
                ProfileStatus.valueOf(rs.getString("status")),
                rs.getString("exportLabel"),
                new FileSettings(rs.getInt("fileSettingsId"),
                        rs.getDouble("hue"),
                        rs.getDouble("brightness"),
                        rs.getDouble("contrast"),
                        rs.getDouble("saturation"))
        );

        return profile;
    }

    private int getOrCreateFileSettings(Connection connection, FileSettings fileSettings) throws SQLException {

        String selectSQL = "SELECT fileSettingsId FROM FileSettings WHERE hue = ? AND brightness = ? AND contrast = ? AND saturation = ?";

        try (PreparedStatement selectPs = connection.prepareStatement(selectSQL)) {
            selectPs.setDouble(1, fileSettings.getHue());
            selectPs.setDouble(2, fileSettings.getBrightness());
            selectPs.setDouble(3, fileSettings.getContrast());
            selectPs.setDouble(4, fileSettings.getSaturation());

            ResultSet rs = selectPs.executeQuery();

            if (rs.next()) {
                return rs.getInt("fileSettingsId");
            }
        }

        String insertSQL = "INSERT INTO FileSettings (hue, brightness, contrast, saturation) VALUES (?, ?, ?, ?)";

        try (PreparedStatement insertPS = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            insertPS.setDouble(1, fileSettings.getHue());
            insertPS.setDouble(2, fileSettings.getBrightness());
            insertPS.setDouble(3, fileSettings.getContrast());
            insertPS.setDouble(4, fileSettings.getSaturation());

            insertPS.executeUpdate();

            ResultSet rs = insertPS.getGeneratedKeys();

            if (rs.next()) {
                return rs.getInt(1);
            }

        }

        throw new SQLException("Could not get or create file settings");
    }
}