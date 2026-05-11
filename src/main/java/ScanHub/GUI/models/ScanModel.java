package ScanHub.GUI.models;

import ScanHub.BE.Box;
import ScanHub.BE.Document;
import ScanHub.BE.File;
import ScanHub.BLL.ScanManager;
import ScanHub.DAL.ApiClient.ScanApiClient;

/**
 * Created via method startSession() in ScanController,
 * and is discarded when they stop.
 */
public class ScanModel {

    private final ScanManager scanManager;

    public ScanModel(Box targetBox) throws Exception {
        this.scanManager = new ScanManager(new ScanApiClient(), targetBox);
    }

    /**
     * Fetches the next page and holds it in memory.
     * Handles barcode-split logic automatically via the profile's SplitBehavior.
     * @param rotation initial rotation in degrees (0, 90, 180, 270)
     * @return a StoredScan record containing the saved File, its Document, and a flag indicating whether a barcode split was triggered
     */
    public ScanManager.StoredScan fetchScan(int rotation) throws Exception {
        return scanManager.fetchScan(rotation);
    }

    /** Persists all staged documents and files in one pass, then refreshes metadata. */
    public void save() throws Exception {
        scanManager.commitAll();
    }

    /** Creates a new empty staged Document in the current Box (manual split). */
    public Document manualSplit() {
        return scanManager.manualSplit();
    }

    /**
     * Persists a rotation change for the given File if not staged,
     * else only updates the in-memory File.
     */
    public void updateFileRotation(File file, int rotation) throws Exception {
        scanManager.updateFileRotation(file, rotation);
    }

    /**
     * Soft-deletes the given File from the DB if it is not staged,
     * else removes it from the in-memory box.
     * Also refreshes box metadata counts.
     */
    public void deleteFile(File file) throws Exception {
        scanManager.deleteFile(file);
    }

    public Document getCurrentDocument() { return scanManager.getCurrentDocument(); }

    public Box getTargetBox() { return scanManager.getTargetBox(); }
}