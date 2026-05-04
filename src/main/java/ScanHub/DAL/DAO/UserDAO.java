package ScanHub.DAL.DAO;

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

public class UserDAO implements IDataAccess<User> {

    DBConnector dbConnector = new DBConnector();

    public UserDAO() throws IOException {
    }

    @Override
    public User createData(User newUser) throws Exception {
        String sql = "INSERT INTO Users (username, passwordHash, role) VALUES (?, ?, ?)";
        String insertJunctionSQL = "INSERT INTO UserProfiles (userId, profileId) VALUES (?, ?)";

        try (Connection connection = dbConnector.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement ps2 = connection.prepareStatement(insertJunctionSQL)) {

                ps.setString(1, newUser.getUsername());
                ps.setString(2, newUser.getPasswordHash());
                ps.setString(3, newUser.getRole().toString());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    newUser.setUserId(rs.getInt(1));

                    for (Profile profile : newUser.getProfiles()) {

                        ps2.setInt(1, newUser.getUserId());
                        ps2.setInt(2, profile.getProfileId());
                        ps2.addBatch();

                    }
                    ps2.executeBatch();
                }

                connection.commit();
                return newUser;
            }
            catch (SQLException e) {
                connection.rollback();
                throw new Exception("Failed to create user in database", e);
            }
            finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new Exception("Could not create user", e);
        }
    }

    @Override
    public List<User> getData() throws Exception {
        List<User> users = new ArrayList<>();

        String sql = "SELECT userId, username, passwordHash, role FROM Users WHERE deleted_at IS NULL";
        String selectProfilesSQL = "SELECT p.profileId, p.profileName, p.splitBehavior, p.exportLabel, p.status FROM Profiles p JOIN UserProfiles up ON p.profileId = up.profileId WHERE up.userId = ? AND p.deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                PreparedStatement ps2 = connection.prepareStatement(selectProfilesSQL);
                ps2.setInt(1, rs.getInt("userId"));
                ResultSet rs2 = ps2.executeQuery();

                List<Profile> profiles = new ArrayList<>();

                while (rs2.next()) {

                    profiles.add(new Profile(rs2.getInt("profileId"),
                            rs2.getString("profileName"),
                            SplitBehavior.valueOf(rs2.getString("splitBehavior")),
                            ProfileStatus.valueOf(rs2.getString("status")),
                            rs2.getString("exportLabel")));
                }

                int userId = rs.getInt("userId");
                String username = rs.getString("username");
                String passwordHash = rs.getString("passwordHash");
                Role role = Role.valueOf(rs.getString("role"));

                User newUser = new User(userId, username, passwordHash, role);
                newUser.setProfiles(profiles);

                users.add(newUser);

            }
        }
        catch (SQLException e) {
            throw new Exception("Could not get users", e);
        }
        return users;
    }

    public User getDataFromName(String name) throws Exception {

        String sql = "SELECT * FROM Users WHERE username = ?";
        String selectProfilesSQL = "SELECT p.profileId, p.profileName, p.splitBehavior, p.exportLabel, p.status FROM Profiles p JOIN UserProfiles up ON p.profileId = up.profileId WHERE up.userId = ? AND p.deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            User user = null;

            if (rs.next()) {

                PreparedStatement ps2 = connection.prepareStatement(selectProfilesSQL);
                ps2.setInt(1, rs.getInt("userId"));
                ResultSet rs2 = ps2.executeQuery();

                List<Profile> profiles = new ArrayList<>();

                while (rs2.next()) {

                    profiles.add(new Profile(rs2.getInt("profileId"),
                            rs2.getString("profileName"),
                            SplitBehavior.valueOf(rs2.getString("splitBehavior")),
                            ProfileStatus.valueOf(rs2.getString("status")),
                            rs2.getString("exportLabel")));
                }

                user = new User(rs.getInt("userId"), rs.getString("username"), rs.getString("passwordHash"), Role.valueOf(rs.getString("role")));
                user.setProfiles(profiles);
            }

            return user;

        } catch (SQLException e) {
            throw new Exception("Could not fetch user from username " + name, e);
        }
    }

    @Override
    public void updateData(User updatedUser) throws Exception {
        String sql = "UPDATE Users SET username = ?, passwordHash = ?, role = ? WHERE userId = ?";
        String deleteJunctionSQL = "DELETE FROM UserProfiles WHERE userId = ?";
        String insertJunctionSQL = "INSERT INTO UserProfiles (userId, profileId) VALUES (?, ?)";

        try (Connection connection = dbConnector.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql);
            PreparedStatement ps2 = connection.prepareStatement(deleteJunctionSQL);
            PreparedStatement ps3 = connection.prepareStatement(insertJunctionSQL)) {

                ps.setString(1, updatedUser.getUsername());
                ps.setString(2, updatedUser.getPasswordHash());
                ps.setString(3, updatedUser.getRole().toString());
                ps.setInt(4, updatedUser.getUserId());
                ps.executeUpdate();

                ps2.setInt(1, updatedUser.getUserId());
                ps2.executeUpdate();

                for (Profile profile : updatedUser.getProfiles()) {

                    ps3.setInt(1, updatedUser.getUserId());
                    ps3.setInt(2, profile.getProfileId());
                    ps3.addBatch();
                }
                ps3.executeBatch();

                connection.commit();
            }
            catch (SQLException e) {
                connection.rollback();
                throw new Exception("Failed to update user in database");
            }
            finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new Exception("Could not update user", e);
        }
    }

    @Override
    public void deleteData(User selectedUser) throws Exception {
        String sql = "UPDATE Users SET deleted_at = GETDATE() WHERE userId = ?";
        String deleteJunctionSQL = "DELETE FROM UserProfiles WHERE userId = ?";

        try (Connection connection = dbConnector.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql);
                 PreparedStatement ps2 = connection.prepareStatement(deleteJunctionSQL)) {

                ps.setInt(1, selectedUser.getUserId());
                ps.executeUpdate();

                ps2.setInt(1, selectedUser.getUserId());
                ps2.executeUpdate();

                connection.commit();

            }
            catch (SQLException e) {
                connection.rollback();
                throw new Exception("Failed to delete user from database", e);
            }
            finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new Exception("Could not delete user", e);
        }
    }
}

