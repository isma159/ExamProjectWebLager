package ScanHub.DAL.DAO;

import ScanHub.BE.Box;
import ScanHub.BE.Client;
import ScanHub.BE.FileSettings;
import ScanHub.BE.Profile;
import ScanHub.BE.enums.ProfileStatus;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BoxDAO implements IDataAccess<Box> {

    private final DBConnector dbConnector;

    public BoxDAO() throws IOException {
        this.dbConnector = new DBConnector();
    }

    @Override
    public Box createData(Box box) throws Exception {
        String sql = """
                INSERT INTO Boxes (boxName, profileId)
                OUTPUT INSERTED.boxId, INSERTED.boxName, INSERTED.profileId,
                       INSERTED.created_at, INSERTED.modified_at
                VALUES (?, ?)
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, box.getBoxName());
            ps.setInt(2, box.getProfileId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Box created = mapBox(rs);
                    created.setProfile(box.getProfile());
                    return created;
                }
            }

            throw new SQLException("Insert returned no boxId");
        } catch (SQLException e) {
            throw new Exception("Could not create box", e);
        }
    }

    @Override
    public List<Box> getData() throws Exception {
        List<Box> boxes = new ArrayList<>();
        String sql = baseSelectSql() + " WHERE b.deleted_at IS NULL ORDER BY b.created_at DESC";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                boxes.add(mapJoinedBox(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Could not get boxes", e);
        }

        return boxes;
    }

    @Override
    public Box getDataFromName(String name) throws Exception {
        String sql = baseSelectSql() + " WHERE b.boxName = ? AND b.deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapJoinedBox(rs) : null;
            }
        } catch (SQLException e) {
            throw new Exception("Could not fetch box from name " + name, e);
        }
    }

    public Box getDataFromId(int boxId) throws Exception {
        String sql = baseSelectSql() + " WHERE b.boxId = ? AND b.deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, boxId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapJoinedBox(rs) : null;
            }
        } catch (SQLException e) {
            throw new Exception("Could not fetch box from id " + boxId, e);
        }
    }

    @Override
    public void updateData(Box box) throws Exception {
        String sql = "UPDATE Boxes SET boxName = ?, profileId = ?, modified_at = SYSDATETIME() WHERE boxId = ? AND deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, box.getBoxName());
            ps.setInt(2, box.getProfileId());
            ps.setInt(3, box.getBoxId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Could not update box", e);
        }
    }

    @Override
    public void deleteData(Box box) throws Exception {
        String sql = "UPDATE Boxes SET deleted_at = SYSDATETIME() WHERE boxId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, box.getBoxId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Could not delete box", e);
        }
    }

    private String baseSelectSql() {
        return """
                SELECT b.boxId, b.boxName, b.profileId, b.created_at, b.modified_at,
                       p.clientId, p.profileName, p.status, p.exportLabel, p.fileSettingsId,
                       c.clientName, fs.hue, fs.brightness, fs.contrast, fs.saturation
                FROM Boxes b
                JOIN Profiles p ON b.profileId = p.profileId
                JOIN FileSettings fs ON p.fileSettingsId = fs.fileSettingsId
                LEFT JOIN Clients c ON p.clientId = c.clientId
                """;
    }

    private Box mapJoinedBox(ResultSet rs) throws SQLException {
        Box box = mapBox(rs);

        Profile profile = new Profile(
                rs.getInt("profileId"),
                new Client(rs.getInt("clientId"),
                        rs.getString("clientName")),
                rs.getString("profileName"),
                ProfileStatus.valueOf(rs.getString("status")),
                rs.getString("exportLabel"),
                new FileSettings(rs.getInt("fileSettingsId"),
                        rs.getDouble("hue"),
                        rs.getDouble("brightness"),
                        rs.getDouble("contrast"),
                        rs.getDouble("saturation"))
        );

        box.setProfile(profile);
        return box;
    }

    private Box mapBox(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp modifiedAt = rs.getTimestamp("modified_at");

        return new Box(
                rs.getInt("boxId"),
                rs.getString("boxName"),
                rs.getInt("profileId"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                modifiedAt != null ? modifiedAt.toLocalDateTime() : null
        );
    }
}
