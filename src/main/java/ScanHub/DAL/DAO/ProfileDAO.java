package ScanHub.DAL.DAO;

import ScanHub.BE.Profile;
import ScanHub.BE.ProfileStatus;
import ScanHub.BE.SplitBehavior;
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

    public ProfileDAO() throws IOException {
    }

    @Override
    public Profile createData(Profile newProfile) throws Exception {
        String sql = "INSERT INTO Profiles (profileName, splitBehavior, status, exportLabel) VALUES (?, ?, ?, ?)";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, newProfile.getProfileName());
            ps.setString(2, newProfile.getSplitBehavior().toString());
            ps.setString(3, newProfile.getStatus().toString());
            ps.setString(4, newProfile.getExportLabel());
            ps.executeUpdate();

            Profile createdProfile = null;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    createdProfile = new Profile(rs.getInt(1),
                            newProfile.getProfileName(),
                            newProfile.getSplitBehavior(),
                            newProfile.getStatus(),
                            newProfile.getExportLabel());
                }
            }

            return createdProfile;

        } catch (SQLException e) {
            throw new Exception("Could not create user", e);
        }
    }

    @Override
    public List<Profile> getData() throws Exception {

        List<Profile> profiles = new ArrayList<>();
        String sql = "SELECT profileId, profileName, splitBehavior, status, exportLabel FROM Profiles WHERE deleted_at IS NULL ORDER BY profileName";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int profileId = rs.getInt("profileId");
                String profileName = rs.getString("profileName");
                SplitBehavior splitBehavior = SplitBehavior.valueOf(rs.getString("splitBehavior"));
                ProfileStatus status = ProfileStatus.valueOf(rs.getString("status"));
                String exportLabel = rs.getString("exportLabel");

                profiles.add(new Profile(profileId, profileName, splitBehavior, status, exportLabel));
            }
        } catch (SQLException e) {
            throw new Exception("Could not get users", e);
        }

        return profiles;
    }

    @Override
    public Profile getDataFromName(String name) throws Exception {
        return null;
    }

    @Override
    public void updateData(Profile newData) throws Exception {
        String sql = "UPDATE Profiles SET profileName = ?, splitBehavior = ?, status = ?, exportLabel = ? WHERE profileId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newData.getProfileName());
            ps.setString(2, newData.getSplitBehavior().toString());
            ps.setString(3, newData.getStatus().toString());
            ps.setString(4, newData.getExportLabel());
            ps.setInt(5, newData.getProfileId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not update user", e);
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
            throw new Exception("Could not delete user", e);
        }
    }
}
