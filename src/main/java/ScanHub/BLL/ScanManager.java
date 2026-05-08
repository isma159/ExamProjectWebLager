package ScanHub.BLL;

import ScanHub.BE.*;
import ScanHub.BE.File;
import ScanHub.BLL.util.BarcodeDetector;
import ScanHub.DAL.ApiClient.ScanResult;
import ScanHub.DAL.DAO.DocumentDAO;
import ScanHub.DAL.DAO.FileDAO;
import ScanHub.DAL.interfaces.IScanSource;

public class ScanManager {

    private final IScanSource scanSource;
    private final DocumentDAO documentDAO;
    private final FileDAO fileDAO;
    private final LogManager logManager;

    private Document currentDocument;
    private final Box targetBox;
    private final int userId;
    private int referenceCounter = 0;

    public ScanManager(IScanSource scanSource, Box targetBox, int userId) throws Exception {
        this.scanSource = scanSource;
        this.targetBox = targetBox;
        this.userId = userId;
        this.documentDAO = new DocumentDAO();
        this.fileDAO = new FileDAO();
        this.logManager = new LogManager();
        this.currentDocument = documentDAO.createDocument(targetBox.getBoxId());
    }

    public File fetchAndStore() throws Exception {
        ScanResult result = scanSource.fetchNextScan();
        referenceCounter++;

        SplitBehavior behavior = targetBox.getProfile().getSplitBehavior();

        if (behavior == SplitBehavior.BARCODE && BarcodeDetector.containsBarcode(result.data())) {
            currentDocument = documentDAO.createDocument(targetBox.getBoxId());
            return null;
        }

        File savedFile = fileDAO.createFile(currentDocument.getDocumentId(), referenceCounter, result.data());

        // Log the file creation
        logManager.createLog(userId, savedFile.getFileId(), currentDocument.getDocumentId(), "FILE_CREATED");

        return savedFile;
    }

    public void manualSplit() throws Exception {
        currentDocument = documentDAO.createDocument(targetBox.getBoxId());
    }

    public Document getCurrentDocument() {
        return currentDocument;
    }
}