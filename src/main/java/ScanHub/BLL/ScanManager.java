package ScanHub.BLL;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.FileAdjustmentSettings;
import ScanHub.BE.enums.ExportMode;
import ScanHub.BLL.util.BarcodeDetector;
import ScanHub.DAL.ApiClient.ScanResult;
import ScanHub.DAL.DAO.DocumentDAO;
import ScanHub.DAL.DAO.FileDAO;
import ScanHub.DAL.interfaces.IScanSource;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core scanning logic: fetches pages from the scan source, stages them in memory,
 * commits them to the database, and exports them to the local filesystem.
 */
public class ScanManager {

    private final IScanSource scanSource;
    private final DocumentDAO documentDAO;
    private final FileDAO fileDAO;

    private Document currentDocument;
    private final Box targetBox;
    private int referenceCounter = 0;

    public boolean needsBarcodeFirst; // true when box has no files and documents
    private final List<Integer> pendingDeleteFileIds = new ArrayList<>(); // list of file id's to be deleted at the next commit
    private final List<Integer> pendingDeleteDocumentIds = new ArrayList<>(); // list document id's to be deleted at the next commit

    public record StoredScan(File file, Document document, boolean barcodeSplit) {} // record returned to controller after each successful scan

    public ScanManager(IScanSource scanSource, Box targetBox) throws Exception {
        this.scanSource = scanSource;
        this.targetBox = targetBox;
        this.documentDAO = new DocumentDAO();
        this.fileDAO = new FileDAO();

        if (targetBox.getProfile() == null) {
            throw new IllegalArgumentException("A scan session box must have a profile");
        }

        List<Document> existingDocs = targetBox.getDocuments();
        applyProfileDefaults(existingDocs);
        referenceCounter = highestExistingReference(existingDocs);
        if (!existingDocs.isEmpty()) {
            currentDocument = existingDocs.getLast();
        }

        // first page must be a barcode when Box is empty (no files anywhere)
        needsBarcodeFirst = existingDocs.stream().allMatch(document -> document.getFiles().isEmpty());
    }

    /**
     * Fetches the next scan and holds it in memory only.
     * <p>
     * If this is the very first page of a brand-new (or empty) box, it always
     * fetches a barcode page so that the first document begins with a barcode.
     */
    public StoredScan fetchScan() throws Exception {
        ScanResult result = needsBarcodeFirst ? scanSource.fetchBarcodeFile() : scanSource.fetchNextScan();

        needsBarcodeFirst = false; // only enforce barcode-first on the very first scan.
        boolean barcodeSplit = false;

        // we only create a new document if there is no current document yet or the current document already contains files
        if (BarcodeDetector.containsBarcode(result.data())) {
            if (currentDocument == null || !currentDocument.getFiles().isEmpty()) {
                currentDocument = stageDocument();
            }
            barcodeSplit = true;
        } else if (currentDocument == null) { // just in case, but all documents will always start with a barcode I'm sure
            currentDocument = stageDocument();
        }

        int ref = ++referenceCounter; // generate/increment next page reference
        File file = stageFile(currentDocument, ref, result.data());
        return new StoredScan(file, currentDocument, barcodeSplit);
    }

    /**
     * Applies all pending in-memory deletes to the DB, then walks every staged
     * document and file in order, persists them and updates their real DB ids.
     */
    public void commitAll() throws Exception {
        // delete
        for (int fileId : pendingDeleteFileIds) {
            fileDAO.deleteFile(fileId);
        }

        pendingDeleteFileIds.clear();

        for (int documentId : pendingDeleteDocumentIds) {
            documentDAO.deleteDocument(documentId);
        }
        pendingDeleteDocumentIds.clear();

        // persist staged files and documents
        for (Document document : targetBox.getDocuments()) {
            if (document.isStaged()) {
                Document persisted = documentDAO.createDocument(targetBox.getBoxId());
                document.setDocumentId(persisted.getDocumentId());
                document.setCreatedAt(persisted.getCreatedAt());
                document.setStaged(false);
            }

            for (File file : document.getFiles()) {
                if (file.isStaged()) {
                    file.setDocumentId(document.getDocumentId());
                    File persisted = fileDAO.createFile(
                            document.getDocumentId(),
                            file.getReferenceId(),
                            file.getSortId(),
                            file.getImageData()
                    );
                    file.setFileId(persisted.getFileId());
                    file.setCreatedAt(persisted.getCreatedAt());
                    if (file.hasCustomFileSettings()) {
                        fileDAO.upsertFileSettings(file.getFileId(), file.getFileSettings());
                    }
                    file.setStaged(false);
                }
            }
        }
    }

