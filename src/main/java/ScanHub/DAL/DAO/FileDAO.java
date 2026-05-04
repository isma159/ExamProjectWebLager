package ScanHub.DAL.DAO;

import ScanHub.BE.File;
import ScanHub.DAL.DB.DBConnector;

import java.io.IOException;
import java.sql.*;

/**
 * TODO: make DB reflect
 */
public class FileDAO {

    private final DBConnector dbConnector;

    public FileDAO() throws IOException {
        this.dbConnector = new DBConnector();
    }

    /**
     * Inserts a new scanned TIFF file into the Files table.
     * sortId is initially set equal to referenceId — users can reorder later.
     */
    public File createFile(int documentId, int referenceId, byte[] imageData) throws SQLException {
        String sql = "INSERT INTO Files (documentId, referenceId, sortId, imageData, fileSizeBytes) " +
                "OUTPUT INSERTED.fileId, INSERTED.createdAt " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, documentId);
            ps.setInt(2, referenceId);
            ps.setInt(3, referenceId); // default sortId = arrival order
            ps.setBytes(4, imageData);
            ps.setInt(5, imageData.length);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                File file = new File();
                file.setFileId(rs.getInt("fileId"));
                file.setDocumentId(documentId);
                file.setReferenceId(referenceId);
                file.setSortId(referenceId);
                file.setImageData(imageData);
                file.setFileSizeBytes(imageData.length);
                file.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                return file;
            }
            throw new SQLException("Insert returned no fileId");
        }
    }

    /**
     * Loads imageData only when actively needed (viewing/exporting).
     * The File BE object intentionally omits imageData in list views.
     */
    public byte[] loadImageData(int fileId) throws SQLException {
        String sql = "SELECT imageData FROM Files WHERE fileId = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("imageData");
            throw new SQLException("File not found: " + fileId);
        }
    }
}