package ScanHub.DAL.DAO;

import ScanHub.BE.Box;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class BoxDAO implements IDataAccess<Box> {

    DBConnector dbConnector = new DBConnector();

    public BoxDAO() throws IOException {}

    @Override
    public Box createData(Box data) throws Exception {
        String sql = """
                INSERT INTO Boxes (boxName, profileId)
                OUTPUT INSERTED.boxId, INSERTED.boxName, INSERTED.profileId, INSERTED.created_at, INSERTED.modified_at
                VALUES (?, ?)
                """;
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, data.getBoxName());
            ps.setInt(2, data.getProfileId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Box created = mapBox(rs);
                    created.setProfile(data.getProfile());
                    return created;
                }
            }
            throw new SQLException("Insert returned no boxId");
        } catch (SQLException e) {
            throw new Exception("Could not create Box");
        }
    }

    @Override
    public List<Box> getData() throws Exception {
        return List.of();
    }

    @Override
    public Box getDataFromName(String name) throws Exception {
        return null;
    }

    @Override
    public void updateData(Box newData) throws Exception {

    }

    @Override
    public void deleteData(Box data) throws Exception {
        String sql = "UPDATE Boxes SET deleted_at = SYSDATETIME() WHERE boxId = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, data.getBoxId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Could not delete box", e);
        }
    }


    private Box mapBox(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp modifiedAt = rs.getTimestamp("modified_at");

        return new Box(
                rs.getInt("boxId"),
                rs.getString("boxName"),
                rs.getInt("profileId"),
                createdAt != null ? createdAt.toLocalDateTime() : null, // is createdAt  null then set time, if
                modifiedAt != null ? modifiedAt.toLocalDateTime() : null
        );
    }
}
