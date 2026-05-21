package ScanHub.BE;

// java imports
import java.time.LocalDateTime;
import ScanHub.BE.interfaces.TreeNode;

public class File implements TreeNode {

    private int fileId;
    private int documentId;
    private int referenceId; // scan order (file number) as recieved from API
    private int sortId; // user-reorderable position within the document
    private byte[] imageData; // TIFF blob (only populated when actively viewing/exporting)
    private int fileSizeBytes;
    private LocalDateTime createdAt;
    private boolean staged = false;
    private FileAdjustmentSettings fileAdjustmentSettings;
    private boolean customFileSettings = false;

    public File() {
        fileAdjustmentSettings = new FileAdjustmentSettings();
    }

    public File(int fileId, int documentId, int referenceId, int sortId, int fileSizeBytes, LocalDateTime createdAt) {
        this.fileId = fileId;
        this.documentId = documentId;
        this.referenceId = referenceId;
        this.sortId = sortId;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
        fileAdjustmentSettings = new FileAdjustmentSettings();
    }

    public int getFileId()              { return fileId; }
    public int getDocumentId()          { return documentId; }
    public int getReferenceId()         { return referenceId; }
    public int getSortId()              { return sortId; }
    public byte[] getImageData()        { return imageData; }
    public int getFileSizeBytes()       { return fileSizeBytes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getRotation()            { return fileAdjustmentSettings.getRotation(); }
    public double getHue()              { return  fileAdjustmentSettings.getHue(); }
    public double getBrightness()       { return fileAdjustmentSettings.getBrightness(); }
    public double getContrast()         { return fileAdjustmentSettings.getContrast(); }
    public double getSaturation()       { return fileAdjustmentSettings.getSaturation(); }
    public boolean isStaged()           { return staged; }
    public FileAdjustmentSettings getFileSettings() { return fileAdjustmentSettings; }
    public boolean hasCustomFileSettings() { return customFileSettings; }

    public void setFileId(int fileId)                 { this.fileId = fileId; }
    public void setDocumentId(int documentId)         { this.documentId = documentId; }
    public void setReferenceId(int referenceId)       { this.referenceId = referenceId; }
    public void setSortId(int sortId)                 { this.sortId = sortId; }
    public void setImageData(byte[] imageData)        { this.imageData = imageData; }
    public void setFileSizeBytes(int fileSizeBytes)   { this.fileSizeBytes = fileSizeBytes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRotation(int rotation)             { fileAdjustmentSettings.setRotation(rotation); }
    public void setHue(double hue)                    { fileAdjustmentSettings.setHue(hue); }
    public void setBrightness(double brightness)      { fileAdjustmentSettings.setBrightness(brightness); }
    public void setContrast(double contrast)          { fileAdjustmentSettings.setContrast(contrast); }
    public void setSaturation(double saturation)      { fileAdjustmentSettings.setSaturation(saturation); }
    public void setStaged(boolean staged)             { this.staged = staged; }
    public void setFileSettings(FileAdjustmentSettings fileAdjustmentSettings) {
        this.fileAdjustmentSettings = fileAdjustmentSettings == null ? new FileAdjustmentSettings() : FileAdjustmentSettings.copyOf(fileAdjustmentSettings);
    }
    public void setCustomFileSettings(boolean customFileSettings) { this.customFileSettings = customFileSettings; }

    public void applyDefaultFileSettings(FileAdjustmentSettings defaultSettings) {
        setFileSettings(defaultSettings);
        setCustomFileSettings(false);
    }

    public void applyCustomFileSettings(FileAdjustmentSettings customSettings) {
        setFileSettings(customSettings);
        setCustomFileSettings(true);
    }

    @Override
    public String toString() { return "File #" + this.referenceId; }
}
