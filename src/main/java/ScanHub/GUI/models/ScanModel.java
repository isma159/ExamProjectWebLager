package ScanHub.GUI.models;

import ScanHub.BE.*;
import ScanHub.BE.enums.ExportMode;
import ScanHub.BLL.ScanManager;
import ScanHub.DAL.ApiClient.ScanApiClient;

import java.awt.image.BufferedImage;
import java.util.function.DoubleConsumer;

/** Created via startSession() in ScanController and discarded when the session ends. */
public class ScanModel {

    private final ScanManager scanManager;

    public ScanModel(Box targetBox) throws Exception {
        this.scanManager = new ScanManager(new ScanApiClient(), targetBox);
    }

    public StoredScan fetchScan() throws Exception { return scanManager.fetchScan(); }

    public byte[] loadImageData(File file) throws Exception { return scanManager.resolveImageData(file); }

    public void save() throws Exception { scanManager.commitAll(); }

    public void export(java.io.File exportDirectory, ExportMode mode, DoubleConsumer progressCallback) throws Exception {
        scanManager.exportToDirectory(exportDirectory, mode, progressCallback);
    }

    public Document manualSplit() { return scanManager.manualSplit(); }

    public void updateFileRotation(File file, int rotation) throws Exception { scanManager.updateFileRotation(file, rotation); }

    public void updateFileSettings(File file, FileAdjustmentSettings settings) throws Exception { scanManager.updateFileSettings(file, settings); }

    public void deleteFile(File file) throws Exception { scanManager.deleteFile(file); }

    public void deleteDocument(Document document) { scanManager.deleteDocument(document); }

    public void deleteBox() throws Exception {scanManager.deleteBox();}

    public Box getTargetBox() { return scanManager.getTargetBox(); }
}
