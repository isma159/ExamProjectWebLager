package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.File;
import ScanHub.BE.FileAdjustmentSettings;
import ScanHub.DAL.DB.DBConnector;
import ScanHub.DAL.interfaces.IDataAccess;

// java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public FileDAO() {}

    /**
     * Inserts a new scanned TIFF file into the Files table.
     * sortId is initially set equal to referenceId - users can reorder later.
     */
    public File createFile(int documentId, int referenceId, int sortId, byte[] imageData) throws SQLException {
        String sql = """
                INSERT INTO Files (documentId, referenceId, sortId, imageData, fileSizeBytes)
                OUTPUT INSERTED.fileId, INSERTED.created_at
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, documentId);
            ps.setInt(2, referenceId);
            ps.setInt(3, sortId);
            ps.setBytes(4, imageData);
            ps.setInt(5, imageData.length);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                File file = new File();
                file.setFileId(rs.getInt("fileId"));
                file.setDocumentId(documentId);
                file.setReferenceId(referenceId);
                file.setSortId(sortId);
                file.setImageData(imageData);
                file.setFileSizeBytes(imageData.length);
                file.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return file;
            }
            throw new SQLException("Insert returned no fileId");
        }
    }

    /**
     * Loads raw image data for preview generation and exporting.
     * JavaFX Image previews are cached lazily in the File BE object
     * to avoid repeated TIFF decoding and PNG re-encoding.
     */
    public byte[] loadImageData(int fileId) throws SQLException {
        String sql = "SELECT imageData FROM Files WHERE fileId = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("imageData");
            throw new SQLException("File not found: " + fileId);
        }
    }

    /**
     * Inserts or updates (upserts) the adjustment settings for a file.
     * If a settings row already exists for the given fileId, it is updated.
     * If no row exists, a new one is inserted.
     */
    public void upsertFileSettings(int fileId, FileAdjustmentSettings settings) throws SQLException {
        if (settings == null) settings = new FileAdjustmentSettings();

        String updateSql = """
                UPDATE FileAdjustmentSettings
                SET rotation = ?, hue = ?, brightness = ?, contrast = ?, saturation = ?,
                    modified_at = SYSUTCDATETIME(), deleted_at = NULL
                WHERE fileId = ?
                """;
        String insertSql = """
                INSERT INTO FileAdjustmentSettings (fileId, rotation, hue, brightness, contrast, saturation)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnector.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setInt(1, settings.getRotation());
                updatePs.setInt(2, settingValue(settings.getHue()));
                updatePs.setInt(3, settingValue(settings.getBrightness()));
                updatePs.setInt(4, settingValue(settings.getContrast()));
                updatePs.setInt(5, settingValue(settings.getSaturation()));
                updatePs.setInt(6, fileId);

                int updated = updatePs.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, fileId);
                        insertPs.setInt(2, settings.getRotation());
                        insertPs.setInt(3, settingValue(settings.getHue()));
                        insertPs.setInt(4, settingValue(settings.getBrightness()));
                        insertPs.setInt(5, settingValue(settings.getContrast()));
                        insertPs.setInt(6, settingValue(settings.getSaturation()));
                        insertPs.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<File> getFilesForDocument(int documentId) throws SQLException {
        List<File> files = new ArrayList<>();
        String sql = """
            SELECT f.fileId, f.documentId, f.referenceId, f.sortId, f.fileSizeBytes, f.created_at,
                   fas.fileAdjustmentSettingsId, fas.rotation AS adjustmentRotation,
                   fas.hue AS adjustmentHue, fas.brightness AS adjustmentBrightness,
                   fas.contrast AS adjustmentContrast, fas.saturation AS adjustmentSaturation,
                   fas.sharpness AS adjustmentSharpness
            FROM Files f
            LEFT JOIN FileAdjustmentSettings fas
                ON f.fileId = fas.fileId AND fas.deleted_at IS NULL
            WHERE f.documentId = ? AND f.deleted_at IS NULL
            ORDER BY f.sortId
            """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    File file = new File();
                    file.setFileId(rs.getInt("fileId"));
                    file.setDocumentId(rs.getInt("documentId"));
                    file.setReferenceId(rs.getInt("referenceId"));
                    file.setSortId(rs.getInt("sortId"));
                    file.setFileSizeBytes(rs.getInt("fileSizeBytes"));
                    file.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    if (rs.getObject("fileAdjustmentSettingsId") != null) {
                        file.applyCustomFileSettings(new FileAdjustmentSettings(
                                rs.getInt("adjustmentRotation"),
                                rs.getDouble("adjustmentHue"),
                                rs.getDouble("adjustmentBrightness"),
                                rs.getDouble("adjustmentContrast"),
                                rs.getDouble("adjustmentSaturation"),
                                rs.getDouble("adjustmentSharpness")
                        ));
                    }
                    files.add(file);
                }
            }
        }
        return files;
    }

    public void deleteFile(int fileId) throws SQLException {
        String sql = "UPDATE Files SET deleted_at = SYSUTCDATETIME() WHERE fileId = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fileId);
            ps.executeUpdate();
        }
    }

    public void moveFile(int fileId, int newDocumentId) throws SQLException {
        String sql = "UPDATE Files SET documentId = ? WHERE fileId = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newDocumentId);
            ps.setInt(2, fileId);
            ps.executeUpdate();
        }
    }

    private int settingValue(double value) { return (int) Math.round(value); }
}
