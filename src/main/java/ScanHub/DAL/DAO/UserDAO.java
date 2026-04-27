package ScanHub.DAL.DAO;

import ScanHub.BE.Role;
import ScanHub.BE.User;
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

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, newUser.getUsername());
            ps.setString(2, newUser.getPasswordHash());
            ps.setString(3, newUser.getRole().toString());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                newUser.setUserId(rs.getInt(1));
            }

            return newUser;

        } catch (SQLException e) {
            throw new Exception("Could not create user", e);
        }
    }

    @Override
    public List<User> getData() throws Exception {
        List<User> users = new ArrayList<>();

        String sql = "SELECT userId, username, passwordHash, role FROM Users WHERE deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("userId");
                String username = rs.getString("username");
                String passwordHash = rs.getString("passwordHash");
                Role role = Role.valueOf(rs.getString("role"));

                users.add(new User(userId, username, passwordHash, role));
            }
        }
        catch (SQLException e) {
            throw new Exception("Could not get users", e);
        }
        return users;
    }

    public User getDataFromName(String name) throws Exception {

        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            User user = null;

            if (rs.next()) {
                user = new User(rs.getInt("userId"), rs.getString("username"), rs.getString("passwordHash"), Role.valueOf(rs.getString("role")));
            }

            return user;

        } catch (SQLException e) {
            throw new Exception("Could not fetch user from username " + name, e);
        }
    }

    @Override
    public void updateData(User updatedUser) throws Exception {
        String sql = "UPDATE Users SET username = ?, passwordHash = ?, role = ? WHERE userId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, updatedUser.getUsername());
            ps.setString(2, updatedUser.getPasswordHash());
            ps.setString(3, updatedUser.getRole().toString());
            ps.setInt(4, updatedUser.getUserId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not update user", e);
        }
    }

    @Override
    public void deleteData(User selectedUser) throws Exception {
        String sql = "UPDATE Users SET deleted_at = GETDATE() WHERE userId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, selectedUser.getUserId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not delete user", e);
        }
    }
}

