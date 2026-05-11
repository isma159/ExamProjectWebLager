package ScanHub.DAL.DAO;

import ScanHub.BE.BoxMetadata;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IMetadataDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoxMetadataDAO implements IMetadataDataAccess {

    DBConnector dbConnector = new DBConnector();

    public BoxMetadataDAO() throws IOException {}

    @Override
    public BoxMetadata createData(BoxMetadata metadata) throws Exception {
        String sql = """
                INSERT INTO BoxMetadata (boxId, boxName, profileName, documentCount, fileCount, boxCreatedAt)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, metadata.getBoxId());
            ps.setString(2, metadata.getBoxName());
            ps.setString(3, metadata.getProfileName());
            ps.setInt(4, metadata.getDocumentCount());
            ps.setInt(5, metadata.getFileCount());
            ps.setObject(6, metadata.getBoxCreatedAt());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new BoxMetadata(
                            rs.getInt(1),
                            metadata.getBoxId(),
                            metadata.getProfileName(),
                            metadata.getDocumentCount(),
                            metadata.getFileCount(),
                            metadata.getBoxCreatedAt()
                    );
                }
            }

            throw new SQLException("Insert returned no generated key for BoxMetadata");

        } catch (SQLException e) {
            throw new Exception("Could not create box metadata", e);
        }
    }

    @Override
    public List<BoxMetadata> getData() throws Exception {
        List<BoxMetadata> metadataList = new ArrayList<>();

        String sql = """
                SELECT metadataId, boxId, boxName, profileName, documentCount, fileCount, boxCreatedAt
                FROM BoxMetadata
                WHERE deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                metadataList.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new Exception("Could not retrieve box metadata list", e);
        }

        return metadataList;
    }

    @Override
    public BoxMetadata getDataFromId(int metadataId) throws Exception {
        String sql = """
                SELECT metadataId, boxId, boxName, profileName, documentCount, fileCount, boxCreatedAt
                FROM BoxMetadata
                WHERE metadataId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, metadataId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new Exception("Could not fetch metadata with id " + metadataId, e);
        }
    }

    @Override
    public BoxMetadata getDataByBoxId(int boxId) throws Exception {
        String sql = """
                SELECT metadataId, boxId, boxName, profileName, documentCount, fileCount, boxCreatedAt
                FROM BoxMetadata
                WHERE boxId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, boxId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new Exception("Could not fetch metadata for boxId " + boxId, e);
        }
    }

    @Override
    public void updateData(BoxMetadata metadata) throws Exception {
        String sql = """
                UPDATE BoxMetadata
                SET profileName = ?, boxName = ?, documentCount = ?, fileCount = ?, boxCreatedAt = ?
                WHERE metadataId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, metadata.getProfileName());
            ps.setString(2, metadata.getBoxName());
            ps.setInt(3, metadata.getDocumentCount());
            ps.setInt(4, metadata.getFileCount());
            ps.setObject(5, metadata.getBoxCreatedAt());
            ps.setInt(6, metadata.getMetadataId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not update metadata with id " + metadata.getMetadataId(), e);
        }
    }

    @Override
    public void deleteData(BoxMetadata metadata) throws Exception {
        String sql = "UPDATE BoxMetadata SET deleted_at = SYSDATETIME() WHERE metadataId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, metadata.getMetadataId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not delete metadata with id " + metadata.getMetadataId(), e);
        }
    }

    private BoxMetadata mapRow(ResultSet rs) throws SQLException {
        Timestamp boxCreatedAt = rs.getTimestamp("boxCreatedAt");

        BoxMetadata meta = new BoxMetadata(
                rs.getInt("metadataId"),
                rs.getInt("boxId"),
                rs.getString("profileName"),
                rs.getInt("documentCount"),
                rs.getInt("fileCount"),
                boxCreatedAt != null ? boxCreatedAt.toLocalDateTime() : null
        );

        String storedBoxName = rs.getString("boxName");
        if (storedBoxName != null) {
            meta.setBoxName(storedBoxName);
        }

        return meta;
    }
}