    /**
     * Exports every document in the box to the local filesystem under {@code exportDir}.
     * <p>
     * <b>Single-Page TIFF</b> - each page is its own TIFF inside its own sub-folder:
     * <pre>
     *   exportDir/boxName/Document1/File1.tiff
     *   exportDir/boxName/Document1/File2.tiff
     * </pre>
     * <p>
     * <b>Multi-Page TIFF</b> - all pages of a document are merged into one
     * multi-frame TIFF placed flat inside the document folder:
     * <pre>
     *   exportDir/boxName/Document_1/Document_1.tiff
     * </pre>
     */
    public void exportToDirectory(java.io.File exportDirectory, ExportMode mode) throws Exception {
        ImageIO.scanForPlugins(); // ensure TwelveMonkeys TIFF writer/reader is registered

        Path boxRoot = exportDirectory.toPath().resolve(targetBox.getBoxName());
        Files.createDirectories(boxRoot);

        int documentIndex = 1;
        for (Document document : targetBox.getDocuments()) {
            if (document.getFiles().isEmpty()) { documentIndex++; continue; }

            String documentFolderName = "Document" + documentIndex;
            Path docDirectory = boxRoot.resolve(documentFolderName);
            Files.createDirectories(docDirectory);

            if (mode == ExportMode.SinglePageTIFF) {
                exportSinglePage(document, docDirectory);
            } else {
                exportMultiPage(document, docDirectory, documentFolderName);
            }

            documentIndex++;
        }
    }

    /** Single-page mode: each file becomes a TIFF directly under the document folder. */
    private void exportSinglePage(Document document, Path documentDirectory) throws Exception {
        int fileIndex = 1;
        for (File file : document.getFiles()) {
            byte[] data = renderAdjustedImageData(file);
            if (data == null) { fileIndex++; continue; }

            Path outputFile = documentDirectory.resolve("File" + fileIndex + ".tiff");
            Files.write(outputFile, data);
            fileIndex++;
        }
    }

