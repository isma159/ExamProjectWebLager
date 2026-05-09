package ScanHub.BE;

import ScanHub.BE.interfaces.TreeNode;

// java imports
import java.time.LocalDateTime;

public class File implements TreeNode {

    private int fileId;
    private int documentId;
    private int referenceId; // scan order (file number) as recieved from API
    private int sortId; // user-reorderable position within the document
    private byte[] imageData; // TIFF blob (only populated when actively viewing/exporting)
    private int fileSizeBytes;
    private LocalDateTime createdAt;
    private int rotation;
    private int brightness;
    private int contrast;

    public File() {
    }

    public File(int fileId, int documentId, int referenceId, int sortId, int fileSizeBytes, LocalDateTime createdAt) {
        this.fileId = fileId;
        this.documentId = documentId;
        this.referenceId = referenceId;
        this.sortId = sortId;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
        this.brightness = brightness;
        this.contrast = contrast;
    }

    public int getFileId() { return fileId; }
    public int getDocumentId() { return documentId; }
    public int getReferenceId() { return referenceId; }
    public int getSortId() { return sortId; }
    public byte[] getImageData() { return imageData; }
    public int getFileSizeBytes() { return fileSizeBytes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getRotation() { return rotation; }
    public int getBrightness() { return brightness; }
    public int getContrast() { return contrast; }

    public void setFileId(int fileId) { this.fileId = fileId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }
    public void setReferenceId(int referenceId) { this.referenceId = referenceId; }
    public void setSortId(int sortId) { this.sortId = sortId; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public void setFileSizeBytes(int fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRotation(int rotation) { this.rotation = rotation; }
    public void setBrightness(int brightness) { this.brightness = brightness; }
    public void setContrast(int contrast) { this.contrast = contrast; }

    // TODO: find out what to say
    @Override
    public String toString() {
        return "File " + this.referenceId;
    }

    @Override
    public String getDisplayName() {
        return "\uE958" + " " + "File #" + this.referenceId;
    }
}
