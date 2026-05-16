package ScanHub.BLL;

import ScanHub.BE.Box;
import ScanHub.BE.BoxMetadata;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.enums.ExportMode;
import ScanHub.BLL.util.BarcodeDetector;
import ScanHub.DAL.ApiClient.ScanResult;
import ScanHub.DAL.DAO.BoxMetadataDAO;
import ScanHub.DAL.DAO.DocumentDAO;
import ScanHub.DAL.DAO.FileDAO;
import ScanHub.DAL.interfaces.IScanSource;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
    private final BoxMetadataDAO boxMetadataDAO;

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
        this.boxMetadataDAO = new BoxMetadataDAO();

        if (targetBox.getProfile() == null) {
            throw new IllegalArgumentException("A scan session box must have a profile");
        }

        refreshMetadata();

        List<Document> existingDocs = targetBox.getDocuments();
        if (!existingDocs.isEmpty()) {
            currentDocument = existingDocs.getLast();
        }

        // Box is empty (no files anywhere) -> first page must be a barcode
        needsBarcodeFirst = existingDocs.stream().allMatch(document -> document.getFiles().isEmpty());
    }

    /**
     * Fetches the next scan and holds it in memory only.
     * <p>
     * If this is the very first page of a brand-new (or empty) box, it always
     * fetches a barcode page so that the first document begins with a barcode.
     */
    public StoredScan fetchScan(int rotation) throws Exception {
        ScanResult result = needsBarcodeFirst ? scanSource.fetchBarcodeFile() : scanSource.fetchNextScan();

        needsBarcodeFirst = false; // only enforce barcode-first on the very first scan.
        boolean barcodeSplit = false;

        // we only create a new document if there is no current document yet or the current document already contains pages
        if (BarcodeDetector.containsBarcode(result.data())) {
            if (currentDocument == null || !currentDocument.getFiles().isEmpty()) {
                currentDocument = stageDocument();
            }
            barcodeSplit = true;
        } else if (currentDocument == null) { // just in case, but all documents will always start with a barcode
            currentDocument = stageDocument();
        }

        int ref = ++referenceCounter; // generate/increment next page reference
        File file = stageFile(currentDocument, ref, result.data(), rotation);
        return new StoredScan(file, currentDocument, barcodeSplit);
    }

    /**
     * Applies all pending in-memory deletes to the DB, then walks every staged
     * document and file in order, persists them, updates their real DB ids, and
     * refreshes metadata.
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
                            file.getImageData(),
                            file.getRotation()
                    );
                    file.setFileId(persisted.getFileId());
                    file.setCreatedAt(persisted.getCreatedAt());
                    file.setStaged(false);
                }
            }
        }
        refreshMetadata();
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
            byte[] data = resolveImageData(file);
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
            byte[] data = resolveImageData(file);
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
        if (!file.isStaged()) {
            fileDAO.updateRotation(file.getFileId(), rotation);
        }
        file.setRotation(rotation);
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
     * <p>
     * Staged items are discarded immediately with no DB call.
     * Persisted files and the document record itself are queued for soft-delete
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

    /** Creates metadata if it doesn't exist, otherwise keeps it up to date. */
    public void refreshMetadata() throws Exception {
        BoxMetadata metadata = boxMetadataDAO.getDataByBoxId(targetBox.getBoxId());
        int documentCount = documentDAO.countDocumentsForBox(targetBox.getBoxId());
        int fileCount = fileDAO.countFilesForBox(targetBox.getBoxId());

        if (metadata == null) {
            metadata = new BoxMetadata(0,
                    targetBox.getBoxId(),
                    targetBox.getProfile().getProfileName(),
                    documentCount,
                    fileCount,
                    targetBox.getCreatedAt()
            );
            boxMetadataDAO.createData(metadata);
        } else {
            metadata.setProfileName(targetBox.getProfile().getProfileName());
            metadata.setBoxName(targetBox.getBoxName());
            metadata.setDocumentCount(documentCount);
            metadata.setFileCount(fileCount);
            metadata.setBoxCreatedAt(targetBox.getCreatedAt());
            boxMetadataDAO.updateData(metadata);
        }
    }

    public Document getCurrentDocument() { return currentDocument; }

    public Box getTargetBox() { return targetBox; }

    /**
     * Re-evaluates whether the next scan must be a barcode page.
     * True whenever the box is completely empty (no documents or every document
     * has had all its files removed), so that starting to scan again always
     * produces a valid barcode-led document.
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

    private File stageFile(Document document, int referenceId, byte[] imageData, int rotation) {
        File file = new File();
        file.setStaged(true);
        file.setReferenceId(referenceId);
        file.setSortId(referenceId);
        file.setImageData(imageData);
        file.setFileSizeBytes(imageData.length);
        file.setCreatedAt(LocalDateTime.now());
        file.setRotation(normaliseRotation(rotation));
        document.getFiles().add(file);
        if (!document.isStaged()) {
            document.setModified(true); // visual
        }
        return file;
    }

    private static int normaliseRotation(int rotation) {
        int normalised = ((rotation % 360) + 360) % 360;
        return switch (normalised) {
            case 90, 180, 270 -> normalised;
            default -> 0;
        };
    }
}