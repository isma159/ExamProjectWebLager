package ScanHub.DAL.DAO;

import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.DAL.DB.DBConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    private final DBConnector dbConnector;

    public DocumentDAO() throws IOException {
        this.dbConnector = new DBConnector();
    }

    public Document createDocument(int boxId) throws SQLException {
        String sql = """
                INSERT INTO Documents (boxId)
                OUTPUT INSERTED.documentId, INSERTED.boxId, INSERTED.created_at
                VALUES (?)
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, boxId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

            throw new SQLException("Insert returned no documentId");
        }
    }

    public List<Document> getDocumentsByBoxId(int boxId) throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = """
                SELECT documentId, boxId, created_at
                FROM Documents
                WHERE boxId = ? AND deleted_at IS NULL
                ORDER BY documentId
                """;

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, boxId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documents.add(mapRow(rs));
                }
            }
        }

        return documents;
    }

    public List<Document> getDocumentsWithFilesByBoxId(int boxId) throws SQLException, IOException {
        List<Document> documents = getDocumentsByBoxId(boxId);
        FileDAO fileDAO = new FileDAO(); // or inject it
        for (Document doc : documents) {
            List<File> files = fileDAO.getFilesForDocument(doc.getDocumentId());
            doc.getFiles().addAll(files);
        }
        return documents;
    }

    public void deleteDocument(int documentId) throws SQLException {
        String sql = "UPDATE Documents SET deleted_at = SYSDATETIME() WHERE documentId = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, documentId);
            ps.executeUpdate();
        }
    }

    public int countDocumentsForBox(int boxId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Documents WHERE boxId = ? AND deleted_at IS NULL";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, boxId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Document mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Document(
                rs.getInt("documentId"),
                rs.getInt("boxId"),
                createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }
}
