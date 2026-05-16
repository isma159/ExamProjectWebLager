package ScanHub.GUI.models;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BE.enums.ExportMode;
import ScanHub.BLL.ScanManager;
import ScanHub.DAL.ApiClient.ScanApiClient;

/**
 * Created via startSession() in ScanController and discarded when the session ends.
 * Thin facade over ScanManager - keeps the controller free of BLL details.
 */
public class ScanModel {

    private final ScanManager scanManager;

    public ScanModel(Box targetBox) throws Exception {
        this.scanManager = new ScanManager(new ScanApiClient(), targetBox);
    }

    /**
     * Fetches the next page and holds it in memory.
     * For an empty box the very first fetch is always a barcode page (enforced by ScanManager).
     *
     * @param rotation initial rotation in degrees (0, 90, 180, 270)
     * @return a StoredScan record with the File, its Document, and a barcode-split flag
     */
    public ScanManager.StoredScan fetchScan(int rotation) throws Exception { return scanManager.fetchScan(rotation); }

    /** Persists all staged documents and files in one pass, then refreshes metadata. */
    public void save() throws Exception { scanManager.commitAll(); }

    /**
     * Exports all documents to the given directory using the chosen mode.
     * Single-Page TIFF: each file in its own sub-folder.
     * Multi-Page TIFF: all pages of a document merged into one TIFF.
     */
    public void export(java.io.File exportDirectory, ExportMode mode) throws Exception {
        scanManager.exportToDirectory(exportDirectory, mode);
    }

    /** Creates a new empty staged Document in the current Box (manual split). */
    public Document manualSplit() { return scanManager.manualSplit(); }

    /**
     * Persists a rotation change for the given File if not staged;
     * otherwise only updates the in-memory File.
     */
    public void updateFileRotation(File file, int rotation) throws Exception { scanManager.updateFileRotation(file, rotation); }

    /**
     * Soft-deletes the given File from the DB if not staged;
     * otherwise removes it from the in-memory box.
     * Also refreshes box metadata counts.
     */
    public void deleteFile(File file) throws Exception { scanManager.deleteFile(file); }

    /**
     * Deletes an entire document and all its files.
     * Persisted files are soft-deleted in the DB.
     */
    public void deleteDocument(Document document) throws Exception { scanManager.deleteDocument(document); }

    public Document getCurrentDocument() { return scanManager.getCurrentDocument(); }

    public Box getTargetBox() { return scanManager.getTargetBox(); }
}
