package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.File;
import ScanHub.DAL.DB.DBConnector;

// java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public File createFile(int documentId, int referenceId, byte[] imageData, int rotation) throws SQLException {
        String sql = """
                INSERT INTO Files (documentId, referenceId, sortId, imageData, fileSizeBytes, rotation)
                OUTPUT INSERTED.fileId, INSERTED.created_at
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, documentId);
            ps.setInt(2, referenceId);
            ps.setInt(3, referenceId); // default sortId = arrival order
            ps.setBytes(4, imageData);
            ps.setInt(5, imageData.length);
            ps.setInt(6, normalizeRotation(rotation));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                File file = new File();
                file.setFileId(rs.getInt("fileId"));
                file.setDocumentId(documentId);
                file.setReferenceId(referenceId);
                file.setSortId(referenceId);
                file.setImageData(imageData);
                file.setFileSizeBytes(imageData.length);
                file.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                file.setRotation(normalizeRotation(rotation));
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
        String sql = "SELECT imageData FROM Files WHERE fileId = ? AND deleted_at IS NULL";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("imageData");
            throw new SQLException("File not found: " + fileId);
        }
    }

    public void updateRotation(int fileId, int rotation) throws SQLException {
        String sql = "UPDATE Files SET rotation = ? WHERE fileId = ? AND deleted_at IS NULL";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, normalizeRotation(rotation));
            ps.setInt(2, fileId);
            ps.executeUpdate();
        }
    }

    public List<File> getFilesForDocument(int documentId) throws SQLException {
        List<File> files = new ArrayList<>();
        String sql = """
            SELECT fileId, documentId, referenceId, sortId, imageData, fileSizeBytes, created_at, rotation
            FROM Files
            WHERE documentId = ? AND deleted_at IS NULL
            ORDER BY sortId
            """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    File file = new File();
                    file.setFileId(rs.getInt("fileId"));
                    file.setDocumentId(rs.getInt("documentId"));
                    file.setReferenceId(rs.getInt("referenceId"));
                    file.setSortId(rs.getInt("sortId"));
                    file.setImageData(rs.getBytes("imageData")); // loads the TIFF blob
                    file.setFileSizeBytes(rs.getInt("fileSizeBytes"));
                    file.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    file.setRotation(rs.getInt("rotation"));
                    files.add(file);
                }
            }
        }
        return files;
    }

    public void deleteFile(int fileId) throws SQLException {
        String sql = "UPDATE Files SET deleted_at = SYSDATETIME() WHERE fileId = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fileId);
            ps.executeUpdate();
        }
    }

    public void moveFile(int fileId, int newDocumentId) throws SQLException {
        String sql = "UPDATE Files SET documentId = ? WHERE fileId = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newDocumentId);
            ps.setInt(2, fileId);
            ps.executeUpdate();
        }
    }

    public int countFilesForBox(int boxId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM Files f
                JOIN Documents d ON f.documentId = d.documentId
                WHERE d.boxId = ?
                  AND d.deleted_at IS NULL
                  AND f.deleted_at IS NULL
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, boxId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int normalizeRotation(int rotation) {
        int normalized = ((rotation % 360) + 360) % 360;
        return switch (normalized) {
            case 90, 180, 270 -> normalized;
            default -> 0;
        };
    }
}