    /** Multi-Page mode: all pages of a document are merged into one multi-frame TIFF. */
    private void exportMultiPage(Document document, Path docDirectory, String baseName) throws Exception {
        List<byte[]> pages = new ArrayList<>();
        for (File file : document.getFiles()) {
            byte[] data = renderAdjustedImageData(file);
            if (data != null) pages.add(data);
        }
        if (pages.isEmpty()) return;

        java.io.File outFile = docDirectory.resolve(baseName + ".tiff").toFile();

        ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outFile)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);

            for (byte[] tiffBytes : pages) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(tiffBytes));
                if (img != null) {
                    writer.writeToSequence(new IIOImage(img, null, null), param);
                }
            }

            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    /**
     * Returns the image bytes for a file.
     * Staged files already carry data in memory; persisted files are fetched on demand from DB.
     */
    private byte[] resolveImageData(File file) throws Exception {
        if (file.getImageData() != null) return file.getImageData();
        if (!file.isStaged() && file.getFileId() > 0) {
            return fileDAO.loadImageData(file.getFileId());
        }
        return null;
    }

    /** Loads a file image, applies any active adjustments, and returns the rendered TIFF bytes. */
    private byte[] renderAdjustedImageData(File file) throws Exception {
        byte[] sourceData = resolveImageData(file);
        if (sourceData == null) return null;

        BufferedImage source = ImageIO.read(new ByteArrayInputStream(sourceData));
        if (source == null) return sourceData;

        BufferedImage adjusted = applyAdjustments(source, file.getFileSettings());
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(adjusted, "tiff", output);
            return output.toByteArray();
        }
    }

    /** Applies color adjustments first, then rotates the final image if needed. */
    private BufferedImage applyAdjustments(BufferedImage source, FileAdjustmentSettings settings) {
        BufferedImage colorAdjusted = applyColorAdjustments(source, settings);
        return rotateFile(colorAdjusted, settings.getRotation());
    }

    /**
     * Applies brightness, contrast, hue, and saturation adjustments pixel-by-pixel.
     * Explained in-depth in method cause there's A LOT of new going on
     */
    private BufferedImage applyColorAdjustments(BufferedImage source, FileAdjustmentSettings settings) {

        // original image dimensions
        int width = source.getWidth();
        int height = source.getHeight();

        // preserve alpha channel support if the source image contains transparency
        int type = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        // destination image that will contain the adjusted pixels
        BufferedImage result = new BufferedImage(width, height, type);

        // Contrast multiplier: 0 -> 1.0 (no change), 100 -> 2.0 (stronger contrast), -50 -> 0.5 (reduced contrast)
        double contrastFactor = 1.0 + (settings.getContrast() / 100.0);

        // Hue is stored in HSB as a circular value between 0.0-1.0 (positive/negative values rotate colors around the color wheel)
        float hueShift = (float) (settings.getHue() / 100.0);

        // Saturation multiplier: 1.0 = unchanged, >1 = more vivid/saturated colors, <1 = more gray/desaturated
        float saturationFactor = (float) (1.0 + (settings.getSaturation() / 100.0));

        // Brightness offset: positive values brighten, negative values darken
        float brightnessShift = (float) (settings.getBrightness() / 100.0);

        // walk through every pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // read the packed ARGB integer from the source image
                int argb = source.getRGB(x, y);

                // extract each 8-bit color channel using bit shifting
                int alpha = (argb >>> 24) & 0xff;
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;

                // convert RGB to HSB because hue/saturation/brightness are easier to manipulate independently in HSB space
                float[] hsb = Color.RGBtoHSB(red, green, blue, null);

                // Shift hue around the color wheel (Hue is circular, unlike Brightness, Saturation and Contrast (which is linear) so values must wrap instead of clamp
                hsb[0] = ((hsb[0] + hueShift) % 1.0f + 1.0f) % 1.0f;

                // increase/decrease saturation - clamp keeps the value inside the valid 0.0-1.0 HSB range
                hsb[1] = Math.clamp(hsb[1] * saturationFactor, 0.0f, 1.0f);

                // increase/decrease brightness - clamp prevents invalid brightness values
                hsb[2] = Math.clamp(hsb[2] + brightnessShift, 0.0f, 1.0f);

                // convert adjusted HSB values back into packed RGB format
                int adjustedRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);

                // extract adjusted RGB channels from the packed integer
                red = (adjustedRgb >>> 16) & 0xff;
                green = (adjustedRgb >>> 8) & 0xff;
                blue = adjustedRgb & 0xff;

                // apply contrast around midpoint 128 (RGB channels are integer-based): values above 128 become brighter, values below 128 become darker
                red = Math.clamp(Math.round(((red - 128) * contrastFactor) + 128), 0, 255);
                green = Math.clamp(Math.round(((green - 128) * contrastFactor) + 128), 0, 255);
                blue = Math.clamp(Math.round(((blue - 128) * contrastFactor) + 128), 0, 255);

                // repack ARGB channels back into a single integer pixel value
                int adjustedArgb = (alpha << 24) | (red << 16) | (green << 8) | blue;

                // write the adjusted pixel into the destination image
                result.setRGB(x, y, adjustedArgb);
            }
        }

        return result;
    }

    /**
     * Applies rotation (by 90, 180, 270 degrees for now) and smoothes the pixels when rotated
     * Explained in-depth in method cause there's A LOT of new stuff going on.
     */
    private BufferedImage rotateFile(BufferedImage source, int rotation) {

        // normalize rotation so only valid values remain: 0/90/180/270
        int normalised = normaliseRotation(rotation);

        // no rotation needed
        if (normalised == 0) return source;

        // original image dimensions
        int width = source.getWidth();
        int height = source.getHeight();

        // rotating 90 or 270 degrees swaps width/height
        int rotatedWidth = normalised == 180 ? width : height;
        int rotatedHeight = normalised == 180 ? height : width;

        // keeps transparency (like PNG see-through areas) if the image has it (people online said it was a good idea)
        int type = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        // destination image that will contain the rotated result
        BufferedImage rotated = new BufferedImage(rotatedWidth, rotatedHeight, type);

        // tool used to draw the image onto the new rotated canvas
        Graphics2D graphics = rotated.createGraphics();

        // use higher-quality interpolation during rotation = image looks smoother and less pixelated when rotated (without the image will becoming jagged or blocky)
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // defines how the image should be transformed when drawn (rotation + translation)
        AffineTransform transform = new AffineTransform();
        switch (normalised) {
            case 90 -> {
                transform.translate(height, 0); // move drawing origin right so rotated image stays visible
                transform.rotate(Math.toRadians(90)); // rotate 90 degrees around origin to the right
            }
            case 180 -> {
                transform.translate(width, height); // move image into positive coordinate space after rotation
                transform.rotate(Math.toRadians(180)); // rotate image upside down
            }
            case 270 -> {
                transform.translate(0, width); // move drawing origin downward after rotation
                transform.rotate(Math.toRadians(270)); // rotate 270 degrees (or 90 to the left)
            }
            default -> { }
        }

        graphics.drawImage(source, transform, null); // draw the source image using the configured transformation
        graphics.dispose(); // Release native graphics resources
        return rotated;
    }

    /** Creates a new staged document and sets it as the current one. */
    public Document manualSplit() {
        currentDocument = stageDocument();
        return currentDocument;
    }

    /**
     * Rotates a file.
     * Staged files are updated in memory only; persisted files are written to DB immediately.
     */
    public void updateFileRotation(File file, int rotation) throws Exception {
        FileAdjustmentSettings settings = FileAdjustmentSettings.copyOf(file.getFileSettings());
        settings.setRotation(normaliseRotation(rotation));
        updateFileSettings(file, settings);
    }

    /**
     * Applies file-specific adjustment overrides without changing the source TIFF bytes.
     */
    public void updateFileSettings(File file, FileAdjustmentSettings settings) throws Exception {
        file.applyCustomFileSettings(settings);
        if (!file.isStaged()) {
            fileDAO.upsertFileSettings(file.getFileId(), file.getFileSettings());
        }
    }

    /**
     * Removes a file from the in-memory box.
     * <p>
     * Staged files are discarded immediately with no DB call.
     * Persisted files are queued for soft-delete and written to the DB only when
     * {@link #commitAll()} is called, so the session stays consistent until saved on export.
     * <p>
     * {@code needsBarcodeFirst} is re-evaluated after removal (if everything is deleted the first file should be a barcode).
     */
    public void deleteFile(File file) throws Exception {
        // remove from memory (also sets the owning document to modified if it is persisted)
        for (Document document : targetBox.getDocuments()) {
            boolean removed = file.isStaged() ? document.getFiles().removeIf(f -> f == file) : document.getFiles().removeIf(f -> f.getFileId() == file.getFileId());
            if (removed && !document.isStaged()) {
                document.setModified(true);
            }
        }

        // queue db delete for persisted files (applied at commitAll)
        if (!file.isStaged() && file.getFileId() > 0) {
            pendingDeleteFileIds.add(file.getFileId());
        }
        refreshNeedsBarcodeFirst();
    }

    /**
     * Removes a document (and all its files) from the in-memory box.
     * Staged files and the document are discarded immediately with no DB call, since it is only in-memory.
     * Persisted files and the document are queued for soft-delete
     * and written to the DB only when {@link #commitAll()} is called.
     * <p>
     * If {@code currentDocument} is the one being deleted, it is updated to the
     * nearest preceding document so the next scan lands in the right place.
     * {@code needsBarcodeFirst} is re-evaluated after removal (if everything is deleted the first file should be a barcode).
     */
    public void deleteDocument(Document document) throws Exception {
        // queue all persisted files for deletion
        for (File file : new ArrayList<>(document.getFiles())) {
            if (!file.isStaged() && file.getFileId() > 0) {
                pendingDeleteFileIds.add(file.getFileId());
            }
        }
        document.getFiles().clear();

        // queue the document row for deletion
        if (!document.isStaged() && document.getDocumentId() > 0) {
            pendingDeleteDocumentIds.add(document.getDocumentId());
        }

        // update currentDocument before removing from the list so indexOf still works
        if (document == currentDocument) {
            int index = targetBox.getDocuments().indexOf(document);
            targetBox.getDocuments().remove(document);
            List<Document> remaining = targetBox.getDocuments();
            // set currentDocument to the preceding document, but if the deleted one was first stay at index 0, else null
            if (!remaining.isEmpty()) {
                currentDocument = remaining.get(Math.max(0, index - 1));
            } else {
                currentDocument = null;
            }
        } else {
            targetBox.getDocuments().remove(document);
        }

        refreshNeedsBarcodeFirst();
    }

    public void deleteBox(Box box) {

        

    }

    public Document getCurrentDocument() { return currentDocument; }

    public Box getTargetBox() { return targetBox; }

    /**
     * Re-evaluates whether the next scan must be a barcode file.
     * True whenever the box is completely empty (no documents or every document
     * has had all its files removed), so that starting to scan again always
     * fetches a valid barcode-led document.
     */
    private void refreshNeedsBarcodeFirst() {
        needsBarcodeFirst = targetBox.getDocuments().isEmpty() || targetBox.getDocuments().stream().allMatch(document -> document.getFiles().isEmpty());
    }

    private Document stageDocument() {
        Document document = new Document(0, targetBox.getBoxId(), LocalDateTime.now());
        document.setStaged(true);
        targetBox.getDocuments().add(document);
        return document;
    }

    private File stageFile(Document document, int referenceId, byte[] imageData) {
        File file = new File();
        file.setStaged(true);
        file.setReferenceId(referenceId);
        file.setSortId(referenceId);
        file.setImageData(imageData);
        file.setFileSizeBytes(imageData.length);
        file.setCreatedAt(LocalDateTime.now());
        file.applyDefaultFileSettings(targetBox.getProfile().getFileAdjustmentSettings());
        document.getFiles().add(file);
        if (!document.isStaged()) {
            document.setModified(true); // visual
        }
        return file;
    }

    private int highestExistingReference(List<Document> documents) {
        int max = 0;
        for (Document document : documents) {
            for (File file : document.getFiles()) {
                max = Math.max(max, Math.max(file.getReferenceId(), file.getSortId()));
            }
        }
        return max;
    }

    private void applyProfileDefaults(List<Document> documents) {
        for (Document document : documents) {
            for (File file : document.getFiles()) {
                if (!file.hasCustomFileSettings()) {
                    file.applyDefaultFileSettings(targetBox.getProfile().getFileAdjustmentSettings());
                }
            }
        }
    }

    private static int normaliseRotation(int rotation) {
        int normalised = ((rotation % 360) + 360) % 360;
        return switch (normalised) {
            case 90, 180, 270 -> normalised;
            default -> 0;
        };
    }
}