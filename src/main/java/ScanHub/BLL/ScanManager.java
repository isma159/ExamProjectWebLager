package ScanHub.BLL;

import ScanHub.BE.Box;
import ScanHub.BE.BoxMetadata;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.SplitBehavior;
import ScanHub.BLL.util.BarcodeDetector;
import ScanHub.DAL.ApiClient.ScanResult;
import ScanHub.DAL.DAO.BoxMetadataDAO;
import ScanHub.DAL.DAO.DocumentDAO;
import ScanHub.DAL.DAO.FileDAO;
import ScanHub.DAL.interfaces.IScanSource;

import java.util.List;

public class ScanManager {

    private final IScanSource scanSource;
    private final DocumentDAO documentDAO;
    private final FileDAO fileDAO;
    private final BoxMetadataDAO boxMetadataDAO;

    private Document currentDocument;
    private final Box targetBox;
    private int referenceCounter = 0;

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

    public StoredScan fetchAndStore() throws Exception {
        return fetchAndStore(0);
    }

    public StoredScan fetchAndStore(int rotation) throws Exception {
        ScanResult result = scanSource.fetchNextScan();
        referenceCounter++;

        SplitBehavior behavior = targetBox.getProfile().getSplitBehavior();
        if (behavior == SplitBehavior.BARCODE && BarcodeDetector.containsBarcode(result.data())) {
            if (currentDocument == null || !currentDocument.getFiles().isEmpty()) {
                currentDocument = createDocument();
            }
            File file = fileDAO.createFile(currentDocument.getDocumentId(), referenceCounter, result.data(), rotation);
            currentDocument.getFiles().add(file);
            refreshMetadata();
            return new StoredScan(file, currentDocument, true);
        }

        if (currentDocument == null) {
            currentDocument = createDocument();
        }

        File file = fileDAO.createFile(currentDocument.getDocumentId(), referenceCounter, result.data(), rotation);
        currentDocument.getFiles().add(file);
        refreshMetadata();
        return new StoredScan(file, currentDocument, false);
    }

    public Document manualSplit() throws Exception {
        currentDocument = createDocument();
        refreshMetadata();
        return currentDocument;
    }

    public Document getCurrentDocument() {
        return currentDocument;
    }

    public Box getTargetBox() {
        return targetBox;
    }

    public void refreshMetadata() throws Exception {
        BoxMetadata metadata = boxMetadataDAO.getDataByBoxId(targetBox.getBoxId());
        int documentCount = documentDAO.countDocumentsForBox(targetBox.getBoxId());
        int fileCount = fileDAO.countFilesForBox(targetBox.getBoxId());

        if (metadata == null) {
            metadata = new BoxMetadata(
                    0,
                    targetBox.getBoxId(),
                    targetBox.getProfile().getProfileName(),
                    targetBox.getBoxName(),
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

    public void updateFileRotation(File file, int rotation) throws Exception {
        fileDAO.updateRotation(file.getFileId(), rotation);
        file.setRotation(rotation);
    }

    public void deleteFile(File file) throws Exception {
        fileDAO.softDelete(file.getFileId());
        for (Document document : targetBox.getDocuments()) {
            document.getFiles().removeIf(f -> f.getFileId() == file.getFileId());
        }
        refreshMetadata();
    }

    private Document createDocument() throws Exception {
        Document document = documentDAO.createDocument(targetBox.getBoxId());
        targetBox.getDocuments().add(document);
        return document;
    }
}
