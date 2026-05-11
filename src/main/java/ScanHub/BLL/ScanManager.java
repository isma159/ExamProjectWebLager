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
    private final int userId;
    private int referenceCounter = 0;

    public ScanManager(IScanSource scanSource, Box targetBox, int userId) throws Exception {
        this.scanSource = scanSource;
        this.targetBox = targetBox;
        this.userId = userId;
        this.documentDAO = new DocumentDAO();
        this.fileDAO = new FileDAO();
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
        System.out.println(">>> DEBUG: Using Profile: " + targetBox.getProfile().getProfileName());
        System.out.println(">>> DEBUG: Brightness: " + targetBox.getProfile().getBrightness());
        System.out.println(">>> DEBUG: Contrast: " + targetBox.getProfile().getContrast());

        File savedFile = fileDAO.createFile(
                currentDocument.getDocumentId(),
                referenceCounter,
                result.data(),
                targetBox.getProfile().getBrightness(),
                targetBox.getProfile().getContrast()
        );

        return savedFile;
    }

    public void manualSplit() throws Exception {
        currentDocument = documentDAO.createDocument(targetBox.getBoxId());
    }

    public Document getCurrentDocument() {
        return currentDocument;
    }
}