package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.*;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

// java imports
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
        String sql = "INSERT INTO Profiles (profileName, splitBehavior, status, exportLabel) VALUES (?, ?, ?, ?)";
        String insertJunctionSQL = "INSERT INTO UserProfiles (userId, profileId) VALUES (?, ?)";

        try (Connection connection = dbConnector.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connection.prepareStatement(insertJunctionSQL)) {

                ps.setString(1, newProfile.getProfileName());
                ps.setString(2, newProfile.getSplitBehavior().toString());
                ps.setString(3, newProfile.getStatus().toString());
                ps.setString(4, newProfile.getExportLabel());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                Profile createdProfile = null;

                if (rs.next()) {

                    for (User user: newProfile.getUsers()) {

                        ps2.setInt(1, user.getUserId());
                        ps2.setInt(2, rs.getInt(1));
                        ps2.addBatch();

                    }

                    ps2.executeBatch();

                    createdProfile = new Profile(rs.getInt(1),
                            newProfile.getProfileName(),
                            newProfile.getSplitBehavior(),
                            newProfile.getStatus(),
                            newProfile.getExportLabel());

                    createdProfile.setUsers(newProfile.getUsers());
                }

                connection.commit();
                return createdProfile;
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

        String selectProfileSQL = "SELECT profileId, profileName, splitBehavior, status, exportLabel FROM Profiles WHERE deleted_at IS NULL";
        String selectUsersSQL = "SELECT u.userId, u.username, u.passwordHash, u.role FROM Users u JOIN UserProfiles up ON u.userId = up.userId WHERE up.profileId = ? AND u.deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection()) {

            PreparedStatement ps = connection.prepareStatement(selectProfileSQL);
            PreparedStatement ps2 = connection.prepareStatement(selectUsersSQL);
            ResultSet rs = ps.executeQuery();

            List<User> users = new ArrayList<>();

            while (rs.next()) {

                ps2.setInt(1, rs.getInt(1));
                ResultSet rs2 = ps2.executeQuery();

                while (rs2.next()) {

                    users.add(new User(rs2.getInt("userId"),
                            rs2.getString("username"),
                            rs2.getString("passwordHash"),
                            Role.valueOf(rs2.getString("role"))));

                }

                int profileId = rs.getInt("profileId");
                String profileName = rs.getString("profileName");
                SplitBehavior splitBehavior = SplitBehavior.valueOf(rs.getString("splitBehavior"));
                ProfileStatus status = ProfileStatus.valueOf(rs.getString("status"));
                String exportLabel = rs.getString("exportLabel");

                Profile profile = new Profile(profileId, profileName, splitBehavior, status, exportLabel);
                profile.setUsers(users);

                profiles.add(profile);

            }
        } catch (SQLException e) {
            throw new Exception("Could not get profiles", e);
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
        String deleteJunctionSQL = "DELETE FROM UserProfiles WHERE profileId = ?";
        String insertJunctionSQL = "INSERT INTO UserProfiles (userId, profileId) VALUES (?, ?)";

        try (Connection connection = dbConnector.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql);
            PreparedStatement ps2 = connection.prepareStatement(deleteJunctionSQL);
            PreparedStatement ps3 = connection.prepareStatement(insertJunctionSQL)) {

                ps.setString(1, newData.getProfileName());
                ps.setString(2, newData.getSplitBehavior().toString());
                ps.setString(3, newData.getStatus().toString());
                ps.setString(4, newData.getExportLabel());
                ps.setInt(5, newData.getProfileId());
                ps.executeUpdate();

                ps2.setInt(1, newData.getProfileId());
                ps2.executeUpdate();

                for (User user : newData.getUsers()) {

                    ps3.setInt(1, user.getUserId());
                    ps3.setInt(2, newData.getProfileId());
                    ps3.addBatch();

                }

                ps3.executeBatch();

                connection.commit();
            }
            catch (SQLException e) {
                connection.rollback();
                throw new Exception("Failed to update profile in database", e);
            }
            finally {
                connection.setAutoCommit(true);
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

            }
            catch (SQLException e) {
                connection.rollback();
                throw new Exception("Failed to delete profile", e);
            }
            finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new Exception("Could not delete profile", e);
        }
    }
}