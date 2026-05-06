package ScanHub.DAL.DAO;

import ScanHub.BE.DocumentMetadata;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IMetadataDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentMetadataDAO implements IMetadataDataAccess {

    DBConnector dbConnector = new DBConnector();

    public DocumentMetadataDAO() throws IOException {}

    @Override
    public DocumentMetadata createData(DocumentMetadata metadata) throws Exception {
        String sql = """
                INSERT INTO BoxMetadata
                    (boxId, profileName, boxName, documentCount, fileCount, boxCreatedAt)
                VALUES (?, ?, ?, ?, ?, GETDATE())
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, metadata.getBoxId());
            ps.setString(2, metadata.getProfileName());
            ps.setString(3, metadata.getBoxName());
            ps.setInt(4, metadata.getDocumentCount());
            ps.setInt(5, metadata.getFileCount());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                metadata.setMetadataId(rs.getInt(1));
            }

            return metadata;

        } catch (SQLException e) {
            throw new Exception("Could not create document metadata", e);
        }
    }

    @Override
    public List<DocumentMetadata> getData() throws Exception {
        List<DocumentMetadata> metadataList = new ArrayList<>();

        String sql = """
                SELECT metadataId, boxId, profileName, boxName, documentCount, fileCount, boxCreatedAt
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
            throw new Exception("Could not retrieve document metadata list", e);
        }

        return metadataList;
    }

    @Override
    public DocumentMetadata getDataFromId(int metadataId) throws Exception {
        String sql = """
                SELECT metadataId, boxId, profileName, boxName, documentCount, fileCount, boxCreatedAt
                FROM BoxMetadata
                WHERE metadataId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, metadataId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new Exception("Could not fetch metadata with id " + metadataId, e);
        }
    }

    @Override
    public DocumentMetadata getDataByDocumentId(int documentId) throws Exception {
        String sql = """
                SELECT metadataId, boxId, profileName, boxName, documentCount, fileCount, boxCreatedAt
                FROM BoxMetadata
                WHERE boxId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, documentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new Exception("Could not fetch metadata for boxId " + documentId, e);
        }
    }

    @Override
    public void updateData(DocumentMetadata metadata) throws Exception {
        String sql = """
                UPDATE BoxMetadata
                SET profileName = ?, boxName = ?, documentCount = ?, fileCount = ?
                WHERE metadataId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, metadata.getProfileName());
            ps.setString(2, metadata.getBoxName());
            ps.setInt(3, metadata.getDocumentCount());
            ps.setInt(4, metadata.getFileCount());
            ps.setInt(5, metadata.getMetadataId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not update metadata with id " + metadata.getMetadataId(), e);
        }
    }

    @Override
    public void deleteData(DocumentMetadata metadata) throws Exception {
        String sql = "UPDATE BoxMetadata SET deleted_at = GETDATE() WHERE metadataId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, metadata.getMetadataId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not delete metadata with id " + metadata.getMetadataId(), e);
        }
    }

    private DocumentMetadata mapRow(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("boxCreatedAt");

        return new DocumentMetadata(
                rs.getInt("metadataId"),
                rs.getInt("boxId"),
                rs.getString("profileName"),
                rs.getString("boxName"),
                rs.getInt("documentCount"),
                rs.getInt("fileCount"),
                created != null ? created.toLocalDateTime() : null
        );
    }
}