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
    private FileSettings fileSettings;

    public File() {
    }

    public File(int fileId, int documentId, int referenceId, int sortId, int fileSizeBytes, LocalDateTime createdAt) {
        this.fileId = fileId;
        this.documentId = documentId;
        this.referenceId = referenceId;
        this.sortId = sortId;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
        fileSettings = new FileSettings();
    }

    public int getFileId()              { return fileId; }
    public int getDocumentId()          { return documentId; }
    public int getReferenceId()         { return referenceId; }
    public int getSortId()              { return sortId; }
    public byte[] getImageData()        { return imageData; }
    public int getFileSizeBytes()       { return fileSizeBytes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getRotation()            { return fileSettings.getRotation(); }
    public double getHue()    { return  fileSettings.getHue(); }
    public double getBrightness()       { return fileSettings.getBrightness(); }
    public double getContrast()         { return fileSettings.getContrast(); }
    public double getSaturation()       { return fileSettings.getSaturation(); }
    public boolean isStaged()           { return staged; }

    public void setFileId(int fileId)                 { this.fileId = fileId; }
    public void setDocumentId(int documentId)         { this.documentId = documentId; }
    public void setReferenceId(int referenceId)       { this.referenceId = referenceId; }
    public void setSortId(int sortId)                 { this.sortId = sortId; }
    public void setImageData(byte[] imageData)        { this.imageData = imageData; }
    public void setFileSizeBytes(int fileSizeBytes)   { this.fileSizeBytes = fileSizeBytes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRotation(int rotation)             { fileSettings.setRotation(rotation); }
    public void setHue(double hue)                    { fileSettings.setHue(hue); }
    public void setBrigthness(double brigthness)      { fileSettings.setBrightness(brigthness); }
    public void setContrast(double contrast)          { fileSettings.setContrast(contrast); }
    public void setSaturation(double saturation)      { fileSettings.setSaturation(saturation); }
    public void setStaged(boolean staged)             { this.staged = staged; }

    @Override
    public String toString() { return "File #" + this.referenceId; }
}
