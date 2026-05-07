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

    private Document currentDocument;
    private final Box targetBox;
    private int referenceCounter = 0;

    public ScanManager(IScanSource scanSource, Box targetBox) throws Exception {
        this.scanSource = scanSource;
        this.targetBox = targetBox;
        this.documentDAO = new DocumentDAO();
        this.fileDAO = new FileDAO();
        this.currentDocument = documentDAO.createDocument(targetBox.getBoxId());
    }

    public File fetchAndStore() throws Exception {
        ScanResult result = scanSource.fetchNextScan();
        referenceCounter++;

        SplitBehavior behavior = targetBox.getProfile().getSplitBehavior();

        // Barcode detection happens here, after the fetch
        if (behavior == SplitBehavior.BARCODE && BarcodeDetector.containsBarcode(result.data())) {
            currentDocument = documentDAO.createDocument(targetBox.getBoxId());
            return null; // signals a split to the caller, barcode page is not saved
        }

        return fileDAO.createFile(currentDocument.getDocumentId(), referenceCounter, result.data());
    }

    /**
     * For MANUAL split behavior — called when the user clicks a
     * "New Document" button in the UI.
     */
    public void manualSplit() throws Exception {
        currentDocument = documentDAO.createDocument(targetBox.getBoxId());
    }

    public Document getCurrentDocument() {
        return currentDocument;
    }
}