package ScanHub.DAL.DAO;

// project imports
import ScanHub.BE.File;
import ScanHub.DAL.DB.DBConnector;

// java imports
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
    public File createFile(int documentId, int referenceId, byte[] imageData, int brightness, int contrast) throws SQLException {
        // Apply brightness and contrast before saving
        imageData = applyAdjustments(imageData, brightness, contrast);

        String sql = "INSERT INTO Files (documentId, referenceId, sortId, imageData, fileSizeBytes, brightness, contrast) " +
                "OUTPUT INSERTED.fileId, INSERTED.createdAt " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, documentId);
            ps.setInt(2, referenceId);
            ps.setInt(3, referenceId);
            ps.setBytes(4, imageData);
            ps.setInt(5, imageData.length);
            ps.setInt(6, brightness);
            ps.setInt(7, contrast);

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
                file.setBrightness(brightness);
                file.setContrast(contrast);
                return file;
            }
            throw new SQLException("Insert returned no fileId");
        }
    }

    private byte[] applyAdjustments(byte[] imageData, int brightness, int contrast) {
        try {
            // Read image from bytes
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(
                    new java.io.ByteArrayInputStream(imageData));

            if (img == null) return imageData; // not a readable image format, skip

            // Apply brightness and contrast via RescaleOp
            // contrast: factor around 1.0 (0 = no change, mapped from -100..100)
            // brightness: offset added to each pixel (-100..100 mapped to -255..255)
            float contrastFactor = 1.0f + (contrast / 100.0f);
            float brightnessOffset = brightness * 2.55f;

            java.awt.image.RescaleOp op = new java.awt.image.RescaleOp(
                    contrastFactor, brightnessOffset, null);
            img = op.filter(img, null);

            // Write back to bytes as TIFF
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "TIFF", out);
            return out.toByteArray();

        } catch (Exception e) {
            // If adjustment fails for any reason, return original data untouched
            e.printStackTrace();
            return imageData;
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