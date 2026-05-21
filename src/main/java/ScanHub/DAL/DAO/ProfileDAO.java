package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.*;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO implements IDataAccess<Profile> {

    private final DBConnector dbConnector = new DBConnector();

    public ProfileDAO() throws IOException {}

    @Override
    public Profile createData(Profile newProfile) throws Exception {
        String sql = """
                INSERT INTO Profiles
                    (clientId, profileName, status, exportLabel, rotation, hue, brightness, contrast, saturation)
                OUTPUT INSERTED.profileId
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            FileAdjustmentSettings settings = safeSettings(newProfile.getFileAdjustmentSettings());
            ps.setInt(1, newProfile.getClient().getClientId());
            ps.setString(2, newProfile.getProfileName());
            ps.setString(3, newProfile.getStatus().toString());
            ps.setString(4, newProfile.getExportLabel());
            bindSettings(ps, settings, 5);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    newProfile.setProfileId(rs.getInt("profileId"));
                    return newProfile;
                }
            }

            throw new SQLException("No generated profileId returned");
        } catch (SQLException e) {
            throw new Exception("Could not create profile", e);
        }
    }

    @Override
    public List<Profile> getData() throws Exception {
        List<Profile> profiles = new ArrayList<>();

        String selectProfileSQL = """
                SELECT p.profileId, p.clientId, p.profileName, p.status,
                       p.exportLabel, p.rotation, p.hue, p.brightness,
                       p.contrast, p.saturation, c.clientName
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
                SELECT TOP 1 p.profileId, p.clientId, p.profileName,
                       p.status, p.exportLabel, p.rotation, p.hue,
                       p.brightness, p.contrast, p.saturation, c.clientName
                FROM Profiles p
                LEFT JOIN Clients c ON p.clientId = c.clientId
                WHERE p.profileName = ? AND p.deleted_at IS NULL
                ORDER BY c.clientName, p.profileName
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
        String sql = """
                UPDATE Profiles
                SET clientId = ?, profileName = ?, status = ?, exportLabel = ?,
                    rotation = ?, hue = ?, brightness = ?, contrast = ?, saturation = ?
                WHERE profileId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            FileAdjustmentSettings settings = safeSettings(newData.getFileAdjustmentSettings());
            ps.setInt(1, newData.getClient().getClientId());
            ps.setString(2, newData.getProfileName());
            ps.setString(3, newData.getStatus().toString());
            ps.setString(4, newData.getExportLabel());
            bindSettings(ps, settings, 5);
            ps.setInt(10, newData.getProfileId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Could not update profile", e);
        }
    }

    @Override
    public void deleteData(Profile data) throws Exception {
        String sql = "UPDATE Profiles SET deleted_at = SYSUTCDATETIME() WHERE profileId = ?";
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
        return new Profile(
                rs.getInt("profileId"),
                new Client(rs.getInt("clientId"), rs.getString("clientName")),
                rs.getString("profileName"),
                ProfileStatus.valueOf(rs.getString("status")),
                rs.getString("exportLabel"),
                mapSettings(rs)
        );
    }

    private FileAdjustmentSettings mapSettings(ResultSet rs) throws SQLException {
        return new FileAdjustmentSettings(
                rs.getInt("rotation"),
                rs.getInt("hue"),
                rs.getInt("brightness"),
                rs.getInt("contrast"),
                rs.getInt("saturation")
        );
    }

    private FileAdjustmentSettings safeSettings(FileAdjustmentSettings settings) {
        return settings == null ? new FileAdjustmentSettings() : settings;
    }

    private void bindSettings(PreparedStatement ps, FileAdjustmentSettings settings, int startIndex) throws SQLException {
        ps.setInt(startIndex, settings.getRotation());
        ps.setInt(startIndex + 1, settingValue(settings.getHue()));
        ps.setInt(startIndex + 2, settingValue(settings.getBrightness()));
        ps.setInt(startIndex + 3, settingValue(settings.getContrast()));
        ps.setInt(startIndex + 4, settingValue(settings.getSaturation()));
    }

    private int settingValue(double value) { return (int) Math.round(value); }
}
