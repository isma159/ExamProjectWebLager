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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                 PreparedStatement profilePS = connection.prepareStatement(insertJunctionSQL)) {

                ps.setString(1, newUser.getUsername());
                ps.setString(2, newUser.getPasswordHash());
                ps.setString(3, newUser.getRole().toString());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        newUser.setUserId(rs.getInt(1));
                    } else {
                        throw new SQLException("No generated userId returned");
                    }
                }

                for (Profile profile: newUser.getProfiles()) {
                    profilePS.setInt(1, newUser.getUserId());
                    profilePS.setInt(2, profile.getProfileId());
                    profilePS.addBatch();
                }
                profilePS.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            return newUser;

        } catch (SQLException e) {
            throw new Exception("Could not create user", e);
        }
    }

    @Override
    public List<User> getData() throws Exception {
        Map<Integer, User> usersById = new LinkedHashMap<>();

        String sql = """
                SELECT u.userId, u.username, u.passwordHash, u.role,
                       p.profileId, p.profileName, p.splitBehavior, p.exportLabel, p.status
                FROM Users u
                LEFT JOIN UserProfiles up ON u.userId = up.userId
                LEFT JOIN Profiles p ON up.profileId = p.profileId AND p.deleted_at IS NULL
                WHERE u.deleted_at IS NULL
                ORDER BY u.username, p.profileName
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("userId");
                User user = usersById.get(userId);
                if (user == null) {
                    user = new User(
                            userId,
                            rs.getString("username"),
                            rs.getString("passwordHash"),
                            Role.valueOf(rs.getString("role")),
                            new ArrayList<>());
                    usersById.put(userId, user);
                }

                int profileId = rs.getInt("profileId");
                if (!rs.wasNull()) {
                    user.getProfiles().add(new Profile(profileId,
                            rs.getString("profileName"),
                            SplitBehavior.valueOf(rs.getString("splitBehavior")),
                            ProfileStatus.valueOf(rs.getString("status")),
                            rs.getString("exportLabel")));
                }
            }
        }
        catch (SQLException e) {
            throw new Exception("Could not get users", e);
        }
        return new ArrayList<>(usersById.values());
    }

    public User getDataFromName(String name) throws Exception {

        String sql = """
                SELECT u.userId, u.username, u.passwordHash, u.role,
                       p.profileId, p.profileName, p.splitBehavior, p.exportLabel, p.status
                FROM Users u
                LEFT JOIN UserProfiles up ON u.userId = up.userId
                LEFT JOIN Profiles p ON up.profileId = p.profileId AND p.deleted_at IS NULL
                WHERE u.username = ? AND u.deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                User user = null;

                while (rs.next()) {
                    if (user == null) {
                        user = new User(rs.getInt("userId"),
                                rs.getString("username"),
                                rs.getString("passwordHash"),
                                Role.valueOf(rs.getString("role")),
                                new ArrayList<>());
                    }

                    int profileId = rs.getInt("profileId");
                    if (!rs.wasNull()) {
                        user.getProfiles().add(new Profile(profileId,
                                rs.getString("profileName"),
                                SplitBehavior.valueOf(rs.getString("splitBehavior")),
                                ProfileStatus.valueOf(rs.getString("status")),
                                rs.getString("exportLabel")));
                    }
                }

                return user;
            }

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
                 PreparedStatement deleteProfilesPS = connection.prepareStatement(deleteJunctionSQL);
                 PreparedStatement insertProfilePS = connection.prepareStatement(insertJunctionSQL)) {

                ps.setString(1, updatedUser.getUsername());
                ps.setString(2, updatedUser.getPasswordHash());
                ps.setString(3, updatedUser.getRole().toString());
                ps.setInt(4, updatedUser.getUserId());
                ps.executeUpdate();

                deleteProfilesPS.setInt(1, updatedUser.getUserId());
                deleteProfilesPS.executeUpdate();

                for (Profile profile: updatedUser.getProfiles()) {
                    insertProfilePS.setInt(1, updatedUser.getUserId());
                    insertProfilePS.setInt(2, profile.getProfileId());
                    insertProfilePS.addBatch();
                }
                insertProfilePS.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
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
                 PreparedStatement deleteProfilesPS = connection.prepareStatement(deleteJunctionSQL)) {

                ps.setInt(1, selectedUser.getUserId());
                ps.executeUpdate();

                deleteProfilesPS.setInt(1, selectedUser.getUserId());
                deleteProfilesPS.executeUpdate();

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

