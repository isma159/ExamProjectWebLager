package ScanHub.BLL;

import ScanHub.BE.Box;
import ScanHub.BE.BoxMetadata;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.enums.SplitBehavior;
import ScanHub.BLL.util.BarcodeDetector;
import ScanHub.DAL.ApiClient.ScanResult;
import ScanHub.DAL.DAO.BoxMetadataDAO;
import ScanHub.DAL.DAO.DocumentDAO;
import ScanHub.DAL.DAO.FileDAO;
import ScanHub.DAL.interfaces.IScanSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO explain
 */
public class ScanManager {

    private final IScanSource scanSource;
    private final DocumentDAO documentDAO;
    private final FileDAO fileDAO;
    private final BoxMetadataDAO boxMetadataDAO;

    private Document currentDocument;
    private final Box targetBox;
    private int referenceCounter = 0;

    /**
     *
     */
    public record StoredScan(File file, Document document, boolean barcodeSplit) {}

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
            currentDocument = existingDocs.get(existingDocs.size() - 1);
        }
    }

    /**
     * Fetches the next scan and holds it in memory only.
     */
    public StoredScan fetchScan(int rotation) throws Exception {
        ScanResult result = scanSource.fetchNextScan();
        referenceCounter++;

        SplitBehavior behavior = targetBox.getProfile().getSplitBehavior();
        if (behavior == SplitBehavior.BARCODE && BarcodeDetector.containsBarcode(result.data())) {
            if (currentDocument == null || !currentDocument.getFiles().isEmpty()) {
                currentDocument = stageDocument();
            }
            File file = stageFile(currentDocument, referenceCounter, result.data(), rotation);
            return new StoredScan(file, currentDocument, true);
        }

        if (currentDocument == null) {
            currentDocument = stageDocument();
        }

        File file = stageFile(currentDocument, referenceCounter, result.data(), rotation);
        return new StoredScan(file, currentDocument, false);
    }

    /**
     * Walks every staged document and file in order, persists them, then
     * updates the in-memory objects with their real DB ids and refreshes metadata.
     */
    public void commitAll() throws Exception {
        for (Document doc : targetBox.getDocuments()) {
            if (doc.isStaged()) {
                Document persisted = documentDAO.createDocument(targetBox.getBoxId());
                doc.setDocumentId(persisted.getDocumentId());
                doc.setCreatedAt(persisted.getCreatedAt());
                doc.setStaged(false);
            }

            for (File file : doc.getFiles()) {
                if (file.isStaged()) {
                    file.setDocumentId(doc.getDocumentId());
                    File persisted = fileDAO.createFile(
                            doc.getDocumentId(),
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
     * Creates a new staged document and sets it as the current one.
     */
    public Document manualSplit() {
        currentDocument = stageDocument();
        return currentDocument;
    }

    /**
     * Rotates a file.
     * Staged files are updated in memory only, while persisted files are written to the database immediately.
     */
    public void updateFileRotation(File file, int rotation) throws Exception {
        if (!file.isStaged()) {
            fileDAO.updateRotation(file.getFileId(), rotation);
        }
        file.setRotation(rotation);
    }

    /**
     * Deletes a single file.
     * Staged files are removed from memory only
     * Persisted files are soft-deleted in the DB and metadata is refreshed.
     */
    public void deleteFile(File file) throws Exception {
        if (!file.isStaged()) {
            fileDAO.softDelete(file.getFileId());
        }
        for (Document document : targetBox.getDocuments()) {
            document.getFiles().removeIf(f -> file.isStaged() ? f == file : f.getFileId() == file.getFileId());
        }
        if (!file.isStaged()) {
            refreshMetadata();
        }
    }

    /**
     * Deletes an entire document and all its files.
     * Persisted files are soft-deleted in the DB.
     * The document is removed from the in-memory box.
     */
    public void deleteDocument(Document document) throws Exception {
        boolean hadPersistedFiles = false;
        for (File file : new ArrayList<>(document.getFiles())) {
            if (!file.isStaged()) {
                fileDAO.softDelete(file.getFileId());
                hadPersistedFiles = true;
            }
        }
        document.getFiles().clear();
        targetBox.getDocuments().remove(document);
        if (hadPersistedFiles) {
            refreshMetadata();
        }
    }

    public Document getCurrentDocument() { return currentDocument; }

    public Box getTargetBox() { return targetBox; }

    /**
     * Creates metadata if it doesn't exist. If it already does, then update it to keep it up to date.
     */
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

    private Document stageDocument() {
        Document doc = new Document(0, targetBox.getBoxId(), LocalDateTime.now());
        doc.setStaged(true);
        targetBox.getDocuments().add(doc);
        return doc;
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