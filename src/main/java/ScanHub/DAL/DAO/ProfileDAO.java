package ScanHub.DAL.DAO;

import ScanHub.BE.Profile;
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
        String sql = "INSERT INTO Profiles (profileId, name, splitBehavior ) VALUES (?, ?, ?)";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, newProfile.getProfileId());
            ps.setString(2, newProfile.getProfileName());
            ps.setString(3, newProfile.getSplitBehaviour().toString());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                newProfile.setProfileId(rs.getInt(1));
            }

            return newProfile;

        } catch (SQLException e) {
            throw new Exception("Could not create user", e);
        }


    }

    @Override
    public List<Profile> getData() throws Exception {
        List<Profile> profiles = new ArrayList<>();
        try (Connection connection = dbConnector.getConnection()) {

            PreparedStatement ps = connection.prepareStatement("SELECT profileId, profileName, splitBehavior FROM Profiles WHERE deleted_at IS NULL");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int profileId = rs.getInt("profileId");
                String profileName = rs.getString("profileName");
                SplitBehavior splitBehavior = SplitBehavior.valueOf(rs.getString("splitBehavior"));

                profiles.add(new Profile(profileId, profileName, splitBehavior));

            }

        }
        catch (SQLException e) {

            throw new Exception("Could not get users", e);

        }

        return profiles;

    }

    @Override
    public void updateData(Profile newData) throws Exception {
        String sql = "UPDATE Profiles SET profileName = ?, splitBehavior = ? WHERE profileId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newData.getProfileName());
            ps.setString(2, newData.getSplitBehaviour().toString());
            ps.setInt(3, newData.getProfileId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not update user", e);
        }

    }

    @Override
    public void deleteData(Profile data) throws Exception {
        String sql = "UPDATE Profiles SET deleted_at = GETDATE() WHERE profileId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, data.getProfileId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not delete user", e);
        }
    }
}
