package ScanHub.DAL.DAO;

import ScanHub.BE.DocumentMetadata;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IMetadataDataAccess;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentMetadataDAO implements IMetadataDataAccess {

    DBConnector dbConnector = new DBConnector();

    public DocumentMetadataDAO() throws IOException {}

    @Override
    public DocumentMetadata createData(DocumentMetadata metadata) throws Exception {
        String sql = """
                INSERT INTO DocumentMetadata
                    (documentId, title, documentType, referenceNumber, author, notes, documentDate, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, metadata.getDocumentId());
            ps.setString(2, metadata.getTitle());
            ps.setString(3, metadata.getDocumentType());
            ps.setString(4, metadata.getReferenceNumber());
            ps.setString(5, metadata.getAuthor());
            ps.setString(6, metadata.getNotes());
            ps.setObject(7, metadata.getDocumentDate());

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
                SELECT metadataId, documentId, title, documentType, referenceNumber,
                       author, notes, documentDate, createdAt, updatedAt
                FROM DocumentMetadata
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
                SELECT metadataId, documentId, title, documentType, referenceNumber,
                       author, notes, documentDate, createdAt, updatedAt
                FROM DocumentMetadata
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
                SELECT metadataId, documentId, title, documentType, referenceNumber,
                       author, notes, documentDate, createdAt, updatedAt
                FROM DocumentMetadata
                WHERE documentId = ? AND deleted_at IS NULL
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
            throw new Exception("Could not fetch metadata for documentId " + documentId, e);
        }
    }

    @Override
    public void updateData(DocumentMetadata metadata) throws Exception {
        String sql = """
                UPDATE DocumentMetadata
                SET title = ?, documentType = ?, referenceNumber = ?,
                    author = ?, notes = ?, documentDate = ?, updatedAt = GETDATE()
                WHERE metadataId = ? AND deleted_at IS NULL
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, metadata.getTitle());
            ps.setString(2, metadata.getDocumentType());
            ps.setString(3, metadata.getReferenceNumber());
            ps.setString(4, metadata.getAuthor());
            ps.setString(5, metadata.getNotes());
            ps.setObject(6, metadata.getDocumentDate());
            ps.setInt(7, metadata.getMetadataId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not update metadata with id " + metadata.getMetadataId(), e);
        }
    }

    @Override
    public void deleteData(DocumentMetadata metadata) throws Exception {
        // Soft delete — consistent with how Users are deleted in this project
        String sql = "UPDATE DocumentMetadata SET deleted_at = GETDATE() WHERE metadataId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, metadata.getMetadataId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new Exception("Could not delete metadata with id " + metadata.getMetadataId(), e);
        }
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private DocumentMetadata mapRow(ResultSet rs) throws SQLException {
        Timestamp docDate  = rs.getTimestamp("documentDate");
        Timestamp created  = rs.getTimestamp("createdAt");
        Timestamp updated  = rs.getTimestamp("updatedAt");

        return new DocumentMetadata(
                rs.getInt("metadataId"),
                rs.getInt("documentId"),
                rs.getString("title"),
                rs.getString("documentType"),
                rs.getString("referenceNumber"),
                rs.getString("author"),
                rs.getString("notes"),
                docDate  != null ? docDate.toLocalDateTime()  : null,
                created  != null ? created.toLocalDateTime()  : null,
                updated  != null ? updated.toLocalDateTime()  : null
        );
    }
}